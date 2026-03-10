package com.mahi.repository;

import com.mahi.entity.KeywordDetail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface KeywordDetailRepository extends JpaRepository<KeywordDetail, Long> {
}
