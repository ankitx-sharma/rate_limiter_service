package com.project.rate_limiter.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;

import com.project.rate_limiter.controller.dto.DemoEvent;
import com.project.rate_limiter.controller.dto.DemoRunRequest;
import com.project.rate_limiter.controller.dto.DemoRunResponse;
import com.project.rate_limiter.helper.ResponseTextHelper;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "Demo Playground", description = "Run prebuilt scenarios to visualize algorithm tradeoffs")
@RestController
@RequestMapping("/limiter/demo")
public class DemoRunController {
	
	private final RestTemplate restTemplate;
	
	@Value("${rate.request.limit.count}")
	private int limit;
	
	@Value("${rate.request.limit.timeperiod}")
	private long timePeriodLimitInMs;
	
	@Value("${rate.request.limit.refill.rate:1}")
	private int refillRate;

	public DemoRunController(RestTemplate restTemplate) {
		this.restTemplate = restTemplate;
	}
	
	@Operation(
		summary = "Run a rate limiting demo scenario using token bucket algorithm as backend logic",
		description = """
		Runs a preconfigured traffic pattern and returns a with status (200/429).\n
		→ Initial burst consumes all available tokens (5 tokens - 1 token refilled per second) \n
		→ No tokens left so requests are blocked \n
		→ Refill rate is 1 sec so after waiting 1 second, one token refills \n
		→ Requests made at 1 sec interval so they are immediately consumed on each subsequent request.
		"""
	)
	@PostMapping("/run/token")
	public DemoRunResponse runTokenBucketDemo(@RequestBody DemoRunRequest request) throws InterruptedException{
		String alg = "TOKEN_BUCKET";
		String scenario = "TOKEN_BUCKET_BURST_REFILL";
		String userId = (request.userId() == null || request.userId().isBlank() ) ? "demo_user" : request.userId();
		
		List<DemoEvent> timeline = new ArrayList<>();
		
		Map<String, Object> config = ResponseTextHelper.buildConfig(alg, limit, timePeriodLimitInMs, refillRate);
		
		runTokenBucketBurstRefill(alg, scenario, userId, timeline);
		
		int allowed = (int) timeline.stream().filter(e -> e.status() == 200).count();
		int blocked = (int) timeline.stream().filter(e -> e.status() == 429).count();
		
		return new DemoRunResponse(alg, allowed, blocked, config, timeline);
	}
	
	@Operation(
			summary = "Run a rate limiting demo scenario using sliding window algorithm as backend logic",
			description = """
			Runs a preconfigured traffic pattern and returns a with status (200/429).\n
			→ Burst fills the rolling window (5 requests per 6 seconds) \n
			→ Further requests are blocked until the oldest request expires \n 
			→ System triggered to sleep wait for certain time, the window slides forward \n
			→ All request data outside time window are purged and capacity gradually becomes available again.
			"""
		)
		@PostMapping("/run/sliding")
		public DemoRunResponse runSlidingWindowDemo(@RequestBody DemoRunRequest request) throws InterruptedException{
			String alg = "SLIDING_WINDOW";
			String scenario = "SLIDING_WINDOW_SMOOTH";
			String userId = (request.userId() == null || request.userId().isBlank() ) ? "demo_user" : request.userId();
			
			List<DemoEvent> timeline = new ArrayList<>();
			
			Map<String, Object> config = ResponseTextHelper.buildConfig(alg, limit, timePeriodLimitInMs, refillRate);
			
			runSlidingWindowSmooth(alg, scenario, userId, timeline);
			
			int allowed = (int) timeline.stream().filter(e -> e.status() == 200).count();
			int blocked = (int) timeline.stream().filter(e -> e.status() == 429).count();
			
			return new DemoRunResponse(alg, allowed, blocked, config, timeline);
		}
	
