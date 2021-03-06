package org.l2j.gameserver.network.clientpackets;

import org.l2j.commons.util.CommonUtil;
import org.l2j.gameserver.instancemanager.ClanEntryManager;
import org.l2j.gameserver.model.actor.instance.L2PcInstance;
import org.l2j.gameserver.network.serverpackets.ExPledgeDraftListSearch;

import java.nio.ByteBuffer;

/**
 * @author Sdw
 */
public class RequestPledgeDraftListSearch extends IClientIncomingPacket {
    private int _levelMin;
    private int _levelMax;
    private int _classId;
    private String _query;
    private int _sortBy;
    private boolean _descending;

    @Override
    public void readImpl(ByteBuffer packet) {
        _levelMin = CommonUtil.constrain(packet.getInt(), 0, 107);
        _levelMax = CommonUtil.constrain(packet.getInt(), 0, 107);
        _classId = packet.getInt();
        _query = readString(packet);
        _sortBy = packet.getInt();
        _descending = packet.getInt() == 2;
    }

    @Override
    public void runImpl() {
        final L2PcInstance activeChar = client.getActiveChar();

        if (activeChar == null) {
            return;
        }

        if (_query.isEmpty()) {
            client.sendPacket(new ExPledgeDraftListSearch(ClanEntryManager.getInstance().getSortedWaitingList(_levelMin, _levelMax, _classId, _sortBy, _descending)));
        } else {
            client.sendPacket(new ExPledgeDraftListSearch(ClanEntryManager.getInstance().queryWaitingListByName(_query.toLowerCase())));
        }
    }
}
