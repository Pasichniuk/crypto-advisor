package com.crypto.advisor.service.dao;

import com.crypto.advisor.entity.User;

import java.util.Optional;

public interface UserDao {

    void save(User user);

    Iterable<User> findAll();

    Optional<User> findByUsername(String username);

    void enable(String username, boolean value);
}
