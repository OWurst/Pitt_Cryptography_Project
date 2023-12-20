package com.project.Auth.Controller;

import java.security.SecureRandom;
import java.util.Base64;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.project.Auth.DTO.GroupDTO;
import com.project.Auth.DTO.RemoveFromGroupDTO;
import com.project.Auth.DTO.SecurePayloadDTO;
import com.project.Auth.Service.EncryptionService;
import com.project.Auth.Service.GroupService;
import com.project.Auth.Service.UserService;
import com.project.Auth.Utils.JwtTokenUtil;

import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/groups")
public class GroupController {
    @Autowired
    GroupService groupService;

    @Autowired
    UserService userService;

    @Autowired
    EncryptionService encryptionService;

    @PostMapping("/create")
    public ResponseEntity<String> createGroup(@RequestBody SecurePayloadDTO securePayloadDTO,
            HttpServletRequest request) {
        String key = encryptionService.getAESKey(securePayloadDTO.getUid());
        String iv = securePayloadDTO.getIv();

        if (!verifyMAC(securePayloadDTO.getHmac(),
                securePayloadDTO.getEncryptedPayloadString(), key)) {
            return new ResponseEntity<>(encaseMessage("Invalid MAC", key),
                    HttpStatus.UNAUTHORIZED);
        }

        String payload = encryptionService.decryptAES(securePayloadDTO.getEncryptedPayloadString(), key, iv);
        System.out.println(payload);
        GroupDTO groupDTO = null;
        try {
            groupDTO = getGroupDTO(payload);
        } catch (Exception e) {
            return payloadParseFail(key);
        }

        if (!validateTimestamp(groupDTO.getTimestamp())) {
            return new ResponseEntity<>(encaseMessage("Invalid timestamp", key),
                    HttpStatus.UNAUTHORIZED);
        }

        groupDTO.setUid(securePayloadDTO.getUid());
        if (!verifyToken(groupDTO.getAuthToken(), groupDTO.getUid())) {
            return new ResponseEntity<>(encaseMessage("Invalid token", key), HttpStatus.UNAUTHORIZED);
        }
        ResponseEntity<String> resp = groupService.createGroup(groupDTO);

        resp = new ResponseEntity<>(encase(resp.getBody(), key), resp.getHeaders(), resp.getStatusCode());
        return resp;
    }

    @PostMapping("/updatedGroupKey")
    public ResponseEntity<String> updatedGroupKey(@RequestBody SecurePayloadDTO securePayloadDTO) {
        String key = encryptionService.getAESKey(securePayloadDTO.getUid());
        String iv = securePayloadDTO.getIv();

        if (!verifyMAC(securePayloadDTO.getHmac(), securePayloadDTO.getEncryptedPayloadString(), key)) {
            return new ResponseEntity<>(encaseMessage("Invalid MAC", key), HttpStatus.UNAUTHORIZED);
        }

        String payload = encryptionService.decryptAES(securePayloadDTO.getEncryptedPayloadString(), key, iv);
        GroupDTO groupDTO = null;
        try {
            groupDTO = getGroupDTO(payload);
        } catch (Exception e) {
            return payloadParseFail(key);
        }

        if (!validateTimestamp(groupDTO.getTimestamp())) {
            return new ResponseEntity<>(encaseMessage("Invalid timestamp", key), HttpStatus.UNAUTHORIZED);
        }

        groupDTO.setUid(securePayloadDTO.getUid());
        if (!verifyToken(groupDTO.getAuthToken(), groupDTO.getUid())) {
            return new ResponseEntity<>(encaseMessage("Invalid token", key), HttpStatus.UNAUTHORIZED);
        }
        ResponseEntity<String> resp = groupService.updatedGroupKey(groupDTO);

        resp = new ResponseEntity<>(encase(resp.getBody(), key), resp.getHeaders(), resp.getStatusCode());
        return resp;
    }

    @PutMapping("/join")
    public ResponseEntity<String> joinGroup(@RequestBody SecurePayloadDTO securePayloadDTO,
            HttpServletRequest request) {
        String key = encryptionService.getAESKey(securePayloadDTO.getUid());
        String iv = securePayloadDTO.getIv();

        if (!verifyMAC(securePayloadDTO.getHmac(),
                securePayloadDTO.getEncryptedPayloadString(), key)) {
            return new ResponseEntity<>(encaseMessage("Invalid MAC", key),
                    HttpStatus.UNAUTHORIZED);
        }

        String payload = encryptionService.decryptAES(securePayloadDTO.getEncryptedPayloadString(), key, iv);
        GroupDTO groupDTO = null;
        try {
            groupDTO = getGroupDTO(payload);
        } catch (Exception e) {
            return payloadParseFail(key);
        }

        if (!validateTimestamp(groupDTO.getTimestamp())) {
            return new ResponseEntity<>(encaseMessage("Invalid timestamp", key), HttpStatus.UNAUTHORIZED);
        }

        groupDTO.setUid(securePayloadDTO.getUid());
        if (!verifyToken(groupDTO.getAuthToken(), groupDTO.getUid())) {
            return new ResponseEntity<>(encaseMessage("Invalid token", key), HttpStatus.UNAUTHORIZED);
        }
        ResponseEntity<String> resp = groupService.joinGroup(groupDTO);

        resp = new ResponseEntity<>(encase(resp.getBody(), key), resp.getHeaders(), resp.getStatusCode());
        return resp;
    }

