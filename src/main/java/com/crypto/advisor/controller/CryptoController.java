package com.crypto.advisor.controller;

import com.crypto.advisor.entity.CryptoStats;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;

import com.crypto.advisor.service.CryptoService;

import java.util.Comparator;
import java.util.stream.Collectors;

@Controller
public class CryptoController {

    private static final String ALL_CRYPTO_STATS_PAGE_PATH = "all-crypto-stats";
    private static final String CRYPTO_STATS_PAGE_PATH = "crypto-stats";
    private static final String CONTACTS_PAGE_PATH = "contacts";
    private static final String ABOUT_PAGE_PATH = "about";
    private static final String ERROR_PAGE_PATH = "error";

    private final CryptoService cryptoService;

    @Autowired
    public CryptoController(CryptoService cryptoService) {
        this.cryptoService = cryptoService;
    }

    @GetMapping("/stats")
    public String getCryptoStatistics(Model model) {
        var cryptoStatistics = cryptoService.getCryptoStatistics();
        model.addAttribute("cryptoStatsSet", cryptoStatistics);
        model.addAttribute("trendingCryptoList",
                cryptoStatistics.stream()
                        .sorted(Comparator.comparingDouble(CryptoStats::getPercentChangeWeek).reversed())
                        .limit(3)
                        .collect(Collectors.toList())
        );
        return ALL_CRYPTO_STATS_PAGE_PATH;
    }

    @GetMapping("/stats/{symbol}")
    public String getCryptoStatistics(@PathVariable @NonNull String symbol, Model model) {
        try {
            model.addAttribute("cryptoStats", cryptoService.getCryptoStatisticsBySymbol(symbol));
            model.addAttribute("historicalData", cryptoService.getHistoricalAndPredictedData("DIGITAL_CURRENCY_DAILY", symbol));
            return CRYPTO_STATS_PAGE_PATH;
        } catch (IllegalArgumentException e) {
            model.addAttribute("message", e.getMessage());
            return ERROR_PAGE_PATH;
        }
    }

    @GetMapping("/contacts")
    public String contacts() {
        return CONTACTS_PAGE_PATH;
    }

    @GetMapping("/about")
    public String about() {
        return ABOUT_PAGE_PATH;
    }
}