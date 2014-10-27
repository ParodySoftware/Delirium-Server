package com.p.p.server.controller;


import com.p.p.server.model.bean.User;
import com.p.p.server.model.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.annotation.web.servlet.configuration.EnableWebMvcSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
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
import java.util.ArrayList;
import java.util.Collection;

@Configuration
@EnableWebMvcSecurity
public class SecurityConfig extends WebSecurityConfigurerAdapter {

    @Autowired
    private UserRepository userRepository;

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.csrf().disable().sessionManagement().sessionAuthenticationStrategy(new SessionAuthenticationStrategy() {
            @Override
            public void onAuthentication(Authentication authentication, HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) throws SessionAuthenticationException {
                authentication.isAuthenticated();
            }
        });
        http.anonymous().disable();
        http.addFilterBefore(new Filter() {
            @Override
            public void init(FilterConfig filterConfig) throws ServletException {
                filterConfig.getServletContext().getContextPath();
            }

            @Override
            public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {

                final String username = servletRequest.getParameter("username");
                final String password = servletRequest.getParameter("username");
                final String csrf = ((HttpServletRequest)servletRequest).getHeader("X-CSRF-TOKEN");
                final String cookie = ((HttpServletRequest)servletRequest).getHeader("cookie");

                SecurityContextHolder.getContext().setAuthentication(new Authentication() {
                    @Override
                    public Collection<? extends GrantedAuthority> getAuthorities() {
                        return null;
                    }

                    @Override
                    public Object getCredentials() {
                        return null;
                    }

                    @Override
                    public Object getDetails() {
                        return null;
                    }

                    @Override
                    public Object getPrincipal() {
                        return new User("",username, "");
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
                        return username;
                    }
                });

                filterChain.doFilter(servletRequest, servletResponse);
            }

            @Override
            public void destroy() {

            }
        }, SessionManagementFilter.class);
    }

    @Autowired
    public void configureGlobal(AuthenticationManagerBuilder auth) throws Exception {
        auth.authenticationProvider(new CustomAuthenticationProvider()).userDetailsService(new UserDetailsService() {
            @Override
            public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {

                final User user = userRepository.getByMail(username);

                if (user != null) {
                    return new UserDetails() {
                        @Override
                        public Collection<? extends GrantedAuthority> getAuthorities() {
                            return user.getRoles();
                        }

                        @Override
                        public String getPassword() {
                            return user.getPassword();
                        }

                        @Override
                        public String getUsername() {
                            return user.getMail();
                        }

                        @Override
                        public boolean isAccountNonExpired() {
                            return true;
                        }

                        @Override
                        public boolean isAccountNonLocked() {
                            return user.isEnabled();
                        }

                        @Override
                        public boolean isCredentialsNonExpired() {
                            return user.isEnabled();
                        }

                        @Override
                        public boolean isEnabled() {
                            return user.isEnabled();
                        }
                    };
                } else {
                    throw new UsernameNotFoundException(String.format("User not found: %s!", username));
                }
            }
        });
    }

    private static class CustomAuthenticationProvider implements AuthenticationProvider {

        @Override
        public Authentication authenticate(final Authentication authentication) throws AuthenticationException {
            return new Authentication() {

                private boolean authenticated = true;

                @Override
                public Collection<? extends GrantedAuthority> getAuthorities() {
                    return new ArrayList<>();
                }

                @Override
                public Object getCredentials() {
                    return new ArrayList<>();
                }

                @Override
                public Object getDetails() {
                    return new ArrayList<>();
                }

                @Override
                public Object getPrincipal() {
                    return new User();
                }

                @Override
                public boolean isAuthenticated() {
                    return false;
                }

                @Override
                public void setAuthenticated(boolean b) throws IllegalArgumentException {
                    this.authenticated = b;
                }

                @Override
                public String getName() {
                    return authentication.getName();
                }
            };
        }

        @Override
        public boolean supports(Class<?> aClass) {
            return true;
        }
    }
}
