package org.l2j.gameserver.network.clientpackets;

import org.l2j.gameserver.data.sql.impl.ClanTable;
import org.l2j.gameserver.model.L2Clan;
import org.l2j.gameserver.model.L2World;
import org.l2j.gameserver.model.actor.instance.L2PcInstance;
import org.l2j.gameserver.network.serverpackets.*;

import java.nio.ByteBuffer;

/**
 * This class ...
 *
 * @version $Revision: 1.1.2.2.2.2 $ $Date: 2005/03/27 15:29:30 $
 */
public final class RequestGMCommand extends IClientIncomingPacket {
    private String _targetName;
    private int _command;

    @Override
    public void readImpl(ByteBuffer packet) {
        _targetName = readString(packet);
        _command = packet.getInt();
    }

    @Override
    public void runImpl() {
        // prevent non gm or low level GMs from vieweing player stuff
        if (!client.getActiveChar().isGM() || !client.getActiveChar().getAccessLevel().allowAltG()) {
            return;
        }

        final L2PcInstance player = L2World.getInstance().getPlayer(_targetName);

        final L2Clan clan = ClanTable.getInstance().getClanByName(_targetName);

        // player name was incorrect?
        if ((player == null) && ((clan == null) || (_command != 6))) {
            return;
        }

        switch (_command) {
            case 1: // player status
            {
                client.sendPacket(new GMViewCharacterInfo(player));
                client.sendPacket(new GMHennaInfo(player));
                break;
            }
            case 2: // player clan
            {
                if ((player != null) && (player.getClan() != null)) {
                    client.sendPacket(new GMViewPledgeInfo(player.getClan(), player));
                }
                break;
            }
            case 3: // player skills
            {
                client.sendPacket(new GMViewSkillInfo(player));
                break;
            }
            case 4: // player quests
            {
                client.sendPacket(new GmViewQuestInfo(player));
                break;
            }
            case 5: // player inventory
            {
                client.sendPacket(new GMViewItemList(1, player));
                client.sendPacket(new GMViewItemList(2, player));
                client.sendPacket(new GMHennaInfo(player));
                break;
            }
            case 6: // player warehouse
            {
                // gm warehouse view to be implemented
                if (player != null) {
                    client.sendPacket(new GMViewWarehouseWithdrawList(player));
                    // clan warehouse
                } else {
                    client.sendPacket(new GMViewWarehouseWithdrawList(clan));
                }
                break;
            }
        }
    }
}
