package com.p.p.server.security;

import com.p.p.server.model.bean.User;
import com.p.p.server.model.bean.UserSession;
import com.p.p.server.model.repository.SessionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.session.SessionAuthenticationException;
import org.springframework.stereotype.Service;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Date;
import java.util.UUID;

@Service
public class SessionCreationStrategy {

    @Autowired
	private SessionRepository sessionRepository;

//	public SessionCreationStrategy(SessionRepository sessionRepository) {
//		this.sessionRepository = sessionRepository;
//	}

	public void createSession(Authentication authentication, HttpServletRequest request,
	  HttpServletResponse response) throws SessionAuthenticationException {

		if (authentication != null && authentication.isAuthenticated() && authentication.getPrincipal() != null
		  && authentication.getPrincipal() instanceof User) {

            Cookie cookie = AuthenticationStrategy.findCookie(request.getCookies());

            if (cookie == null || sessionRepository.findOne(cookie.getValue()) == null) {

                Cookie javaCookie = new Cookie(CustomAuthenticationFilter.COOKIES_NAME, UUID.randomUUID().toString());
                javaCookie.setPath("/oldcrap-rest/");
                javaCookie.setHttpOnly(true);
                response.addCookie(javaCookie);

                UserSession userSession = new UserSession();
                userSession.setUser((User) authentication.getPrincipal());
                userSession.setId(javaCookie.getValue());
                userSession.setCreated(new Date());

                userSession.setCsrf((String) request.getSession().getAttribute("csrf"));

                userSession.setHost("");

                sessionRepository.save(userSession);
            }
        }
	}
}
