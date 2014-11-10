package com.p.p.server.security;


import com.p.p.server.model.bean.User;
import com.p.p.server.model.bean.UserSession;
import com.p.p.server.model.repository.SessionRepository;
import com.p.p.server.model.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.annotation.web.servlet.configuration.EnableWebMvcSecurity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.authentication.session.SessionAuthenticationException;
import org.springframework.security.web.authentication.session.SessionAuthenticationStrategy;
import org.springframework.security.web.session.SessionManagementFilter;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.file.AccessDeniedException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;

//@Configuration
//@EnableWebMvcSecurity
public class SecurityConfig extends WebSecurityConfigurerAdapter {

//    @Autowired
//    private UserRepository userRepository;
//
//    @Autowired
//    private SessionRepository sessions;
//
//    @Override
//    protected void configure(HttpSecurity http) throws Exception {
//        http.csrf().disable().sessionManagement().sessionAuthenticationStrategy(new SessionAuthenticationStrategy() {
//            @Override
//            public void onAuthentication(Authentication authentication, HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) throws SessionAuthenticationException {
//                if (authentication.isAuthenticated()) {
//                    UserSession session = sessions.findOne(httpServletRequest.getHeader("Cookie"));
//                    if (session == null) {
//                        session = new UserSession();
//                        session.setUser((User)authentication.getPrincipal());
//                        session.setId(httpServletRequest.getHeader("Cookie"));
//                        session.setCreated(new Date());
//                        session.setCsrf(httpServletRequest.getHeader("X-CSRF-TOKEN"));
//                        session.setHost(""); // TODO: Set proper host
//                    }
//                }
//            }
//        });
//        http.anonymous().disable();
//        http.addFilterBefore(new Filter() {
//            @Override
//            public void init(FilterConfig filterConfig) throws ServletException {
//                filterConfig.getServletContext().getContextPath();
//            }
//
//            @Override
//            public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
//
//                final String cookie = ((HttpServletRequest) servletRequest).getHeader("Cookie");
//
//                if (cookie == null) {
//                    final String username = servletRequest.getParameter("username");
//                    final String password = servletRequest.getParameter("password");
//                    String csrf = ((HttpServletRequest) servletRequest).getHeader("X-CSRF-TOKEN");
//                    csrf = csrf != null ? csrf : servletRequest.getParameter("_csrf");
//
//                    if (username != null && password != null && csrf != null) {
//                        final User user = userRepository.getByMail(username);
//
//                        if (!user.getPassword().equals(password)) {
//                            throw new AccessDeniedException("Wrong credentials!");
//                        }
//
//                        SecurityContextHolder.getContext().setAuthentication(new UserAuthentication(user));
//                    } else {
//                        throw new UsernameNotFoundException("User not found! May be invalid session!");
//                    }
//                } else {
//
//                    final UserSession session = sessions.findOne(cookie);
//
//                    if (session != null && session.getUser() != null) {
//                        SecurityContextHolder.getContext().setAuthentication(new UserAuthentication(session.getUser()));
//                    }
//                }
//
//                filterChain.doFilter(servletRequest, servletResponse);
//            }
//
//            @Override
//            public void destroy() {
//
//            }
//        }, SessionManagementFilter.class);
//    }
//
//    protected class UserAuthentication implements Authentication {
//
//        private final User user;
//
//        UserAuthentication(User user) {
//            this.user = user;
//        }
//
//        @Override
//        public Collection<? extends GrantedAuthority> getAuthorities() {
//            return user.getRoles();
//        }
//
//        @Override
//        public Object getCredentials() {
//            return user.getRoles();
//        }
//
//        @Override
//        public Object getDetails() {
//            return user.getName();
//        }
//
//        @Override
//        public Object getPrincipal() {
//            return user;
//        }
//
//        @Override
//        public boolean isAuthenticated() {
//            return true;
//        }
//
//        @Override
//        public void setAuthenticated(boolean b) throws IllegalArgumentException {
//
//        }
//
//        @Override
//        public String getName() {
//            return user.getMail();
//        }
//    }
}
