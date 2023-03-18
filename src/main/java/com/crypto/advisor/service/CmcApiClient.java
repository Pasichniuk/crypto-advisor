package com.crypto.advisor.service;

import org.apache.http.HttpHeaders;
import org.apache.http.NameValuePair;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

@Service
public class CmcApiClient {

    private static final Logger LOGGER = LoggerFactory.getLogger(CmcApiClient.class);

    // TODO: get these values from properties
    private static final String API_KEY = "8b8bab9c-6ac5-496a-8bda-e8264a4d7ac4";
    private static final String PROD_URL = "https://pro-api.coinmarketcap.com";
    private static final String LISTINGS_LATEST_ENDPOINT = "/v1/cryptocurrency/listings/latest";

    public String getLatestListings() {
        List<NameValuePair> parameters = new ArrayList<>();
        parameters.add(new BasicNameValuePair("start","1"));
        parameters.add(new BasicNameValuePair("limit","10"));
        parameters.add(new BasicNameValuePair("convert","USD"));

        try {
            return makeAPICall(PROD_URL + LISTINGS_LATEST_ENDPOINT, parameters);
        } catch (IOException | URISyntaxException e) {
            LOGGER.error("Caught exception during API call: " + e.getMessage());
        }

        return "";
    }

    public static String makeAPICall(String uri, List<NameValuePair> parameters)
            throws URISyntaxException, IOException {

        String responseContent;

        var query = new URIBuilder(uri);
        query.addParameters(parameters);

        var request = new HttpGet(query.build());
        request.setHeader(HttpHeaders.ACCEPT, "application/json");
        request.addHeader("X-CMC_PRO_API_KEY", API_KEY);

        try (var client = HttpClients.createDefault();
             var response = client.execute(request)) {

            var entity = response.getEntity();
            responseContent = EntityUtils.toString(entity);
            EntityUtils.consume(entity);
        }

        return responseContent;
    }
}