package org.l2j.gameserver.network.clientpackets;

import org.l2j.gameserver.instancemanager.FortManager;
import org.l2j.gameserver.model.entity.Fort;
import org.l2j.gameserver.network.serverpackets.ActionFailed;
import org.l2j.gameserver.network.serverpackets.ExShowFortressMapInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.ByteBuffer;

/**
 * @author KenM
 */
public class RequestFortressMapInfo extends IClientIncomingPacket {
    private static final Logger LOGGER = LoggerFactory.getLogger(RequestFortressMapInfo.class);
    private int _fortressId;

    @Override
    public void readImpl(ByteBuffer packet) {
        _fortressId = packet.getInt();
    }

    @Override
    public void runImpl() {
        final Fort fort = FortManager.getInstance().getFortById(_fortressId);
        if (fort == null) {
            LOGGER.warn("Fort is not found with id (" + _fortressId + ") in all forts with size of (" + FortManager.getInstance().getForts().size() + ") called by player (" + client.getActiveChar() + ")");

            if (client.getActiveChar() == null) {
                return;
            }

            client.sendPacket(ActionFailed.STATIC_PACKET);
            return;
        }
        client.sendPacket(new ExShowFortressMapInfo(fort));
    }
}
