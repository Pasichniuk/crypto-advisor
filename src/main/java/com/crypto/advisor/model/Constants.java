package com.crypto.advisor.model;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class Constants {

    public static final String REGISTRATION_PATH = "/registration";
    public static final String LOGIN_PATH = "/login";
    public static final String USERS_PATH = "/users";
    public static final String HOME_PATH = "/home";
    public static final String ALL_CRYPTO_STATS_PATH = "/all-crypto-stats";
    public static final String CRYPTO_STATS_PATH = "/crypto-stats";
    public static final String CONTACTS_PATH = "/contacts";
    public static final String ABOUT_PATH = "/about";
    public static final String ERROR_PATH = "/error";
}