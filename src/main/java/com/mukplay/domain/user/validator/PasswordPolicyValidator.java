package com.mukplay.domain.user.validator;

import com.mukplay.common.exception.BusinessException;
import com.mukplay.common.exception.ErrorCode;
import org.springframework.stereotype.Component;

import java.util.regex.Pattern;

@Component
public class PasswordPolicyValidator {

    // Minimum 8 characters, at least one letter and one number
    private static final Pattern PASSWORD_PATTERN = Pattern.compile("^(?=.*[A-Za-z])(?=.*\\d)[A-Za-z\\d@$!%*#?&]{8,50}$");

    public void validate(String rawPassword) {
        if (rawPassword == null || !PASSWORD_PATTERN.matcher(rawPassword).matches()) {
            throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE, "비밀번호는 영문과 숫자를 포함하여 8자 이상 50자 이하여야 합니다.");
        }
    }
}
