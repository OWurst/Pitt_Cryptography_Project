package com.project.Auth.Service;

import java.util.List;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Base64;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.project.Auth.DTO.UserDTO;
import com.project.Auth.Entity.Group;
import com.project.Auth.Entity.MembershipTableEntry;
import com.project.Auth.Entity.SymmetricKeyTable;
import com.project.Auth.Entity.Timestamp;
import com.project.Auth.Entity.User;
import com.project.Auth.Repository.GroupRepository;
import com.project.Auth.Repository.MembershipTableEntryRepository;
import com.project.Auth.Repository.SymmetricKeyTableRepository;
import com.project.Auth.Repository.UserRepository;

import com.project.Auth.Utils.JwtTokenUtil;

import jakarta.servlet.http.HttpServletRequest;

@Service
public class UserServiceImpl implements UserService {
    @Autowired
    private UserRepository userRepository;

    @Autowired
    MembershipTableEntryRepository membershipTableEntryRepository;

    @Autowired
    EncryptionService encryptionService;

    @Autowired
    SymmetricKeyTableRepository symmetricKeyTableRepository;

    @Autowired
    GroupRepository groupRepository;

    @Autowired
    KeyManagerService keyManagerService;

    @Override
    public ResponseEntity<String> createAccount(UserDTO userDTO, HttpServletRequest request, String key) {
        String body;
        HttpStatus status;

        if (userDTO.getPassword().length() < 12) {
            body = "{\"msg\":\"Registration unsuccessful: password must be at least 12 characters\","
                    + timestampString()
                    + "}";
            status = HttpStatus.BAD_REQUEST;
            return new ResponseEntity<>(body, status);
        }

        if (userRepository.findByUsername(userDTO.getUsername()) == null) {
            User user = new User(userDTO.getUsername(), hashPassword(userDTO.getPassword()), userDTO.getFirstname(),
                    userDTO.getLastname());

            userRepository.save(user);

            body = "{" + timestampString() + ",\"msg\":\"Registration successful\"";
            status = HttpStatus.OK;
            return this.loginUser(userDTO, request, key);
        } else {
            body = "{" + timestampString() + ",\"msg\":\"Registration unsuccessful: username already taken";
            status = HttpStatus.CONFLICT;
            return new ResponseEntity<>(body, status);
        }
    }

    @Override
    public ResponseEntity<String> refreshAuthToken(UserDTO userDTO) {
        String body;
        HttpStatus status;

        int id = userDTO.getUid();
        User user = userRepository.findOneById(id);

        if (user != null && user.isLoggedIn()) {
            body = "{" + jwtTokenString(id, userDTO.getAuthToken()) + "," + timestampString() + "}";
            status = HttpStatus.OK;
        } else {
            body = "User not verified";
            body = reqBody(body, id, userDTO.getAuthToken());
            status = HttpStatus.FORBIDDEN;
        }
        return new ResponseEntity<>(body, status);
    }

    @Override
    public ResponseEntity<String> loginUser(UserDTO loginDTO, HttpServletRequest request, String key) {
        String body;
        HttpStatus status;

        User user = userRepository.findByUsername(loginDTO.getUsername());

        if (user == null) {
            body = "Login unsuccessful: Invalid username or password";
            status = HttpStatus.UNAUTHORIZED;
            return new ResponseEntity<>(body, status);
        }

        if (isUserOverLoginLimit(user.getFailedLoginLog())) {
            body = "Login unsuccessful: too many failed login attempts";
            body = reqBody(body, loginDTO.getUid(), loginDTO.getAuthToken());
            status = HttpStatus.TOO_MANY_REQUESTS;
            return new ResponseEntity<>(body, status);
        }

        if (user != null && hashPassword(loginDTO.getPassword()).equals(user.getPassword())) {
            user.setLoggedIn(true);
            user.logLoggedIn();
            userRepository.save(user);

            int sessionID = generateSessionID();
            encryptionService.storeAESKey(key, user.getId(), sessionID);

            int id = user.getId();
            JwtTokenUtil jwtTokenUtil = new JwtTokenUtil();

            String jwt = jwtTokenUtil.generateToken(id, encryptionService.getPrivateKeyPath(),
                    sessionID, getTeamList(id), loginDTO.getRsid());

            body = "{\"username\":\"" + user.getUsername()
                    + "\",\"firstname\":\"" + user.getFirstname()
                    + "\",\"lastname\":\"" + user.getLastname()
                    + "\",\"uid\":" + user.getId() + ","
                    + "\"authToken\":\"" + jwt + "\","
                    + timestampString()
                    + "}";
            status = HttpStatus.OK;
        } else {
            user.logFailedLogin();
            body = "Login unsuccessful: Invalid username or password";
            status = HttpStatus.UNAUTHORIZED;
        }
        return new ResponseEntity<>(body, status);
    }

