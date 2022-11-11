package com.xm.crypto.entity;

import lombok.Builder;
import lombok.Data;

/**
 * Represents crypto statistical data
 */
@Data
@Builder
public class CryptoStats {

    private CryptoSymbol symbol;
    private Double minPrice;
    private Double maxPrice;
    private Double oldestPrice;
    private Double newestPrice;
    private Double normalizedRange;
}
