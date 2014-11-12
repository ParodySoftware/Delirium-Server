package com.p.p.server.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.p.p.server.model.bean.Role;
import com.p.p.server.model.bean.User;
import com.p.p.server.model.bean.UserSession;
import com.p.p.server.model.repository.RoleRepository;
import com.p.p.server.model.repository.SessionRepository;
import com.p.p.server.model.repository.UserRepository;
import com.p.p.server.security.CustomAuthenticationFilter;
import com.p.p.server.util.DBUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;

import javax.annotation.PostConstruct;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
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
	DBUtils dbUtils;

	@RequestMapping(value = { "/login" }, method = RequestMethod.GET)
	@ResponseStatus(HttpStatus.OK)
	public HttpEntity<String> login() {

		String csrf = UUID.randomUUID().toString();

		String body = "<html><body><form method='POST'>\n"
		  + "<label>Username:</label>\n"
		  + "<input name='username' type='text'>\n"
		  + "<br/>\n"
		  + "<label>Password:</label>\n"
		  + "<input name='password' type='password'><br/>\n"
		  + "<input type=\"hidden\" name=\"_csrf\" value=\"735d21d8-062d-4e02-b170-761d0df81e9a\">\n"
		  + "<br/>\n"
		  + "<input type=\"submit\" title=\"login\" value=\"Login\">\n"
		  + "</form>\n"
		  + "</body>\n"
		  + "</html>\n";

		MultiValueMap headers = new LinkedMultiValueMap();
		List list = new ArrayList<>();
		list.add(csrf);
		headers.put("X-CSRF-TOKEN", list);

		return new HttpEntity<>(body, headers);
	}

	@RequestMapping(value = { "/login" }, method = RequestMethod.POST)
	@ResponseStatus(HttpStatus.OK)
	public String login(String username, String password, String _csrf) {
		User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
		return String.format("User %s logged in!", user.getName());
	}

	@RequestMapping(value = { "/info/{userId}" }, method = { RequestMethod.GET, RequestMethod.POST })
	@ResponseStatus(HttpStatus.OK)
	public User userInfo(@PathVariable String userId) {
		return findUser(userId);
	}

	@RequestMapping(value = { "/delete/{userId}" }, method = { RequestMethod.GET, RequestMethod.POST })
	@ResponseStatus(HttpStatus.OK)
	public void userDelete(@PathVariable String userId) {
		userRepository.delete(findUser(userId));
	}

	@RequestMapping(value = { "/info" }, method = { RequestMethod.GET, RequestMethod.POST })
	@ResponseStatus(HttpStatus.OK)
	@Secured({"ROLE_ADMIN", "ROLE_USER"})
	public User userInfo() {
		String name = SecurityContextHolder.getContext().getAuthentication().getName();
		return findUser(name);
	}

	@RequestMapping(value = { "/logout" }, method = { RequestMethod.POST, RequestMethod.GET })
	@ResponseStatus(HttpStatus.OK)
	public String logout(HttpServletRequest request, String csrf) {

		return String.format("User logged out!");
	}

	@RequestMapping(value = { "/add" }, method = { RequestMethod.POST, RequestMethod.GET })
	@ResponseStatus(HttpStatus.OK)
	public void addUser(String name, String mail, String pass) {
		dbUtils.createOrGetUser(name, mail, pass, dbUtils.createOrGetRole("USER"));
	}

	@PostConstruct
	public void init() {

		Role ra = dbUtils.createOrGetRole("ADMIN");

		Role ru = dbUtils.createOrGetRole("USER");

		dbUtils.createOrGetUser("Michail Smith", "mail", "pass", new Role[] {ra, ru});

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
