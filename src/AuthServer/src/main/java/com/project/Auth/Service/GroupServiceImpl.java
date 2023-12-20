package com.project.Auth.Service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.project.Auth.DTO.GroupDTO;
import com.project.Auth.DTO.RemoveFromGroupDTO;
import com.project.Auth.Entity.Group;
import com.project.Auth.Entity.MembershipTableEntry;
import com.project.Auth.Entity.User;
import com.project.Auth.Repository.GroupRepository;
import com.project.Auth.Repository.MembershipTableEntryRepository;
import com.project.Auth.Repository.UserRepository;
import com.project.Auth.Utils.JwtTokenUtil;

@Service
public class GroupServiceImpl implements GroupService {
    @Autowired
    UserRepository userRepository;

    @Autowired
    GroupRepository groupRepository;

    @Autowired
    EncryptionService encryptionService;

    @Autowired
    UserService userService;

    @Autowired
    MembershipTableEntryRepository membershipTableEntryRepository;

    @Autowired
    KeyManagerService keyManagerService;

    public ResponseEntity<String> createGroup(GroupDTO groupDTO) {
        String body;
        HttpStatus status;

        User user = userRepository.findOneById(groupDTO.getUid());

        if (user != null && user.isLoggedIn()) {
            if (groupRepository.findByName(groupDTO.getGroupName()) == null) {
                // create group
                Group group = new Group(groupDTO.getGroupName(), user);

                // save group (with no key)
                groupRepository.save(group);

                group = groupRepository.findByName(groupDTO.getGroupName());
                System.out.println("Group id is " + group.getId());
                JwtTokenUtil jwtTokenUtil = new JwtTokenUtil();
                String rsid = jwtTokenUtil.getRsid(groupDTO.getAuthToken(), encryptionService.getPublicKeyPath());

                System.out.println("RSID: " + rsid);

                // generate key for group, save in group|key|rsid table
                group = keyManagerService.changeKey(group,
                        jwtTokenUtil.getRsid(groupDTO.getAuthToken(), encryptionService.getPublicKeyPath()));

                // make table entry reflect that rs is operating on the newest key
                keyManagerService.updateKey(group,
                        jwtTokenUtil.getRsid(groupDTO.getAuthToken(), encryptionService.getPublicKeyPath()));

                // save group (with key)
                groupRepository.save(group);

                MembershipTableEntry entry = new MembershipTableEntry(user.getId(), group.getId());
                membershipTableEntryRepository.save(entry);

                body = "Success Creating Group: " + groupDTO.getGroupName();
                body = reqBody(body, user.getId(), groupDTO.getAuthToken());
                status = HttpStatus.OK;
            } else {
                body = "Group Creation Unsuccessful: Group Name is Taken";
                body = reqBody(body, user.getId(), groupDTO.getAuthToken());
                status = HttpStatus.BAD_REQUEST;
            }
        } else {
            body = "User not verified";
            body = reqBody(body, groupDTO.getUid(), groupDTO.getAuthToken());
            status = HttpStatus.FORBIDDEN;
        }
        return new ResponseEntity<>(body, status);
    }

    public ResponseEntity<String> getJoinCode(GroupDTO groupDTO) {
        String body;
        HttpStatus status;

        User user = userRepository.findOneById(groupDTO.getUid());
        if (user != null && user.isLoggedIn()) {
            if (userIsInGroup(user.getId(), groupDTO.getGroupID())) {

                Group group = groupRepository.findByName(groupDTO.getGroupName());

                body = "{\"joinCode\":\"" + group.getJoinCode() + ","
                        + jwtTokenString(group.getCreator().getId(), groupDTO.getAuthToken()) + "," + timestampString()
                        + "\"}";
                status = HttpStatus.OK;
            } else {
                body = "Request Denied: User not part of Group";
                body = reqBody(body, user.getId(), groupDTO.getAuthToken());
                status = HttpStatus.FORBIDDEN;
            }
        } else {
            body = "User not verified";
            body = reqBody(body, groupDTO.getUid(), groupDTO.getAuthToken());
            status = HttpStatus.FORBIDDEN;
        }
        return new ResponseEntity<>(body, status);
    }

    public ResponseEntity<String> updatedGroupKey(GroupDTO groupDTO) {
        String body;
        HttpStatus status;

        User user = userRepository.findOneById(groupDTO.getUid());
        if (user != null && user.isLoggedIn()) {
            if (userIsInGroup(user.getId(), groupDTO.getGroupID())) {
                Group group = groupRepository.findOneById(groupDTO.getGroupID());

                if (group.getCreator().getId() == user.getId()) {
                    try {
                        JwtTokenUtil jwtTokenUtil = new JwtTokenUtil();
                        String rsid = jwtTokenUtil.getRsid(groupDTO.getAuthToken(),
                                encryptionService.getPublicKeyPath());

                        keyManagerService.updateKey(group, rsid);

                        body = "Success";
                        body = reqBody(body, user.getId(), groupDTO.getAuthToken());
                        status = HttpStatus.OK;
                    } catch (Exception e) {
                        body = "Request Failed: Exception thrown";
                        status = HttpStatus.INTERNAL_SERVER_ERROR;
                        e.printStackTrace();
                    }
                } else {
                    body = "Request Denied: User is not Group Creator";
                    body = reqBody(body, user.getId(), groupDTO.getAuthToken());
                    status = HttpStatus.FORBIDDEN;
                }
            } else {
                body = "Request Denied: User not part of Group";
                body = reqBody(body, user.getId(), groupDTO.getAuthToken());
                status = HttpStatus.FORBIDDEN;
            }
        } else {
            body = "User not verified";
            body = reqBody(body, groupDTO.getUid(), groupDTO.getAuthToken());
            status = HttpStatus.FORBIDDEN;
        }
        return new ResponseEntity<>(body, status);
    }

