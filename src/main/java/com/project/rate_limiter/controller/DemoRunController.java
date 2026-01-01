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
	private long windowMs;
	
	@Value("${rate.request.limit.refill.rate:1}")
	private int refillRate;

	public DemoRunController(RestTemplate restTemplate) {
		this.restTemplate = restTemplate;
	}

	@Operation(
		summary = "Run a rate limiting demo scenario using fixed window algorithm as backend logic",
		description = "Runs a preconfigured traffic pattern and returns a timeline (200/429 + remaining/retryAfter/reset)."
	)
	@PostMapping("/run/fixed")
	public DemoRunResponse runFixedWindowDemo(@RequestBody DemoRunRequest request) throws InterruptedException{
		String alg = "FIXED_WINDOW";
		String scenario = "FIXED_WINDOW_BOUNDARY_BURST";
		String userId = (request.userId() == null || request.userId().isBlank() ) ? "demo_user" : request.userId();
		
		long start = System.currentTimeMillis();
		List<DemoEvent> timeline = new ArrayList<>();
		
		Map<String, Object> config = ResponseTextHelper.buildConfig(alg, limit, start, refillRate);
		
		runFixedWindowBoundaryBurst(alg, scenario, userId, start, timeline);
		
		int allowed = (int) timeline.stream().filter(e -> e.status() == 200).count();
		int blocked = (int) timeline.stream().filter(e -> e.status() == 429).count();
		
		return new DemoRunResponse(alg, allowed, blocked, config, timeline);
	}
	
	@Operation(
		summary = "Run a rate limiting demo scenario using sliding window algorithm as backend logic",
		description = "Runs a preconfigured traffic pattern and returns a timeline (200/429 + remaining/retryAfter/reset)."
	)
	@PostMapping("/run/sliding")
	public DemoRunResponse runSlidingWindowDemo(@RequestBody DemoRunRequest request) throws InterruptedException{
		String alg = "SLIDING_WINDOW";
		String scenario = "SLIDING_WINDOW_SMOOTH";
		String userId = (request.userId() == null || request.userId().isBlank() ) ? "demo_user" : request.userId();
		
		long start = System.currentTimeMillis();
		List<DemoEvent> timeline = new ArrayList<>();
		
		Map<String, Object> config = ResponseTextHelper.buildConfig(alg, limit, start, refillRate);
		
		runSlidingWindowSmooth(alg, scenario, userId, start, timeline);
		
		int allowed = (int) timeline.stream().filter(e -> e.status() == 200).count();
		int blocked = (int) timeline.stream().filter(e -> e.status() == 429).count();
		
		return new DemoRunResponse(alg, allowed, blocked, config, timeline);
	}
	
	@Operation(
		summary = "Run a rate limiting demo scenario using token bucket algorithm as backend logic",
		description = "Runs a preconfigured traffic pattern and returns a timeline (200/429 + remaining/retryAfter/reset)."
	)
	@PostMapping("/run/token")
	public DemoRunResponse runTokenBucketDemo(@RequestBody DemoRunRequest request) throws InterruptedException{
		String alg = "TOKEN_BUCKET";
		String scenario = "TOKEN_BUCKET_BURST_REFILL";
		String userId = (request.userId() == null || request.userId().isBlank() ) ? "demo_user" : request.userId();
		
		long start = System.currentTimeMillis();
		List<DemoEvent> timeline = new ArrayList<>();
		
		Map<String, Object> config = ResponseTextHelper.buildConfig(alg, limit, start, refillRate);
		
		runTokenBucketBurstRefill(alg, scenario, userId, start, timeline);
		
		int allowed = (int) timeline.stream().filter(e -> e.status() == 200).count();
		int blocked = (int) timeline.stream().filter(e -> e.status() == 429).count();
		
		return new DemoRunResponse(alg, allowed, blocked, config, timeline);
	}
	
	private void runFixedWindowBoundaryBurst(String alg, String scenario, String userId, 
			long start,	List<DemoEvent> timeline) throws InterruptedException {
		
		callCheck(alg, userId, start, timeline, 1);
		
		long waitMs = Math.max(0, windowMs - 200);
		Thread.sleep(waitMs);
		
		//Limit - 1: burst calls 
		for(int i=0; i<Math.max(0, limit-1); i++) {
			callCheck(alg, userId, start, timeline, timeline.size() + 1);
		}
		
		//short delay at boundary
		Thread.sleep(250);
		
		//Burst call again
		for(int i=0; i<limit; i++) {
			callCheck(alg, userId, start, timeline, timeline.size() + 1);
		}
	}
	
	private void runSlidingWindowSmooth(String alg, String scenario, String userId, 
			long start,	List<DemoEvent> timeline) throws InterruptedException {
		
		for(int i=0; i<limit; i++) {
			callCheck(alg, userId, start, timeline, i+1);
		}
		
		for(int i=0; i<5; i++) {
			callCheck(alg, userId, start, timeline, timeline.size()+1);
		}
		
		Thread.sleep(Math.max(200, windowMs / 2));
		
		for(int i=0; i<5; i++) {
			callCheck(alg, userId, start, timeline, timeline.size()+1);
			Thread.sleep(150);
		}
	}
	
	private void runTokenBucketBurstRefill(String alg, String scenario, String userId, 
			long start,	List<DemoEvent> timeline) throws InterruptedException {
		
		int burst = limit + 3;
		
		for(int i=0; i<burst; i++) {
			callCheck(alg, userId, start, timeline, i+1);
		}
		
		for(int i=0; i<5; i++) {
			Thread.sleep(1000);
			callCheck(alg, userId, start, timeline, timeline.size()+1);
		}
	}
	
	private void callCheck(String alg, String userId, long start, 
			List<DemoEvent> timeline, int index) {
		
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
		
		timeline.add(new DemoEvent(status, remaining, retryAfterMs));
	}
	
	private long parseLong(String v) {
		try { return (v==null) ? 0 : Long.parseLong(v); }
		catch (Exception ex) { return 0; }
	}
}
