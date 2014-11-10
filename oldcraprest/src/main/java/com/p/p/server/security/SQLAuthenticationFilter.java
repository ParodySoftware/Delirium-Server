package com.p.p.server.security;

import com.p.p.server.model.bean.User;
import com.p.p.server.model.bean.UserSession;
import com.p.p.server.model.repository.SessionRepository;
import com.p.p.server.model.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import javax.servlet.*;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.HttpCookie;
import java.util.Collection;
import java.util.Date;
import java.util.UUID;

public class SQLAuthenticationFilter extends org.springframework.web.filter.GenericFilterBean {

	private static final String COOKIES_NAME = "JSESSIONID";

	@Autowired
	UserRepository userRepository;

	@Autowired
	SessionRepository sessionRepository;

	@Override
	public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain
	  filterChain) throws IOException, ServletException {

		final String cookie = ((HttpServletRequest) servletRequest).getHeader("Cookie");

		if (cookie == null) {
			final String username = servletRequest.getParameter("username");
			final String password = servletRequest.getParameter("password");
			String csrf = ((HttpServletRequest) servletRequest).getHeader("X-CSRF-TOKEN");
			csrf = csrf != null ? csrf : servletRequest.getParameter("_csrf");

			if (username != null && password != null && csrf != null) {
				final User user = userRepository.getByMail(username);

				if (!user.getPassword().equals(password)) {
					throw new AccessDeniedException("Wrong credentials!");
				}


				Cookie javaCookie = new Cookie("JSESSIONID", UUID.randomUUID().toString());
				((HttpServletResponse)servletResponse).addCookie(javaCookie);

				UserSession userSession = new UserSession();
				userSession.setUser(user);
				userSession.setId(javaCookie.getValue());
				userSession.setCreated(new Date());
				userSession.setCsrf(csrf);
				userSession.setHost("");
				sessionRepository.save(userSession);

				servletRequest.getServletContext().setAttribute("authentication", new UserAuthentication(user));
			} else {
				throw new UsernameNotFoundException("User not found! May be invalid cookie!");
			}
		} else {

			final UserSession session = sessionRepository.findOne(cookie);

			if (session != null && session.getUser() != null) {
				SecurityContextHolder.getContext().setAuthentication(new UserAuthentication(session.getUser()));
			}
		}

		filterChain.doFilter(servletRequest, servletResponse);
	}

	protected class UserAuthentication implements Authentication {

		private final User user;

		UserAuthentication(User user) {
			this.user = user;
		}

		@Override
		public Collection<? extends GrantedAuthority> getAuthorities() {
			return user.getRoles();
		}

		@Override
		public Object getCredentials() {
			return user.getRoles();
		}

		@Override
		public Object getDetails() {
			return user.getName();
		}

		@Override
		public Object getPrincipal() {
			return user;
		}

		@Override
		public boolean isAuthenticated() {
			return true;
		}

		@Override
		public void setAuthenticated(boolean b) throws IllegalArgumentException {

		}

		@Override
		public String getName() {
			return user.getMail();
		}
	}
}