	@Operation(
			summary = "Run a rate limiting demo scenario using fixed window algorithm as backend logic",
			description = """
					Runs a preconfigured traffic pattern and returns a with status (200/429). \n
					→ System sleep wait for 5 seconds \n
					→ Burst fills the fixed window (5 requests per 6 seconds) \n
					→ Request counter reaches its limit so further calls blocked \n 
					→ When the fixed time window resets, the counter is cleared (5 requests available again for next 6 seconds window) \n
					→ At boundary, a new burst is triggered and allowed \n
					→ As a result in a window of 6 seconds we are able to trigger more than 5 calls (double-dip effect)
					"""
		)
		@PostMapping("/run/fixed")
		public DemoRunResponse runFixedWindowDemo(@RequestBody DemoRunRequest request) throws InterruptedException{
			String alg = "FIXED_WINDOW";
			String scenario = "FIXED_WINDOW_BOUNDARY_BURST";
			String userId = (request.userId() == null || request.userId().isBlank() ) ? "demo_user" : request.userId();
			
			List<DemoEvent> timeline = new ArrayList<>();
			
			Map<String, Object> config = ResponseTextHelper.buildConfig(alg, limit, timePeriodLimitInMs, refillRate);
			
			runFixedWindowBoundaryBurst(alg, scenario, userId, timeline);
			
			int allowed = (int) timeline.stream().filter(e -> e.status() == 200).count();
			int blocked = (int) timeline.stream().filter(e -> e.status() == 429).count();
			
			return new DemoRunResponse(alg, allowed, blocked, config, timeline);
		}
	
	private void runFixedWindowBoundaryBurst(String alg, String scenario, String userId, 
			List<DemoEvent> timeline) throws InterruptedException {
		callCheck(alg, userId, 1, timeline);
		
		long waitMs = Math.max(0, timePeriodLimitInMs - 200);
		Thread.sleep(waitMs);
		
		//Limit - 1: burst calls 
		for(int i=0; i<Math.max(0, limit+1); i++) {
			callCheck(alg, userId, timeline.size() + 1, timeline);
		}
		
		timeline.add(new DemoEvent(0, 0, 0, "triggered system sleep and counter resets as 6 seconds are up"));
		Thread.sleep(250);
		
		//Burst call again
		for(int i=0; i<limit; i++) {
			callCheck(alg, userId, timeline.size() + 1, timeline);
		}
	}
	
	private void runSlidingWindowSmooth(String alg, String scenario, String userId, 
			List<DemoEvent> timeline) throws InterruptedException {
		
		for(int i=0; i<limit; i++) {
			callCheck(alg, userId, i+1, timeline);
		}
		
		for(int i=0; i<5; i++) {
			callCheck(alg, userId, timeline.size()+1, timeline);
		}
		
		timeline.add(new DemoEvent(0, 0, 0, "triggered sleep for system: "+timePeriodLimitInMs+" Ms"));
		
		Thread.sleep(Math.max(200, timePeriodLimitInMs));
		
		for(int i=0; i<5; i++) {
			callCheck(alg, userId, timeline.size()+1, timeline);
			Thread.sleep(150);
		}
	}
	
	private void runTokenBucketBurstRefill(String alg, String scenario, String userId, 
			List<DemoEvent> timeline) throws InterruptedException {
		
		int burst = limit + 3;
		
		for(int i=0; i<burst; i++) {
			callCheck(alg, userId, i+1, timeline);
		}
		
		timeline.add(new DemoEvent(0, 0, 0, "triggered calls at 1 sec interval"));
		
		for(int i=0; i<5; i++) {
			Thread.sleep(1000);
			callCheck(alg, userId, timeline.size()+1, timeline);
		}
	}
	
	private void callCheck(String alg, String userId, int index,
			List<DemoEvent> timeline) {
		
		HttpHeaders headers = new HttpHeaders();
		headers.set("X-RateLimit-Alg", alg);
		headers.set("X-User-Id", userId);
		
		HttpEntity<Void> entity = new HttpEntity<>(headers);
		int status;
		HttpHeaders resultHeaders;
		
		try{
			ResponseEntity<String> res = restTemplate.exchange(
				"http://localhost:8080/limiter/api/check",
				HttpMethod.GET,
				entity,
				String.class);
			status = res.getStatusCode().value();
			resultHeaders = res.getHeaders();
		} catch (HttpStatusCodeException ex) {
			status = ex.getStatusCode().value();
			resultHeaders = ex.getResponseHeaders();
			if(resultHeaders == null ) { resultHeaders = new HttpHeaders(); }
		} catch (Exception ex) {
			status = 0;
			resultHeaders = new HttpHeaders();
		}
		
		long remaining = parseLong(resultHeaders.getFirst("X-RateLimit-Remaining"));
		long retryAfterMs = parseLong(resultHeaders.getFirst("X-RateLimit-RetryAfter-Ms"));
		
		timeline.add(new DemoEvent(status, remaining, retryAfterMs, ""));
	}
	
	private long parseLong(String v) {
		try { return (v==null) ? 0 : Long.parseLong(v); }
		catch (Exception ex) { return 0; }
	}
}
