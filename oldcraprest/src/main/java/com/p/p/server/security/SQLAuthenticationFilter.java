package com.p.p.server.security;

import com.p.p.server.model.bean.User;
import com.p.p.server.model.bean.UserSession;
import com.p.p.server.model.repository.SessionRepository;
import com.p.p.server.model.repository.UserRepository;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@WebFilter
public class SQLAuthenticationFilter extends org.springframework.web.filter.GenericFilterBean {

	protected static final String COOKIES_NAME = "JSESSIONID-DELIRIUM";

	protected static final String CSRF_TOKEN = "X-CSRF-TOKEN";

	final UserRepository userRepository;
	final SessionRepository sessionRepository;

	SQLAuthenticationFilter(UserRepository userRepository, SessionRepository sessionRepository) {
		this.userRepository = userRepository;
		this.sessionRepository = sessionRepository;
	}

	@Override
	public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain
	  filterChain) throws IOException, ServletException {

		final Cookie[] cookies = ((HttpServletRequest) servletRequest).getCookies();

		Cookie cookie = findCookie(cookies);

		if (cookie == null || sessionRepository.findOne(cookie.getValue()) == null) {
			final String username = servletRequest.getParameter("username");
			final String password = servletRequest.getParameter("password");
			String csrf = ((HttpServletRequest) servletRequest).getHeader("X-CSRF-TOKEN");
			csrf = csrf != null ? csrf : servletRequest.getParameter("_csrf");

			if (username != null && password != null && csrf != null && ((HttpServletRequest) servletRequest)
			  .getMethod().equals("POST")) {
				final User user = userRepository.getByMail(username);

				if (!user.getPassword().equals(password)) {
					throw new AccessDeniedException("Wrong credentials!");
				}

				((HttpServletResponse) servletResponse).setHeader(CSRF_TOKEN, csrf);
				SecurityContextHolder.getContext().setAuthentication(new UserAuthentication(user));
			} else if (((HttpServletRequest) servletRequest).getServletPath().endsWith("user/login")) {
				SecurityContextHolder.getContext().setAuthentication(new UserAuthentication(null));
			} else {
				throw new UsernameNotFoundException("User not found! May be invalid cookie!");
			}
		} else {

			final UserSession session = sessionRepository.findOne(cookie.getValue());

			if (session != null && session.getUser() != null) {
				if (!((HttpServletRequest) servletRequest).getServletPath().endsWith("user/logout")) {
					SecurityContextHolder.getContext().setAuthentication(new UserAuthentication(session.getUser()));
				} else {
					sessionRepository.delete(session);
				}
			} else {
				throw new UsernameNotFoundException("Session not found! May be invalid cookie!");
			}
		}

		filterChain.doFilter(servletRequest, servletResponse);
	}

	public static Cookie findCookie(Cookie[] cookies) {
		if (cookies != null) {
			for (Cookie c : cookies) {
				if (COOKIES_NAME.equals(c.getName())) {
					return c;
				}
			}
		}
		return null;
	}
}
