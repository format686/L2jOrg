package org.l2j.gameserver.network.serverpackets;

import org.l2j.gameserver.network.L2GameClient;
import org.l2j.gameserver.network.OutgoingPackets;

import java.nio.ByteBuffer;

/**
 * Send Private (Friend) Message
 *
 * @author Tempy
 */
public class L2FriendSay extends IClientOutgoingPacket {
    private final String _sender;
    private final String _receiver;
    private final String _message;

    public L2FriendSay(String sender, String reciever, String message) {
        _sender = sender;
        _receiver = reciever;
        _message = message;
    }

    @Override
    public void writeImpl(L2GameClient client, ByteBuffer packet) {
        OutgoingPackets.L2_FRIEND_SAY.writeId(packet);

        packet.putInt(0); // ??
        writeString(_receiver, packet);
        writeString(_sender, packet);
        writeString(_message, packet);
    }
}
