package com.nineties.bhr.global.security;

import com.nineties.bhr.employee.dto.CustomUserDetails;
import com.nineties.bhr.employee.service.CustomEmployeeDetailsService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.startsWith;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc  // Security Filter 활성화
@ActiveProfiles("test")
class SecurityPolicyIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CustomEmployeeDetailsService customEmployeeDetailsService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @BeforeEach
    void setUpTestUser() {

        CustomUserDetails testUser = mock(CustomUserDetails.class);

        when(testUser.getEmpId())
                .thenReturn("testEmp001");

        when(testUser.getUsername())
                .thenReturn("testuser");

        when(testUser.getPassword())
                .thenReturn(passwordEncoder.encode("1234"));

        doReturn(AuthorityUtils.createAuthorityList("ROLE_EMPLOYEE"))
                .when(testUser)
                .getAuthorities();

        when(testUser.isAccountNonExpired())
                .thenReturn(true);

        when(testUser.isAccountNonLocked())
                .thenReturn(true);

        when(testUser.isCredentialsNonExpired())
                .thenReturn(true);

        when(testUser.isEnabled())
                .thenReturn(true);

        when(customEmployeeDetailsService.loadUserByUsername("testuser"))
                .thenReturn(testUser);
    }


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

        mockMvc.perform(post("/login")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("username", "testuser")
                        .param("password", "1234"))
                .andExpect(status().isOk())
                .andExpect(header().string(
                        "Authorization",
                        startsWith("Bearer ")
                ))
                .andExpect(header().string(
                        "Refresh-Token",
                        startsWith("Bearer ")
                ));
    }
}