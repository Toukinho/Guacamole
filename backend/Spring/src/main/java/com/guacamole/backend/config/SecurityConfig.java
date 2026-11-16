package com.guacamole.backend.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
public class SecurityConfig {
	@Bean
	FilterRegistrationBean<JwtAuthenticationFilter> jwtFilterRegistration(JwtAuthenticationFilter filter) {
		FilterRegistrationBean<JwtAuthenticationFilter> registration = new FilterRegistrationBean<>(filter);
		registration.setEnabled(false);
		return registration;
	}
	@Bean
	SecurityFilterChain securityFilterChain(HttpSecurity http, JwtAuthenticationFilter jwt) throws Exception {
		return http.csrf(csrf -> csrf.disable())
				.logout(logout -> logout.disable())
				.sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
				.exceptionHandling(errors -> errors
						.authenticationEntryPoint((request, response, exception) -> response.sendError(401))
						.accessDeniedHandler((request, response, exception) -> response.sendError(403)))
				.authorizeHttpRequests(auth -> auth
						.requestMatchers(HttpMethod.POST, "/register", "/login").permitAll()
						.requestMatchers(HttpMethod.GET, "/courses/{id}/progress").authenticated()
						.requestMatchers(HttpMethod.GET, "/courses", "/courses/{id}", "/courses/{id}/lessons", "/lessons/{id}").permitAll()
						.anyRequest().authenticated())
				.addFilterBefore(jwt, UsernamePasswordAuthenticationFilter.class).build();
	}
}
