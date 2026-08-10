package com.nineties.bhr.admin.controller;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = {
        "JWT_SECRET=test-jwt-secret-key-that-is-longer-than-32-bytes",
        "SPRING_DATASOURCE_DRIVER_CLASS_NAME=org.h2.Driver",
        "SPRING_DATASOURCE_URL=jdbc:h2:mem:bhr-test;MODE=MariaDB;DB_CLOSE_DELAY=-1",
        "SPRING_DATASOURCE_USERNAME=sa",
        "SPRING_DATASOURCE_PASSWORD=",
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.sql.init.mode=never"
})
@AutoConfigureMockMvc(addFilters = false)
@Transactional
class JoinControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @DisplayName("회원가입 입력값 검증에 실패하면 공통 검증 오류 형식으로 400을 반환한다")
    void join_validationFailed_returnsBadRequest() throws Exception {

        mockMvc.perform(
                        post("/api/join")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("{}")
                )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.code").value("VALIDATION_FAILED"))
                .andExpect(jsonPath("$.message")
                        .value("입력값을 확인해 주세요."))
                .andExpect(jsonPath("$.errors").exists())
                .andExpect(jsonPath("$.errors.username")
                        .value("아이디는 필수 사항입니다"));
    }

    @Test
    @DisplayName("회원가입 요청 본문이 없으면 400을 반환한다")
    void join_missingRequestBody_returnsBadRequest() throws Exception {

        mockMvc.perform(
                        post("/api/join")
                                .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.code")
                        .value("INVALID_REQUEST_BODY"))
                .andExpect(jsonPath("$.message")
                        .value("요청 본문을 읽을 수 없습니다."))
                .andExpect(jsonPath("$.path")
                        .value("/api/join"));
    }

    @Test
    @DisplayName("회원가입 JSON 형식이 잘못되면 400을 반환한다")
    void join_invalidJson_returnsBadRequest() throws Exception {

        mockMvc.perform(
                        post("/api/join")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("{\"name\":")
                )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.code")
                        .value("INVALID_REQUEST_BODY"))
                .andExpect(jsonPath("$.message")
                        .value("요청 본문을 읽을 수 없습니다."))
                .andExpect(jsonPath("$.path")
                        .value("/api/join"));
    }
}