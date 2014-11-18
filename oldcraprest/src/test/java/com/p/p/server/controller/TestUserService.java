package com.p.p.server.controller;


import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.p.p.server.model.bean.User;
import com.p.p.server.security.CustomAuthenticationFilter;
import junit.framework.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.http.*;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.net.URI;
import java.text.DateFormat;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = "classpath:META-INF/spring/applicationContext.xml")
public class TestUserService {

    private static RestTemplate restTemplate = new RestTemplate();

    private static ObjectMapper objectMapper = new ObjectMapper() {{
        setDateFormat(DateFormat.getDateTimeInstance());
    }};

    @Test
    @Ignore
    public void testUserLogin() {
        userLogin("mail","passs");
    }

    @Test
    @Ignore
    public void testUserInfo() throws IOException {

        String sessionCookie = userLogin("mail", "pass");

        HttpHeaders requestHeaders = new HttpHeaders();
        requestHeaders.add("Cookie", sessionCookie);

        HttpEntity<MultiValueMap<String,String>> requestEntity = new HttpEntity<>(null, requestHeaders);
        ResponseEntity<String> responseJSON = restTemplate.exchange(
                "http://localhost:8080/oldcrap-rest/user/info",
                HttpMethod.POST,
                requestEntity,
                String.class);

        User user = objectMapper.readValue(responseJSON.getBody(), User.class);

        assertEquals("mail", user.getMail());

        objectMapper.writerWithDefaultPrettyPrinter().writeValue(System.out, user);
    }

    private String userLogin(String user, String pass) {

        String[] result = getLoginPage();

        String csrf = result[0];
        String cookie = result[1];

        return postLoginData(user, pass, csrf, cookie);
    }

    private String postLoginData(String user, String pass, String csrf, String cookie) {
        HttpHeaders requestHeaders = new HttpHeaders();
        requestHeaders.add("Cookie", cookie);
        requestHeaders.add(CustomAuthenticationFilter.CSRF_TOKEN, csrf);

        MultiValueMap<String, String> mvm = new LinkedMultiValueMap();
        mvm.add("username", user);
        mvm.add("password", pass);

        HttpEntity<MultiValueMap<String,String>> requestEntity = new HttpEntity<>(mvm, requestHeaders);
        ResponseEntity<String> response = restTemplate.exchange(
                "http://localhost:8080/oldcrap-rest/user/login",
                HttpMethod.POST,
                requestEntity,
                String.class);

        String loginResponse = response.getBody();
        HttpStatus status = response.getStatusCode();

        System.out.println("Response status: " + status);
        assertEquals(HttpStatus.OK, status);
        System.out.println("Response body: " + loginResponse);
        assertEquals("User Michail Smith logged in!", loginResponse);
        String cookieDelirium = response.getHeaders().get("Set-Cookie").get(0);
        assertNotNull(cookieDelirium);
        assertTrue(cookieDelirium.startsWith(CustomAuthenticationFilter.COOKIES_NAME));
        System.out.println("Set-Cookie (0): " + response.getHeaders().get("Set-Cookie").get(0));
        return cookieDelirium;
    }

    private String[] getLoginPage() {
        ResponseEntity<String> responseHtml = restTemplate.getForEntity(URI.create("http://localhost:8080/oldcrap-rest/user/login"), String.class);
        String cookie = responseHtml.getHeaders().get("Set-Cookie").get(0);
        System.out.println("COOKIE: " + cookie);
        String html = responseHtml.getBody();
        System.out.println("BODY: " + html);
        String csrf = responseHtml.getHeaders().get(CustomAuthenticationFilter.CSRF_TOKEN).get(0);
        System.out.println("CSRF TOKEN: " + csrf);
        assertNotNull(csrf);
        return new String[] {csrf, cookie};
    }
}
