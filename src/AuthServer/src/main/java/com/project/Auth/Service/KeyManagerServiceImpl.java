package com.project.Auth.Service;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;

import java.util.Base64;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.project.Auth.Entity.Group;
import com.project.Auth.Entity.KeyTableEntry;
import com.project.Auth.Repository.KeyTableRepository;

@Service
public class KeyManagerServiceImpl implements KeyManagerService {

    @Autowired
    private KeyTableRepository keyTableRepository;

    public Group changeKey(Group group, String rsid) {
        int gid = group.getId();

        String newKey = generateAESKey();
        group.setAesKey(newKey);

        List<KeyTableEntry> oldKeys = keyTableRepository.findAllByGid(gid);
        for (KeyTableEntry keyTableEntry : oldKeys) {
            keyTableEntry.setOutdated(true);
            keyTableRepository.save(keyTableEntry);
        }

        return group;
    }

    public boolean isRsOnOutdatedKey(Group group, String rsid) {
        try {
            KeyTableEntry keyTableEntry = keyTableRepository.findByGidAndRsid(group.getId(), rsid);

            if (keyTableEntry == null) {
                keyTableEntry = new KeyTableEntry(group.getId(), rsid, group.getAesKey());
                keyTableEntry.setOutdated(false);
            }

            return keyTableEntry.isOutdated();
        } catch (Exception e) {
            return false;
        }
    }

    public String getOldGroupKey(Group group, String rsid) {
        try {
            KeyTableEntry keyTableEntry = keyTableRepository.findByGidAndRsid(group.getId(), rsid);
            return keyTableEntry.getAesKey();
        } catch (Exception e) {
            return null;
        }
    }

    public void updateKey(Group group, String rsid) {
        KeyTableEntry keyTableEntry = keyTableRepository.findByGidAndRsid(group.getId(), rsid);
        if (keyTableEntry != null) {
            keyTableEntry = new KeyTableEntry(group.getId(), rsid, group.getAesKey());
            keyTableEntry.setOutdated(false);
        } else {
            keyTableEntry = new KeyTableEntry(group.getId(), rsid, group.getAesKey());
        }
        keyTableRepository.save(keyTableEntry);
    }

    private String generateAESKey() {
        KeyGenerator keyGenerator = null;
        try {
            keyGenerator = KeyGenerator.getInstance("AES");
            keyGenerator.init(192);
        } catch (Exception e) {
            System.out.println("Failed to generate key. Error: " + e);
            return null;
        }
        SecretKey key = keyGenerator.generateKey();
        return Base64.getEncoder().encodeToString(key.getEncoded());
    }
}
