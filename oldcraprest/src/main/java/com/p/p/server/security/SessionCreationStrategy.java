package com.p.p.server.security;

import com.p.p.server.model.bean.User;
import com.p.p.server.model.bean.UserSession;
import com.p.p.server.model.repository.SessionRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.session.SessionAuthenticationException;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Date;
import java.util.UUID;

public class SessionCreationStrategy {

	private final SessionRepository sessionRepository;

	SessionCreationStrategy(SessionRepository sessionRepository) {
		this.sessionRepository = sessionRepository;
	}

	public void createSession(Authentication authentication, HttpServletRequest request,
	  HttpServletResponse response) throws SessionAuthenticationException {

		if (authentication != null && authentication.isAuthenticated() && authentication.getPrincipal() != null
		  && authentication.getPrincipal() instanceof User) {

			Cookie cookie = AuthenticationStrategy.findCookie(request.getCookies());

			if (cookie == null || sessionRepository.findOne(cookie.getValue()) == null) {

				Cookie javaCookie = new Cookie(CustomAuthenticationFilter.COOKIES_NAME, UUID.randomUUID().toString());
				response.addCookie(javaCookie);

				UserSession userSession = new UserSession();
				userSession.setUser((User) authentication.getPrincipal());
				userSession.setId(javaCookie.getValue());
				userSession.setCreated(new Date());

				String csrf = request.getHeader("X-CSRF-TOKEN");
				csrf = csrf != null ? csrf : request.getParameter("_csrf");
				userSession.setCsrf(csrf);

				userSession.setHost(request.getRemoteHost());

				sessionRepository.save(userSession);
			}

			SecurityContextHolder.getContext().setAuthentication(authentication);
		} else {
			SecurityContextHolder.getContext().setAuthentication(new UserAuthentication(null));
		}
	}
}
