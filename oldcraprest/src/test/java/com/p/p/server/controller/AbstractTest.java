package com.p.p.server.controller;

import com.p.p.server.model.bean.Picture;
import com.p.p.server.model.bean.Posting;
import com.p.p.server.model.bean.Role;
import com.p.p.server.model.bean.User;
import com.p.p.server.model.repository.PictureRepository;
import com.p.p.server.model.repository.PostingRepository;
import com.p.p.server.model.repository.RoleRepository;
import com.p.p.server.model.repository.UserRepository;
import com.p.p.server.util.DBUtils;
import org.junit.Before;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import javax.sql.DataSource;
import java.io.IOException;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class AbstractTest {

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

    @Before
    @Transactional(readOnly = false)
    public void setup() throws IllegalAccessException, IOException, ClassNotFoundException {
        deleteData();
        fillData();
    }

    @Transactional(readOnly = false)
    protected void deleteData() {
        userRepository.deleteAll();
        roleRepository.deleteAll();
        postingRepository.deleteAll();
        pictureRepository.deleteAll();
    }

    @Transactional(readOnly = false)
    private void fillData() {
        List<User> userList = userRepository.findAll();

        int initialCount = userList.size();

        User user1 = new User("Mihail Nenov", "mihail.n@gmail.com", "pass");
        User user2 = new User("Stancho Nenov", "stancho.n@gmail.com", "pass");
        userRepository.save(user1);
        userRepository.save(user2);

        Role role = new Role("ADMIN");
        role.getUsers().add(user1);
        role.getUsers().add(user2);
        roleRepository.save(role);

        Posting posting1 = new Posting(user1, "TV for present - like brand new one!");
        postingRepository.save(posting1);
        Picture picture1 = new Picture(posting1, "Front view - really nice!");
        pictureRepository.save(picture1);

		Posting posting2 = new Posting(user2, "TV for present - like brand new one!");
		postingRepository.save(posting2);
		Picture picture2 = new Picture(posting2, "Front view - really nice!");
		pictureRepository.save(picture2);

        userList = userRepository.findAll();
        assertEquals(2, userList.size() - initialCount);
    }
}
