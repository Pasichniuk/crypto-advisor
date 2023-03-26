package com.crypto.advisor.controller;

import com.crypto.advisor.exception.CryptoNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;

import com.crypto.advisor.service.CryptoService;

@Controller
@RequestMapping("stats")
public class CryptoController {

    private static final String ALL_CRYPTO_STATS_PAGE_PATH = "all-crypto-stats";
    private static final String CRYPTO_STATS_PAGE_PATH = "crypto-stats";

    private final CryptoService cryptoService;

    @Autowired
    public CryptoController(CryptoService cryptoService) {
        this.cryptoService = cryptoService;
    }

    @GetMapping
    public String getCryptoStatistics(Model model) {
        model.addAttribute("cryptoStatsSet", cryptoService.getCryptoStatistics());
        return ALL_CRYPTO_STATS_PAGE_PATH;
    }

    @GetMapping("{symbol}")
    public String getCryptoStatistics(@PathVariable @NonNull String symbol, Model model) {
        try {
            model.addAttribute("cryptoStats", cryptoService.getCryptoStatisticsBySymbol(symbol));
            // TODO: get function from PathVariable
            model.addAttribute("historicalData", cryptoService.getHistoricalData("DIGITAL_CURRENCY_MONTHLY", symbol));
            return CRYPTO_STATS_PAGE_PATH;
        } catch (CryptoNotFoundException e) {
            model.addAttribute("message", e.getMessage());
            return "error";
        }
    }
}