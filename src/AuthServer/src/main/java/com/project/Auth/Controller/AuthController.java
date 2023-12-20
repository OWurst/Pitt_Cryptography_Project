package com.project.Auth.Controller;

import java.nio.charset.StandardCharsets;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.Security;
import java.util.Base64;

import javax.crypto.Cipher;

import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.project.Auth.DTO.SecurePayloadDTO;
import com.project.Auth.DTO.UserDTO;
import com.project.Auth.Service.EncryptionService;
import com.project.Auth.Service.UserService;
import com.project.Auth.Utils.JwtTokenUtil;

//import java.util.List;

import jakarta.servlet.http.HttpServletRequest;

@RestController
public class AuthController {

    static {
        Security.addProvider(new BouncyCastleProvider());
    }

    @Autowired
    UserService userService;

    @Autowired
    EncryptionService encryptionService;

    @GetMapping("/requestPubKey")
    public ResponseEntity<String> requestPubKey() {
        return encryptionService.requestPubKey();
    }

    @PostMapping("/createAccount")
    public ResponseEntity<String> createAccount(@RequestBody SecurePayloadDTO securePayloadDTO,
            HttpServletRequest request) {
        String iv = securePayloadDTO.getIv();

        String key = decryptAESKey(securePayloadDTO.getEncryptedAESKeyString());

        if (!verifyMAC(securePayloadDTO.getHmac(),
                securePayloadDTO.getEncryptedPayloadString(), key)) {
            return new ResponseEntity<>(encaseMessage("Invalid MAC", key),
                    HttpStatus.UNAUTHORIZED);
        }
        String payload = encryptionService.decryptAES(securePayloadDTO.getEncryptedPayloadString(), key, iv);
        System.out.println(payload);

        UserDTO userDTO = null;
        try {
            userDTO = getUserDTO(payload);
        } catch (Exception e) {
            return new ResponseEntity<>(encaseMessage("Failed to parse payload", key), HttpStatus.UNAUTHORIZED);
        }
        if (!validateTimestamp(userDTO.getTimestamp())) {
            return new ResponseEntity<>(encaseMessage("Invalid timestamp", key), HttpStatus.UNAUTHORIZED);
        }
        ResponseEntity<String> resp = userService.createAccount(userDTO, request, key);

        resp = new ResponseEntity<>(encase(resp.getBody(), key), resp.getHeaders(), resp.getStatusCode());
        return resp;
    }

    @PostMapping("/login")
    public ResponseEntity<String> loginUser(@RequestBody SecurePayloadDTO securePayloadDTO,
            HttpServletRequest request) {
        String key = null;
        UserDTO userDTO = null;
        try {
            String iv = securePayloadDTO.getIv();

            key = decryptAESKey(securePayloadDTO.getEncryptedAESKeyString());

            if (!verifyMAC(securePayloadDTO.getHmac(), securePayloadDTO.getEncryptedPayloadString(), key)) {
                return new ResponseEntity<>(encaseMessage("Invalid MAC", key), HttpStatus.UNAUTHORIZED);
            }

            String payload = encryptionService.decryptAES(securePayloadDTO.getEncryptedPayloadString(), key, iv);
            try {
                userDTO = getUserDTO(payload);
            } catch (Exception e) {
                e.printStackTrace();
                return new ResponseEntity<>(encaseMessage("Failed to parse payload", key), HttpStatus.UNAUTHORIZED);
            }

            if (!validateTimestamp(userDTO.getTimestamp())) {
                return new ResponseEntity<>(encaseMessage("Invalid timestamp", key), HttpStatus.UNAUTHORIZED);
            }

            userDTO.setUid(securePayloadDTO.getUid());
            ResponseEntity<String> resp = userService.loginUser(userDTO, request, key);

            resp = new ResponseEntity<>(encase(resp.getBody(), key), resp.getHeaders(), resp.getStatusCode());
            return resp;
        } catch (Exception err) {
            err.printStackTrace();
            return new ResponseEntity<>(encase(err.toString(), key), HttpStatus.UNAUTHORIZED);
        }
    }

    @PostMapping("/userInfo")
    public ResponseEntity<String> getUserInfo(@RequestBody SecurePayloadDTO securePayloadDTO) {
        String key = encryptionService.getAESKey(securePayloadDTO.getUid());
        String iv = securePayloadDTO.getIv();

        if (!verifyMAC(securePayloadDTO.getHmac(), securePayloadDTO.getEncryptedPayloadString(), key)) {
            return new ResponseEntity<>(encaseMessage("Invalid MAC", key), HttpStatus.UNAUTHORIZED);
        }

        String payload = encryptionService.decryptAES(securePayloadDTO.getEncryptedPayloadString(), key, iv);

        UserDTO userDTO = null;
        try {
            userDTO = getUserDTO(payload);
        } catch (Exception e) {
            return payloadParseFail(key);
        }

        if (!validateTimestamp(userDTO.getTimestamp())) {
            return new ResponseEntity<>(encaseMessage("Invalid timestamp", key), HttpStatus.UNAUTHORIZED);
        }

        userDTO.setUid(securePayloadDTO.getUid());
        if (!verifyToken(userDTO.getAuthToken(), userDTO.getUid())) {
            return new ResponseEntity<>(encaseMessage("Invalid token", key), HttpStatus.UNAUTHORIZED);
        }
        ResponseEntity<String> resp = userService.getUserInfo(userDTO);

        resp = new ResponseEntity<>(encase(resp.getBody(), key), resp.getHeaders(), resp.getStatusCode());
        return resp;
    }

