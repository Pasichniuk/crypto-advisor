package com.xm.crypto.service;

import com.xm.crypto.entity.*;
import com.xm.crypto.util.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.*;
import java.io.File;
import java.io.IOException;
import java.util.stream.Collectors;

/**
 * Service responsible for crypto statistics
 */
@Service
public class CryptoService {

    private static final Logger LOGGER = LoggerFactory.getLogger(CryptoService.class);

    @Value("${prices.directory}")
    private String pricesDirectory;

    private Map<CryptoSymbol, CryptoStats> cryptoStatsPerSymbol;

    /**
     * Reads all files with crypto data and puts it to cryptoStatsPerSymbol map
     */
    @PostConstruct
    public void readCryptoStatistics() {
        final var files = FileUtils.listFilesForFolder(new File(pricesDirectory));
        cryptoStatsPerSymbol = new EnumMap<>(CryptoSymbol.class);

        try {
            for (var file : files) {
                final var iterator = FileUtils.getMappingIterator(file);
                final var cryptos = iterator.readAll();
                cryptoStatsPerSymbol.put(getCryptoSymbolFromList(cryptos), buildCryptoStats(cryptos));
            }
        } catch (IOException | NoSuchElementException e) {
            LOGGER.error(e.getMessage());
        }

        cryptoStatsPerSymbol = cryptoStatsPerSymbol.entrySet()
                .stream()
                .sorted(Map.Entry.comparingByValue(Comparator.comparingDouble(CryptoStats::getNormalizedRange).reversed()))
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue,
                        (oldValue, newValue) -> oldValue, LinkedHashMap::new));
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
    private CryptoStats buildCryptoStats(final List<Crypto> cryptos) {
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
                (cryptoStats.getMaxPrice() - cryptoStats.getMinPrice()) / cryptoStats.getMinPrice());
        return cryptoStats;
    }

    /**
     * Returns the symbol of crypto provided in cryptos list
     * @param cryptos - list of crypto data
     * @return CryptoSymbol
     */
    private CryptoSymbol getCryptoSymbolFromList(final List<Crypto> cryptos) {
        return cryptos.stream()
                .map(Crypto::getSymbol)
                .distinct()
                .findFirst()
                .orElseThrow(NoSuchElementException::new);
    }
}
