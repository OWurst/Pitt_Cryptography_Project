package com.project.Auth.Repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
//import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

import com.project.Auth.Entity.MembershipTableEntry;

@EnableJpaRepositories
public interface MembershipTableEntryRepository extends JpaRepository<MembershipTableEntry, Integer> {
    public MembershipTableEntry findOneByGroupIDAndUserID(int groupID, int userID);

    public List<MembershipTableEntry> findByGroupID(int groupID);

    public List<MembershipTableEntry> findByUserID(int userID);

    // @Query(value = """
    // SELECT
    // m.groupID,
    // g.`name`,
    // g.creator_id,
    // CONCAT(u.firstname, ' ', u.lastname) as creatorName,
    // CASE WHEN g.creator_id = ?1 THEN g.join_code ELSE 0 END as join_code
    // FROM membership m
    // JOIN resource_group g on g.gid = m.groupid
    // JOIN users u on u.uid = g.creator_id
    // WHERE m.userid = ?1
    // """, nativeQuery = true)
    // public List<Object[]> findUserGroupsByUserID(int userID);
}
