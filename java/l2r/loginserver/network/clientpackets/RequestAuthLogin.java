/*
 * Copyright (C) 2004-2015 L2J Server
 * 
 * This file is part of L2J Server.
 * 
 * L2J Server is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * L2J Server is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package l2r.loginserver.network.clientpackets;

import l2r.Config;
import l2r.loginserver.GameServerTable.GameServerInfo;
import l2r.loginserver.LoginController;
import l2r.loginserver.LoginController.AuthLoginResult;
import l2r.loginserver.model.data.AccountInfo;
import l2r.loginserver.network.L2LoginClient;
import l2r.loginserver.network.L2LoginClient.LoginClientState;
import l2r.loginserver.network.serverpackets.AccountKicked;
import l2r.loginserver.network.serverpackets.AccountKicked.AccountKickedReason;
import l2r.loginserver.network.serverpackets.LoginFail.LoginFailReason;
import l2r.loginserver.network.serverpackets.LoginOk;
import l2r.loginserver.network.serverpackets.ServerList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.crypto.Cipher;
import java.net.InetAddress;
import java.security.GeneralSecurityException;

/**
 * <pre>
 * Format: x 0 (a leading null) x: the rsa encrypted block with the login an password.
 * 
 * <pre>
 */
public class RequestAuthLogin extends L2LoginClientPacket
{
	private static Logger _log = LoggerFactory.getLogger(RequestAuthLogin.class);
	
	private final byte[] _raw1 = new byte[128];
	private final byte[] _raw2 = new byte[128];
	private boolean _newAuthMethod = false;
	
	private String _user;
	private String _password;
	private int _ncotp;
	
	/**
	 * @return
	 */
	public String getPassword()
	{
		return _password;
	}
	
	/**
	 * @return
	 */
	public String getUser()
	{
		return _user;
	}
	
	public int getOneTimePassword()
	{
		return _ncotp;
	}
	
	@Override
	public boolean readImpl()
	{
		if (super._buf.remaining() >= 256)
		{
			_newAuthMethod = true;
			readB(_raw1, 0, _raw1.length);
			readB(_raw2, 0, _raw2.length);
			return true;
		}
		else if (super._buf.remaining() >= 128)
		{
			readB(_raw1, 0, _raw1.length);
			return true;
		}
		return false;
	}
	
	@Override
	public void run()
	{
		byte[] decrypted = new byte[_newAuthMethod ? 256 : 128];
		
		final L2LoginClient client = getClient();
		try
		{
			final Cipher rsaCipher = Cipher.getInstance("RSA/ECB/nopadding");
			rsaCipher.init(Cipher.DECRYPT_MODE, client.getRSAPrivateKey());
			rsaCipher.doFinal(_raw1, 0, 128, decrypted, 0);
			if (_newAuthMethod)
			{
				rsaCipher.doFinal(_raw2, 0, 128, decrypted, 128);
			}
		}
		catch (GeneralSecurityException e)
		{
			_log.info("", e);
			return;
		}
		
		try
		{
			if (_newAuthMethod)
			{
				_user = new String(decrypted, 0x4E, 50).trim() + new String(decrypted, 0xCE, 14).trim();
				_password = new String(decrypted, 0xDC, 16).trim();
			}
			else
			{
				_user = new String(decrypted, 0x5E, 14).trim();
				_password = new String(decrypted, 0x6C, 16).trim();
			}
			
			_ncotp = decrypted[0x7c];
			_ncotp |= decrypted[0x7d] << 8;
			_ncotp |= decrypted[0x7e] << 16;
			_ncotp |= decrypted[0x7f] << 24;
		}
		catch (Exception e)
		{
			_log.warn("", e);
			return;
		}
		
		if ((Config.AUTO_CREATE_ACCOUNTS) && (!_user.matches(Config.ANAME_TEMPLATE) || !_password.matches(Config.APASSWD_TEMPLATE)))
		{
			// Invalid account template
			client.close(LoginFailReason.REASON_USER_OR_PASS_WRONG);
			return;
		}
		
		InetAddress clientAddr = getClient().getConnection().getInetAddress();
		
		final LoginController lc = LoginController.getInstance();
		AccountInfo info = lc.retriveAccountInfo(clientAddr, _user, _password);
		if (info == null)
		{
			// user or pass wrong
			client.close(LoginFailReason.REASON_USER_OR_PASS_WRONG);
			return;
		}
		
		AuthLoginResult result = lc.tryCheckinAccount(client, clientAddr, info);
		switch (result)
		{
			case AUTH_SUCCESS:
				client.setAccount(info.getLogin());
				client.setState(LoginClientState.AUTHED_LOGIN);
				client.setSessionKey(lc.assignSessionKeyToClient(info.getLogin(), client));
				lc.getCharactersOnAccount(info.getLogin());
				if (Config.SHOW_LICENCE)
				{
					client.sendPacket(new LoginOk(getClient().getSessionKey()));
				}
				else
				{
					getClient().sendPacket(new ServerList(getClient()));
				}
				break;
			case INVALID_PASSWORD:
				client.close(LoginFailReason.REASON_USER_OR_PASS_WRONG);
				break;
			case ACCOUNT_BANNED:
				client.close(new AccountKicked(AccountKickedReason.REASON_PERMANENTLY_BANNED));
				return;
			case ALREADY_ON_LS:
				L2LoginClient oldClient = lc.getAuthedClient(info.getLogin());
				if (oldClient != null)
				{
					// kick the other client
					oldClient.close(LoginFailReason.REASON_ACCOUNT_IN_USE);
					lc.removeAuthedLoginClient(info.getLogin());
				}
				// kick also current client
				client.close(LoginFailReason.REASON_ACCOUNT_IN_USE);
				break;
			case ALREADY_ON_GS:
				GameServerInfo gsi = lc.getAccountOnGameServer(info.getLogin());
				if (gsi != null)
				{
					client.close(LoginFailReason.REASON_ACCOUNT_IN_USE);
					
					// kick from there
					if (gsi.isAuthed())
					{
						gsi.getGameServerThread().kickPlayer(info.getLogin());
					}
				}
				break;
		}
	}
}
