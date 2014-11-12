package com.p.p.server.controller;

import com.p.p.server.model.bean.User;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.Collection;

import static org.junit.Assert.assertNotNull;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = "classpath:META-INF/spring/applicationContext.xml")
public class UsersControllerTest extends AbstractTest {

    @Autowired
    UsersController controller;

    @Test
    public void testUserInfoWithExistingUser() throws Exception {
        controller.userInfo("mihail.n@gmail.com");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testUserInfoWithNonExistingUser() throws Exception {
        controller.userInfo("gosho@gmail.com");
    }

    @Test
    public void testUserInfoForLoggedUser() {
        SecurityContextHolder.getContext().setAuthentication(new Authentication() {
            @Override
            public Collection<? extends GrantedAuthority> getAuthorities() {
                return roleRepository.findAll();
            }

            @Override
            public Object getCredentials() {
                return roleRepository.findAll();
            }

            @Override
            public Object getDetails() {
                return "";
            }

            @Override
            public Object getPrincipal() {
                return userRepository.getByMail("mihail.n@gmail.com");
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
                return "mihail.n@gmail.com";
            }
        });

        User user = controller.userInfo();

		assertNotNull(user);
    }
}
