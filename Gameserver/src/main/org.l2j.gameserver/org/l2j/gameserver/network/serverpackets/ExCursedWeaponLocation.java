package org.l2j.gameserver.network.serverpackets;

import org.l2j.gameserver.model.Location;
import org.l2j.gameserver.network.L2GameClient;
import org.l2j.gameserver.network.OutgoingPackets;

import java.nio.ByteBuffer;
import java.util.List;

/**
 * Format: (ch) d[ddddd]
 *
 * @author -Wooden-
 */
public class ExCursedWeaponLocation extends IClientOutgoingPacket {
    private final List<CursedWeaponInfo> _cursedWeaponInfo;

    public ExCursedWeaponLocation(List<CursedWeaponInfo> cursedWeaponInfo) {
        _cursedWeaponInfo = cursedWeaponInfo;
    }

    @Override
    public void writeImpl(L2GameClient client, ByteBuffer packet) {
        OutgoingPackets.EX_CURSED_WEAPON_LOCATION.writeId(packet);

        if (!_cursedWeaponInfo.isEmpty()) {
            packet.putInt(_cursedWeaponInfo.size());
            for (CursedWeaponInfo w : _cursedWeaponInfo) {
                packet.putInt(w.id);
                packet.putInt(w.activated);

                packet.putInt(w.pos.getX());
                packet.putInt(w.pos.getY());
                packet.putInt(w.pos.getZ());
            }
        } else {
            packet.putInt(0);
        }
    }

    public static class CursedWeaponInfo {
        public Location pos;
        public int id;
        public int activated; // 0 - not activated ? 1 - activated

        public CursedWeaponInfo(Location p, int ID, int status) {
            pos = p;
            id = ID;
            activated = status;
        }

    }
}