    @PostMapping("/refreshAuthToken")
    public ResponseEntity<String> refreshAuthToken(@RequestBody SecurePayloadDTO securePayloadDTO) {
        String key = encryptionService.getAESKey(securePayloadDTO.getUid());
        String iv = securePayloadDTO.getIv();

        if (!verifyMAC(securePayloadDTO.getHmac(), securePayloadDTO.getEncryptedPayloadString(), key)) {
            return new ResponseEntity<>(encaseMessage("Invalid MAC", key), HttpStatus.UNAUTHORIZED);
        }

        String payload = encryptionService.decryptAES(securePayloadDTO.getEncryptedPayloadString(), key, iv);

        UserDTO userDTO = null;
        try {
            userDTO = getUserDTO(payload);
        } catch (Exception e) {
            return payloadParseFail(key);
        }

        if (!validateTimestamp(userDTO.getTimestamp())) {
            return new ResponseEntity<>(encaseMessage("Invalid timestamp", key), HttpStatus.UNAUTHORIZED);
        }

        userDTO.setUid(securePayloadDTO.getUid());
        if (!verifyToken(userDTO.getAuthToken(), userDTO.getUid())) {
            return new ResponseEntity<>(encaseMessage("Invalid token", key), HttpStatus.UNAUTHORIZED);
        }
        ResponseEntity<String> resp = userService.refreshAuthToken(userDTO);

        resp = new ResponseEntity<>(encase(resp.getBody(), key), resp.getHeaders(), resp.getStatusCode());
        return resp;
    }

    @PostMapping("/userGroups")
    public ResponseEntity<String> getUserGroups(@RequestBody SecurePayloadDTO securePayloadDTO) {
        String key = encryptionService.getAESKey(securePayloadDTO.getUid());
        String iv = securePayloadDTO.getIv();

        if (!verifyMAC(securePayloadDTO.getHmac(),
                securePayloadDTO.getEncryptedPayloadString(), key)) {
            return new ResponseEntity<>(encaseMessage("Invalid MAC", key), HttpStatus.UNAUTHORIZED);
        }

        String payload = encryptionService.decryptAES(securePayloadDTO.getEncryptedPayloadString(), key, iv);
        System.out.println(payload);

        UserDTO userDTO = null;
        try {
            userDTO = getUserDTO(payload);
        } catch (Exception e) {
            return payloadParseFail(key);
        }

        if (!validateTimestamp(userDTO.getTimestamp())) {
            return new ResponseEntity<>(encaseMessage("Invalid timestamp", key),
                    HttpStatus.UNAUTHORIZED);
        }

        userDTO.setUid(securePayloadDTO.getUid());
        if (!verifyToken(userDTO.getAuthToken(), userDTO.getUid())) {
            return new ResponseEntity<>(encaseMessage("Invalid token", key), HttpStatus.UNAUTHORIZED);
        }
        ResponseEntity<String> resp = userService.getUserGroups(userDTO);

        resp = new ResponseEntity<>(encase(resp.getBody(), key), resp.getHeaders(), resp.getStatusCode());
        return resp;
    }

    @PostMapping("/logout")
    public ResponseEntity<String> logout(@RequestBody SecurePayloadDTO securePayloadDTO) {
        String key = encryptionService.getAESKey(securePayloadDTO.getUid());
        String iv = securePayloadDTO.getIv();

        if (!verifyMAC(securePayloadDTO.getHmac(), securePayloadDTO.getEncryptedPayloadString(), key)) {
            return new ResponseEntity<>(encaseMessage("Invalid MAC", key), HttpStatus.UNAUTHORIZED);
        }

        String payload = encryptionService.decryptAES(securePayloadDTO.getEncryptedPayloadString(), key, iv);

        UserDTO userDTO = null;
        try {
            userDTO = getUserDTO(payload);
        } catch (Exception e) {
            return payloadParseFail(key);
        }
        if (!validateTimestamp(userDTO.getTimestamp())) {
            return new ResponseEntity<>(encaseMessage("Invalid timestamp", key), HttpStatus.UNAUTHORIZED);
        }

        userDTO.setUid(securePayloadDTO.getUid());
        if (!verifyToken(userDTO.getAuthToken(), userDTO.getUid())) {
            return new ResponseEntity<>(encaseMessage("Invalid token", key), HttpStatus.UNAUTHORIZED);
        }
        ResponseEntity<String> resp = userService.logout(userDTO);

        resp = new ResponseEntity<>(encase(resp.getBody(), key), resp.getHeaders(), resp.getStatusCode());
        return resp;
    }

