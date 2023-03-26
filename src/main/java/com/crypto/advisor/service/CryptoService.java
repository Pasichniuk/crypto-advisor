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

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
public class CryptoService {

    private static final Logger LOGGER = LoggerFactory.getLogger(CryptoService.class);

    private final CmcApiClient cmcApiClient;
    private final AlphaVantageClient avApiClient;

    @Autowired
    public CryptoService(CmcApiClient cmcApiClient, AlphaVantageClient avApiClient) {
        this.cmcApiClient = cmcApiClient;
        this.avApiClient = avApiClient;
    }

    public Set<CryptoStats> getCryptoStatistics() {
        var apiResponse = cmcApiClient.getLatestListings();

        var object = (JsonObject) JsonParser.parseString(apiResponse);
        var data = object.get("data");

        CryptoStats[] cryptoStats = new CryptoStats[0];
        try {
            cryptoStats = new ObjectMapper().readValue(data.toString(), CryptoStats[].class);
        } catch (JsonProcessingException e) {
            LOGGER.error("Failed to process json. Reason: " + e.getMessage());
        }

        return Stream.of(cryptoStats)
                .sorted(Comparator.comparing(CryptoStats::getRank))
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    public CryptoStats getCryptoStatisticsBySymbol(String symbol) {
        return getCryptoStatistics().stream()
                .filter(c -> c.getSymbol().equals(symbol))
                .findFirst()
                .orElseThrow(() -> new CryptoNotFoundException(symbol));
    }

    public String getHistoricalData(String function, String symbol) {
        var apiResponse = avApiClient.getHistoricalData(function, symbol);

        var object = (JsonObject) JsonParser.parseString(apiResponse);
        // TODO: generate this key from function
        var data = object.get("Time Series (Digital Currency Monthly)").getAsJsonObject();

        Map<String, BigDecimal> historicalData = new LinkedHashMap<>();

        for (String currentKey : data.keySet()) {
            Object value = data.get(currentKey);

            if (value instanceof JsonObject) {
                String price = ((JsonObject) value).get("2b. high (USD)").toString();
                BigDecimal stripedVal = new BigDecimal(price.replace("\"", "")).stripTrailingZeros();
                historicalData.put(currentKey, stripedVal);
            }
        }

        return histMapToJsonString(historicalData);
    }

    private String histMapToJsonString(Map<String, BigDecimal> histData) {
        var sb = new StringBuilder();

        sb.append("[[\"Date\", \"Price\"],");

        histData.forEach((k, v) -> sb.append(String.format("[\"%s\", %s],", k, v)));

        sb.deleteCharAt(sb.length() - 1);
        sb.append("]");

        return sb.toString();
    }
}