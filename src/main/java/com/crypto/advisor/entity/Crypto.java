package com.crypto.advisor.entity;

import lombok.Data;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

/**
 * Represents the data read from CSV files
 */
@Data
@JsonPropertyOrder({ "timestamp", "symbol", "price" })
public class Crypto {

    private String timestamp;
    private CryptoSymbol symbol;
    private Double price;
}
