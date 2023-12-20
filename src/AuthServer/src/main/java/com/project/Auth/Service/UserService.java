package com.project.Auth.Service;

import java.util.List;

import org.springframework.http.ResponseEntity;

import com.project.Auth.DTO.UserDTO;

import jakarta.servlet.http.HttpServletRequest;

public interface UserService {
    public ResponseEntity<String> createAccount(UserDTO registerDTO, HttpServletRequest request, String key);

    public ResponseEntity<String> loginUser(UserDTO loginDTO, HttpServletRequest request, String key);

    public ResponseEntity<String> getUserInfo(UserDTO userDTO);

    public ResponseEntity<String> getUserGroups(UserDTO userDTO);

    public ResponseEntity<String> logout(UserDTO userDTO);

    public ResponseEntity<String> deleteAccount(UserDTO userDTO);

    public List<Integer> getTeamList(int uid);

    public int getSessionID(int uid);

    public ResponseEntity<String> refreshAuthToken(UserDTO userDTO);
}
