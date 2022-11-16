package com.crypto.advisor.controller;

import java.util.Locale;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.apache.commons.lang3.EnumUtils;

import com.crypto.advisor.service.CryptoService;
import com.crypto.advisor.entity.CryptoSymbol;

/**
 * Controller responsible for crypto statistics data
 */

@Controller
@RequestMapping("stats")
public class CryptoController {

    private static final String CRYPTO_STATS_PAGE_PATH = "crypto-stats";

    private final CryptoService cryptoService;

    @Autowired
    public CryptoController(CryptoService cryptoService) {
        this.cryptoService = cryptoService;
    }

    /**
     * Returns statistics for all supported cryptos
     *
     * @param model Thymeleaf model
     * @return Path to HTML page
     */
    @GetMapping
    public String getCryptoStatistics(Model model) {
        model.addAttribute("cryptoStatsSet", cryptoService.getCryptoStatistics().values());
        return "all-crypto-stats";
    }

    /**
     * Returns statistics for the requested crypto
     *
     * @param cryptoSymbol the requested crypto's symbol
     * @param model        Thymeleaf model
     * @return Path to HTML page
     */
    @GetMapping("{cryptoSymbol}")
    public String getCryptoStatistics(@PathVariable @NonNull String cryptoSymbol, Model model) {
        if (EnumUtils.isValidEnum(CryptoSymbol.class, cryptoSymbol.toUpperCase(Locale.ENGLISH))) {
            var symbol = CryptoSymbol.valueOf(cryptoSymbol);
            model.addAttribute("cryptoStats", cryptoService.getCryptoStatistics(symbol));
            return CRYPTO_STATS_PAGE_PATH;
        } else {
            model.addAttribute("message", "Sorry, this crypto is not currently supported");
            return "error";
        }
    }

    /**
     * Return statistics for the crypto with the highest normalized range
     *
     * @param model Thymeleaf model
     * @return Path to HTML page
     */
    @GetMapping("best")
    public String getCryptoWithHighestNormalizedRange(Model model) {
        model.addAttribute("cryptoStats", cryptoService.getCryptoWithHighestNormalizedRange());
        return CRYPTO_STATS_PAGE_PATH;
    }
}