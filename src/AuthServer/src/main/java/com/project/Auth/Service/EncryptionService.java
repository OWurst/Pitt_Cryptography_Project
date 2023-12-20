package com.project.Auth.Service;

import org.springframework.http.ResponseEntity;

public interface EncryptionService {
    public String encryptAES(String plaintext, String key, String IV);

    public String decryptAES(String ciphertext, String key, String IV);

    public ResponseEntity<String> requestPubKey();

    public ResponseEntity<String> requestPrivKey();

    public void storeAESKey(String aesKey, int uid, int sessionID);

    public String getAESKey(int uid);

    public String getPublicKeyPath();

    public String getPrivateKeyPath();

    public String generateMAC(String message, String key);
}
