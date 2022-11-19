package com.crypto.advisor.service;

import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.crypto.advisor.entity.CryptoStats;
import com.crypto.advisor.entity.CryptoSymbol;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@RunWith(MockitoJUnitRunner.class)
class CryptoServiceTest {

    @Autowired
    private CryptoService cryptoService;

    @Test
    void getCryptoStatistics() {

        assertThat(cryptoService.getCryptoStatistics()).hasSizeGreaterThan(1);

        var btcStats = CryptoStats.builder()
                .symbol(CryptoSymbol.BTC)
                .minPrice(33276.59)
                .maxPrice(47722.66)
                .oldestPrice(46813.21)
                .newestPrice(38415.79)
                .normalizedRange(0.43412110435594536)
                .build();

        assertThat(cryptoService.getCryptoStatistics(CryptoSymbol.BTC)).isEqualTo(btcStats);
    }

    @Test
    void getCryptoWithHighestNormalizedRange() {

        var ethStats = CryptoStats.builder()
            .symbol(CryptoSymbol.ETH)
            .minPrice(2336.52)
            .maxPrice(3828.11)
            .oldestPrice(3715.32)
            .newestPrice(2672.5)
            .normalizedRange(0.6383810110763016)
            .build();

        assertThat(cryptoService.getCryptoWithHighestNormalizedRange()).isEqualTo(ethStats);
    }
}