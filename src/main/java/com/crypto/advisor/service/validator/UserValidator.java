package com.crypto.advisor.service.validator;

import com.crypto.advisor.entity.User;
import com.crypto.advisor.service.dao.UserDao;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

@Component
@RequiredArgsConstructor
public class UserValidator implements Validator {

    private static final String ERROR_CODE = "error.user";

    private final UserDao userDao;

    @Override
    public boolean supports(@NonNull Class<?> type) {
        return User.class.equals(type);
    }

    @Override
    public void validate(@NonNull Object target, @NonNull Errors errors) {
        var user = (User) target;

        if (userDao.findByUsername(user.getUsername()).isPresent()) {
            errors.rejectValue("username", ERROR_CODE, "This username is already taken");
        }

        if (!user.getPassword().equals(user.getPasswordConfirmation())) {
            errors.rejectValue("passwordConfirmation", ERROR_CODE, "Password mismatch");
        }
    }
}