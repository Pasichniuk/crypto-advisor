package com.crypto.advisor.model;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class CryptoData {
    private String date;
    private String symbol;
    private double price;
}