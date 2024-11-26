package com.crypto.advisor.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.math.RoundingMode;
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

    private BigDecimal price;
    private BigDecimal marketCap;
    private BigDecimal percentChangeHour;
    private BigDecimal percentChangeDay;
    private BigDecimal percentChangeWeek;
    private BigDecimal percentChangeMonth;
    private BigDecimal percentChangeTwoMonths;
    private BigDecimal percentChangeThreeMonths;

    public CryptoStats(CryptoStats statsToClone) {
        this.id = statsToClone.getId();
        this.rank = statsToClone.getRank();
        this.name = statsToClone.getName();
        this.symbol = statsToClone.getSymbol();
        this.lastUpdated = statsToClone.getLastUpdated();
        this.price = statsToClone.getPrice();
        this.marketCap = statsToClone.getMarketCap();
        this.percentChangeHour = statsToClone.getPercentChangeHour();
        this.percentChangeDay = statsToClone.getPercentChangeDay();
        this.percentChangeWeek = statsToClone.getPercentChangeWeek();
        this.percentChangeMonth = statsToClone.getPercentChangeMonth();
        this.percentChangeTwoMonths = statsToClone.getPercentChangeTwoMonths();
        this.percentChangeThreeMonths = statsToClone.getPercentChangeThreeMonths();
    }

    @SuppressWarnings("unchecked")
    @JsonProperty("quote")
    private void unpackFieldsFromQuote(Map<String, Object> quote) {
        Map<String, Object> node = (Map<String, Object>) quote.get("USD");

        lastUpdated = (String) node.get("last_updated");

        price = getRoundedValue((Double) node.get("price"));
        marketCap = getRoundedValue((Double) node.get("market_cap"));
        percentChangeHour = getRoundedValue((Double) node.get("percent_change_1h"));
        percentChangeDay = getRoundedValue((Double) node.get("percent_change_24h"));
        percentChangeWeek = getRoundedValue((Double) node.get("percent_change_7d"));
        percentChangeMonth = getRoundedValue((Double) node.get("percent_change_30d"));
        percentChangeTwoMonths = getRoundedValue((Double) node.get("percent_change_60d"));
        percentChangeThreeMonths = getRoundedValue((Double) node.get("percent_change_90d"));
    }

    private BigDecimal getRoundedValue(Double value) {
        return BigDecimal.valueOf(value).setScale(5, RoundingMode.CEILING);
    }
}