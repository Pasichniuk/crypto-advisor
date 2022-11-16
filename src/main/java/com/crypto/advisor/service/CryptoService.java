package com.crypto.advisor.service;

import java.util.*;
import java.util.stream.Collectors;
import java.io.File;
import java.io.IOException;

import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Value;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.crypto.advisor.util.FileUtils;
import com.crypto.advisor.entity.*;

/**
 * Service responsible for crypto statistics
 */

@Service
public class CryptoService {

    private static final Logger LOGGER = LoggerFactory.getLogger(CryptoService.class);

    private final Map<CryptoSymbol, CryptoStats> cryptoStatsPerSymbol;

    public CryptoService(
        @Value("${prices.directory}") String pricesDirectory
    ) {
        // TODO: this logic should be re-worked ASAP

        var files = FileUtils.listFilesForFolder(
            new File(pricesDirectory)
        );

        Map<CryptoSymbol, CryptoStats> cryptoStatsPerSymbolBuilder = new EnumMap<>(CryptoSymbol.class);

        try {

            for (var file : files) {
                var iterator = FileUtils.getMappingIterator(file);
                var cryptos = iterator.readAll();
                cryptoStatsPerSymbolBuilder.put(getCryptoSymbolFromList(cryptos), buildCryptoStats(cryptos));
            }

        } catch (IOException | NoSuchElementException e) {
            LOGGER.error(e.getMessage());
        }

        this.cryptoStatsPerSymbol = cryptoStatsPerSymbolBuilder.entrySet().stream()
            .sorted(Map.Entry.comparingByValue(Comparator.comparingDouble(CryptoStats::getNormalizedRange).reversed()))
            .collect(Collectors.toMap(
                Map.Entry::getKey,
                Map.Entry::getValue,
                (oldValue, newValue) -> oldValue, LinkedHashMap::new)
            );
    }

    /**
     * Getter for cryptoStatsPerSymbol map
     * @return Map<CryptoSymbol, CryptoStats>
     */
    public Map<CryptoSymbol, CryptoStats> getCryptoStatistics() {
        return cryptoStatsPerSymbol;
    }

    /**
     * Returns statistics for the provided cryptoSymbol
     * @param cryptoSymbol - the symbol for which statistics are returned
     * @return CryptoStats
     */
    public CryptoStats getCryptoStatistics(CryptoSymbol cryptoSymbol) {
        return cryptoStatsPerSymbol.get(cryptoSymbol);
    }

    /**
     * Returns statistics for the crypto with the highest normalized range
     * @return CryptoStats
     */
    public CryptoStats getCryptoWithHighestNormalizedRange() {
        return cryptoStatsPerSymbol.values().stream()
            .findFirst()
            .orElseThrow(NoSuchElementException::new);
    }

    /**
     * Builds CryptoStats objects from the provided list of crypto data
     * @param cryptos - list of crypto data
     * @return CryptoStats
     */
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

    /**
     * Returns the symbol of crypto provided in cryptos list
     * @param cryptos - list of crypto data
     * @return CryptoSymbol
     */
    private static CryptoSymbol getCryptoSymbolFromList(List<Crypto> cryptos) {

        // TODO: this method will be likely retired after the logic re-design

        return cryptos.stream()
            .map(Crypto::getSymbol)
            .distinct()
            .findFirst()
            .orElseThrow(NoSuchElementException::new);
    }
}