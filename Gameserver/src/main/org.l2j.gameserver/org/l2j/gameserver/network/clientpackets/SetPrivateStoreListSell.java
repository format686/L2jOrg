package org.l2j.gameserver.network.clientpackets;

import org.l2j.gameserver.Config;
import org.l2j.gameserver.enums.PrivateStoreType;
import org.l2j.gameserver.model.TradeList;
import org.l2j.gameserver.model.actor.instance.L2PcInstance;
import org.l2j.gameserver.model.itemcontainer.Inventory;
import org.l2j.gameserver.model.zone.ZoneId;
import org.l2j.gameserver.network.InvalidDataPacketException;
import org.l2j.gameserver.network.SystemMessageId;
import org.l2j.gameserver.network.serverpackets.ActionFailed;
import org.l2j.gameserver.network.serverpackets.ExPrivateStoreSetWholeMsg;
import org.l2j.gameserver.network.serverpackets.PrivateStoreManageListSell;
import org.l2j.gameserver.network.serverpackets.PrivateStoreMsgSell;
import org.l2j.gameserver.taskmanager.AttackStanceTaskManager;
import org.l2j.gameserver.util.Util;

import java.nio.ByteBuffer;

/**
 * This class ...
 *
 * @version $Revision: 1.2.2.1.2.5 $ $Date: 2005/03/27 15:29:30 $
 */
public class SetPrivateStoreListSell extends IClientIncomingPacket {
    private static final int BATCH_LENGTH = 20; // length of the one item

    private boolean _packageSale;
    private Item[] _items = null;

    @Override
    public void readImpl(ByteBuffer packet) throws InvalidDataPacketException {
        _packageSale = (packet.getInt() == 1);
        final int count = packet.getInt();
        if ((count < 1) || (count > Config.MAX_ITEM_IN_PACKET) || ((count * BATCH_LENGTH) != packet.remaining())) {
            throw new InvalidDataPacketException();
        }

        _items = new Item[count];
        for (int i = 0; i < count; i++) {
            final int itemId = packet.getInt();
            final long cnt = packet.getLong();
            final long price = packet.getLong();

            if ((itemId < 1) || (cnt < 1) || (price < 0)) {
                _items = null;
                throw new InvalidDataPacketException();
            }
            _items[i] = new Item(itemId, cnt, price);
        }
    }

    @Override
    public void runImpl() {
        final L2PcInstance player = client.getActiveChar();
        if (player == null) {
            return;
        }

        if (_items == null) {
            player.sendPacket(SystemMessageId.INCORRECT_ITEM_COUNT);
            player.setPrivateStoreType(PrivateStoreType.NONE);
            player.broadcastUserInfo();
            return;
        }

        if (!player.getAccessLevel().allowTransaction()) {
            player.sendPacket(SystemMessageId.YOU_ARE_NOT_AUTHORIZED_TO_DO_THAT);
            return;
        }

        if (AttackStanceTaskManager.getInstance().hasAttackStanceTask(player) || player.isInDuel()) {
            player.sendPacket(SystemMessageId.WHILE_YOU_ARE_ENGAGED_IN_COMBAT_YOU_CANNOT_OPERATE_A_PRIVATE_STORE_OR_PRIVATE_WORKSHOP);
            player.sendPacket(new PrivateStoreManageListSell(1, player, _packageSale));
            player.sendPacket(new PrivateStoreManageListSell(2, player, _packageSale));
            player.sendPacket(ActionFailed.STATIC_PACKET);
            return;
        }

        if (player.isInsideZone(ZoneId.NO_STORE)) {
            player.sendPacket(new PrivateStoreManageListSell(1, player, _packageSale));
            player.sendPacket(new PrivateStoreManageListSell(2, player, _packageSale));
            player.sendPacket(SystemMessageId.YOU_CANNOT_OPEN_A_PRIVATE_STORE_HERE);
            player.sendPacket(ActionFailed.STATIC_PACKET);
            return;
        }

        // Check maximum number of allowed slots for pvt shops
        if (_items.length > player.getPrivateSellStoreLimit()) {
            player.sendPacket(new PrivateStoreManageListSell(1, player, _packageSale));
            player.sendPacket(new PrivateStoreManageListSell(2, player, _packageSale));
            player.sendPacket(SystemMessageId.YOU_HAVE_EXCEEDED_THE_QUANTITY_THAT_CAN_BE_INPUTTED);
            return;
        }

        final TradeList tradeList = player.getSellList();
        tradeList.clear();
        tradeList.setPackaged(_packageSale);

        long totalCost = player.getAdena();
        for (Item i : _items) {
            if (!i.addToTradeList(tradeList)) {
                Util.handleIllegalPlayerAction(player, "Warning!! Character " + player.getName() + " of account " + player.getAccountName() + " tried to set price more than " + Inventory.MAX_ADENA + " adena in Private Store - Sell.", Config.DEFAULT_PUNISH);
                return;
            }

            totalCost += i.getPrice();
            if (totalCost > Inventory.MAX_ADENA) {
                Util.handleIllegalPlayerAction(player, "Warning!! Character " + player.getName() + " of account " + player.getAccountName() + " tried to set total price more than " + Inventory.MAX_ADENA + " adena in Private Store - Sell.", Config.DEFAULT_PUNISH);
                return;
            }
        }

        player.sitDown();
        if (_packageSale) {
            player.setPrivateStoreType(PrivateStoreType.PACKAGE_SELL);
        } else {
            player.setPrivateStoreType(PrivateStoreType.SELL);
        }

        player.broadcastUserInfo();

        if (_packageSale) {
            player.broadcastPacket(new ExPrivateStoreSetWholeMsg(player));
        } else {
            player.broadcastPacket(new PrivateStoreMsgSell(player));
        }
    }

    private static class Item {
        private final int _objectId;
        private final long _count;
        private final long _price;

        public Item(int objectId, long count, long price) {
            _objectId = objectId;
            _count = count;
            _price = price;
        }

        public boolean addToTradeList(TradeList list) {
            if ((Inventory.MAX_ADENA / _count) < _price) {
                return false;
            }

            list.addItem(_objectId, _count, _price);
            return true;
        }

        public long getPrice() {
            return _count * _price;
        }
    }
}
