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
package org.l2j.gameserver.mobius.gameserver.model.instancezone.conditions;

import org.l2j.gameserver.mobius.gameserver.instancemanager.QuestManager;
import org.l2j.gameserver.mobius.gameserver.model.StatsSet;
import org.l2j.gameserver.mobius.gameserver.model.actor.L2Npc;
import org.l2j.gameserver.mobius.gameserver.model.actor.instance.L2PcInstance;
import org.l2j.gameserver.mobius.gameserver.model.instancezone.InstanceTemplate;
import org.l2j.gameserver.mobius.gameserver.model.quest.Quest;
import org.l2j.gameserver.mobius.gameserver.model.quest.QuestState;
import org.l2j.gameserver.mobius.gameserver.network.SystemMessageId;

/**
 * Instance quest condition
 * @author malyelfik
 */
public final class ConditionQuest extends Condition
{
	public ConditionQuest(InstanceTemplate template, StatsSet parameters, boolean onlyLeader, boolean showMessageAndHtml)
	{
		super(template, parameters, onlyLeader, showMessageAndHtml);
		// Set message
		setSystemMessage(SystemMessageId.C1_S_QUEST_REQUIREMENT_IS_NOT_SUFFICIENT_AND_CANNOT_BE_ENTERED, (message, player) -> message.addString(player.getName()));
	}
	
	@Override
	protected boolean test(L2PcInstance player, L2Npc npc)
	{
		final int id = getParameters().getInt("id");
		final Quest q = QuestManager.getInstance().getQuest(id);
		if (q == null)
		{
			return false;
		}
		
		final QuestState qs = player.getQuestState(q.getName());
		if (qs == null)
		{
			return false;
		}
		
		final int cond = getParameters().getInt("cond", -1);
		return (cond != -1) ? qs.isCond(cond) : true;
	}
}