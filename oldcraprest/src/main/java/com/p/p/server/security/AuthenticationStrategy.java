package com.p.p.server.security;

import com.p.p.server.model.bean.User;
import com.p.p.server.model.bean.UserSession;
import com.p.p.server.model.repository.SessionRepository;
import com.p.p.server.model.repository.UserRepository;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class AuthenticationStrategy {

	private final SessionRepository sessions;
	private final UserRepository users;

	public AuthenticationStrategy(SessionRepository sessions, UserRepository users) {
		this.sessions = sessions;
		this.users = users;
	}

	public Authentication authenticate(HttpServletRequest request, HttpServletResponse response) {

		Cookie cookie = findCookie(request.getCookies());

		if (cookie == null || sessions.findOne(cookie.getValue()) == null) {
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

			} else if (request.getServletPath().endsWith("user/login")) {
				return null;
			} else {
				throw new UsernameNotFoundException("User not found!");
			}
		} else {
            return validateSession(request, response);
		}
	}

    protected Authentication validateSession(HttpServletRequest request, HttpServletResponse response) {

        Cookie cookie = findCookie(request.getCookies());

        final UserSession session = sessions.findOne(cookie.getValue());

        if (session != null && session.getUser() != null) {
            if (!request.getServletPath().endsWith("user/logout")) {
                // Logout the currently logged user
                Authentication authentication = new UserAuthentication(session.getUser());
                SecurityContextHolder.getContext().setAuthentication(authentication);
                return authentication;
            } else {
                // Delete the session if user requested logout
                if (checkCsrf(request)) {
                    sessions.delete(session);
                    return null;
                } else {
                    throw new IllegalArgumentException("Wrong CSFR found - attempt to end wrong session!");
                }
            }
        } else {
            // Try to invalidate and delete the session and the cookie
            Cookie javaCookie = new Cookie(CustomAuthenticationFilter.COOKIES_NAME, cookie.getValue());
            javaCookie.setMaxAge(1);
            response.addCookie(javaCookie);
            if (session != null) {
                sessions.delete(session);
            }
            throw new UsernameNotFoundException("Session not found!");
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

    protected boolean checkCsrf(HttpServletRequest request) {
        String csrf = request.getHeader(CustomAuthenticationFilter.CSRF_TOKEN);
        csrf = csrf != null ? csrf : request.getParameter("_csrf");
        String sessionCSRF = (String)request.getSession().getAttribute("csrf");
        return csrf != null && sessionCSRF != null && csrf.equals(sessionCSRF);
    }
}
