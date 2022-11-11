package com.xm.crypto.service;

import com.xm.crypto.entity.CryptoStats;
import com.xm.crypto.entity.CryptoSymbol;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@RunWith(MockitoJUnitRunner.class)
public class CryptoServiceTest {

    @Autowired
    private CryptoService cryptoService;

    @Test
    public void getCryptoStatisticsTest() throws IOException {
        assertThat(cryptoService.getCryptoStatistics().size()).isGreaterThan(1);

        var btcStats = CryptoStats.builder()
                .symbol(CryptoSymbol.BTC)
                .minPrice(33276.59)
                .maxPrice(47722.66)
                .oldestPrice(46813.21)
                .newestPrice(38415.79)
                .normalizedRange(0.43412110435594536)
                .build();

        assertThat(cryptoService.getCryptoStatistics(CryptoSymbol.BTC)).isEqualTo(btcStats);

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
