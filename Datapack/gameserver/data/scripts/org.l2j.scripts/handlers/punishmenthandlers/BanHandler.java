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
package handlers.punishmenthandlers;

import org.l2j.gameserver.handler.IPunishmentHandler;
import org.l2j.gameserver.model.L2World;
import org.l2j.gameserver.model.actor.instance.L2PcInstance;
import org.l2j.gameserver.model.punishment.PunishmentTask;
import org.l2j.gameserver.model.punishment.PunishmentType;
import org.l2j.gameserver.network.Disconnection;
import org.l2j.gameserver.network.L2GameClient;
import org.l2j.gameserver.network.authcomm.AuthServerCommunication;

/**
 * This class handles ban punishment.
 * @author UnAfraid
 */
public class BanHandler implements IPunishmentHandler
{
	@Override
	public void onStart(PunishmentTask task)
	{
		switch (task.getAffect())
		{
			case CHARACTER:
			{
				final int objectId = Integer.parseInt(String.valueOf(task.getKey()));
				final L2PcInstance player = L2World.getInstance().getPlayer(objectId);
				if (player != null)
				{
					applyToPlayer(player);
				}
				break;
			}
			case ACCOUNT:
			{
				final String account = String.valueOf(task.getKey());
				final L2GameClient client = AuthServerCommunication.getInstance().getAuthedClient(account);
				if (client != null)
				{
					final L2PcInstance player = client.getActiveChar();
					if (player != null)
					{
						applyToPlayer(player);
					}
					else
					{
						Disconnection.of(client).defaultSequence(false);
					}
				}
				break;
			}
			case IP:
			{
				final String ip = String.valueOf(task.getKey());
				for (L2PcInstance player : L2World.getInstance().getPlayers())
				{
					if (player.getIPAddress().equals(ip))
					{
						applyToPlayer(player);
					}
				}
				break;
			}
		}
	}
	
	@Override
	public void onEnd(PunishmentTask task)
	{
		
	}
	
	/**
	 * Applies all punishment effects from the player.
	 * @param player
	 */
	private static void applyToPlayer(L2PcInstance player)
	{
		Disconnection.of(player).defaultSequence(false);
	}
	
	@Override
	public PunishmentType getType()
	{
		return PunishmentType.BAN;
	}
}
