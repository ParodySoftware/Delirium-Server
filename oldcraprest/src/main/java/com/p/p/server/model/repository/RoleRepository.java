package com.p.p.server.model.repository;

import com.p.p.server.model.bean.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface RoleRepository extends JpaRepository<Role, String> {

    @Query("select r from Role r where r.name = ?1")
    Role getByName(String name);
}