    public ResponseEntity<String> joinGroup(GroupDTO groupDTO) {
        String body;
        HttpStatus status;

        User user = userRepository.findOneById(groupDTO.getUid());
        if (user != null && user.isLoggedIn()) {
            Group group = groupRepository.findByJoinCode(groupDTO.getJoinCode());
            if (group != null) {
                int groupID = group.getId();
                int uid = user.getId();

                MembershipTableEntry quickCheck = membershipTableEntryRepository.findOneByGroupIDAndUserID(groupID,
                        uid);
                if (quickCheck != null) {
                    body = "Request Denied: User is Already Part of Group";
                    body = reqBody(body, user.getId(), groupDTO.getAuthToken());
                    status = HttpStatus.FORBIDDEN;
                } else {
                    MembershipTableEntry entry = new MembershipTableEntry(uid, groupID);
                    membershipTableEntryRepository.save(entry);

                    body = "Success";
                    body = reqBody(body, user.getId(), groupDTO.getAuthToken());
                    status = HttpStatus.OK;
                }
            } else {
                body = "Request Denied: Invalid Join Code";
                body = reqBody(body, user.getId(), groupDTO.getAuthToken());
                status = HttpStatus.FORBIDDEN;
            }
        } else {
            body = "Request Denied: User not verified";
            body = reqBody(body, groupDTO.getUid(), groupDTO.getAuthToken());
            status = HttpStatus.FORBIDDEN;
        }
        return new ResponseEntity<>(body, status);
    }

    public ResponseEntity<String> getGroupInfo(GroupDTO groupDTO) {
        String body;
        HttpStatus status;

        User user = userRepository.findOneById(groupDTO.getUid());
        if (user != null && user.isLoggedIn()) {
            if (userIsInGroup(user.getId(), groupDTO.getGroupID())) {
                Group group = groupRepository.findOneById(groupDTO.getGroupID());

                JwtTokenUtil jwtTokenUtil = new JwtTokenUtil();
                String rsid = jwtTokenUtil.getRsid(groupDTO.getAuthToken(), encryptionService.getPublicKeyPath());

                String oldKey;
                boolean isOutdated = keyManagerService.isRsOnOutdatedKey(group, rsid);
                if (isOutdated) {
                    oldKey = keyManagerService.getOldGroupKey(group, rsid);
                } else {
                    oldKey = "";
                }

                body = "{";
                body += timestampString() + ",";
                body += "\"groupID\":" + group.getId() + ",";
                body += "\"groupName\":\"" + group.getName() + "\",";
                body += "\"creator\":\"" + group.getCreator().getUsername() + "\",";
                body += "\"creatorID\":" + group.getCreator().getId() + ",";
                body += "\"joinCode\":\"" + group.getJoinCode() + "\",";
                body += "\"members\":" + userJSONList(membershipTableEntryRepository.findByGroupID(group.getId()))
                        + ",";
                body += "\"groupKey\":\"" + group.getAesKey() + "\",";
                body += "\"isGroupKeyOutOfDate\":" + isOutdated + ",";
                body += "\"oldKey\":\"" + oldKey + "\",";
                body += jwtTokenString(user.getId(), groupDTO.getAuthToken());
                body += "}";

                status = HttpStatus.OK;
            } else {
                body = "Request Denied: User not part of Group";
                body = reqBody(body, user.getId(), groupDTO.getAuthToken());
                status = HttpStatus.FORBIDDEN;
            }
        } else {
            body = "User not verified";
            body = reqBody(body, groupDTO.getUid(), groupDTO.getAuthToken());
            status = HttpStatus.FORBIDDEN;
        }
        return new ResponseEntity<>(body, status);
    }

