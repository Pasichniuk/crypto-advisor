package com.crypto.advisor.service;

import com.crypto.advisor.entity.CryptoStats;
import com.crypto.advisor.exception.CryptoNotFoundException;
import com.crypto.advisor.service.prediction.predict.CryptoPricePrediction;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.time.LocalDate;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
public class CryptoService {

    private static final Logger LOGGER = LoggerFactory.getLogger(CryptoService.class);

    private static final String CSV_FILE_NAME = "%s-prices.csv";

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

    public String getHistoricalData(String function, String symbol) {
        var apiResponse = avApiClient.getHistoricalData(function, symbol);

        var object = (JsonObject) JsonParser.parseString(apiResponse);
        // TODO: generate this key from function
        var data = object.get("Time Series (Digital Currency Daily)").getAsJsonObject();

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

        TreeMap<String, String> sortedHistData = new TreeMap<>(histData);
        var histDataCsvString = histMapToCsvString(sortedHistData, symbol);
        writeHistDataToCsvFile(histDataCsvString, symbol);

        double[] predictedPrices = new double[0];
        try {
            predictedPrices = CryptoPricePrediction.predict(symbol);
        } catch (IOException e) {
            e.printStackTrace();
        }

        var currentDate = LocalDate.now();
        var daysCount = new AtomicInteger(predictedPrices.length);

        Map<String, BigDecimal> bigDecimalPrices = Arrays.stream(predictedPrices).boxed()
                .map(BigDecimal::new)
                .collect(Collectors.toMap(
                        x -> currentDate.plusDays(daysCount.getAndDecrement()).toString(),
                        price -> price,
                        (x, y) -> y,
                        LinkedHashMap::new)
                );

        Map<String, BigDecimal> preparedData = new LinkedHashMap<>(bigDecimalPrices);
        histData.forEach((key, value) -> preparedData.put(key, new BigDecimal(value)));

        return histMapToJsonString(preparedData);
    }

    private void writeHistDataToCsvFile(String histData, String symbol) {
        var fileName = String.format(CSV_FILE_NAME, symbol);
        var outputFile = new File(fileName);

        try (var pw = new PrintWriter(outputFile)) {
            pw.println(histData);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    private String histMapToCsvString(Map<String, String> histData, String symbol) {
        var sb = new StringBuilder();
        histData.forEach((k, v) -> sb.append(String.format("%s,%s,%s\n", k, symbol, v)));
        return sb.toString();
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