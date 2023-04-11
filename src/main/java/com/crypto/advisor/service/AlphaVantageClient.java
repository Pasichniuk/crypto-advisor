package com.crypto.advisor.service;

import org.apache.http.NameValuePair;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

@Service
public class AlphaVantageClient {

    private static final Logger LOGGER = LoggerFactory.getLogger(AlphaVantageClient.class);

    private final String url;
    private final String apiKey;

    public AlphaVantageClient(@Value("${alpha-vantage-api-url}") String url,
                              @Value("${alpha-vantage-api-key}") String apiKey) {
        this.url = url;
        this.apiKey = apiKey;
    }

    public String getHistoricalData(String function, String symbol) {
        List<NameValuePair> parameters = new ArrayList<>();
        parameters.add(new BasicNameValuePair("function",function));
        parameters.add(new BasicNameValuePair("symbol",symbol));
        parameters.add(new BasicNameValuePair("market","CNY"));
        parameters.add(new BasicNameValuePair("apikey", apiKey));

        try {
            return makeAPICall(parameters);
        } catch (IOException | URISyntaxException e) {
            LOGGER.error("Caught exception during API call: " + e.getMessage());
        }

        return "";
    }

    private String makeAPICall(List<NameValuePair> parameters) throws URISyntaxException, IOException {
        String responseContent;

        var query = new URIBuilder(url);
        query.addParameters(parameters);

        var request = new HttpGet(query.build());

        try (var client = HttpClients.createDefault();
             var response = client.execute(request)) {

            var entity = response.getEntity();
            responseContent = EntityUtils.toString(entity);
            EntityUtils.consume(entity);
        }

        return responseContent;
    }
}