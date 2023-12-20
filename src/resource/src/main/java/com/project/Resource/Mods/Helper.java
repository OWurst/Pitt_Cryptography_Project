
package com.project.Resource.Mods;


import java.io.File;
import java.io.FileReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.Signature;
import java.security.interfaces.RSAPrivateKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.util.Date;
import java.util.List;
import java.util.Random;

import javax.crypto.spec.IvParameterSpec;

import org.bouncycastle.openssl.PEMKeyPair;
import org.bouncycastle.openssl.PEMParser;
import org.bouncycastle.openssl.jcajce.JcaPEMKeyConverter;
import org.aspectj.bridge.Message;
import org.bouncycastle.asn1.pkcs.PrivateKeyInfo;
import org.bouncycastle.asn1.pkcs.RSAPublicKey;
import org.bouncycastle.asn1.x509.SubjectPublicKeyInfo;
import org.bouncycastle.crypto.signers.RSADigestSigner;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import java.security.Security;


import javax.crypto.Cipher;
import javax.crypto.Mac;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;


import com.project.Resource.Repository.UserSessionRepository;


import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtParser;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.InvalidKeyException;
import jakarta.annotation.PostConstruct;


@Component
public class Helper {
    String RSID;


    @Autowired
    private UserSessionRepository userSessionRepository;


    @Value("${resource.pubKeyPath}")
    private String pubKeyPath;


    @Value("${resource.privKeyPath}")
    private String privKeyPath;


    @Value("${auth.authPubKeyPath}")
    private String authPubKeyPath;


    @PostConstruct
    public void init() {
        Security.addProvider(new BouncyCastleProvider());
    }

    public IvParameterSpec generateIV() {
        byte[] iv = new byte[16];
        new Random().nextBytes(iv);
        return new IvParameterSpec(iv);
    }

