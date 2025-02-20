package com.crypto.advisor.controller;

import com.crypto.advisor.model.Constants;
import com.crypto.advisor.service.dao.UserDao;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

@Controller
@RequestMapping(Constants.USERS_PATH)
@RequiredArgsConstructor
public class UserController {

    private final UserDao userDao;

    @GetMapping
    public ModelAndView findAll() {
        var users = userDao.findAll();
        return new ModelAndView(Constants.USERS_PATH)
            .addObject("users", users);
    }

    // TODO: use POST
    @GetMapping("{username}/enable")
    public ModelAndView block(@PathVariable @NonNull String username,
                              @RequestParam(defaultValue = "false") boolean value) {
        userDao.enable(username, value);
        var users = userDao.findAll();
        return new ModelAndView(Constants.USERS_PATH)
                .addObject("users", users);
    }
}