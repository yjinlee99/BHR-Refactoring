package com.nineties.bhr.badge.controller;

import com.nineties.bhr.badge.dto.BadgeProjection;
import com.nineties.bhr.badge.exception.BadgeNotFoundException;
import com.nineties.bhr.badge.service.BadgeManageService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/badge")
public class AdminBadgeController {

    private final BadgeManageService badgeManageService;

    public AdminBadgeController(BadgeManageService badgeManageService) {
        this.badgeManageService = badgeManageService;
    }

    @GetMapping("/list")
    public List<BadgeProjection> showBadgeList() {
        return badgeManageService.showBadgeList();
    }

    @PostMapping("/activate")
    public ResponseEntity<String> activateBadge(@RequestParam String badgeName) {
        badgeManageService.activateBadgeByName(badgeName);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/deactivate")
    public ResponseEntity<String> deactivateBadge(@RequestParam String badgeName) {
        badgeManageService.disableBadgeAndRelatedEmpBadges(badgeName);
        return ResponseEntity.noContent().build();
    }
}
