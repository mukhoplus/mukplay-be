package com.mukplay.domain.user.entity;

import com.mukplay.common.entity.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "users")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class User extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "login_id", nullable = false, unique = true, length = 50)
    private String loginId;

    @Column(name = "password", nullable = false, length = 255)
    private String password;

    @Column(name = "nickname", nullable = false, length = 50)
    private String nickname;

    @Column(name = "level", nullable = false)
    private int level = 1;

    @Column(name = "exp", nullable = false)
    private int exp = 0;

    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false, length = 20)
    private UserRole role = UserRole.ROLE_USER;

    @Builder
    public User(String loginId, String password, String nickname, UserRole role) {
        this.loginId = loginId;
        this.password = password;
        this.nickname = nickname;
        this.role = role != null ? role : UserRole.ROLE_USER;
        this.level = 1;
        this.exp = 0;
    }

    public void addExp(int earnedExp) {
        if (earnedExp <= 0) return;
        this.exp += earnedExp;
        // Level up rule: every 100 EXP grants +1 level
        int newLevel = 1 + (this.exp / 100);
        if (newLevel > this.level) {
            this.level = newLevel;
        }
    }
}
