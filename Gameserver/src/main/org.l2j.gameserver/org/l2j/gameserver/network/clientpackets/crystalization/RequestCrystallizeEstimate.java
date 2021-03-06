package org.l2j.gameserver.network.clientpackets.crystalization;

import org.l2j.gameserver.Config;
import org.l2j.gameserver.data.xml.impl.ItemCrystallizationData;
import org.l2j.gameserver.enums.PrivateStoreType;
import org.l2j.gameserver.model.actor.instance.L2PcInstance;
import org.l2j.gameserver.model.holders.ItemChanceHolder;
import org.l2j.gameserver.model.items.instance.L2ItemInstance;
import org.l2j.gameserver.model.items.type.CrystalType;
import org.l2j.gameserver.model.skills.CommonSkill;
import org.l2j.gameserver.network.SystemMessageId;
import org.l2j.gameserver.network.clientpackets.IClientIncomingPacket;
import org.l2j.gameserver.network.serverpackets.ActionFailed;
import org.l2j.gameserver.network.serverpackets.crystalization.ExGetCrystalizingEstimation;
import org.l2j.gameserver.util.Util;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.ByteBuffer;
import java.util.List;

/**
 * @author UnAfraid
 */
public class RequestCrystallizeEstimate extends IClientIncomingPacket {

    private static final Logger LOGGER = LoggerFactory.getLogger(RequestCrystallizeEstimate.class);

    private int _objectId;
    private long _count;

    @Override
    public void readImpl(ByteBuffer packet) {
        _objectId = packet.getInt();
        _count = packet.getLong();
    }

    @Override
    public void runImpl() {
        final L2PcInstance activeChar = client.getActiveChar();
        if ((activeChar == null) || activeChar.isInCrystallize()) {
            return;
        }

        // if (!client.getFloodProtectors().getTransaction().tryPerformAction("crystallize"))
        // {
        // activeChar.sendMessage("You are crystallizing too fast.");
        // return;
        // }

        if (_count <= 0) {
            Util.handleIllegalPlayerAction(activeChar, "[RequestCrystallizeItem] count <= 0! ban! oid: " + _objectId + " owner: " + activeChar.getName(), Config.DEFAULT_PUNISH);
            return;
        }

        if ((activeChar.getPrivateStoreType() != PrivateStoreType.NONE) || activeChar.isInCrystallize()) {
            client.sendPacket(SystemMessageId.WHILE_OPERATING_A_PRIVATE_STORE_OR_WORKSHOP_YOU_CANNOT_DISCARD_DESTROY_OR_TRADE_AN_ITEM);
            return;
        }

        final int skillLevel = activeChar.getSkillLevel(CommonSkill.CRYSTALLIZE.getId());
        if (skillLevel <= 0) {
            client.sendPacket(SystemMessageId.YOU_MAY_NOT_CRYSTALLIZE_THIS_ITEM_YOUR_CRYSTALLIZATION_SKILL_LEVEL_IS_TOO_LOW);
            client.sendPacket(ActionFailed.STATIC_PACKET);
            return;
        }

        final L2ItemInstance item = activeChar.getInventory().getItemByObjectId(_objectId);
        if ((item == null) || item.isShadowItem() || item.isTimeLimitedItem()) {
            client.sendPacket(ActionFailed.STATIC_PACKET);
            return;
        }

        if (item.isHeroItem()) {
            client.sendPacket(ActionFailed.STATIC_PACKET);
            return;
        }

        if (!item.getItem().isCrystallizable() || (item.getItem().getCrystalCount() <= 0) || (item.getItem().getCrystalType() == CrystalType.NONE)) {
            client.sendPacket(ActionFailed.STATIC_PACKET);
            LOGGER.warn("{} tried to crystallize {}", activeChar, item.getItem());
            return;
        }

        if (_count > item.getCount()) {
            _count = activeChar.getInventory().getItemByObjectId(_objectId).getCount();
        }

        if (!activeChar.getInventory().canManipulateWithItemId(item.getId())) {
            activeChar.sendMessage("You cannot use this item.");
            return;
        }

        // Check if the char can crystallize items and return if false;
        boolean canCrystallize = true;

        switch (item.getItem().getCrystalType()) {
            case D: {
                if (skillLevel < 1) {
                    canCrystallize = false;
                }
                break;
            }
            case C: {
                if (skillLevel < 2) {
                    canCrystallize = false;
                }
                break;
            }
            case B: {
                if (skillLevel < 3) {
                    canCrystallize = false;
                }
                break;
            }
            case A: {
                if (skillLevel < 4) {
                    canCrystallize = false;
                }
                break;
            }
        }

        if (!canCrystallize) {
            client.sendPacket(SystemMessageId.YOU_MAY_NOT_CRYSTALLIZE_THIS_ITEM_YOUR_CRYSTALLIZATION_SKILL_LEVEL_IS_TOO_LOW);
            client.sendPacket(ActionFailed.STATIC_PACKET);
            return;
        }

        // Show crystallization rewards window.
        final List<ItemChanceHolder> crystallizationRewards = ItemCrystallizationData.getInstance().getCrystallizationRewards(item);
        if ((crystallizationRewards != null) && !crystallizationRewards.isEmpty()) {
            activeChar.setInCrystallize(true);
            client.sendPacket(new ExGetCrystalizingEstimation(crystallizationRewards));
        } else {
            client.sendPacket(SystemMessageId.CRYSTALLIZATION_CANNOT_BE_PROCEEDED_BECAUSE_THERE_ARE_NO_ITEMS_REGISTERED);
        }

    }
}
