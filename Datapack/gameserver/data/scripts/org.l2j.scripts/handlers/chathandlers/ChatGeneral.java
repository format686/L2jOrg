/*
 * This file is part of the L2J Mobius project.
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package handlers.chathandlers;

import org.l2j.gameserver.Config;
import org.l2j.gameserver.enums.ChatType;
import org.l2j.gameserver.handler.IChatHandler;
import org.l2j.gameserver.handler.IVoicedCommandHandler;
import org.l2j.gameserver.handler.VoicedCommandHandler;
import org.l2j.gameserver.model.BlockList;
import org.l2j.gameserver.model.L2World;
import org.l2j.gameserver.model.PcCondOverride;
import org.l2j.gameserver.model.actor.instance.L2PcInstance;
import org.l2j.gameserver.network.SystemMessageId;
import org.l2j.gameserver.network.serverpackets.CreatureSay;
import org.l2j.gameserver.network.serverpackets.SystemMessage;

import java.util.StringTokenizer;

/**
 * General Chat Handler.
 * @author durgus
 */
public final class ChatGeneral implements IChatHandler
{
	private static final ChatType[] CHAT_TYPES =
	{
		ChatType.GENERAL,
	};
	
	@Override
	public void handleChat(ChatType type, L2PcInstance activeChar, String params, String text)
	{
		boolean vcd_used = false;
		if (text.startsWith("."))
		{
			final StringTokenizer st = new StringTokenizer(text);
			final IVoicedCommandHandler vch;
			String command = "";
			
			if (st.countTokens() > 1)
			{
				command = st.nextToken().substring(1);
				params = text.substring(command.length() + 2);
			}
			else
			{
				command = text.substring(1);
			}
			vch = VoicedCommandHandler.getInstance().getHandler(command);
			if (vch != null)
			{
				vch.useVoicedCommand(command, activeChar, params);
				vcd_used = true;
			}
			else
			{
				vcd_used = false;
			}
		}
		
		if (!vcd_used)
		{
			if (activeChar.isChatBanned() && Config.BAN_CHAT_CHANNELS.contains(type))
			{
				activeChar.sendPacket(SystemMessageId.CHATTING_IS_CURRENTLY_PROHIBITED_IF_YOU_TRY_TO_CHAT_BEFORE_THE_PROHIBITION_IS_REMOVED_THE_PROHIBITION_TIME_WILL_INCREASE_EVEN_FURTHER);
				return;
			}
			
			if ((activeChar.getLevel() < Config.MINIMUM_CHAT_LEVEL) && !activeChar.canOverrideCond(PcCondOverride.CHAT_CONDITIONS))
			{
				activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.GENERAL_CHAT_CANNOT_BE_USED_BY_NON_PREMIUM_USERS_LV_S1_OR_LOWER).addInt(Config.MINIMUM_CHAT_LEVEL));
				return;
			}
			
			final CreatureSay cs = new CreatureSay(activeChar.getObjectId(), type, activeChar.getAppearance().getVisibleName(), text);
			final CreatureSay csRandom = new CreatureSay(activeChar.getObjectId(), type, activeChar.getAppearance().getVisibleName(), ChatRandomizer.randomize(text));
			L2World.getInstance().forEachVisibleObjectInRange(activeChar, L2PcInstance.class, 1250, player ->
			{
				if ((player != null) && !BlockList.isBlocked(player, activeChar))
				{
					player.sendPacket(cs);
				}
			});
			
			activeChar.sendPacket(cs);
		}
	}
	
	@Override
	public ChatType[] getChatTypeList()
	{
		return CHAT_TYPES;
	}
}