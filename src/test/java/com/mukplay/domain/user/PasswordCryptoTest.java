package com.mukplay.domain.user;

import com.mukplay.common.exception.BusinessException;
import com.mukplay.domain.user.validator.PasswordPolicyValidator;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
class PasswordCryptoTest {

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private PasswordPolicyValidator passwordPolicyValidator;

    @Test
    @DisplayName("BCrypt 암호화 결과는 평문과 달라야 하며 matches로 일치해야 한다")
    void testPasswordEncodingAndMatching() {
        String rawPassword = "password123!";
        String encoded = passwordEncoder.encode(rawPassword);

        assertThat(encoded).isNotEqualTo(rawPassword);
        assertThat(encoded).startsWith("$2a$");
        assertThat(passwordEncoder.matches(rawPassword, encoded)).isTrue();
        assertThat(passwordEncoder.matches("wrongpass123", encoded)).isFalse();
    }

    @Test
    @DisplayName("8자 미만이거나 숫자가 없는 비밀번호는 예외가 발생해야 한다")
    void testPasswordPolicyValidation() {
        // Valid
        passwordPolicyValidator.validate("validPass1");

        // Invalid: too short
        assertThatThrownBy(() -> passwordPolicyValidator.validate("short1"))
                .isInstanceOf(BusinessException.class);

        // Invalid: no digit
        assertThatThrownBy(() -> passwordPolicyValidator.validate("onlyletters"))
                .isInstanceOf(BusinessException.class);
    }
}
