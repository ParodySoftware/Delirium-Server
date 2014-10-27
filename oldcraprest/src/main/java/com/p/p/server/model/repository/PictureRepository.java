package com.p.p.server.model.repository;

import com.p.p.server.model.bean.Picture;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PictureRepository extends JpaRepository<Picture, String> {
}
