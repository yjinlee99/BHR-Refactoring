package com.nineties.bhr.badge.exception;

import com.nineties.bhr.global.exception.BusinessException;
import com.nineties.bhr.global.exception.ErrorCode;

public class BadgeNotFoundException extends BusinessException {

    public BadgeNotFoundException(String badgeName) {
        super(
                ErrorCode.BADGE_NOT_FOUND,
                "배지를 찾을 수 없습니다: " + badgeName
        );
    }
}
