package com.p.p.server.model.repository;

import com.p.p.server.model.bean.UserSession;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SessionRepository extends JpaRepository<UserSession, String> {
}
