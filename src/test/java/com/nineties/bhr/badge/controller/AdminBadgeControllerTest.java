package com.nineties.bhr.badge.controller;


import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.mockito.Mockito.doThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.nineties.bhr.badge.domain.BadgeMaster;
import com.nineties.bhr.badge.domain.BadgeStatus;
import com.nineties.bhr.badge.repository.BadgeMasterRepository;
import com.nineties.bhr.badge.service.BadgeManageService;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

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
class AdminBadgeControllerIntegrationTest {

    private static final String TEST_BADGE_NAME = "X세대";
    private static final String NOT_FOUND_BADGE_NAME = "없는배지";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private BadgeMasterRepository badgeMasterRepository;

    @Autowired
    private EntityManager entityManager;

    /*
     * 실제 Service를 사용한다.
     *
     * 예상하지 못한 오류 테스트에서만 강제로 RuntimeException을
     * 발생시키기 위해 SpyBean으로 등록한다.
     * 별도의 설정이 없으면 실제 Service 메서드가 실행된다.
     */
    @SpyBean
    private BadgeManageService badgeManageService;

    @BeforeEach
    void setUp() {
        BadgeMaster badge = badgeMasterRepository.findByBadgeName(TEST_BADGE_NAME);

        if (badge == null) {
            badge = new BadgeMaster();
            badge.setBadgeName(TEST_BADGE_NAME);
            badge.setBadgeDetail("배지 API 통합 테스트용 배지");
            badge.setStatus(BadgeStatus.Disabled);
        } else {
            badge.setStatus(BadgeStatus.Disabled);
        }

        badgeMasterRepository.saveAndFlush(badge);
        entityManager.clear();
    }

    @Test
    @DisplayName("배지 활성화에 성공하면 204를 반환하고 상태를 Enabled로 변경한다")
    void activateBadge_success() throws Exception {
        mockMvc.perform(
                        post("/api/admin/badge/activate")
                                .param("badgeName", TEST_BADGE_NAME)
                )
                .andExpect(status().isNoContent())
                .andExpect(content().string(""));

        entityManager.flush();
        entityManager.clear();

        BadgeMaster badge =
                badgeMasterRepository.findByBadgeName(TEST_BADGE_NAME);

        assertThat(badge).isNotNull();
        assertThat(badge.getStatus()).isEqualTo(BadgeStatus.Enabled);
    }

    @Test
    @DisplayName("배지 비활성화에 성공하면 204를 반환하고 상태를 Disabled로 변경한다")
    void deactivateBadge_success() throws Exception {
        BadgeMaster badge =
                badgeMasterRepository.findByBadgeName(TEST_BADGE_NAME);

        badge.setStatus(BadgeStatus.Enabled);
        badgeMasterRepository.saveAndFlush(badge);
        entityManager.clear();

        mockMvc.perform(
                        post("/api/admin/badge/deactivate")
                                .param("badgeName", TEST_BADGE_NAME)
                )
                .andExpect(status().isNoContent())
                .andExpect(content().string(""));

        entityManager.flush();
        entityManager.clear();

        BadgeMaster result =
                badgeMasterRepository.findByBadgeName(TEST_BADGE_NAME);

        assertThat(result).isNotNull();
        assertThat(result.getStatus()).isEqualTo(BadgeStatus.Disabled);
    }

    @Test
    @DisplayName("존재하지 않는 배지를 활성화하면 공통 형식의 404 응답을 반환한다")
    void activateBadge_notFound() throws Exception {
        mockMvc.perform(
                        post("/api/admin/badge/activate")
                                .param("badgeName", NOT_FOUND_BADGE_NAME)
                )
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.code").value("BADGE_NOT_FOUND"))
                .andExpect(jsonPath("$.message")
                        .value("배지를 찾을 수 없습니다: " + NOT_FOUND_BADGE_NAME))
                .andExpect(jsonPath("$.path")
                        .value("/api/admin/badge/activate"));
    }

    @Test
    @DisplayName("존재하지 않는 배지를 비활성화하면 공통 형식의 404 응답을 반환한다")
    void deactivateBadge_notFound() throws Exception {
        mockMvc.perform(
                        post("/api/admin/badge/deactivate")
                                .param("badgeName", NOT_FOUND_BADGE_NAME)
                )
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.code").value("BADGE_NOT_FOUND"))
                .andExpect(jsonPath("$.message")
                        .value("배지를 찾을 수 없습니다: " + NOT_FOUND_BADGE_NAME))
                .andExpect(jsonPath("$.path")
                        .value("/api/admin/badge/deactivate"));
    }

    @Test
    @DisplayName("예상하지 못한 오류는 내부 메시지를 숨기고 500을 반환한다")
    void activateBadge_unexpectedError() throws Exception {
        String internalMessage =
                "데이터베이스 비밀번호가 노출될 수 있는 내부 오류";

        doThrow(new IllegalStateException(internalMessage))
                .when(badgeManageService)
                .activateBadgeByName("오류배지");

        mockMvc.perform(
                        post("/api/admin/badge/activate")
                                .param("badgeName", "오류배지")
                )
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.status").value(500))
                .andExpect(jsonPath("$.code")
                        .value("INTERNAL_SERVER_ERROR"))
                .andExpect(jsonPath("$.message")
                        .value("서버 내부 오류가 발생했습니다."))
                .andExpect(jsonPath("$.path")
                        .value("/api/admin/badge/activate"))
                .andExpect(content().string(
                        not(containsString(internalMessage))
                ));
    }
}