package org.l2j.gameserver.util;

import org.l2j.gameserver.enums.ChatType;
import org.l2j.gameserver.model.L2World;
import org.l2j.gameserver.model.actor.L2Character;
import org.l2j.gameserver.model.actor.L2Summon;
import org.l2j.gameserver.model.actor.instance.L2PcInstance;
import org.l2j.gameserver.network.serverpackets.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * This class ...
 *
 * @version $Revision: 1.2 $ $Date: 2004/06/27 08:12:59 $
 */
public final class Broadcast {
    private static final Logger LOGGER = LoggerFactory.getLogger(Broadcast.class);

    /**
     * Send a packet to all L2PcInstance in the _KnownPlayers of the L2Character that have the Character targeted.<BR>
     * <B><U> Concept</U> :</B><BR>
     * L2PcInstance in the detection area of the L2Character are identified in <B>_knownPlayers</B>.<BR>
     * In order to inform other players of state modification on the L2Character, server just need to go through _knownPlayers to send Server->Client Packet<BR>
     * <FONT COLOR=#FF0000><B> <U>Caution</U> : This method DOESN'T SEND Server->Client packet to this L2Character (to do this use method toSelfAndKnownPlayers)</B></FONT><BR>
     *
     * @param character
     * @param mov
     */
    public static void toPlayersTargettingMyself(L2Character character, IClientOutgoingPacket mov) {
        L2World.getInstance().forEachVisibleObject(character, L2PcInstance.class, player ->
        {
            if (player.getTarget() == character) {
                player.sendPacket(mov);
            }
        });
    }

    /**
     * Send a packet to all L2PcInstance in the _KnownPlayers of the L2Character.<BR>
     * <B><U> Concept</U> :</B><BR>
     * L2PcInstance in the detection area of the L2Character are identified in <B>_knownPlayers</B>.<BR>
     * In order to inform other players of state modification on the L2Character, server just need to go through _knownPlayers to send Server->Client Packet<BR>
     * <FONT COLOR=#FF0000><B> <U>Caution</U> : This method DOESN'T SEND Server->Client packet to this L2Character (to do this use method toSelfAndKnownPlayers)</B></FONT><BR>
     *
     * @param character
     * @param mov
     */
    public static void toKnownPlayers(L2Character character, IClientOutgoingPacket mov) {
        L2World.getInstance().forEachVisibleObject(character, L2PcInstance.class, player ->
        {
            try {
                player.sendPacket(mov);
                if ((mov instanceof CharInfo) && (character.isPlayer())) {
                    final int relation = ((L2PcInstance) character).getRelation(player);
                    final Integer oldrelation = character.getKnownRelations().get(player.getObjectId());
                    if ((oldrelation != null) && (oldrelation != relation)) {
                        final RelationChanged rc = new RelationChanged();
                        rc.addRelation((L2PcInstance) character, relation, character.isAutoAttackable(player));
                        if (character.hasSummon()) {
                            final L2Summon pet = character.getPet();
                            if (pet != null) {
                                rc.addRelation(pet, relation, character.isAutoAttackable(player));
                            }
                            if (character.hasServitors()) {
                                character.getServitors().values().forEach(s -> rc.addRelation(s, relation, character.isAutoAttackable(player)));
                            }
                        }
                        player.sendPacket(rc);
                        character.getKnownRelations().put(player.getObjectId(), relation);
                    }
                }
            } catch (NullPointerException e) {
                LOGGER.warn(e.getMessage(), e);
            }
        });
    }

    /**
     * Send a packet to all L2PcInstance in the _KnownPlayers (in the specified radius) of the L2Character.<BR>
     * <B><U> Concept</U> :</B><BR>
     * L2PcInstance in the detection area of the L2Character are identified in <B>_knownPlayers</B>.<BR>
     * In order to inform other players of state modification on the L2Character, server just needs to go through _knownPlayers to send Server->Client Packet and check the distance between the targets.<BR>
     * <FONT COLOR=#FF0000><B> <U>Caution</U> : This method DOESN'T SEND Server->Client packet to this L2Character (to do this use method toSelfAndKnownPlayers)</B></FONT><BR>
     *
     * @param character
     * @param mov
     * @param radius
     */
    public static void toKnownPlayersInRadius(L2Character character, IClientOutgoingPacket mov, int radius) {
        if (radius < 0) {
            radius = 1500;
        }

        L2World.getInstance().forEachVisibleObjectInRange(character, L2PcInstance.class, radius, mov::sendTo);
    }

    /**
     * Send a packet to all L2PcInstance in the _KnownPlayers of the L2Character and to the specified character.<BR>
     * <B><U> Concept</U> :</B><BR>
     * L2PcInstance in the detection area of the L2Character are identified in <B>_knownPlayers</B>.<BR>
     * In order to inform other players of state modification on the L2Character, server just need to go through _knownPlayers to send Server->Client Packet<BR>
     *
     * @param character
     * @param mov
     */
    public static void toSelfAndKnownPlayers(L2Character character, IClientOutgoingPacket mov) {
        if (character.isPlayer()) {
            character.sendPacket(mov);
        }

        toKnownPlayers(character, mov);
    }

    // To improve performance we are comparing values of radius^2 instead of calculating sqrt all the time
    public static void toSelfAndKnownPlayersInRadius(L2Character character, IClientOutgoingPacket mov, int radius) {
        if (radius < 0) {
            radius = 600;
        }

        if (character.isPlayer()) {
            character.sendPacket(mov);
        }

        L2World.getInstance().forEachVisibleObjectInRange(character, L2PcInstance.class, radius, mov::sendTo);
    }

    /**
     * Send a packet to all L2PcInstance present in the world.<BR>
     * <B><U> Concept</U> :</B><BR>
     * In order to inform other players of state modification on the L2Character, server just need to go through _allPlayers to send Server->Client Packet<BR>
     * <FONT COLOR=#FF0000><B> <U>Caution</U> : This method DOESN'T SEND Server->Client packet to this L2Character (to do this use method toSelfAndKnownPlayers)</B></FONT><BR>
     *
     * @param packet
     */
    public static void toAllOnlinePlayers(IClientOutgoingPacket packet) {
        for (L2PcInstance player : L2World.getInstance().getPlayers()) {
            if (player.isOnline()) {
                player.sendPacket(packet);
            }
        }
    }

    public static void toAllOnlinePlayers(String text) {
        toAllOnlinePlayers(text, false);
    }

    public static void toAllOnlinePlayers(String text, boolean isCritical) {
        toAllOnlinePlayers(new CreatureSay(0, isCritical ? ChatType.CRITICAL_ANNOUNCE : ChatType.ANNOUNCEMENT, "", text));
    }

    public static void toAllOnlinePlayersOnScreen(String text) {
        toAllOnlinePlayers(new ExShowScreenMessage(text, 10000));
    }
}
