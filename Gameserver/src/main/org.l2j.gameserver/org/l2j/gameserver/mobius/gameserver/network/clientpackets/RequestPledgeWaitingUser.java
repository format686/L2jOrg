package org.l2j.gameserver.mobius.gameserver.network.clientpackets;

import org.l2j.gameserver.mobius.gameserver.instancemanager.ClanEntryManager;
import org.l2j.gameserver.mobius.gameserver.model.actor.instance.L2PcInstance;
import org.l2j.gameserver.mobius.gameserver.model.clan.entry.PledgeApplicantInfo;
import org.l2j.gameserver.mobius.gameserver.network.serverpackets.ExPledgeWaitingList;
import org.l2j.gameserver.mobius.gameserver.network.serverpackets.ExPledgeWaitingUser;

import java.nio.ByteBuffer;

/**
 * @author Sdw
 */
public class RequestPledgeWaitingUser extends IClientIncomingPacket
{
    private int _clanId;
    private int _playerId;

    @Override
    public void readImpl(ByteBuffer packet)
    {
        _clanId = packet.getInt();
        _playerId = packet.getInt();
    }

    @Override
    public void runImpl()
    {
        final L2PcInstance activeChar = client.getActiveChar();
        if ((activeChar == null) || (activeChar.getClanId() != _clanId))
        {
            return;
        }

        final PledgeApplicantInfo infos = ClanEntryManager.getInstance().getPlayerApplication(_clanId, _playerId);
        if (infos == null)
        {
            client.sendPacket(new ExPledgeWaitingList(_clanId));
        }
        else
        {
            client.sendPacket(new ExPledgeWaitingUser(infos));
        }
    }
}