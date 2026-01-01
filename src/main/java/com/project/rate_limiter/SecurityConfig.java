package com.project.rate_limiter;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {
	
	@Bean
	public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
		http.authorizeHttpRequests(request -> 
			request.requestMatchers("/limiter/api/**").permitAll()
				   .requestMatchers("/swagger-ui/**", "swagger-ui.html", "/v3/api-docs/**").permitAll()
				   .requestMatchers("/limiter/demo/**").permitAll()
				.anyRequest().authenticated())
			.csrf( csrf -> csrf.disable())
			.httpBasic(Customizer.withDefaults());
		
		return http.build();
	}

}
