package com.project.rate_limiter.service;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.project.rate_limiter.entity.TokenBucket;

@Service
public class TokenBucketRateLimiterService {

    @Value("${rate.request.limit.count}")
    private int CAPACITY;

    @Value("${rate.request.limit.refill.rate}")
    private int REFILL_RATE_PER_SECOND;

    private final Map<String, TokenBucket> buckets = new HashMap<>();

    public boolean isAllowed(String userId) {
        long currentTime = Instant.now().toEpochMilli();

        TokenBucket bucket = buckets.getOrDefault(userId, new TokenBucket(CAPACITY, REFILL_RATE_PER_SECOND, currentTime));
        bucket.refill(currentTime);

        if (bucket.getTokens() > 0) {
            bucket.setTokens(bucket.getTokens()-1);
            buckets.put(userId, bucket);
            return true;
        }
        return false;
    }
}
