package org.l2j.gameserver.network.authcomm.as2gs;

import org.l2j.commons.database.DatabaseFactory;
import org.l2j.gameserver.network.authcomm.AuthServerCommunication;
import org.l2j.gameserver.network.authcomm.ReceivablePacket;
import org.l2j.gameserver.network.authcomm.gs2as.SetAccountInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.ByteBuffer;

/**
 * @author VISTALL
 * 21:05/25.03.2011
 */
public class GetAccountInfo extends ReceivablePacket
{
	private static final Logger _log = LoggerFactory.getLogger(GetAccountInfo.class);
	private String _account;

	@Override
	protected void readImpl(ByteBuffer buffer)
	{
		_account = readString(buffer);
	}

	@Override
	protected void runImpl() {
		int playerSize = 0;
		try(var con = DatabaseFactory.getInstance().getConnection();
			var statement = con.prepareStatement("SELECT COUNT(1) FROM characters WHERE account_name=?")) {
			statement.setString(1, _account);
			var rset = statement.executeQuery();
			if(rset.next()) {
				playerSize = rset.getInt(1);
			}
			AuthServerCommunication.getInstance().sendPacket(new SetAccountInfo(_account, playerSize));


		} catch(Exception e) {
			_log.error("GetAccountInfo:runImpl():" + e, e);
		}

	}
}
