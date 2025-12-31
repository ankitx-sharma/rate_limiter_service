package com.project.rate_limiter.controller.dto;

import java.util.List;
import java.util.Map;

public class DemoRunResponse {
	public String scenario;
	public String algorithm;
	public String userId;
	public Map<String, Object> notes;
	public int allowedCount;
	public int blockedCount;
	public List<DemoEvent> timeline;
	
	public DemoRunResponse(String scenario, String algorithm, String userId, Map<String, Object> notes,
			int allowedCount, int blockedCount, List<DemoEvent> timeline) {

		this.scenario = scenario;
		this.algorithm = algorithm;
		this.userId = userId;
		this.notes = notes;
		this.allowedCount = allowedCount;
		this.blockedCount = blockedCount;
		this.timeline = timeline;
	}
}