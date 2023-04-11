package com.crypto.advisor.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class CryptoStats {

    private Long id;

    @JsonProperty("cmc_rank")
    private Long rank;

    private String name;
    private String symbol;
    private String lastUpdated;

    private Double price;
    private Double marketCap;
    private Double percentChangeHour;
    private Double percentChangeDay;
    private Double percentChangeWeek;
    private Double percentChangeMonth;
    private Double percentChangeTwoMonths;
    private Double percentChangeThreeMonths;

    @SuppressWarnings("unchecked")
    @JsonProperty("quote")
    private void unpackFieldsFromQuote(Map<String, Object> quote) {
        Map<String, Object> node = (Map<String, Object>) quote.get("USD");

        lastUpdated = (String) node.get("last_updated");

        price = (Double) node.get("price");
        marketCap = (Double) node.get("market_cap");
        percentChangeHour = (Double) node.get("percent_change_1h");
        percentChangeDay = (Double) node.get("percent_change_24h");
        percentChangeWeek = (Double) node.get("percent_change_7d");
        percentChangeMonth = (Double) node.get("percent_change_30d");
        percentChangeTwoMonths = (Double) node.get("percent_change_60d");
        percentChangeThreeMonths = (Double) node.get("percent_change_90d");
    }
}