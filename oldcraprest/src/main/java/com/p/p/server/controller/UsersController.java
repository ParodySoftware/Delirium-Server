package com.p.p.server.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.p.p.server.model.bean.Role;
import com.p.p.server.model.bean.User;
import com.p.p.server.model.repository.RoleRepository;
import com.p.p.server.model.repository.SessionRepository;
import com.p.p.server.model.repository.UserRepository;
import com.p.p.server.security.AuthenticationStrategy;
import com.p.p.server.security.CustomAuthenticationFilter;
import com.p.p.server.security.SessionCreationStrategy;
import com.p.p.server.util.DBUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.logging.Logger;

@RestController
@RequestMapping(value = "/user")
public class UsersController {

    private static final Logger LOGGER = Logger.getLogger(UsersController.class.getName());

    @Autowired
    UserRepository userRepository;

    @Autowired
    RoleRepository roleRepository;

    @Autowired
    SessionRepository sessionRepository;

    @Autowired
    AuthenticationStrategy authenticationStrategy;

    @Autowired
    SessionCreationStrategy sessionCreationStrategy;

    @Autowired
    DBUtils dbUtils;

    @RequestMapping(value = {"/login"}, method = RequestMethod.GET)
    @ResponseStatus(HttpStatus.OK)
    public HttpEntity<String> login(HttpServletRequest request) {

        String csrf = UUID.randomUUID().toString();
        request.getSession().setAttribute("csrf", csrf);
        String body = "<html><body><form method='POST'>\n"
                + "<label>Username:</label>\n"
                + "<input name='username' type='text'>\n"
                + "<br/>\n"
                + "<label>Password:</label>\n"
                + "<input name='password' type='password'><br/>\n"
                + "<input type=\"hidden\" name=\"_csrf\" value=\"" + csrf + "\">\n"
                + "<br/>\n"
                + "<input type=\"submit\" title=\"login\" value=\"Login\">\n"
                + "</form>\n"
                + "</body>\n"
                + "</html>\n";

        MultiValueMap headers = new LinkedMultiValueMap();
        List<String> list = new ArrayList<>();
        list.add(csrf);
        headers.put(CustomAuthenticationFilter.CSRF_TOKEN, list);

        return new HttpEntity<>(body, headers);
    }

    @RequestMapping(value = {"/login"}, method = RequestMethod.POST)
    @ResponseStatus(HttpStatus.OK)
    public String login(HttpServletRequest request, HttpServletResponse response,
                        @RequestParam(required = true) String username,
                        @RequestParam(required = true) String password, String _csrf) {

        Authentication authentication = authenticationStrategy.authenticate(request, response);
        sessionCreationStrategy.createSession(authentication, request, response);
        SecurityContextHolder.getContext().setAuthentication(authentication);
        request.getSession().setAttribute("csrf", _csrf);
        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return String.format("User %s logged in!", user.getName());
    }

    @RequestMapping(value = {"/info/{userId}"}, method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseStatus(HttpStatus.OK)
    @Secured({"ROLE_ADMIN"})
    public User userInfo(@PathVariable String userId) {
        return findUser(userId);
    }

    @RequestMapping(value = {"/delete/{userId}"}, method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseStatus(HttpStatus.OK)
    public void userDelete(@PathVariable() String userId) {
        userRepository.delete(findUser(userId));
    }

    @RequestMapping(value = {"/info"}, method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseStatus(HttpStatus.OK)
    @Secured({"ROLE_ADMIN", "ROLE_USER"})
    public User userInfo() {
        String name = SecurityContextHolder.getContext().getAuthentication().getName();
        return findUser(name);
    }

    @RequestMapping(value = {"/logout"}, method = {RequestMethod.POST, RequestMethod.GET})
    @ResponseStatus(HttpStatus.OK)
    public String logout(HttpServletRequest request, String _csrf) {
        authenticationStrategy.endValidSession(request);
        return String.format("User logged out!");
    }

    @RequestMapping(value = {"/add"}, method = {RequestMethod.POST, RequestMethod.GET})
    @ResponseStatus(HttpStatus.OK)
    public void addUser(@RequestParam(required = true) String name, @RequestParam(required = true) String mail,
                        @RequestParam(required = true) String pass) {
        dbUtils.createOrGetUser(name, mail, pass, dbUtils.createOrGetRole("USER"));
    }

    @PostConstruct
    public void init() {

        Role ra = dbUtils.createOrGetRole("ADMIN");

        Role ru = dbUtils.createOrGetRole("USER");

        dbUtils.createOrGetUser("Michail Smith", "mail", "pass", ra, ru);
        dbUtils.createOrGetUser("Katran Maznejkov", "olio", "olio", ru);

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
                        "Found users: \n" + mapper.writerWithDefaultPrettyPrinter()
                                .writeValueAsString(userRepository.findAll()));
                System.out.println(
                        "Found roles: \n" + mapper.writerWithDefaultPrettyPrinter()
                                .writeValueAsString(roleRepository.findAll()));
            } catch (Exception e) {
                e.printStackTrace();
            }

            dbUtils
                    .dumpNativeQuery("select u.mail as username, r.name as authority from USERS u, ROLES r, USER_ROLES ur "
                            + "where ur.role_id = r.id and ur.user_id = u.id");
        } catch (Throwable t) {
            LOGGER.severe("Error while dumping debug info: " + t.getMessage());
        }
    }
}
