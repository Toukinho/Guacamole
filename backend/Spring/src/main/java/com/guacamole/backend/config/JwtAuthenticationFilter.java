package com.guacamole.backend.config;

import java.io.IOException;
import java.util.List;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.server.ResponseStatusException;
import com.guacamole.backend.repositories.UserRepository;
import com.guacamole.backend.services.TokenService;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {
	private final TokenService tokens;
	private final UserRepository users;
	public JwtAuthenticationFilter(TokenService tokens, UserRepository users) { this.tokens = tokens; this.users = users; }

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
			throws ServletException, IOException {
		String authorization = request.getHeader("Authorization");
		if (authorization != null) {
			try {
				Long userId = tokens.require(authorization);
				if (!users.existsById(userId)) { response.sendError(401); return; }
				SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(userId, null, List.of()));
			} catch (ResponseStatusException ex) {
				SecurityContextHolder.clearContext();
				response.sendError(401);
				return;
			}
		}
		chain.doFilter(request, response);
	}
}
