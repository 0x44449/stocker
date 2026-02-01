package com.hanzi.stocker.repositories;

import com.hanzi.stocker.entities.CompanyNameMappingEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CompanyNameMappingRepository extends JpaRepository<CompanyNameMappingEntity, Long> {

    List<CompanyNameMappingEntity> findByNewsId(Long newsId);
}
