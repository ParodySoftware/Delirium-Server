package com.p.p.server.controller;

import org.springframework.core.annotation.Order;
import org.springframework.security.web.context.AbstractSecurityWebApplicationInitializer;

@Order(2)
public class MessageSecurityWebApplicationInitializer extends AbstractSecurityWebApplicationInitializer {
}
