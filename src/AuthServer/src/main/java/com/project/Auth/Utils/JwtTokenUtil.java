package com.project.Auth.Utils;

import java.io.FileReader;
import java.io.Serializable;
import java.net.URI;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Security;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.List;

import org.springframework.stereotype.Component;

import org.bouncycastle.openssl.PEMKeyPair;
import org.bouncycastle.openssl.PEMParser;
import org.bouncycastle.openssl.jcajce.JcaPEMKeyConverter;
import org.bouncycastle.asn1.pkcs.PrivateKeyInfo;
import org.bouncycastle.asn1.x509.SubjectPublicKeyInfo;
import org.bouncycastle.jce.provider.BouncyCastleProvider;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtParser;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;

@Component
public class JwtTokenUtil implements Serializable {

    static {
        Security.addProvider(new BouncyCastleProvider());
    }

    public String generateToken(int uid, String privKeyPath, int sessionID, List<Integer> teamList, String rsid) {
        System.out.println("rsid: " + rsid);
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + 600000);

        Map<String, Object> claims = new HashMap<>();
        claims.put("sessionID", sessionID);
        claims.put("teams", teamList);
        claims.put("uid", uid);
        claims.put("expires", expiryDate.getTime());
        claims.put("rsid", rsid);

        PrivateKey privKey = getPrivateKey(privKeyPath);

        String token = Jwts.builder()
                .setIssuer("AuthServer")
                .setSubject(Integer.toString(uid))
                .setExpiration(expiryDate)
                .setIssuedAt(new Date())
                .setClaims(claims)
                .signWith(privKey, SignatureAlgorithm.RS256)
                .compact();

        return token;
    }

    public boolean verifyToken(String token, int uid, String pubKeyPath) {
        try {
            JwtParser parser = Jwts.parserBuilder().setSigningKey(getPublicKey(pubKeyPath)).build();

            Claims claims = parser.parseClaimsJws(token).getBody();
            int id = claims.get("uid", Integer.class);

            if (id != uid) {
                System.out.println("error id" + id + " and uid " + uid);
                return false;
            }

            Date expiration = claims.get("expires", Date.class);
            Date now = new Date();
            if (expiration.before(now)) {
                System.out.println("error expiration");
                return false;
            }

            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public String getRsid(String token, String pubKeyPath) {
        try {
            JwtParser parser = Jwts.parserBuilder().setSigningKey(getPublicKey(pubKeyPath)).build();

            Claims claims = parser.parseClaimsJws(token).getBody();
            String rsid = claims.get("rsid", String.class);

            System.out.println("rsid: " + rsid);

            return rsid;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public PrivateKey getPrivateKey(String privKeyPath) {
        try {
            Path actualPrivKeyPath = Paths.get(new URI("file:///" + privKeyPath));
            FileReader keyReader = new FileReader(actualPrivKeyPath.toFile());

            PEMParser pemParser = new PEMParser(keyReader);
            JcaPEMKeyConverter converter = new JcaPEMKeyConverter();
            Object object = pemParser.readObject();
            pemParser.close();
            if (object instanceof PEMKeyPair) {
                KeyPair keyPair = converter.getKeyPair((PEMKeyPair) object);
                return (PrivateKey) keyPair.getPrivate();
            } else if (object instanceof PrivateKeyInfo) {
                return (PrivateKey) converter.getPrivateKey((PrivateKeyInfo) object);
            } else {
                return null;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public PublicKey getPublicKey(String pubKeyPath) {
        try {
            Path actualPubKeyPath = Paths.get(new URI("file:///" + pubKeyPath));
            FileReader keyReader = new FileReader(actualPubKeyPath.toFile());

            PEMParser pemParser = new PEMParser(keyReader);
            JcaPEMKeyConverter converter = new JcaPEMKeyConverter();
            Object object = pemParser.readObject();
            pemParser.close();
            if (object instanceof PEMKeyPair) {
                KeyPair keyPair = converter.getKeyPair((PEMKeyPair) object);
                return (PublicKey) keyPair.getPublic();
            } else if (object instanceof SubjectPublicKeyInfo) {
                return (PublicKey) converter.getPublicKey((SubjectPublicKeyInfo) object);
            } else {
                return null;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}