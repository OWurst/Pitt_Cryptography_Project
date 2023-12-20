package com.project.Auth.Entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "Membership")
public class MembershipTableEntry {
    @Id
    @Column
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int mid;

    @Column
    private int userID;

    @Column
    private int groupID;

    public MembershipTableEntry() {
    }

    public MembershipTableEntry(int userID, int groupID) {
        this.userID = userID;
        this.groupID = groupID;
    }

    public void setMid(int mid) {
        this.mid = mid;
    }

    public void setUserID(int userID) {
        this.userID = userID;
    }

    public void setGroupID(int groupID) {
        this.groupID = groupID;
    }

    public int getId() {
        return mid;
    }

    public int getUserID() {
        return userID;
    }

    public int getGroupID() {
        return groupID;
    }
}