    public ResponseEntity<String> leaveGroup(GroupDTO groupDTO) {
        String body;
        HttpStatus status;

        User user = userRepository.findOneById(groupDTO.getUid());
        if (user != null && user.isLoggedIn()) {
            if (userIsInGroup(user.getId(), groupDTO.getGroupID())) {
                Group group = groupRepository.findOneById(groupDTO.getGroupID());

                List<MembershipTableEntry> groupMembers = membershipTableEntryRepository.findByGroupID(group.getId());

                if (group.getCreator().getId() == user.getId()) {
                    if (groupMembers.size() > 1) {
                        int newLeaderID = groupDTO.getNewLeaderID();
                        User newLeader = userRepository.findOneById(newLeaderID);
                        if (newLeader != null && userIsInGroup(newLeaderID, groupDTO.getGroupID())) {
                            group.setCreator(newLeader);
                            groupRepository.save(group);
                        } else {
                            body = "Request Denied: Invalid New Leader ID";
                            body = reqBody(body, user.getId(), groupDTO.getAuthToken());
                            status = HttpStatus.FORBIDDEN;
                            return new ResponseEntity<>(body, status);
                        }
                    } else {
                        groupRepository.delete(group);
                    }
                }

                MembershipTableEntry entry = membershipTableEntryRepository.findOneByGroupIDAndUserID(group.getId(),
                        groupDTO.getUid());
                membershipTableEntryRepository.delete(entry);

                keyManagerService.changeKey(group, groupDTO.getAuthToken());

                body = "Delete Successful";
                body = reqBody(body, user.getId(), groupDTO.getAuthToken());
                status = HttpStatus.OK;
            } else {
                body = "Request Denied: User not part of Group";
                body = reqBody(body, user.getId(), groupDTO.getAuthToken());
                status = HttpStatus.FORBIDDEN;
            }
        } else {
            body = "User not verified";
            body = reqBody(body, groupDTO.getUid(), groupDTO.getAuthToken());
            status = HttpStatus.FORBIDDEN;
        }
        return new ResponseEntity<>(body, status);
    }

    public ResponseEntity<String> removeUserFromGroup(RemoveFromGroupDTO removeFromGroupDTO) {
        String body;
        HttpStatus status;

        User user = userRepository.findOneById(removeFromGroupDTO.getUid());
        User userToRemove = userRepository.findOneById(removeFromGroupDTO.getUserToRemoveID());
        if (user != null && user.isLoggedIn()
                && userIsInGroup(removeFromGroupDTO.getUserToRemoveID(), removeFromGroupDTO.getGroupID())) {
            if (userIsInGroup(user.getId(), removeFromGroupDTO.getGroupID())) {
                Group group = groupRepository.findOneById(removeFromGroupDTO.getGroupID());

                if (group.getCreator().getId() == user.getId() && userToRemove.getId() != group.getCreator().getId()) {
                    MembershipTableEntry entry = membershipTableEntryRepository.findOneByGroupIDAndUserID(group.getId(),
                            userToRemove.getId());
                    membershipTableEntryRepository.delete(entry);

                }
                keyManagerService.changeKey(group, removeFromGroupDTO.getAuthToken());
                body = "Delete Successful";
                body = reqBody(body, user.getId(), removeFromGroupDTO.getAuthToken());
                status = HttpStatus.OK;
            } else {
                body = "Request Denied: User not part of Group";
                body = reqBody(body, user.getId(), removeFromGroupDTO.getAuthToken());
                status = HttpStatus.FORBIDDEN;
            }
        } else {
            body = "Action is not permitted";
            body = reqBody(body, removeFromGroupDTO.getUid(), removeFromGroupDTO.getAuthToken());
            status = HttpStatus.FORBIDDEN;
        }
        return new ResponseEntity<>(body, status);
    }

    private boolean userIsInGroup(int userID, int groupID) {
        Group group = groupRepository.findOneById(groupID);
        if (group == null)
            return false;

        MembershipTableEntry entry = membershipTableEntryRepository.findOneByGroupIDAndUserID(group.getId(), userID);
        if (entry == null)
            return false;
        if (entry.getGroupID() == group.getId() && entry.getUserID() == userID)
            return true;

        return false;
    }

    private String userJSONList(List<MembershipTableEntry> userList) {
        String body = "[";

        for (int i = 0; i < userList.size(); i++) {
            User user = userRepository.findOneById(userList.get(i).getUserID());

            body += "{\"username\":\"" + user.getUsername() + "\",\"firstname\":\"" + user.getFirstname()
                    + "\",\"lastname\":\"" + user.getLastname() + "\",\"id\":" + user.getId() + "}";

            if (i + 1 < userList.size())
                body += ",";
        }

        body += "]";
        return body;
    }

    private String jwtTokenString(int id, String token) {
        JwtTokenUtil jwtTokenUtil = new JwtTokenUtil();

        String rsid = jwtTokenUtil.getRsid(token, encryptionService.getPublicKeyPath());

        String jwt = jwtTokenUtil.generateToken(id, encryptionService.getPrivateKeyPath(),
                userService.getSessionID(id),
                getTeamList(id), rsid);

        String ret = "\"authToken\":\"" + jwt + "\"";
        return ret;
    }

    private String reqBody(String body, int id, String token) {
        return "{\"msg\":\"" + body + "\"," + jwtTokenString(id, token) + "," + timestampString() + "}";
    }

    public List<Integer> getTeamList(int uid) {
        List<MembershipTableEntry> entries = membershipTableEntryRepository.findByUserID(uid);

        List<Integer> ret = new ArrayList<Integer>();

        for (MembershipTableEntry entry : entries) {
            ret.add(entry.getGroupID());
        }
        return ret;
    }

    private String timestampString() {
        return "\"timestamp\":\"" + System.currentTimeMillis() / 1000L + "\"";
    }
}