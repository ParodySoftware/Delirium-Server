package com.p.p.server.model.repository;

import com.p.p.server.model.bean.Posting;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PostingRepository extends JpaRepository<Posting, String> {
}