    @PostMapping("/info")
    public ResponseEntity<String> getGroupInfo(@RequestBody SecurePayloadDTO securePayloadDTO,
            HttpServletRequest request) {
        String key = encryptionService.getAESKey(securePayloadDTO.getUid());
        String iv = securePayloadDTO.getIv();

        if (!verifyMAC(securePayloadDTO.getHmac(),
                securePayloadDTO.getEncryptedPayloadString(), key)) {
            return new ResponseEntity<>(encaseMessage("Invalid MAC", key),
                    HttpStatus.UNAUTHORIZED);
        }

        String payload = encryptionService.decryptAES(securePayloadDTO.getEncryptedPayloadString(), key, iv);
        GroupDTO groupDTO = null;
        try {
            groupDTO = getGroupDTO(payload);
        } catch (Exception e) {
            return payloadParseFail(key);
        }

        if (!validateTimestamp(groupDTO.getTimestamp())) {
            return new ResponseEntity<>(encaseMessage("Invalid timestamp", key), HttpStatus.UNAUTHORIZED);
        }

        groupDTO.setUid(securePayloadDTO.getUid());
        if (!verifyToken(groupDTO.getAuthToken(), groupDTO.getUid())) {
            return new ResponseEntity<>(encaseMessage("Invalid token", key), HttpStatus.UNAUTHORIZED);
        }
        ResponseEntity<String> resp = groupService.getGroupInfo(groupDTO);

        resp = new ResponseEntity<>(encase(resp.getBody(), key), resp.getHeaders(), resp.getStatusCode());
        return resp;
    }

    @PostMapping("/joincode")
    public ResponseEntity<String> getJoinCode(@RequestBody SecurePayloadDTO securePayloadDTO,
            HttpServletRequest request) {
        String key = encryptionService.getAESKey(securePayloadDTO.getUid());
        String iv = securePayloadDTO.getIv();

        if (!verifyMAC(securePayloadDTO.getHmac(), securePayloadDTO.getEncryptedPayloadString(), key)) {
            return new ResponseEntity<>(encaseMessage("Invalid MAC", key), HttpStatus.UNAUTHORIZED);
        }

        String payload = encryptionService.decryptAES(securePayloadDTO.getEncryptedPayloadString(), key, iv);
        GroupDTO groupDTO = null;
        try {
            groupDTO = getGroupDTO(payload);
        } catch (Exception e) {
            return payloadParseFail(key);
        }

        if (!validateTimestamp(groupDTO.getTimestamp())) {
            return new ResponseEntity<>(encaseMessage("Invalid timestamp", key), HttpStatus.UNAUTHORIZED);
        }

        groupDTO.setUid(securePayloadDTO.getUid());
        if (!verifyToken(groupDTO.getAuthToken(), groupDTO.getUid())) {
            return new ResponseEntity<>(encaseMessage("Invalid token", key), HttpStatus.UNAUTHORIZED);
        }
        ResponseEntity<String> resp = groupService.getJoinCode(groupDTO);

        resp = new ResponseEntity<>(encase(resp.getBody(), key), resp.getHeaders(), resp.getStatusCode());
        return resp;
    }

    @DeleteMapping("/leave")
    public ResponseEntity<String> leaveGroup(@RequestBody SecurePayloadDTO securePayloadDTO,
            HttpServletRequest request) {
        String key = encryptionService.getAESKey(securePayloadDTO.getUid());
        String iv = securePayloadDTO.getIv();

        if (!verifyMAC(securePayloadDTO.getHmac(), securePayloadDTO.getEncryptedPayloadString(), key)) {
            return new ResponseEntity<>(encaseMessage("Invalid MAC", key), HttpStatus.UNAUTHORIZED);
        }

        String payload = encryptionService.decryptAES(securePayloadDTO.getEncryptedPayloadString(), key, iv);
        GroupDTO groupDTO = null;
        try {
            groupDTO = getGroupDTO(payload);
        } catch (Exception e) {
            return payloadParseFail(key);
        }

        if (!validateTimestamp(groupDTO.getTimestamp())) {
            return new ResponseEntity<>(encaseMessage("Invalid timestamp", key), HttpStatus.UNAUTHORIZED);
        }

        groupDTO.setUid(securePayloadDTO.getUid());
        if (!verifyToken(groupDTO.getAuthToken(), groupDTO.getUid())) {
            return new ResponseEntity<>(encaseMessage("Invalid token", key), HttpStatus.UNAUTHORIZED);
        }
        ResponseEntity<String> resp = groupService.leaveGroup(groupDTO);

        resp = new ResponseEntity<>(encase(resp.getBody(), key), resp.getHeaders(), resp.getStatusCode());
        return resp;
    }

