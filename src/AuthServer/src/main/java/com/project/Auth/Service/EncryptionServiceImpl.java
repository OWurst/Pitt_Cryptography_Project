package com.project.Auth.Service;

import java.nio.charset.StandardCharsets;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.Base64;

import javax.crypto.Cipher;
import javax.crypto.Mac;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import com.project.Auth.Entity.SymmetricKeyTable;
import com.project.Auth.Repository.SymmetricKeyTableRepository;
import com.project.Auth.Utils.JwtTokenUtil;

@Service
public class EncryptionServiceImpl implements EncryptionService {
    @Autowired
    SymmetricKeyTableRepository symmetricKeyTableRepository;

    @Value("${auth.pubKeyPath}")
    private String pubKeyPath;

    @Value("${auth.privKeyPath}")
    private String privKeyPath;

    // static {
    // Security.addProvider(new BouncyCastleProvider());
    // }

    public String encryptAES(String plaintext, String key, String IV) {
        try {
            byte[] aesKeyBytes = Base64.getDecoder().decode(key);

            SecretKeySpec secretKey = new SecretKeySpec(aesKeyBytes, "AES");

            Cipher cipher = Cipher.getInstance("AES/CTR/NoPadding");

            byte[] ivBytes = Base64.getDecoder().decode(IV);
            IvParameterSpec iv = new IvParameterSpec(ivBytes);

            cipher.init(Cipher.ENCRYPT_MODE, secretKey, iv);

            byte[] ciphertextBytes = cipher.doFinal(plaintext.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(ciphertextBytes);

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public String decryptAES(String ciphertext, String key, String IV) {
        try {
            byte[] aesKeyBytes = Base64.getDecoder().decode(key);
            SecretKeySpec secretKey = new SecretKeySpec(aesKeyBytes, "AES");
            Cipher cipher = Cipher.getInstance("AES/CTR/NoPadding");
            byte[] ivBytes = Base64.getDecoder().decode(IV);
            IvParameterSpec iv = new IvParameterSpec(ivBytes);
            cipher.init(Cipher.DECRYPT_MODE, secretKey, iv);
            byte[] encryptedBytes = cipher.doFinal(Base64.getDecoder().decode(ciphertext));
            return new String(encryptedBytes, StandardCharsets.UTF_8);

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public ResponseEntity<String> requestPubKey() {
        String body;
        HttpStatus status;
        JwtTokenUtil jwtTokenUtil = new JwtTokenUtil();

        try {
            String key = publicKeyToString(jwtTokenUtil.getPublicKey(pubKeyPath));

            body = "{\"publicKey\":\"" + key + "\"}";
            status = HttpStatus.OK;
        } catch (Exception e) {
            body = "Failed to read public key";
            status = HttpStatus.INTERNAL_SERVER_ERROR;
            e.printStackTrace();
        }
        return new ResponseEntity<>(body, status);
    }

    public ResponseEntity<String> requestPrivKey() {
        String body;
        HttpStatus status;
        JwtTokenUtil jwtTokenUtil = new JwtTokenUtil();

        try {
            String key = privateKeyToString(jwtTokenUtil.getPrivateKey(privKeyPath));

            body = "{\"privateKey\":\"" + key + "\"}";
            status = HttpStatus.OK;
        } catch (Exception e) {
            body = "Failed to read private key";
            status = HttpStatus.INTERNAL_SERVER_ERROR;
            e.printStackTrace();
        }
        return new ResponseEntity<>(body, status);
    }

    public void storeAESKey(String aesKey, int uid, int sessionID) {
        SymmetricKeyTable entry = symmetricKeyTableRepository.findByUid(uid);

        if (entry == null) {
            entry = new SymmetricKeyTable(uid, aesKey, sessionID);
        } else {
            entry.setAesKey(aesKey);
            entry.setSessionID(sessionID);
        }
        symmetricKeyTableRepository.save(entry);
    }

    public String getAESKey(int uid) {
        String aesKey = null;
        try {
            SymmetricKeyTable symmetricKeyTable = symmetricKeyTableRepository.findByUid(uid);
            aesKey = symmetricKeyTable.getAesKey();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return aesKey;
    }

    public String byteArrayToString(byte[] bytes) {
        return Base64.getEncoder().encodeToString(bytes);
    }

    public String getPublicKeyPath() {
        return pubKeyPath;
    }

    public String getPrivateKeyPath() {
        return privKeyPath;
    }

    public String generateMAC(String message, String key) {
        try {
            byte[] keyBytes = Base64.getDecoder().decode(key);
            SecretKeySpec secretKeySpec = new SecretKeySpec(keyBytes, "HmacSHA256");

            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(secretKeySpec);
            byte[] byteSignature = mac.doFinal(Base64.getDecoder().decode(message));

            String hmac = Base64.getEncoder().encodeToString(byteSignature);
            return hmac;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private String publicKeyToString(PublicKey publicKey) {
        byte[] publicKeyBytes = publicKey.getEncoded();
        return Base64.getEncoder().encodeToString(publicKeyBytes);
    }

    private String privateKeyToString(PrivateKey privateKey) {
        byte[] privateKeyBytes = privateKey.getEncoded();
        return Base64.getEncoder().encodeToString(privateKeyBytes);
    }
}