    @Override
    public ResponseEntity<String> getUserInfo(UserDTO userDTO) {
        String body;
        HttpStatus status;

        int id = userDTO.getUid();
        User user = userRepository.findOneById(id);

        if (user != null && user.isLoggedIn()) {
            body = "{\"username\":\"" + user.getUsername() + "\",\"firstname\":\"" + user.getFirstname()
                    + "\",\"lastname\":\"" + user.getLastname() + "\",\"uid\":" + user.getId() + ","
                    + jwtTokenString(id, userDTO.getAuthToken()) + "," + timestampString() + "}";
            status = HttpStatus.OK;
        } else {
            body = "User not verified";
            body = reqBody(body, id, userDTO.getAuthToken());
            status = HttpStatus.FORBIDDEN;
        }
        return new ResponseEntity<>(body, status);
    }

    @Override
    public ResponseEntity<String> getUserGroups(UserDTO userDTO) {
        String body;
        HttpStatus status;

        int id = userDTO.getUid();
        User user = userRepository.findOneById(id);

        if (user != null && user.isLoggedIn()) {
            List<MembershipTableEntry> groupList = membershipTableEntryRepository.findByUserID(user.getId());

            if (groupList.size() > 0) {
                body = "{" + timestampString() + ",\"groups\":[";
                for (int i = 0; i < groupList.size(); i++) {
                    Group group = groupRepository.findOneById(groupList.get(i).getGroupID());

                    JwtTokenUtil jwtTokenUtil = new JwtTokenUtil();
                    String rsid = jwtTokenUtil.getRsid(userDTO.getAuthToken(), encryptionService.getPublicKeyPath());

                    String oldKey;
                    boolean isOutdated = keyManagerService.isRsOnOutdatedKey(group, rsid);
                    if (isOutdated) {
                        oldKey = keyManagerService.getOldGroupKey(group, rsid);
                    } else {
                        oldKey = "";
                    }

                    body += "{\"groupID\":" + groupList.get(i).getGroupID() + ",\"groupName\":\"" + group.getName()
                            + "\",\"creatorID\":" + group.getCreator().getId()
                            + ",\"creator\":\"" + group.getCreator().getUsername() + "\""
                            + ",\"joinCode\":\"" + group.getJoinCode() + "\""
                            + ",\"groupKey\":\"" + group.getAesKey() + "\""
                            + ",\"isGroupKeyOutOfDate\":" + isOutdated + ","
                            + "\"oldKey\":\"" + oldKey + "\""
                            + "}";
                    if (i + 1 < groupList.size())
                        body += ",";
                }
                body += "]," + jwtTokenString(id, userDTO.getAuthToken()) + "}";
            } else {
                body = "{\"groups\":[]," + jwtTokenString(id, userDTO.getAuthToken()) + "," + timestampString() + "}";
            }

            status = HttpStatus.OK;
        } else {
            body = "User not verified";
            body = reqBody(body, id, userDTO.getAuthToken());
            status = HttpStatus.FORBIDDEN;
        }
        return new ResponseEntity<>(body, status);
    }

    @Override
    public ResponseEntity<String> logout(UserDTO userDTO) {
        String body;
        HttpStatus status;

        int id = userDTO.getUid();
        User user = userRepository.findOneById(id);

        if (user != null && user.isLoggedIn()) {
            user.setLoggedIn(false);
            user.logLoggedOut();
            userRepository.save(user);

            body = "Logout successful";
            body = reqBody(body, id, userDTO.getAuthToken());
            status = HttpStatus.OK;
        } else {
            body = "Logout Unsuccessful";
            body = reqBody(body, id, userDTO.getAuthToken());
            status = HttpStatus.BAD_REQUEST;
        }
        return new ResponseEntity<>(body, status);
    }

