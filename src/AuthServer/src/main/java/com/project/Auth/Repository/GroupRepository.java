package com.project.Auth.Repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

import com.project.Auth.Entity.Group;

@EnableJpaRepositories
public interface GroupRepository extends JpaRepository<Group, Integer> {
    public Group findByName(String name);

    public Group findOneById(int id);

    public Group findByJoinCode(int joinCode);
}