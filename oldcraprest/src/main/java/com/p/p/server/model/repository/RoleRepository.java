package com.p.p.server.model.repository;

import com.p.p.server.model.bean.Role;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RoleRepository extends JpaRepository<Role, String> {
}
