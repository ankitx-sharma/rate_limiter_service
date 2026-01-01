package com.project.rate_limiter.service;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.project.rate_limiter.entity.RateLimiterDecision;
import com.project.rate_limiter.entity.TokenBucket;

@Service
public class TokenBucketRateLimiterService {

    @Value("${rate.request.limit.count}")
    private int CAPACITY;

    @Value("${rate.request.limit.refill.rate}")
    private int REFILL_RATE_PER_SECOND;

    private final Map<String, TokenBucket> buckets = new HashMap<>();

    public RateLimiterDecision decision(String user) {
		long now = Instant.now().toEpochMilli();
		return decision(user, now);
	}
	
	public RateLimiterDecision decision(String user, long currentTime) {
		TokenBucket bucket = buckets.getOrDefault(user, new TokenBucket(CAPACITY, REFILL_RATE_PER_SECOND, currentTime));
        bucket.refill(currentTime);
        
        if (bucket.getTokens() > 0) {
            bucket.setTokens(bucket.getTokens()-1);
            buckets.put(user, bucket);

            int remaining = Math.max(0, bucket.getTokens());
            long msPerToken = (1000L / REFILL_RATE_PER_SECOND);
            long missing = Math.max(0, CAPACITY - bucket.getTokens());
            long resetInMs =  msPerToken * missing;
            
            return new RateLimiterDecision(true, remaining, 0, resetInMs);
            
        }
        
        long retryAfterMs = (1000L / REFILL_RATE_PER_SECOND);
		return new RateLimiterDecision(false, 0, retryAfterMs, retryAfterMs);
	}
	
	public boolean isAllowed(String user) {
		return decision(user).isAllowed();
	}
}
