package org.l2j.gameserver.instancemanager;

import org.l2j.commons.database.DatabaseFactory;
import org.l2j.gameserver.model.L2Mentee;
import org.l2j.gameserver.model.L2World;
import org.l2j.gameserver.model.actor.instance.L2PcInstance;
import org.l2j.gameserver.model.skills.BuffInfo;
import org.l2j.gameserver.model.skills.Skill;
import org.l2j.gameserver.model.variables.PlayerVariables;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;


/**
 * @author UnAfraid
 */
public class MentorManager {
    private static final Logger LOGGER = LoggerFactory.getLogger(MentorManager.class);

    private final Map<Integer, Map<Integer, L2Mentee>> _menteeData = new ConcurrentHashMap<>();
    private final Map<Integer, L2Mentee> _mentors = new ConcurrentHashMap<>();

    private MentorManager() {
        load();
    }

    private void load() {
        try (Connection con = DatabaseFactory.getInstance().getConnection();
             Statement statement = con.createStatement();
             ResultSet rset = statement.executeQuery("SELECT * FROM character_mentees")) {
            while (rset.next()) {
                addMentor(rset.getInt("mentorId"), rset.getInt("charId"));
            }
        } catch (Exception e) {
            LOGGER.warn(e.getMessage(), e);
        }
    }

    /**
     * Removes mentee for current L2PcInstance
     *
     * @param mentorId
     * @param menteeId
     */
    public void deleteMentee(int mentorId, int menteeId) {
        try (Connection con = DatabaseFactory.getInstance().getConnection();
             PreparedStatement statement = con.prepareStatement("DELETE FROM character_mentees WHERE mentorId = ? AND charId = ?")) {
            statement.setInt(1, mentorId);
            statement.setInt(2, menteeId);
            statement.execute();
        } catch (Exception e) {
            LOGGER.warn(e.getMessage(), e);
        }
    }

    /**
     * @param mentorId
     * @param menteeId
     */
    public void deleteMentor(int mentorId, int menteeId) {
        try (Connection con = DatabaseFactory.getInstance().getConnection();
             PreparedStatement statement = con.prepareStatement("DELETE FROM character_mentees WHERE mentorId = ? AND charId = ?")) {
            statement.setInt(1, mentorId);
            statement.setInt(2, menteeId);
            statement.execute();
        } catch (Exception e) {
            LOGGER.warn(e.getMessage(), e);
        } finally {
            removeMentor(mentorId, menteeId);
        }
    }

    public boolean isMentor(int objectId) {
        return _menteeData.containsKey(objectId);
    }

    public boolean isMentee(int objectId) {
        return _menteeData.values().stream().anyMatch(map -> map.containsKey(objectId));
    }

    public Map<Integer, Map<Integer, L2Mentee>> getMentorData() {
        return _menteeData;
    }

    public void cancelAllMentoringBuffs(L2PcInstance player) {
        if (player == null) {
            return;
        }

        //@formatter:off
        player.getEffectList().getEffects()
                .stream()
                .map(BuffInfo::getSkill)
                .filter(Skill::isMentoring)
                .forEach(player::stopSkillEffects);
        //@formatter:on
    }

    public void setPenalty(int mentorId, long penalty) {
        final L2PcInstance player = L2World.getInstance().getPlayer(mentorId);
        final PlayerVariables vars = player != null ? player.getVariables() : new PlayerVariables(mentorId);
        vars.set("Mentor-Penalty-" + mentorId, String.valueOf(System.currentTimeMillis() + penalty));
    }

    public long getMentorPenalty(int mentorId) {
        final L2PcInstance player = L2World.getInstance().getPlayer(mentorId);
        final PlayerVariables vars = player != null ? player.getVariables() : new PlayerVariables(mentorId);
        return vars.getLong("Mentor-Penalty-" + mentorId, 0);
    }

    /**
     * @param mentorId
     * @param menteeId
     */
    public void addMentor(int mentorId, int menteeId) {
        final Map<Integer, L2Mentee> mentees = _menteeData.computeIfAbsent(mentorId, map -> new ConcurrentHashMap<>());
        if (mentees.containsKey(menteeId)) {
            mentees.get(menteeId).load(); // Just reloading data if is already there
        } else {
            mentees.put(menteeId, new L2Mentee(menteeId));
        }
    }

    /**
     * @param mentorId
     * @param menteeId
     */
    public void removeMentor(int mentorId, int menteeId) {
        if (_menteeData.containsKey(mentorId)) {
            _menteeData.get(mentorId).remove(menteeId);
            if (_menteeData.get(mentorId).isEmpty()) {
                _menteeData.remove(mentorId);
                _mentors.remove(mentorId);
            }
        }
    }

    /**
     * @param menteeId
     * @return
     */
    public L2Mentee getMentor(int menteeId) {
        for (Entry<Integer, Map<Integer, L2Mentee>> map : _menteeData.entrySet()) {
            if (map.getValue().containsKey(menteeId)) {
                if (!_mentors.containsKey(map.getKey())) {
                    _mentors.put(map.getKey(), new L2Mentee(map.getKey()));
                }
                return _mentors.get(map.getKey());
            }
        }
        return null;
    }

    public Collection<L2Mentee> getMentees(int mentorId) {
        if (_menteeData.containsKey(mentorId)) {
            return _menteeData.get(mentorId).values();
        }
        return Collections.emptyList();
    }

    /**
     * @param mentorId
     * @param menteeId
     * @return
     */
    public L2Mentee getMentee(int mentorId, int menteeId) {
        if (_menteeData.containsKey(mentorId)) {
            return _menteeData.get(mentorId).get(menteeId);
        }
        return null;
    }

    public boolean isAllMenteesOffline(int menteorId, int menteeId) {
        boolean isAllMenteesOffline = true;
        for (L2Mentee men : getMentees(menteorId)) {
            if (men.isOnline() && (men.getObjectId() != menteeId)) {
                if (isAllMenteesOffline) {
                    isAllMenteesOffline = false;
                    break;
                }
            }
        }
        return isAllMenteesOffline;
    }

    public boolean hasOnlineMentees(int menteorId) {
        return getMentees(menteorId).stream().filter(Objects::nonNull).filter(L2Mentee::isOnline).count() > 0;
    }

    public static MentorManager getInstance() {
        return Singleton.INSTANCE;
    }

    private static class Singleton {
        private static final MentorManager INSTANCE = new MentorManager();
    }
}