    public String encryptAES(byte[] plainText, String aesKey, IvParameterSpec iv) {
        try {
            byte[] aesKeyBytes = Base64.getDecoder().decode(aesKey);
            SecretKeySpec secretKey = new SecretKeySpec(aesKeyBytes, "AES");
            Cipher cipher = Cipher.getInstance("AES/CTR/NoPadding");
            cipher.init(Cipher.ENCRYPT_MODE, secretKey, iv);
            return Base64.getEncoder().encodeToString(cipher.doFinal(plainText));
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }


    public byte[] decryptAES(String plainText, String aesKey, String iv) throws Exception {

        try {
            // convert to byte arrays 
            byte[] plainTextBytes = Base64.getDecoder().decode(plainText);
            byte[] aesKeyBytes = Base64.getDecoder().decode(aesKey);
            IvParameterSpec ivBytes =  new IvParameterSpec(Base64.getDecoder().decode(iv));
            
            SecretKeySpec secretKey = new SecretKeySpec(aesKeyBytes, "AES");
            Cipher cipher = Cipher.getInstance("AES/CTR/NoPadding");
            cipher.init(Cipher.DECRYPT_MODE, secretKey, ivBytes);

            return cipher.doFinal(plainTextBytes);
        } catch (Exception e) {
            throw new Exception(e.getMessage());
        }
    }


    public byte[] decryptRSA(byte[] cipherText) throws Exception {
        try {
            Path actualPrivKeyPath = Paths.get(new URI("file:///" + privKeyPath));
            RSAPrivateKey privateKey = readPKCS8PrivateKeySecondApproach(actualPrivKeyPath.toFile());
            Cipher cipher = Cipher.getInstance("RSA/None/OAEPWithSHA256AndMGF1Padding", "BC");
            cipher.init(Cipher.DECRYPT_MODE, privateKey);
            return cipher.doFinal(cipherText);
            
        } catch (Exception e) {
            throw new Exception(e.getMessage());
        }


    }



    public boolean verifyToken(String token, int teamID, int receivedUID) throws Exception {
        try {
            token = new String(token.getBytes(), StandardCharsets.UTF_8);
            Path actualPubKeyPath = Paths.get(new URI("file:///" + authPubKeyPath));
            JwtParser parser = Jwts.parserBuilder().setSigningKey(readPubKey(actualPubKeyPath.toFile())).build();
            Claims claims = parser.parseClaimsJws(token).getBody();
            int uid = claims.get("uid", Integer.class);

            // Is user ID in token the same as the user ID in the payload?
            if (receivedUID != uid) {
                return false;
            }
            // Do the session ID's match ? 
            if (claims.get("sessionID", Integer.class) != userSessionRepository.findByUserUID(receivedUID).getSessionID()) {
                return false;
            }
            
            // is user in list og teams
            List<Integer> userTeams = claims.get("teams", List.class);
            if (!userTeams.contains(teamID)) {
                return  false;
            }

            // Has this token expired ? 
            Date expiration = claims.get("expires", Date.class);
            Date now = new Date();
            if (expiration.before(now)) {
                return false;
            }
            
            return true;
        } catch (Exception e) {
            throw new Exception(e.getMessage());
        }


    }

    public RSAPrivateKey readPKCS8PrivateKeySecondApproach(File file) throws Exception {
        try (FileReader keyReader = new FileReader(file)) {
            PEMParser pemParser = new PEMParser(keyReader);
            JcaPEMKeyConverter converter = new JcaPEMKeyConverter();
            Object object = pemParser.readObject();
            if (object instanceof PEMKeyPair) {
                KeyPair keyPair = converter.getKeyPair((PEMKeyPair) object);
                return (RSAPrivateKey) keyPair.getPrivate();
            } else if (object instanceof PrivateKeyInfo) {
                return (RSAPrivateKey) converter.getPrivateKey((PrivateKeyInfo) object);
            } else {
                throw new IllegalArgumentException("Unsupported object type: " + object.getClass().getName());
            }
        }


    }


    public PublicKey readPubKey(File file) throws Exception {
        try (FileReader keyReader = new FileReader(file)) {
            PEMParser pemParser = new PEMParser(keyReader);
            JcaPEMKeyConverter converter = new JcaPEMKeyConverter();
            SubjectPublicKeyInfo publicKeyInfo = SubjectPublicKeyInfo.getInstance(pemParser.readObject());
            return converter.getPublicKey(publicKeyInfo);
        }
        catch(Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    public void generateRSID(String pubKey) throws Exception {
        MessageDigest digest;
        try {
            Path actualPubKeyPath = Paths.get(new URI("file:///" + pubKey));
            byte[] fileBytes = Files.readAllBytes(actualPubKeyPath); 

            digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(fileBytes); // Hash the file bytes

            String ID = new String(hash);

            Random rand = new Random();
            int randomNumber = rand.nextInt(33);
            if(randomNumber >= 16) {
                RSID = ID.substring(randomNumber-16, randomNumber);
            } else {
                RSID = ID.substring(randomNumber, randomNumber+16);
            }
        } catch (Exception e) {
            throw new Exception(e.getMessage());
        }
    }
    
    public boolean verifyHMAC(String HMAC, String sharedKey, String payload) throws Exception {
        try {
            
            byte[] HMACBytes = Base64.getDecoder().decode(HMAC);

            byte[] sharedKeyBytes = Base64.getDecoder().decode(sharedKey);

            byte [] decodedPayload = Base64.getDecoder().decode(payload);
            SecretKeySpec secretKey = new SecretKeySpec(sharedKeyBytes, "HmacSHA256");

            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(secretKey);
            byte[] hmac = mac.doFinal(decodedPayload);
            return MessageDigest.isEqual(hmac, HMACBytes);
        } catch (Exception e) {
            throw new Exception(e.getMessage());
        }

    }

    public String generateHMAC(String sharedKey, String payload) throws Exception {
        try {
            byte[] payloadBytes = Base64.getDecoder().decode(payload);
            byte[] sharedKeyBytes = Base64.getDecoder().decode(sharedKey);
    
            SecretKeySpec secretKey = new SecretKeySpec(sharedKeyBytes, "HmacSHA256");
    
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(secretKey);
            byte[] hmac = mac.doFinal(payloadBytes);
    
            return Base64.getEncoder().encodeToString(hmac);
        } catch (Exception e) {
            throw new Exception(e.getMessage());
        }
    }
    
    public boolean verifyTimeStamp(long timestampReceived) {

        long generateCurrentTime = System.currentTimeMillis() / 1000; 
        long timeDifference = generateCurrentTime - timestampReceived;

        // If the message was sent more than 6 seconds ago return false
        if (timeDifference >= 6) {
            return false;
        }
        else return true;
    }


}




