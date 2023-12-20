package com.project.Auth.Repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

import com.project.Auth.Entity.KeyTableEntry;

@EnableJpaRepositories
public interface KeyTableRepository extends JpaRepository<KeyTableEntry, Integer> {
    public KeyTableEntry findByGidAndRsid(int gid, String rsid);

    public List<KeyTableEntry> findAllByGid(int gid);
}
