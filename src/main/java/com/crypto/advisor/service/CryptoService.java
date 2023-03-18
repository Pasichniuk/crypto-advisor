package com.crypto.advisor.service;

import com.crypto.advisor.entity.CryptoStats;
import com.crypto.advisor.exception.CryptoNotFoundException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Set;

@Service
public class CryptoService {

    private static final Logger LOGGER = LoggerFactory.getLogger(CryptoService.class);

    private final CmcApiClient cmcApiClient;

    @Autowired
    public CryptoService(CmcApiClient cmcApiClient) {
        this.cmcApiClient = cmcApiClient;
    }

    public Set<CryptoStats> getCryptoStatistics() {
        var apiResponse = cmcApiClient.getLatestListings();
        LOGGER.info("Latest listings: {}", apiResponse);

        JsonObject object = (JsonObject) JsonParser.parseString(apiResponse);
        var data = object.get("data");

        CryptoStats[] cryptoStats = new CryptoStats[0];
        try {
            cryptoStats = new ObjectMapper().readValue(data.toString(), CryptoStats[].class);
        } catch (JsonProcessingException e) {
            LOGGER.error("Failed to process json. Reason: " + e.getMessage());
        }

        return Set.of(cryptoStats);
    }

    public CryptoStats getCryptoStatisticsBySymbol(String symbol) {
        return getCryptoStatistics().stream()
                .filter(c -> c.getSymbol().equals(symbol))
                .findFirst()
                .orElseThrow(() -> new CryptoNotFoundException(symbol));
    }
}