package com.crypto.advisor.entity;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class CryptoData {
    private String date;
    private String symbol;
    private double price;
}