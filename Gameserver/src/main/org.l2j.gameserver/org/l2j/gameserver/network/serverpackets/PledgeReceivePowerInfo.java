package org.l2j.gameserver.network.serverpackets;

import org.l2j.gameserver.model.L2ClanMember;
import org.l2j.gameserver.network.L2GameClient;
import org.l2j.gameserver.network.OutgoingPackets;

import java.nio.ByteBuffer;

/**
 * @author -Wooden-
 */
public class PledgeReceivePowerInfo extends IClientOutgoingPacket {
    private final L2ClanMember _member;

    public PledgeReceivePowerInfo(L2ClanMember member) {
        _member = member;
    }

    @Override
    public void writeImpl(L2GameClient client, ByteBuffer packet) {
        OutgoingPackets.PLEDGE_RECEIVE_POWER_INFO.writeId(packet);

        packet.putInt(_member.getPowerGrade()); // power grade
        writeString(_member.getName(), packet);
        packet.putInt(_member.getClan().getRankPrivs(_member.getPowerGrade()).getBitmask()); // privileges
    }
}
