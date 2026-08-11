package com.nineties.bhr.login.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "refresh_token")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class RefreshToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "emp_id", nullable = false, unique = true)
    private String empId;

    @Column(nullable = false, unique = true, length = 512)
    private String token;

    public RefreshToken(String empId, String token) {
        this.empId = empId;
        this.token = token;
    }

    public void updateToken(String token) {
        this.token = token;
    }
}