    @DeleteMapping("/removeFromGroup")
    public ResponseEntity<String> removeUserFromGroup(@RequestBody SecurePayloadDTO securePayloadDTO,
            HttpServletRequest request) {
        String key = encryptionService.getAESKey(securePayloadDTO.getUid());
        String iv = securePayloadDTO.getIv();

        if (!verifyMAC(securePayloadDTO.getHmac(), securePayloadDTO.getEncryptedPayloadString(), key)) {
            return new ResponseEntity<>(encaseMessage("Invalid MAC", key), HttpStatus.UNAUTHORIZED);
        }

        String payload = encryptionService.decryptAES(securePayloadDTO.getEncryptedPayloadString(), key, iv);
        RemoveFromGroupDTO groupDTO = null;
        try {
            groupDTO = getRemGroupDTO(payload);
        } catch (Exception e) {
            return payloadParseFail(key);
        }

        if (!validateTimestamp(groupDTO.getTimestamp())) {
            return new ResponseEntity<>(encaseMessage("Invalid timestamp", key), HttpStatus.UNAUTHORIZED);
        }

        groupDTO.setUid(securePayloadDTO.getUid());
        if (!verifyToken(groupDTO.getAuthToken(), groupDTO.getUid())) {
            return new ResponseEntity<>(encaseMessage("Invalid token", key), HttpStatus.UNAUTHORIZED);
        }
        ResponseEntity<String> resp = groupService.removeUserFromGroup(groupDTO);

        resp = new ResponseEntity<>(encase(resp.getBody(), key), resp.getHeaders(), resp.getStatusCode());
        return resp;
    }

    public GroupDTO getGroupDTO(String payload) throws Exception {
        ObjectMapper objectMapper = new ObjectMapper();
        GroupDTO groupDTO = null;

        groupDTO = objectMapper.readValue(payload, GroupDTO.class);

        return groupDTO;
    }

    public RemoveFromGroupDTO getRemGroupDTO(String payload) throws Exception {
        ObjectMapper objectMapper = new ObjectMapper();
        RemoveFromGroupDTO groupDTO = null;

        groupDTO = objectMapper.readValue(payload, RemoveFromGroupDTO.class);

        return groupDTO;
    }

    // public String generateToken(int id) {
    // JwtTokenUtil jwtTokenUtil = new JwtTokenUtil();
    // return jwtTokenUtil.generateToken(id, encryptionService.getPrivateKeyPath(),
    // getSessionID(id), getTeamList(id));
    // }

    private boolean verifyToken(String token, int uid) {
        JwtTokenUtil jwtTokenUtil = new JwtTokenUtil();
        return jwtTokenUtil.verifyToken(token, uid, encryptionService.getPublicKeyPath());
    }

    private String encase(String body, String key) {
        String iv = generateRandomString();

        String encryptedBody = encryptionService.encryptAES(body, key, iv);

        String response = "{\"iv\":\"" + iv + "\",\"body\":\""
                + encryptionService.encryptAES(body, key, iv)
                + "\",\"hmac\":\"" + encryptionService.generateMAC(encryptedBody, key)
                + "\"}";

        return response;
    }

    public String generateRandomString() {
        SecureRandom random = new SecureRandom();
        byte[] bytes = new byte[16];
        random.nextBytes(bytes);
        return Base64.getEncoder().encodeToString(bytes);
    }

    List<Integer> getTeamList(int uid) {
        return groupService.getTeamList(uid);
    }

    // private int getSessionID(int uid) {
    // return userService.getSessionID(uid);
    // }

    private ResponseEntity<String> payloadParseFail(String key) {
        return new ResponseEntity<>(encaseMessage("Failed to parse payload", key), HttpStatus.UNAUTHORIZED);
    }

    private boolean validateTimestamp(long timestamp) {
        try {
            // long givenTime = Long.parseLong(timestamp);
            long currTime = System.currentTimeMillis() / 1000L;
            if (Math.abs(currTime - timestamp) > 10) {
                return false;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }

        return true;
    }

    private String encaseMessage(String message, String key) {
        String body = "{\"message\":\"" + message + ",";
        body += timestampString() + "\"}";
        return encase(body, key);
    }

    private boolean verifyMAC(String mac, String body, String key) {
        try {
            String macCheck = encryptionService.generateMAC(body, key);
            if (macCheck.equals(mac)) {
                return true;
            } else {
                return false;
            }
        } catch (Exception e) {
            return false;
        }
    }

    private String timestampString() {
        return "\"timestamp\":\"" + System.currentTimeMillis() / 1000L + "\"";
    }
}