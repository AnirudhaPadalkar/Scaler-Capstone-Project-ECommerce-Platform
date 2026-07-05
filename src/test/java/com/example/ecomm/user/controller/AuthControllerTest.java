package com.example.ecomm.user.controller;

import com.example.ecomm.user.dto.LoginRequestDto;
import com.example.ecomm.user.dto.RegisterRequestDto;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AuthControllerTest {

    @Autowired private MockMvc       mockMvc;
    @Autowired private ObjectMapper  objectMapper;

    @Test
    void register_then_login_then_accessProfile() throws Exception {
        String email = "e2e_" + System.currentTimeMillis() + "@test.com";

        // 1. Register
        RegisterRequestDto registerReq = new RegisterRequestDto();
        registerReq.setEmail(email);
        registerReq.setPassword("Password1");
        registerReq.setFirstName("E2E");
        registerReq.setLastName("Test");

        mockMvc.perform(post("/api/v1/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registerReq)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.userId").exists());

        // 2. Login
        LoginRequestDto loginReq = new LoginRequestDto();
        loginReq.setEmail(email);
        loginReq.setPassword("Password1");

        MvcResult loginResult = mockMvc.perform(post("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").exists())
                .andReturn();

        String body        = loginResult.getResponse().getContentAsString();
        String accessToken = objectMapper.readTree(body).get("accessToken").asText();

        // 3. Access protected profile
        mockMvc.perform(get("/api/v1/profile")
                .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value(email));
    }

    @Test
    void register_duplicateEmail_returns409() throws Exception {
        String email = "dup_" + System.currentTimeMillis() + "@test.com";
        RegisterRequestDto req = new RegisterRequestDto();
        req.setEmail(email);
        req.setPassword("Password1");
        req.setFirstName("Dup");
        req.setLastName("Test");

        mockMvc.perform(post("/api/v1/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/v1/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isConflict());
    }

    @Test
    void login_wrongPassword_returns401() throws Exception {
        LoginRequestDto req = new LoginRequestDto();
        req.setEmail("nobody@test.com");
        req.setPassword("wrong");

        mockMvc.perform(post("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void forgotPassword_unknownEmail_returns200() throws Exception {
        mockMvc.perform(post("/api/v1/auth/forgot-password")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"email\":\"ghost@nowhere.com\"}"))
                .andExpect(status().isOk());
    }
}
