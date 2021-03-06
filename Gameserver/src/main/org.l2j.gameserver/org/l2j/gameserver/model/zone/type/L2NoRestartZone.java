package org.l2j.gameserver.model.zone.type;

import org.l2j.gameserver.model.TeleportWhereType;
import org.l2j.gameserver.model.actor.L2Character;
import org.l2j.gameserver.model.actor.instance.L2PcInstance;
import org.l2j.gameserver.model.zone.L2ZoneType;
import org.l2j.gameserver.model.zone.ZoneId;

/**
 * A simple no restart zone
 *
 * @author GKR
 */
public class L2NoRestartZone extends L2ZoneType {
    private int _restartAllowedTime = 0;
    private int _restartTime = 0;
    private boolean _enabled = true;

    public L2NoRestartZone(int id) {
        super(id);
    }

    @Override
    public void setParameter(String name, String value) {
        if (name.equalsIgnoreCase("default_enabled")) {
            _enabled = Boolean.parseBoolean(value);
        } else if (name.equalsIgnoreCase("restartAllowedTime")) {
            _restartAllowedTime = Integer.parseInt(value) * 1000;
        } else if (name.equalsIgnoreCase("restartTime")) {
            _restartTime = Integer.parseInt(value) * 1000;
        } else if (name.equalsIgnoreCase("instanceId")) {
            // Do nothing.
        } else {
            super.setParameter(name, value);
        }
    }

    @Override
    protected void onEnter(L2Character character) {
        if (!_enabled) {
            return;
        }

        if (character.isPlayer()) {
            character.setInsideZone(ZoneId.NO_RESTART, true);
        }
    }

    @Override
    protected void onExit(L2Character character) {
        if (!_enabled) {
            return;
        }

        if (character.isPlayer()) {
            character.setInsideZone(ZoneId.NO_RESTART, false);
        }
    }

    @Override
    public void onPlayerLoginInside(L2PcInstance player) {
        if (!_enabled) {
            return;
        }

        if (((System.currentTimeMillis() - player.getLastAccess()) > _restartTime) && ((System.currentTimeMillis() - player.getLastAccess()) > _restartAllowedTime)) {
            player.teleToLocation(TeleportWhereType.TOWN);
        }
    }

    public int getRestartAllowedTime() {
        return _restartAllowedTime;
    }

    public void setRestartAllowedTime(int time) {
        _restartAllowedTime = time;
    }

    public int getRestartTime() {
        return _restartTime;
    }

    public void setRestartTime(int time) {
        _restartTime = time;
    }
}