    @Override
    public int getSessionID(int uid) {
        SymmetricKeyTable entry = symmetricKeyTableRepository.findByUid(uid);
        return entry.getSessionID();
    }

    @Override
    public ResponseEntity<String> deleteAccount(UserDTO userDTO) {
        String body;
        HttpStatus status;

        int id = userDTO.getUid();
        User user = userRepository.findOneById(id);

        if (user != null && user.isLoggedIn()) {
            List<MembershipTableEntry> groupList = membershipTableEntryRepository
                    .findByUserID(user.getId());

            for (MembershipTableEntry entry : groupList) {
                Group group = groupRepository.findOneById(entry.getGroupID());

                membershipTableEntryRepository.delete(entry);

                if (group.getCreator().getId() == user.getId()) {
                    List<MembershipTableEntry> groupMembers = membershipTableEntryRepository
                            .findByGroupID(group.getId());

                    if (groupMembers.size() >= 1) {
                        int newLeaderID = groupMembers.get(0).getUserID();
                        User newLeader = userRepository.findOneById(newLeaderID);

                        group.setCreator(newLeader);
                        groupRepository.save(group);
                    } else {
                        groupRepository.delete(group);
                    }
                }
            }

            userRepository.delete(user);

            body = "Account deleted";
            body = reqBody(body, id, userDTO.getAuthToken());
            status = HttpStatus.OK;
        } else {
            body = "User not verified";
            body = reqBody(body, id, userDTO.getAuthToken());
            status = HttpStatus.BAD_REQUEST;
        }
        return new ResponseEntity<>(body, status);
    }

    public static SecretKey convertStringToSecretKey(String keyString) {
        // Decode the base64 encoded key string
        byte[] keyBytes = Base64.getDecoder().decode(keyString);

        // Create a SecretKeySpec from the decoded key bytes
        SecretKey secretKey = new SecretKeySpec(keyBytes, "HmacSHA512");

        return secretKey;
    }

    public boolean isUserOverLoginLimit(List<Timestamp> loginLog) {
        if (loginLog == null) {
            return false;
        }
        if (loginLog.size() < 3) {
            return false;
        }

        Timestamp thirdLastLogin = loginLog.get(loginLog.size() - 3);
        if (thirdLastLogin.getTimeStamp().plusHours(1).isAfter(LocalDateTime.now())) {
            return true;
        }
        return false;
    }

    private static String hashPassword(String password) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(password.getBytes(StandardCharsets.UTF_8));
            BigInteger number = new BigInteger(1, hash);
            StringBuilder hexString = new StringBuilder(number.toString(16));
            while (hexString.length() < 32) {
                hexString.insert(0, '0');
            }
            return hexString.toString();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private String reqBody(String body, int id, String token) {
        return "{\"msg\":\"" + body + "\"," + jwtTokenString(id, token) + "," + timestampString() + "}";
    }

    private String jwtTokenString(int id, String token) {
        JwtTokenUtil jwtTokenUtil = new JwtTokenUtil();

        String rsid = jwtTokenUtil.getRsid(token, encryptionService.getPublicKeyPath());

        String jwt = jwtTokenUtil.generateToken(id, encryptionService.getPrivateKeyPath(),
                getSessionID(id), getTeamList(id), rsid);

        String ret = "\"authToken\":\"" + jwt + "\"";
        return ret;
    }

    public List<Integer> getTeamList(int uid) {
        List<MembershipTableEntry> entries = membershipTableEntryRepository.findByUserID(uid);

        List<Integer> ret = new ArrayList<Integer>();

        for (MembershipTableEntry entry : entries) {
            ret.add(entry.getGroupID());
        }
        return ret;
    }

    private int generateSessionID() {
        return (int) (Math.random() * 1000000);
    }

    private String timestampString() {
        return "\"timestamp\":\"" + System.currentTimeMillis() / 1000L + "\"";
    }
}
