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
package handlers.skillconditionhandlers;

import java.util.List;

import org.l2j.gameserver.enums.SkillConditionAffectType;
import org.l2j.gameserver.model.L2Object;
import org.l2j.gameserver.model.StatsSet;
import org.l2j.gameserver.model.actor.L2Character;
import org.l2j.gameserver.model.base.ClassId;
import org.l2j.gameserver.model.skills.ISkillCondition;
import org.l2j.gameserver.model.skills.Skill;

/**
 * @author UnAfraid
 */
public class OpCheckClassListSkillCondition implements ISkillCondition
{
	private final List<ClassId> _classIds;
	private final SkillConditionAffectType _affectType;
	private final boolean _isWithin;
	
	public OpCheckClassListSkillCondition(StatsSet params)
	{
		_classIds = params.getEnumList("classIds", ClassId.class);
		_affectType = params.getEnum("affectType", SkillConditionAffectType.class);
		_isWithin = params.getBoolean("isWithin");
	}
	
	@Override
	public boolean canUse(L2Character caster, Skill skill, L2Object target)
	{
		switch (_affectType)
		{
			case CASTER:
			{
				return caster.isPlayer() && (_isWithin == _classIds.stream().anyMatch(classId -> classId == caster.getActingPlayer().getClassId()));
			}
			case TARGET:
			{
				if ((target != null) && target.isPlayer())
				{
					return _isWithin == _classIds.stream().anyMatch(classId -> classId.getId() == target.getActingPlayer().getClassId().getId());
				}
				break;
			}
		}
		return false;
	}
}
