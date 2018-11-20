package org.l2j.gameserver.network.l2.s2c;

import org.l2j.gameserver.model.Player;
import org.l2j.gameserver.templates.item.RecipeTemplate;

/**
 * format ddddd
 */
public class RecipeItemMakeInfoPacket extends L2GameServerPacket
{
	private int _id;
	private boolean _isCommon;
	private int _status;
	private int _curMP;
	private int _maxMP;

	public RecipeItemMakeInfoPacket(Player player, RecipeTemplate recipe, int status)
	{
		_id = recipe.getId();
		_isCommon = recipe.isCommon();
		_status = status;
		_curMP = (int) player.getCurrentMp();
		_maxMP = player.getMaxMp();
	}

	@Override
	protected final void writeImpl()
	{
		writeD(_id); //ID рецепта
		writeD(_isCommon ? 0x01 : 0x00);
		writeD(_curMP);
		writeD(_maxMP);
		writeD(_status); //итог крафта; 0xFFFFFFFF нет статуса, 0 удача, 1 провал
	}
}