    @DeleteMapping("/deleteAccount")
    public ResponseEntity<String> deleteAccount(@RequestBody SecurePayloadDTO securePayloadDTO) {
        String key = encryptionService.getAESKey(securePayloadDTO.getUid());
        String iv = securePayloadDTO.getIv();

        if (!verifyMAC(securePayloadDTO.getHmac(), securePayloadDTO.getEncryptedPayloadString(), key)) {
            return new ResponseEntity<>(encaseMessage("Invalid MAC", key), HttpStatus.UNAUTHORIZED);
        }

        String payload = encryptionService.decryptAES(securePayloadDTO.getEncryptedPayloadString(), key, iv);

        UserDTO userDTO = null;
        try {
            userDTO = getUserDTO(payload);
        } catch (Exception e) {
            return payloadParseFail(key);
        }

        if (!validateTimestamp(userDTO.getTimestamp())) {
            return new ResponseEntity<>(encaseMessage("Invalid timestamp", key), HttpStatus.UNAUTHORIZED);
        }

        userDTO.setUid(securePayloadDTO.getUid());
        if (!verifyToken(userDTO.getAuthToken(), userDTO.getUid())) {
            return new ResponseEntity<>(encaseMessage("Invalid token", key), HttpStatus.UNAUTHORIZED);
        }
        ResponseEntity<String> resp = userService.deleteAccount(userDTO);

        resp = new ResponseEntity<>(encase(resp.getBody(), key), resp.getHeaders(), resp.getStatusCode());
        return resp;
    }

    private UserDTO getUserDTO(String payload) throws Exception {
        ObjectMapper objectMapper = new ObjectMapper();
        UserDTO userDTO = null;

        userDTO = objectMapper.readValue(payload, UserDTO.class);

        return userDTO;
    }

    public String decryptRSA(byte[] cipherText) {
        try {
            JwtTokenUtil jwtTokenUtil = new JwtTokenUtil();
            PrivateKey privateKey = jwtTokenUtil.getPrivateKey(encryptionService.getPrivateKeyPath());

            Cipher cipher = Cipher.getInstance("RSA/None/OAEPWithSHA256AndMGF1Padding", "BC");
            cipher.init(Cipher.DECRYPT_MODE, privateKey);
            byte[] plaintext = cipher.doFinal(cipherText);

            return new String(plaintext, StandardCharsets.UTF_8);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public String encryptRSA(String plainText) {
        try {
            byte[] plaintextBytes = Base64.getDecoder().decode(plainText);

            JwtTokenUtil jwtTokenUtil = new JwtTokenUtil();
            PublicKey publicKey = jwtTokenUtil.getPublicKey(encryptionService.getPublicKeyPath());

            Cipher cipher = Cipher.getInstance("RSA/None/OAEPWithSHA256AndMGF1Padding", "BC");
            cipher.init(Cipher.ENCRYPT_MODE, publicKey);
            byte[] ciphertext = cipher.doFinal(plaintextBytes);

            return Base64.getEncoder().encodeToString(ciphertext);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    // public String generateToken(int id) {
    // JwtTokenUtil jwtTokenUtil = new JwtTokenUtil();
    // return jwtTokenUtil.generateToken(id, encryptionService.getPrivateKeyPath(),
    // getSessionID(id), getTeamList(id), rsid);
    // }

    private boolean verifyToken(String token, int uid) {
        JwtTokenUtil jwtTokenUtil = new JwtTokenUtil();
        return jwtTokenUtil.verifyToken(token, uid, encryptionService.getPublicKeyPath());
    }

    private String encaseMessage(String message, String key) {
        String body = "{\"message\":\"" + message + ",";
        body += timestampString() + "\"}";
        return encase(body, key);
    }

    private String encase(String body, String key) {
        String iv = generateRandomString();

        String encryptedBody = encryptionService.encryptAES(body, key, iv);

        String response = "{\"iv\":\"" + iv + "\",\"body\":\""
                + encryptedBody
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

    // private List<Integer> getTeamList(int uid) {
    // return userService.getTeamList(uid);
    // }

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

    private boolean verifyMAC(String mac, String body, String key) {
        try {
            System.out.println("MAC: " + mac);
            String macCheck = encryptionService.generateMAC(body, key);
            System.out.println("MAC CHECK: " + macCheck);
            System.out.println("BODY: " + body);
            System.out.println("KEY: " + key);
            if (macCheck.equals(mac)) {
                return true;
            } else {
                return false;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    private String decryptAESKey(String keyString) {
        byte[] symmetricKeyBytes = Base64.getDecoder().decode(keyString);
        String key = decryptRSA(symmetricKeyBytes);
        return key;
    }

    private String timestampString() {
        return "\"timestamp\":\"" + System.currentTimeMillis() / 1000L + "\"";
    }
}