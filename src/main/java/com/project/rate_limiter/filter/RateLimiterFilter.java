package com.project.rate_limiter.filter;

import java.io.IOException;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.project.rate_limiter.service.TokenBucketRateLimiterService;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class RateLimiterFilter extends OncePerRequestFilter{
	
	private TokenBucketRateLimiterService service = new TokenBucketRateLimiterService();
	
	@Override
	protected void doFilterInternal(HttpServletRequest request, 
			HttpServletResponse response, 
			FilterChain filterChain) throws ServletException, IOException {
		

		if(!service.isAllowed(request.getRemoteAddr())) {
			response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
			return;
		}
		
		filterChain.doFilter(request, response);
	}
	
	@Override
	protected boolean shouldNotFilter(HttpServletRequest request) {
		String path = request.getRequestURI();
		
		return path.startsWith("/swagger-ui") || 
			   path.startsWith("/v3/api-docs");
	}
	

}
