package com.project.Resource.Repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

import com.project.Resource.Entity.UserSessionData;

@EnableJpaRepositories
public interface UserSessionRepository extends JpaRepository<UserSessionData, Integer> {

    public UserSessionData findByUserUID(int userUID);
}
