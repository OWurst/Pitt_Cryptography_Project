package com.project.Auth.Repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

import com.project.Auth.Entity.SymmetricKeyTable;

@EnableJpaRepositories
public interface SymmetricKeyTableRepository extends JpaRepository<SymmetricKeyTable, Integer> {
    public SymmetricKeyTable findByUid(int uid);
}