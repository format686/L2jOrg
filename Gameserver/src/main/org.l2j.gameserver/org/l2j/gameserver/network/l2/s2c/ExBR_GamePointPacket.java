package org.l2j.gameserver.network.l2.s2c;

import org.l2j.gameserver.model.Player;

public class ExBR_GamePointPacket extends L2GameServerPacket
{
	private int _objectId;
	private long _points;

	public ExBR_GamePointPacket(Player player)
	{
		_objectId = player.getObjectId();
		_points = player.getPremiumPoints();
	}

	@Override
	protected void writeImpl()
	{
		writeD(_objectId);
		writeQ(_points);
		writeD(0x00); //??
	}
}