package com.crypto.advisor.service;

import java.util.*;
import java.util.stream.Collectors;
import java.io.File;
import java.io.IOException;

import com.fasterxml.jackson.databind.MappingIterator;
import com.fasterxml.jackson.databind.ObjectReader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Value;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.crypto.advisor.util.FileUtils;
import com.crypto.advisor.entity.*;

/**
 * Service responsible for processing crypto statistics
 */

@Service
public class CryptoService {

    private static final Logger LOGGER = LoggerFactory.getLogger(CryptoService.class);

    private final Map<CryptoSymbol, CryptoStats> cryptoStatsPerSymbol;

    @Autowired
    public CryptoService(@Value("${prices.directory}") String pricesDirectory, ObjectReader reader) {
        this.cryptoStatsPerSymbol = buildCryptoStatsPerSymbol(pricesDirectory, reader);
    }

    public Map<CryptoSymbol, CryptoStats> getCryptoStatistics() {
        return cryptoStatsPerSymbol;
    }

    public CryptoStats getCryptoStatistics(CryptoSymbol cryptoSymbol) {
        return cryptoStatsPerSymbol.get(cryptoSymbol);
    }

    public CryptoStats getCryptoWithHighestNormalizedRange() {
        return cryptoStatsPerSymbol.values().stream()
            .findFirst()
            .orElseThrow(NoSuchElementException::new);
    }

    private static Map<CryptoSymbol, CryptoStats> buildCryptoStatsPerSymbol(
        String pricesDirectory,
        ObjectReader cryptoObjectReader
    ) {

        var files = FileUtils.listFilesInFolderByPattern(
            new File(pricesDirectory), "_values.csv"
        );

        // TODO: this logic should definitely be re-worked

        Map<CryptoSymbol, CryptoStats> cryptoStatsPerSymbolBuilder = new EnumMap<>(CryptoSymbol.class);

        try {

            for (var file : files) {

                try (MappingIterator<Crypto> iterator = cryptoObjectReader.readValues(file)) {
                    var cryptos = iterator.readAll();
                    cryptoStatsPerSymbolBuilder.put(getCryptoSymbolFromList(cryptos), buildCryptoStats(cryptos));
                }
            }

        } catch (IOException | NoSuchElementException e) {
            LOGGER.error(e.getMessage());
        }

        return cryptoStatsPerSymbolBuilder.entrySet().stream()
            .sorted(
                Map.Entry.comparingByValue(
                    Comparator.comparingDouble(CryptoStats::getNormalizedRange).reversed()
                )
            )
            .collect(
                Collectors.collectingAndThen(
                    Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue,
                        (oldValue, newValue) -> oldValue, LinkedHashMap::new
                    ),
                    Map::copyOf
                )
            );
    }

    private static CryptoStats buildCryptoStats(List<Crypto> cryptos) {

        // TODO: review this

        var cryptoStats = CryptoStats.builder()
            .symbol(getCryptoSymbolFromList(cryptos))
            .maxPrice(cryptos.stream()
                .mapToDouble(Crypto::getPrice)
                .max()
                .orElseThrow(NoSuchElementException::new))
            .minPrice(cryptos.stream()
                .mapToDouble(Crypto::getPrice)
                .min()
                .orElseThrow(NoSuchElementException::new))
            .oldestPrice(cryptos.stream()
                .sorted(Comparator.comparing(Crypto::getTimestamp))
                .mapToDouble(Crypto::getPrice)
                .findFirst()
                .orElseThrow(NoSuchElementException::new))
            .newestPrice(cryptos.stream()
                .sorted(Comparator.comparing(Crypto::getTimestamp).reversed())
                .mapToDouble(Crypto::getPrice)
                .findFirst()
                .orElseThrow(NoSuchElementException::new))
            .build();

        cryptoStats.setNormalizedRange(
            (cryptoStats.getMaxPrice() - cryptoStats.getMinPrice()) / cryptoStats.getMinPrice()
        );

        return cryptoStats;
    }

    private static CryptoSymbol getCryptoSymbolFromList(List<Crypto> cryptos) {

        // TODO: this method will be likely retired after the logic re-design

        return cryptos.stream()
            .map(Crypto::getSymbol)
            .distinct()
            .findFirst()
            .orElseThrow(NoSuchElementException::new);
    }
}