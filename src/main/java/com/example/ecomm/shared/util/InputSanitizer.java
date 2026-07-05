package com.example.ecomm.shared.util;

import org.springframework.stereotype.Component;

import java.util.regex.Pattern;

/**
 * Utility for sanitizing user-supplied strings to prevent XSS.
 * Applied in service layer before persisting user input.
 */
@Component
public class InputSanitizer {

    private static final Pattern HTML_TAGS  = Pattern.compile("<[^>]*>");
    private static final Pattern SCRIPT_TAG = Pattern.compile(
            "(?i)<script[\\s\\S]*?</script>|javascript:", Pattern.CASE_INSENSITIVE);

    public String sanitize(String input) {
        if (input == null) return null;
        String cleaned = SCRIPT_TAG.matcher(input).replaceAll("");
        cleaned = HTML_TAGS.matcher(cleaned).replaceAll("");
        return cleaned.trim();
    }
}
