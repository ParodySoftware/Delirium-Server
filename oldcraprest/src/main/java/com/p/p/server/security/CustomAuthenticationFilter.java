package com.p.p.server.security;

import com.p.p.server.model.repository.SessionRepository;
import com.p.p.server.model.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@WebFilter
@Component
public class CustomAuthenticationFilter extends org.springframework.web.filter.GenericFilterBean {

    public static final String COOKIES_NAME = "JSESSIONID-DELIRIUM";

    public static final String CSRF_TOKEN = "X-CSRF-TOKEN";

    @Autowired
    private AuthenticationStrategy authenticationStrategy;

    @Autowired
    private SessionCreationStrategy sessionCreationStrategy;

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain
            filterChain) throws IOException, ServletException {

        if (!((HttpServletRequest) request).getServletPath().endsWith("user/login")) {
            Authentication authentication =
                    authenticationStrategy.validateSession((HttpServletRequest) request,
                            (HttpServletResponse) response);

            SecurityContextHolder.getContext().setAuthentication(authentication);
        }

        filterChain.doFilter(request, response);
    }
}
