package com.crypto.advisor.controller;

import com.crypto.advisor.entity.User;
import com.crypto.advisor.model.Constants;
import com.crypto.advisor.service.dao.UserDao;
import com.crypto.advisor.service.validator.UserValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.ModelAndView;

import javax.validation.Valid;
import java.security.Principal;

@Controller
@RequiredArgsConstructor
public class SecurityController {

    private final UserDao userDao;
    private final UserValidator userValidator;

    @GetMapping(Constants.LOGIN_PATH)
    public String login(Principal principal) {
        return principal != null ? Constants.HOME_PATH : Constants.LOGIN_PATH;
    }

    @GetMapping(Constants.REGISTRATION_PATH)
    public ModelAndView registration() {
        return new ModelAndView(Constants.REGISTRATION_PATH)
                .addObject("user", new User());
    }

    @PostMapping(Constants.REGISTRATION_PATH)
    public ModelAndView registration(@Valid User user, BindingResult result) {
        if (result.hasErrors()) {
            return new ModelAndView(Constants.REGISTRATION_PATH);
        }

        userDao.save(user);

        return new ModelAndView(Constants.LOGIN_PATH)
                .addObject("message", "User has been registered successfully");
    }

    @InitBinder
    private void bindValidator(WebDataBinder binder) {
        binder.setValidator(userValidator);
    }
}
