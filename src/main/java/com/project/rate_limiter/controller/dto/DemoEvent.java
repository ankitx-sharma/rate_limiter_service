package com.project.rate_limiter.controller.dto;

public class DemoEvent {
	public int index;
	public long tMs;
	public int status;
	public long remaining;
	public long retryAfterMs;
	public long resetInMs;
	
	public DemoEvent(int index, long tMs, int status, 
			long remaining, long retryAfterMs, long resetInMs) {
		this.index = index;
		this.tMs = tMs;
		this.status = status;
		this.remaining = remaining;
		this.retryAfterMs = retryAfterMs;
		this.resetInMs = resetInMs;
	}
}
