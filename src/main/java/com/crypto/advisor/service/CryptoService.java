package com.crypto.advisor.service;

import com.crypto.advisor.entity.CryptoStats;
import com.crypto.advisor.exception.CryptoNotFoundException;
import com.crypto.advisor.service.prediction.predict.CryptoPricePrediction;
import com.crypto.advisor.entity.CryptoData;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.time.LocalDate;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
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
        } catch (Exception e) {
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

    public String getHistoricalAndPredictedData(String function, String symbol) {
        var apiResponse = avApiClient.getHistoricalData(function, symbol);
        var object = (JsonObject) JsonParser.parseString(apiResponse);

        JsonObject data;
        try {
            data = object.get("Time Series (Digital Currency Daily)").getAsJsonObject();
        } catch (Exception e) {
            LOGGER.error("Failed to get json object. Reason: " + e.getMessage());
            throw new IllegalArgumentException("Fiat cryptocurrencies are not supported yet!");
        }

        Map<String, String> histData = new LinkedHashMap<>();

        for (String currentKey : data.keySet()) {
            Object value = data.get(currentKey);

            if (value instanceof JsonObject) {
                var price = ((JsonObject) value).get("2b. high (USD)").toString().replace("\"", "");
                var decimalFormat = new DecimalFormat("0.#####");
                price = decimalFormat.format(Double.valueOf(price));
                histData.put(currentKey, price);
            }
        }

        var predictedPrices = getPredictedPrices(histData, symbol);
        Map<String, BigDecimal> preparedData = new LinkedHashMap<>(predictedPrices);
        histData.forEach((key, value) -> preparedData.put(key, new BigDecimal(value)));

        return mapToJsonString(preparedData);
    }

    private Map<String, BigDecimal> getPredictedPrices(Map<String, String> histData, String symbol) {
        List<CryptoData> cryptoData = new ArrayList<>();
        TreeMap<String, String> sortedHistData = new TreeMap<>(histData);
        sortedHistData.forEach((k, v) -> cryptoData.add(new CryptoData(k, symbol, Double.parseDouble(v))));

        double[] predictedPrices = new double[0];
        try {
            predictedPrices = CryptoPricePrediction.predict(cryptoData);
        } catch (IOException e) {
            e.printStackTrace();
        }

        var currentDate = LocalDate.now();
        var daysCount = new AtomicInteger(predictedPrices.length);

        return Arrays.stream(predictedPrices)
                .boxed()
                .map(BigDecimal::new)
                .collect(Collectors.toMap(
                        x -> currentDate.plusDays(daysCount.getAndDecrement()).toString(),
                        price -> price,
                        (x, y) -> y,
                        LinkedHashMap::new)
                );
    }

    private String mapToJsonString(Map<String, BigDecimal> data) {
        var sb = new StringBuilder();
        var counter = new AtomicInteger(data.size() - 1000);
        var certainty = new AtomicBoolean(false);
        var rowFormat = "{\"c\":[{\"v\":\"%s\",\"f\":null},{\"v\":%s,\"f\":null},{\"v\":%s}]},";

        sb.append("{" +
                "\"cols\": [" +
                "{\"id\":\"\",\"label\":\"Date\",\"type\":\"string\"}," +
                "{\"id\":\"\",\"label\":\"Price\",\"type\":\"number\"}," +
                "{\"id\":\"\",\"role\":\"certainty\",\"type\":\"boolean\"}" +
                "]," +
                "\"rows\": ["
        );

        data.forEach((date, price) -> {
            if (counter.getAndDecrement() == 0) {
                certainty.set(true);
            }
            sb.append(String.format(rowFormat, date, price, certainty));
        });

        sb.deleteCharAt(sb.length() - 1);
        sb.append("]}");

        return sb.toString();
    }
}