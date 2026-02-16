package com.hanzi.stocker.repositories;

import com.hanzi.stocker.entities.AllowedUserEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AllowedUserRepository extends JpaRepository<AllowedUserEntity, String> {
}
