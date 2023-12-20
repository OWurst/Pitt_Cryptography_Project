package com.project.Auth.Service;

import com.project.Auth.Entity.Group;

public interface KeyManagerService {
    public Group changeKey(Group group, String rsid);

    public boolean isRsOnOutdatedKey(Group group, String rsid);

    public String getOldGroupKey(Group group, String rsid);

    public void updateKey(Group group, String rsid);
}
