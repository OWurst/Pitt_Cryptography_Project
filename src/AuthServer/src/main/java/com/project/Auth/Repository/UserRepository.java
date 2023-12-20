package com.project.Auth.Repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

import com.project.Auth.Entity.User;

@EnableJpaRepositories
public interface UserRepository extends JpaRepository<User, Integer> {
    public User findOneByUsernameAndPassword(String username, String password);

    public User findByUsername(String username);

    public User findOneById(int id);
}