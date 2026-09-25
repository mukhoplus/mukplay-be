package com.mukplay.domain.user.repository;

import com.mukplay.domain.user.entity.User;
import com.mukplay.domain.user.entity.UserRole;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
class UserRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    @Test
    @DisplayName("User 엔티티 저장 및 loginId로 조회할 수 있어야 한다")
    void testSaveAndFindByLoginId() {
        User user = User.builder()
                .loginId("testuser")
                .password("encoded_pass")
                .nickname("테스터")
                .role(UserRole.ROLE_USER)
                .build();

        User saved = userRepository.save(user);

        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getLevel()).isEqualTo(1);
        assertThat(saved.getExp()).isEqualTo(0);
        assertThat(saved.getCreatedAt()).isNotNull();

        Optional<User> found = userRepository.findByLoginId("testuser");
        assertThat(found).isPresent();
        assertThat(found.get().getNickname()).isEqualTo("테스터");
    }

    @Test
    @DisplayName("EXP 획득 시 100 EXP마다 자동으로 레벨업해야 한다")
    void testUserExpAndLevelUp() {
        User user = User.builder()
                .loginId("leveluser")
                .password("pass")
                .nickname("레벨업유저")
                .build();

        user.addExp(90);
        assertThat(user.getLevel()).isEqualTo(1);
        assertThat(user.getExp()).isEqualTo(90);

        user.addExp(20); // total 110 -> Level 2
        assertThat(user.getLevel()).isEqualTo(2);
        assertThat(user.getExp()).isEqualTo(110);
    }
}
