package com.project.rate_limiter.constants;

public enum RateLimiterAlgorithm {
	TOKEN_BUCKET,
	SLIDING_WINDOW,
	FIXED_WINDOW;
	
	public static RateLimiterAlgorithm from(String arg) {
		if(arg == null || arg.isBlank()) { return TOKEN_BUCKET; }
		
		return RateLimiterAlgorithm.valueOf(arg.trim().toUpperCase());
	}
}
