package com.crypto.advisor.filter;

import java.time.Duration;
import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.stereotype.Component;
import org.springframework.http.HttpStatus;
import org.springframework.web.servlet.HandlerInterceptor;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Refill;
import io.github.bucket4j.Bandwidth;

/**
 * Interceptor responsible for rate limiting
 */
@Component
public class RateLimitInterceptor implements HandlerInterceptor {

    private final Bucket tokenBucket;

    public RateLimitInterceptor() {

        var limit = Bandwidth.classic(50, Refill.greedy(50, Duration.ofMinutes(5)));

        this.tokenBucket = Bucket.builder()
                .addLimit(limit)
                .build();
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws IOException {

        var probe = tokenBucket.tryConsumeAndReturnRemaining(1);

        if (probe.isConsumed()) {
            response.addHeader("X-Rate-Limit-Remaining", String.valueOf(probe.getRemainingTokens()));
            return true;
        } else {
            long waitForRefill = probe.getNanosToWaitForRefill() / 1_000_000_000;
            response.addHeader("X-Rate-Limit-Retry-After-Seconds", String.valueOf(waitForRefill));
            response.sendError(HttpStatus.TOO_MANY_REQUESTS.value());
            return false;
        }
    }
}