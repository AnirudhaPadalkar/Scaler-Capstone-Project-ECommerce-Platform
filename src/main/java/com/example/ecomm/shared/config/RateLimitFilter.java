package com.example.ecomm.shared.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
@Component
public class RateLimitFilter extends OncePerRequestFilter {

    // windowMs → max requests per IP per window
    private static final int AUTH_MAX        = 20;
    private static final int SENSITIVE_MAX   = 5;
    private static final int WINDOW_MS       = 15 * 60 * 1000;

    private final Map<String, RateEntry> authCounters      = new ConcurrentHashMap<>();
    private final Map<String, RateEntry> sensitiveCounters = new ConcurrentHashMap<>();

    @Override
    protected void doFilterInternal(HttpServletRequest req, HttpServletResponse res, FilterChain chain)
            throws ServletException, IOException {

        String ip   = getClientIp(req);
        String path = req.getRequestURI();

        if (isSensitivePath(path)) {
            if (!allow(sensitiveCounters, ip, SENSITIVE_MAX)) {
                res.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
                res.getWriter().write("{\"error\":\"Too many attempts. Try again in an hour.\"}");
                return;
            }
        } else if (isAuthPath(path)) {
            if (!allow(authCounters, ip, AUTH_MAX)) {
                res.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
                res.getWriter().write("{\"error\":\"Too many requests. Please slow down.\"}");
                return;
            }
        }

        chain.doFilter(req, res);
    }

    private boolean allow(Map<String, RateEntry> counters, String ip, int max) {
        long now = System.currentTimeMillis();
        RateEntry entry = counters.compute(ip, (k, v) -> {
            if (v == null || now - v.windowStart > WINDOW_MS) {
                return new RateEntry(now, new AtomicInteger(1));
            }
            v.count.incrementAndGet();
            return v;
        });
        return entry.count.get() <= max;
    }

    private boolean isAuthPath(String path) {
        return path.startsWith("/api/v1/auth/");
    }

    private boolean isSensitivePath(String path) {
        return path.contains("forgot-password") || path.contains("reset-password");
    }

    private String getClientIp(HttpServletRequest req) {
        String forwarded = req.getHeader("X-Forwarded-For");
        return (forwarded != null) ? forwarded.split(",")[0].trim() : req.getRemoteAddr();
    }

    private record RateEntry(long windowStart, AtomicInteger count) {}
}
