package com.p.p.server.model.repository;

import com.p.p.server.model.bean.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface UserRepository extends JpaRepository<User, String> {

    @Query("select u from User u where u.mail = ?1")
    User getByMail(String mail);
}