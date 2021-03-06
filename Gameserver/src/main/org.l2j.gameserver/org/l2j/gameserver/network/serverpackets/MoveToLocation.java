package org.l2j.gameserver.network.serverpackets;

import org.l2j.gameserver.model.actor.L2Character;
import org.l2j.gameserver.network.L2GameClient;
import org.l2j.gameserver.network.OutgoingPackets;

import java.nio.ByteBuffer;

public final class MoveToLocation extends IClientOutgoingPacket {
    private final int _charObjId;
    private final int _x;
    private final int _y;
    private final int _z;
    private final int _xDst;
    private final int _yDst;
    private final int _zDst;

    public MoveToLocation(L2Character cha) {
        _charObjId = cha.getObjectId();
        _x = cha.getX();
        _y = cha.getY();
        _z = cha.getZ();
        _xDst = cha.getXdestination();
        _yDst = cha.getYdestination();
        _zDst = cha.getZdestination();
    }

    @Override
    public void writeImpl(L2GameClient client, ByteBuffer packet) {
        OutgoingPackets.MOVE_TO_LOCATION.writeId(packet);

        packet.putInt(_charObjId);

        packet.putInt(_xDst);
        packet.putInt(_yDst);
        packet.putInt(_zDst);

        packet.putInt(_x);
        packet.putInt(_y);
        packet.putInt(_z);
    }
}
