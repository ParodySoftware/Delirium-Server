package com.p.p.server.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.p.p.server.model.bean.*;
import com.p.p.server.model.repository.PictureRepository;
import com.p.p.server.model.repository.PostingRepository;
import com.p.p.server.model.repository.RoleRepository;
import com.p.p.server.model.repository.UserRepository;
import com.p.p.server.util.DBUtils;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

import javax.sql.DataSource;
import javax.transaction.NotSupportedException;
import javax.transaction.SystemException;
import java.io.IOException;
import java.sql.*;
import java.text.DateFormat;
import java.util.*;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = "classpath:META-INF/spring/applicationContext.xml")
public class DbTest extends AbstractTest {

    @Autowired
    UserRepository userRepository;

    @Autowired
    RoleRepository roleRepository;

    @Autowired
    PostingRepository postingRepository;

    @Autowired
    PictureRepository pictureRepository;

    @Autowired
    DataSource dataSource;

    @Autowired
    DBUtils dbUtils;

    @Transactional(readOnly = true)
    private void dumpDbObject(Object o) throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        mapper.setDateFormat(DateFormat.getDateTimeInstance());
        System.out.println("Found object: \n" + mapper.writerWithDefaultPrettyPrinter().writeValueAsString(o));
    }

	@Test
	public void testPostings() {

		List<Posting> feed = dbUtils.getPostingsBefore(java.util.Calendar.getInstance().getTime(), 5);
		assertEquals(2, feed.size());

		feed = dbUtils.getPostingsBefore(java.util.Calendar.getInstance().getTime(), 1);
		assertEquals(1, feed.size());
	}

    @Test
    public void testNativeQuery() throws IOException, SQLException {
        dbUtils.dumpNativeQuery("select * from users");
    }

    @Test
    public void testContent() throws IOException, SQLException, SystemException, NotSupportedException {

        User foundUser = userRepository.getByMail("mihail.n@gmail.com");
        assertNotNull(foundUser);
        assertEquals(1, foundUser.getRoles().size());

        Role foundRole = foundUser.getRoles().iterator().next();
        assertEquals(2, foundRole.getUsers().size());

        dumpDbObject(foundUser);

        dumpDbObject(roleRepository.findAll());

        foundUser.setPhone("(359) 887 376 552");

        userRepository.save(foundUser);

        dbUtils.dumpNativeQuery("select * from users");

        dbUtils.dumpNativeQuery("select * from roles");

        dbUtils.dumpNativeQuery("select mail as username, password, enabled from users where mail='mihail.n@gmail.com'");

        dbUtils.dumpNativeQuery("select u.mail as username, r.name as authority from USERS u, ROLES r, USER_ROLES ur "
                + "where ur.role_id = r.id and ur.user_id = u.id and u.mail='mihail.n@gmail.com'");
    }

    @Test
    @Ignore
    public void testPreparedStatement() throws SQLException {
        PreparedStatement stmt = dataSource.getConnection().prepareStatement("select u.mail as username, r.name as authority from USERS u, ROLES r, USER_ROLES ur "
                + "where ur.role_id = r.id and ur.user_id = u.id and u.mail=?");

        stmt.setString(1, "mihail.n@gmail.com");
        ResultSet results = stmt.executeQuery();
        dbUtils.dumpStatementQuery(results);
    }
}
