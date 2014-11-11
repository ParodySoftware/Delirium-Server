package com.p.p.server.security;

import com.p.p.server.model.bean.User;
import com.p.p.server.model.bean.UserSession;
import com.p.p.server.model.repository.SessionRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.session.SessionAuthenticationException;
import org.springframework.security.web.authentication.session.SessionAuthenticationStrategy;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Date;
import java.util.UUID;

public class AuthenticationStrategy implements SessionAuthenticationStrategy {

    private final SessionRepository sessionRepository;

    AuthenticationStrategy(SessionRepository sessionRepository) {
        this.sessionRepository = sessionRepository;
    }

    @Override
    public void onAuthentication(Authentication authentication, HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) throws SessionAuthenticationException {
        if (authentication.isAuthenticated()) {
            Cookie javaCookie = new Cookie(SQLAuthenticationFilter.COOKIES_NAME, UUID.randomUUID().toString());
            httpServletResponse.addCookie(javaCookie);

            UserSession userSession = new UserSession();
            userSession.setUser((User)authentication.getPrincipal());
            userSession.setId(javaCookie.getValue());
            userSession.setCreated(new Date());
            userSession.setCsrf(httpServletResponse.getHeader(SQLAuthenticationFilter.CSRF_TOKEN));
            userSession.setHost("");
            sessionRepository.save(userSession);
        }
    }
}
