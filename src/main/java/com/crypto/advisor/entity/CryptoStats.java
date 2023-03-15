package com.crypto.advisor.entity;

import lombok.Data;
import lombok.Builder;

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