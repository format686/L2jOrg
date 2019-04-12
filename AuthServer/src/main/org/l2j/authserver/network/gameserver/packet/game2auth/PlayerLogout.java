package org.l2j.authserver.network.gameserver.packet.game2auth;

import org.l2j.authserver.controller.AuthController;

import java.nio.ByteBuffer;

public class PlayerLogout extends GameserverReadablePacket {
	
	private String account;

	public String getAccount()
	{
		return account;
	}

	@Override
	protected void readImpl(ByteBuffer buffer) {
		account = readString(buffer);
	}

	@Override
	protected void runImpl()  {
		client.getGameServerInfo().removeAccount(account);
		AuthController.getInstance().removeAuthedClient(account);
	}
}