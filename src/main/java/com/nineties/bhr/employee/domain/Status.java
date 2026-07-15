package com.nineties.bhr.employee.domain;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum Status {
    WORKING, REST, LEAVE;
}
