package com.p.p.server.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.p.p.server.model.bean.Role;
import com.p.p.server.model.bean.User;
import com.p.p.server.model.bean.UserSession;
import com.p.p.server.model.repository.RoleRepository;
import com.p.p.server.model.repository.SessionRepository;
import com.p.p.server.model.repository.UserRepository;
import com.p.p.server.security.SQLAuthenticationFilter;
import com.p.p.server.util.DBUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Controller;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;

import javax.annotation.PostConstruct;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import java.text.DateFormat;
import java.util.UUID;
import java.util.logging.Logger;

@Controller
public class UsersController {

    private static final Logger LOGGER = Logger.getLogger(UsersController.class.getName());

    @Autowired
    UserRepository userRepository;

    @Autowired
    RoleRepository roleRepository;

    @Autowired
    SessionRepository sessionRepository;

    @Autowired
    DBUtils dbUtils;

    @RequestMapping(value = {"/login"}, method = RequestMethod.GET)
    @ResponseBody
    @ResponseStatus(HttpStatus.OK)
    public HttpEntity<String> login() {
        String csrf = UUID.randomUUID().toString();
        String body =  "<html><body><form method='POST'>" +
                "Username: <input name='username' type='text'/><br/>" +
                "Password: <input name='password' type='password'/><br/>" +
                "<input type=\"hidden\" name=\"_csrf\" value=\"" +csrf+ "/> " +
                "</form></body></html>";
        MultiValueMap headers = new LinkedMultiValueMap();
        headers.put("X-CSRF-TOKEN", csrf);
        return new HttpEntity<>(body, headers);
    }

    @RequestMapping(value = {"/login"}, method = RequestMethod.POST)
    @ResponseBody
    @ResponseStatus(HttpStatus.OK)
    public String login(String username, String password, String _csrf) {
        User user = (User)SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return String.format("User %s logged in!", user.getName());
    }

    @RequestMapping(value = {"/user/{userId}"}, method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    @ResponseStatus(HttpStatus.OK)
    public User userInfo(@PathVariable String userId) {
        return findUser(userId);
    }

    @RequestMapping(value = {"/user"}, method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    @ResponseStatus(HttpStatus.OK)
    public User userInfo() {
        String name = SecurityContextHolder.getContext().getAuthentication().getName();
        return findUser(name);
    }

    @RequestMapping(value = {"/logout"}, method = {RequestMethod.POST, RequestMethod.POST})
    @ResponseBody
    @ResponseStatus(HttpStatus.OK)
    public String logout(HttpServletRequest request, String _csrf) {
        Cookie cookie = SQLAuthenticationFilter.findCookie(request.getCookies());
        UserSession session = sessionRepository.findOne(cookie.getValue());
        if (session != null) {
            sessionRepository.delete(session);
        }
        return String.format("User %s logged out!", session.getUser().getName());
    }

    @PostConstruct
    public void initAdminUser() {

        User user = userRepository.getByMail("mail");

        if (user == null) {
            user = new User("Michail Smith", "mail", "pass");
            userRepository.save(user);
            Role role = new Role("ADMIN");
            role.getUsers().add(user);
            roleRepository.save(role);
        }

        dumpData();
    }

    private User findUser(String username) {

        dumpData();

        User user = userRepository.findOne(username);
        user = user != null ? user : userRepository.getByMail(username);

        if (user != null) {
            LOGGER.info(String.format("Getting info for user %s.", user));
            return user;
        } else {
            throw new IllegalArgumentException(String.format("User with ID = %s not found!", username));
        }
    }

    private void dumpData() {
        try {
            ObjectMapper mapper = new ObjectMapper();
            mapper.setDateFormat(DateFormat.getDateTimeInstance());

            try {
                System.out.println(
                        "Found users: \n" + mapper.writerWithDefaultPrettyPrinter().writeValueAsString(userRepository.findAll()));
                System.out.println(
                        "Found roles: \n" + mapper.writerWithDefaultPrettyPrinter().writeValueAsString(roleRepository.findAll()));
            } catch (Exception e) {
                e.printStackTrace();
            }

            dbUtils.dumpNativeQuery("select u.mail as username, r.name as authority from USERS u, ROLES r, USER_ROLES ur "
                    + "where ur.role_id = r.id and ur.user_id = u.id");
        } catch (Throwable t) {
            LOGGER.severe("Error while dumping debug info: " + t.getMessage());
        }
    }
}
