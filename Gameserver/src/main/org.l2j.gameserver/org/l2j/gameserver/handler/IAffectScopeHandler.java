package org.l2j.gameserver.handler;

import org.l2j.gameserver.model.L2Object;
import org.l2j.gameserver.model.actor.L2Character;
import org.l2j.gameserver.model.skills.Skill;
import org.l2j.gameserver.model.skills.targets.AffectScope;

import java.util.function.Consumer;

/**
 * @author Nik
 */
public interface IAffectScopeHandler {
    void forEachAffected(L2Character activeChar, L2Object target, Skill skill, Consumer<? super L2Object> action);

    Enum<AffectScope> getAffectScopeType();
}
