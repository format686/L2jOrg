package org.l2j.authserver.network.client;

import io.github.joealisson.mmocore.PacketHandler;
import io.github.joealisson.mmocore.ReadablePacket;
import org.l2j.authserver.network.client.packet.client2auth.AuthGameGuard;
import org.l2j.authserver.network.client.packet.client2auth.RequestAuthLogin;
import org.l2j.authserver.network.client.packet.client2auth.RequestServerList;
import org.l2j.authserver.network.client.packet.client2auth.RequestServerLogin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.ByteBuffer;

import static java.lang.Byte.toUnsignedInt;

/**
 * Handler for packets received by Login Server
 *
 * @author KenM
 */
public final class AuthPacketHandler implements PacketHandler<AuthClient> {

    private static final Logger logger = LoggerFactory.getLogger(AuthPacketHandler.class);

    @Override
    public ReadablePacket<AuthClient> handlePacket(ByteBuffer buffer, AuthClient client) {
        var opcode = toUnsignedInt(buffer.get());

        ReadablePacket<AuthClient> packet = null;
        var state = client.getState();

        switch (state) {
            case CONNECTED:
                if (opcode == 0x07) {
                    packet = new AuthGameGuard();
                } else {
                    debugOpcode(opcode, buffer, state);
                }
                break;
            case AUTHED_GG:
                if (opcode == 0x00) {
                    packet = new RequestAuthLogin();
                } else {
                    debugOpcode(opcode, buffer, state);
                }
                break;
            case AUTHED_LOGIN:
                if (opcode == 0x05) {
                    packet = new RequestServerList();
                } else if (opcode == 0x02) {
                    packet = new RequestServerLogin();
                } else {
                    debugOpcode(opcode, buffer, state);
                }
                break;
        }
        return packet;
    }

    private void debugOpcode(int opcode, ByteBuffer data, AuthClientState state) {
        logger.warn("Unknown Opcode: {} for state {}\n {}", Integer.toHexString(opcode), state, data.array());
    }
}
