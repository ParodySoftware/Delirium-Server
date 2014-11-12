package com.p.p.server.security;

import com.p.p.server.model.repository.SessionRepository;
import com.p.p.server.model.repository.UserRepository;
import org.springframework.security.core.Authentication;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@WebFilter
public class CustomAuthenticationFilter extends org.springframework.web.filter.GenericFilterBean {

	protected static final String COOKIES_NAME = "JSESSIONID-DELIRIUM";

	protected static final String CSRF_TOKEN = "X-CSRF-TOKEN";

	private final AuthenticationStrategy authenticationStrategy;
	private final SessionCreationStrategy sessionCreationStrategy;

	public CustomAuthenticationFilter(UserRepository userRepository, SessionRepository sessionRepository) {
		this.sessionCreationStrategy = new SessionCreationStrategy(sessionRepository);
		this.authenticationStrategy = new AuthenticationStrategy(sessionRepository, userRepository);
	}

	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain
	  filterChain) throws IOException, ServletException {

		Authentication authentication =
		  authenticationStrategy.authenticate((HttpServletRequest) request, (HttpServletResponse) response);

		sessionCreationStrategy
		  .createSession(authentication, (HttpServletRequest) request, (HttpServletResponse) response);

		filterChain.doFilter(request, response);
	}
}
