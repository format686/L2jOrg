package org.l2j.gameserver.network.serverpackets;

import org.l2j.gameserver.model.actor.instance.L2PcInstance;
import org.l2j.gameserver.network.L2GameClient;
import org.l2j.gameserver.network.OutgoingPackets;

import java.nio.ByteBuffer;
import java.util.List;

/**
 * @author mrTJO
 */
public class ExCubeGameTeamList extends IClientOutgoingPacket {
    // Players Lists
    private final List<L2PcInstance> _bluePlayers;
    private final List<L2PcInstance> _redPlayers;

    // Common Values
    private final int _roomNumber;

    /**
     * Show Minigame Waiting List to Player
     *
     * @param redPlayers  Red Players List
     * @param bluePlayers Blue Players List
     * @param roomNumber  Arena/Room ID
     */
    public ExCubeGameTeamList(List<L2PcInstance> redPlayers, List<L2PcInstance> bluePlayers, int roomNumber) {
        _redPlayers = redPlayers;
        _bluePlayers = bluePlayers;
        _roomNumber = roomNumber - 1;
    }

    @Override
    public void writeImpl(L2GameClient client, ByteBuffer packet) {
        OutgoingPackets.EX_BLOCK_UP_SET_LIST.writeId(packet);

        packet.putInt(0x00);

        packet.putInt(_roomNumber);
        packet.putInt(0xffffffff);

        packet.putInt(_bluePlayers.size());
        for (L2PcInstance player : _bluePlayers) {
            packet.putInt(player.getObjectId());
            writeString(player.getName(), packet);
        }
        packet.putInt(_redPlayers.size());
        for (L2PcInstance player : _redPlayers) {
            packet.putInt(player.getObjectId());
            writeString(player.getName(), packet);
        }
    }
}
