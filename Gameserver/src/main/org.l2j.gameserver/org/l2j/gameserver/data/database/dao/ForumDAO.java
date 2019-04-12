package org.l2j.gameserver.data.database.dao;

import org.l2j.commons.database.DAO;
import org.l2j.commons.database.annotation.Query;
import org.l2j.gameserver.communitybbs.BB.Forum;
import org.l2j.gameserver.communitybbs.BB.Topic;

public interface ForumDAO extends DAO {

    @Query("DELETE FROM forums WHERE (forum_parent=2 AND forum_owner_id NOT IN (SELECT clan_id FROM clan_data)) OR (forum_parent=3 AND forum_owner_id NOT IN (SELECT charId FROM characters));")
    int deleteWithoutOwner();

    @Query("SELECT * FROM forums WHERE forum_id=:forumId:")
    Forum findForumById(int forumId);

    @Query("SELECT forum_id FROM forums WHERE forum_parent=:forumId:")
    Forum findParentById(int forumId);

    @Query("INSERT INTO forums (forum_id,forum_name,forum_parent,forum_post,forum_type,forum_perm,forum_owner_id) VALUES (:forumId:,:forumName:,:fParent:,:forumPost:,:forumType:,:forumPerm:,:ownerID:)")
    void save(int forumId, String forumName, int fParent, int forumPost, int forumType, int forumPerm, int ownerID);

    @Query("SELECT * FROM topic WHERE topic_forum_id=:forumId: ORDER BY topic_id DESC")
    Topic findTopicById(int forumId);
}
