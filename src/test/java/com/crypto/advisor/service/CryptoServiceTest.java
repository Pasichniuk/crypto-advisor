package com.crypto.advisor.service;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

// TODO: add tests
@SpringBootTest
@RunWith(MockitoJUnitRunner.class)
class CryptoServiceTest {

    @Autowired
    private CryptoService cryptoService;

    @Test
    void getLatestListingsTest() {
        var response = cryptoService.getCryptoStatistics();

        Assertions.assertTrue(response.size() > 0);
    }
}