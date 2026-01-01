package com.project.rate_limiter.helper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.project.rate_limiter.controller.dto.DemoEvent;

public class ResponseTextHelper {
		
	public static Map<String, Object> buildConfig(String algorithm, int limit, 
			long timePeriodLimitInMs, int refillRate) {
	    Map<String, Object> cfg = new HashMap<>();	    
	    switch (algorithm) {
	        case "FIXED_WINDOW", "SLIDING_WINDOW" -> {
	        	cfg.put("limit", limit);
	        	cfg.put("timePeriodLimitIn_Ms", timePeriodLimitInMs);
	        }
	        case "TOKEN_BUCKET" -> {
	        	cfg.put("capacity", limit);
	        	cfg.put("refillRatePerSec", refillRate);
	        }
	        default -> {}
	    };

	    return cfg;
	}

	public static List<Map<String, Object>> generateTimeline(List<DemoEvent> responseTimeline) {
		List<Map<String, Object>> timeline = new ArrayList<>();
		
		for(DemoEvent event: responseTimeline) {
			switch(event.status()) {
				case 200 -> {
					timeline.add(Map.of("remaining", event.remaining(), "status", event.status()));
				}
				case 429 -> {
					timeline.add(Map.of("retryAfterMs", event.retryAfterMs(), "status", event.status()));
				}
				case 0 -> {
					timeline.add(Map.of("comment", event.comment(), "event", "MARKER"));
				}
				default -> {
					timeline.add(Map.of("retryAfterMs", event.retryAfterMs(), "status", event.status()));
				}
			}
		}
		
		return timeline;
	}
	
	public static Map<String, Object> generateSummary(int allowed, int blocked, 
			Map<String, Object> config) {
		Map<String, Object> summary = new LinkedHashMap<>();
		
		summary.put("allowed", allowed);
		summary.put("blocked", blocked);
		summary.put("config", config);
		
		return summary;
	}
}
