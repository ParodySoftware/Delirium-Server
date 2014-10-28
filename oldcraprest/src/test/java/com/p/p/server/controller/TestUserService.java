package com.p.p.server.controller;


import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.net.URI;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = "classpath:META-INF/spring/applicationContext.xml")
public class TestUserService {

    @Test
    public void testGetUserInfo() {
        RestTemplate restTemplate = new RestTemplate();

        ResponseEntity<String> responseHtml = restTemplate.getForEntity(URI.create("http://localhost:8080/oldcrap-rest/user?username=mail"), String.class);
        String cookie = responseHtml.getHeaders().get("Set-Cookie").get(0);
        System.out.println("COOKIE: " + cookie);
        String html = responseHtml.getBody();

        int start = html.lastIndexOf("type=\"hidden\" value=\"");

        String csrf = html.substring(start + 21, start +21 + 38);

        HttpHeaders requestHeaders = new HttpHeaders();
        //requestHeaders.add("Cookie", "JSESSIONID=" + cookie);
        requestHeaders.add("X-CSRF-TOKEN", csrf);

        MultiValueMap<String, String> mvm = new LinkedMultiValueMap<String, String>();
        mvm.add("username", "mail");
        mvm.add("password", "pass");

        //InstagramResult result = restTemplate .postForObject("https://api.instagram.com/oauth/access_token", mvm, InstagramResult .class);
        HttpEntity requestEntity = new HttpEntity(null, requestHeaders);
        ResponseEntity<String> response = restTemplate.exchange(
                "http://localhost:8080/oldcrap-rest/login?username=mail&password=pass",
                HttpMethod.POST,
                requestEntity,
                String.class);
        String loginResponse = response.getBody();
    }
}
