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
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.HttpCookie;
import java.util.Collection;
import java.util.Date;
import java.util.UUID;

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

                ((HttpServletResponse)servletResponse).setHeader(CSRF_TOKEN, csrf);
				SecurityContextHolder.getContext().setAuthentication(new UserAuthentication(user));
			} else {
				throw new UsernameNotFoundException("User not found! May be invalid cookie!");
			}
		} else {

			final UserSession session = sessionRepository.findOne(cookie.getValue());

			if (session != null && session.getUser() != null) {
				SecurityContextHolder.getContext().setAuthentication(new UserAuthentication(session.getUser()));
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
