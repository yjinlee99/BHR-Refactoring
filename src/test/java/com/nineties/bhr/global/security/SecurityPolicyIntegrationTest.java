package com.nineties.bhr.global.security;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc  // Security Filter 활성화
@ActiveProfiles("test")
class SecurityPolicyIntegrationTest {

    @Autowired
    private MockMvc mockMvc;


    @Test
    @DisplayName("인증되지 않은 사용자는 관리자 배지 API에 접근할 수 없다")
    void adminBadge_withoutAuthentication_returns401() throws Exception {

        mockMvc.perform(get("/api/admin/badge/list"))
                .andExpect(status().isUnauthorized());
    }


    @Test
    @DisplayName("일반 사용자는 관리자 배지 API에 접근할 수 없다")
    @WithMockUser(username = "employee", roles = "EMPLOYEE")
    void adminBadge_withUserRole_returns403() throws Exception {

        mockMvc.perform(get("/api/admin/badge/list"))
                .andExpect(status().isForbidden());
    }


    @Test
    @DisplayName("MANAGER는 관리자 배지 API에 접근할 수 있다")
    @WithMockUser(username = "manager", roles = "MANAGER")
    void adminBadge_withManagerRole_canAccess() throws Exception {

        mockMvc.perform(get("/api/admin/badge/list"))
                .andExpect(status().isOk());
    }


    @Test
    @DisplayName("인증되지 않은 사용자는 신규 직원 생성 API에 접근할 수 없다")
    void join_withoutAuthentication_returns401() throws Exception {

        mockMvc.perform(post("/api/join")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isUnauthorized());
    }


    @Test
    @DisplayName("일반 사용자는 신규 직원 등록 화면에 접근할 수 없다")
    @WithMockUser(username = "employee", roles = "EMPLOYEE")
    void joinPage_withUserRole_returns403() throws Exception {

        mockMvc.perform(get("/api/join/new"))
                .andExpect(status().isForbidden());
    }


    @Test
    @DisplayName("로그인 API는 인증 없이 접근할 수 있다")
    void login_withoutAuthentication_isPublic() throws Exception {

        mockMvc.perform(get("/api/login"))
                .andExpect(result -> {
                    int status = result.getResponse().getStatus();

                    if (status == 401 || status == 403) {
                        throw new AssertionError(
                                "공개 API가 Security에 의해 차단되었습니다. status=" + status
                        );
                    }
                });
    }
}