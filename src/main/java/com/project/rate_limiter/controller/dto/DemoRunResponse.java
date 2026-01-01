package com.project.rate_limiter.controller.dto;

import java.util.List;
import java.util.Map;

import com.project.rate_limiter.helper.ResponseTextHelper;

public class DemoRunResponse {
	public String algorithm;
	public Map<String, Object> summary;
	public List<Map<String, Object>> timeline;
	
	public DemoRunResponse(String algorithm, int allowed, int blocked, 
			Map<String, Object> config, List<DemoEvent> responseTimeline) {
		this.algorithm = algorithm;
		this.summary = ResponseTextHelper.generateSummary(allowed, blocked, config);
		this.timeline = ResponseTextHelper.generateTimeline(responseTimeline);
	}
}