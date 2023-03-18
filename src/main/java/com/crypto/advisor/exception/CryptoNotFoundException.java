package com.crypto.advisor.exception;

public class CryptoNotFoundException extends IllegalArgumentException {

    public CryptoNotFoundException(String symbol) {
        super(String.format("Couldn't find cryptocurrency with symbol: %s", symbol));
    }
}