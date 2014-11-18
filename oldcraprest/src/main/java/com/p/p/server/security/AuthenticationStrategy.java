package com.p.p.server.security;

import com.p.p.server.model.bean.User;
import com.p.p.server.model.bean.UserSession;
import com.p.p.server.model.repository.SessionRepository;
import com.p.p.server.model.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.authentication.session.SessionAuthenticationException;
import org.springframework.stereotype.Service;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Service
public class AuthenticationStrategy {

    @Autowired
    private SessionRepository sessions;

    @Autowired
    private UserRepository users;

    public Authentication authenticate(HttpServletRequest request, HttpServletResponse response) {
        Cookie cookie = findCookie(request.getCookies());

        if (cookie == null || SecurityContextHolder.getContext().getAuthentication() == null
                || sessions.findOne(cookie.getValue()) == null) {

            invalidateSession(request, response);

            final String username = request.getParameter("username");
            final String password = request.getParameter("password");

            if (username != null && password != null && checkCsrf(request) && request.getMethod().equals("POST")) {
                final User user = users.getByMail(username);

                // Check user exists
                if (user == null) {
                    throw new UsernameNotFoundException("User not found!");
                }

                // Check user password
                if (!user.getPassword().equals(password)) {
                    throw new AccessDeniedException("Wrong credentials!");
                }

                return new UserAuthentication(user);
            } else {
                throw new UsernameNotFoundException("User not found!");
            }
        } else {
            throw new SessionAuthenticationException("Session already exists!");
        }
    }

    public void endValidSession(HttpServletRequest request) {
        Cookie cookie = findCookie(request.getCookies());

        if (cookie != null) {
            final UserSession session = sessions.findOne(cookie.getValue());

            // Delete the session if user requested logout
            if (session != null && checkCsrf(request)) {
                sessions.delete(session);
                request.getSession().removeAttribute("csrf");
            }
        }
    }

    protected Authentication validateSession(HttpServletRequest request, HttpServletResponse response) {
        Cookie cookie = findCookie(request.getCookies());

        if (cookie != null) {
            final UserSession session = sessions.findOne(cookie.getValue());

            // Validate existing session
            if (session != null && session.getUser() != null) {
                Authentication authentication = new UserAuthentication(session.getUser());
                SecurityContextHolder.getContext().setAuthentication(authentication);
                return authentication;
            } else {
                invalidateSession(request, response);
                request.getSession().removeAttribute("csrf");
                throw new SessionAuthenticationException("Invalid session found!");
            }
        } else {
            throw new SessionAuthenticationException("Session not found!");
        }
    }

    protected void invalidateSession(HttpServletRequest request, HttpServletResponse response) {
        Cookie cookie = findCookie(request.getCookies());

        if (cookie != null) {
            final UserSession session = sessions.findOne(cookie.getValue());

            // Try to invalidate and delete the session and the cookie
            cookie.setMaxAge(1);
            response.addCookie(cookie);

            if (session != null) {
                sessions.delete(session);
            }
        }
    }

    protected static Cookie findCookie(Cookie[] cookies) {
        if (cookies != null) {
            for (Cookie c : cookies) {
                if (CustomAuthenticationFilter.COOKIES_NAME.equals(c.getName())) {
                    return c;
                }
            }
        }
        return null;
    }

    protected static boolean checkCsrf(HttpServletRequest request) {
        String csrf = request.getHeader(CustomAuthenticationFilter.CSRF_TOKEN);
        csrf = csrf != null ? csrf : request.getParameter("_csrf");
        String sessionCSRF = (String) request.getSession().getAttribute("csrf");
        return csrf != null && sessionCSRF != null && csrf.equals(sessionCSRF);
    }
}
