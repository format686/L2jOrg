/*
 * This file is part of the L2J Mobius project.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package org.l2j.gameserver.communitybbs.BB;

import org.l2j.commons.database.DatabaseFactory;
import org.l2j.commons.database.annotation.*;
import org.l2j.gameserver.communitybbs.Manager.ForumsBBSManager;
import org.l2j.gameserver.communitybbs.Manager.TopicBBSManager;
import org.l2j.gameserver.data.database.dao.ForumDAO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

import static org.l2j.commons.database.DatabaseAccess.getDAO;


public class Forum {
    // type
    public static final int ROOT = 0;
    public static final int NORMAL = 1;
    public static final int CLAN = 2;
    public static final int MEMO = 3;
    public static final int MAIL = 4;
    // perm
    public static final int INVISIBLE = 0;
    public static final int ALL = 1;
    public static final int CLANMEMBERONLY = 2;
    public static final int OWNERONLY = 3;
    private static final Logger LOGGER = LoggerFactory.getLogger(Forum.class);
    private final List<Forum> _children;
    private final Map<Integer, Topic> _topic = new ConcurrentHashMap<>();

    @Column("forum_id")
    private final int _forumId;
    @Column("forum_parent")
    private final Forum _fParent;
    @Column("forum_name")
    private String _forumName;
    @Column("forum_type")
    private int _forumType;
    @Column("forum_post")
    private int _forumPost;
    @Column("forum_perm")
    private int _forumPerm;
    @Column("forum_owner_id")
    private int _ownerID;

    private boolean _loaded = false;

    //Construtor que eu fiz
    public Forum() {
        _children = null;
        _forumId = 0;
        _fParent = null;
    }

    /**
     * Creates new instance of Forum. When you create new forum, use {@link org.l2j.gameserver.communitybbs.Manager.ForumsBBSManager# addForum(org.l2j.gameserver.communitybbs.BB.Forum)} to add forum to the forums manager.
     *
     * @param Forumid
     * @param FParent
     */
    public Forum(int Forumid, Forum FParent) {
        _forumId = Forumid;
        _fParent = FParent;
        _children = new CopyOnWriteArrayList<>();
    }

    /**
     * @param name
     * @param parent
     * @param type
     * @param perm
     * @param OwnerID
     */
    public Forum(String name, Forum parent, int type, int perm, int OwnerID) {
        _forumName = name;
        _forumId = ForumsBBSManager.getInstance().getANewID();
        _forumType = type;
        _forumPost = 0;
        _forumPerm = perm;
        _fParent = parent;
        _ownerID = OwnerID;
        _children = new CopyOnWriteArrayList<>();
        parent._children.add(this);
        ForumsBBSManager.getInstance().addForum(this);
        _loaded = true;
    }


    private void load() {
        getDAO(ForumDAO.class).findForumById(_forumId);


        final Topic t = getDAO(ForumDAO.class).findTopicById(_forumId);
        if (t != null) {
            _topic.put(t.getID(), t);
            if (t.getID() > TopicBBSManager.getInstance().getMaxID(this)) {
                TopicBBSManager.getInstance().setMaxID(t.getID(), this);
            }
        }
    }

    private void getChildren() {
        //Verificar a necessidade de prevenção ao null exception
        final Forum f = getDAO(ForumDAO.class).findParentById(_forumId);
        _children.add(f);
        ForumsBBSManager.getInstance().addForum(f);
    }

    public int getTopicSize() {
        vload();
        return _topic.size();
    }

    public Topic getTopic(int j) {
        vload();
        return _topic.get(j);
    }

    public void addTopic(Topic t) {
        vload();
        _topic.put(t.getID(), t);
    }

    /**
     * @return the forum Id
     */
    public int getID() {
        return _forumId;
    }

    public String getName() {
        vload();
        return _forumName;
    }

    public int getType() {
        vload();
        return _forumType;
    }

    /**
     * @param name the forum name
     * @return the forum for the given name
     */
    public Forum getChildByName(String name) {
        vload();
        return _children.stream().filter(f -> f.getName().equals(name)).findFirst().orElse(null);
    }

    /**
     * @param id
     */
    public void rmTopicByID(int id) {
        _topic.remove(id);
    }

    //TODO: PADRONIZAÇÃO DAS CONSULTAS AO BD UTILIZANDO DAO
    public void insertIntoDb() {
        getDAO(ForumDAO.class).save(_forumId, _forumName, _fParent.getID(), _forumPost, _forumType, _forumPerm, _ownerID);
    }

    public void vload() {
        if (!_loaded) {
            load();
            getChildren();
            _loaded = true;
        }
    }
}