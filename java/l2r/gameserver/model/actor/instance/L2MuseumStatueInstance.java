/*
 * Copyright (C) 2004-2014 L2J Server
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
package l2r.gameserver.model.actor.instance;

import custom.museum.MuseumCategory;
import custom.museum.MuseumManager;
import l2r.L2DatabaseFactory;
import l2r.gameserver.model.CharSelectInfoPackage;
import l2r.gameserver.model.actor.templates.L2NpcTemplate;
import l2r.gameserver.network.serverpackets.ActionFailed;
import l2r.gameserver.network.serverpackets.CharSelectionInfo;
import l2r.gameserver.network.serverpackets.ShowBoard;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

/**
 * @author Erlandys
 */
public final class L2MuseumStatueInstance extends L2NpcInstance
{
	private int _type;
	private int _playerObjectId;
	private CharSelectInfoPackage _charLooks;
	private MuseumCategory _category;
	
	/**
	 * @param template
	 * @param playerObjectId
	 * @param type
	 */
	public L2MuseumStatueInstance(L2NpcTemplate template, int playerObjectId, int type)
	{
		super(template);
		_playerObjectId = playerObjectId;
		_type = type;
		restoreCharLooks();
		_category = MuseumManager.getInstance().getAllCategories().get(type);
		setTitle(_category.getTypeName());
	}
	
	private void restoreCharLooks()
	{
		try (Connection con = L2DatabaseFactory.getInstance().getConnection();
             PreparedStatement statement = con.prepareStatement("SELECT * FROM characters WHERE charId=?"))
		{
			statement.setInt(1, _playerObjectId);
			try (ResultSet rset = statement.executeQuery())
			{
				if (rset.next())// fills the package
				{
					_charLooks = CharSelectionInfo.restoreChar(rset);
					if (_charLooks == null)
					{
						System.out.println("Player with id[" + _playerObjectId + "] not found.");
					}
				}
				statement.close();
			}
		}
		catch (Exception e)
		{
			_log.warn("Could not restore char info: " + e.getMessage(), e);
		}
	}
	
	public CharSelectInfoPackage getCharLooks()
	{
		return _charLooks;
	}
	
	@Override
	public void onBypassFeedback(L2PcInstance player, String command)
	{
		player.sendPacket(ActionFailed.STATIC_PACKET);
		
		// StringTokenizer st = new StringTokenizer(command, " ");
		// String actualCommand = st.nextToken(); // Get actual command
		
		super.onBypassFeedback(player, command);
	}
	
	@Override
	public void showChatWindow(L2PcInstance player)
	{
		String html = "<html><body scroll=no>";
		html += MuseumManager.getInstance().showStatue(_category);
		html += "</body></html>";
		separateAndSend(html, player);
	}
	
	@Override
	public void showChatWindow(L2PcInstance player, int page)
	{
		String html = "<html><body scroll=no>";
		html += MuseumManager.getInstance().showStatue(_category);
		html += "</body></html>";
		separateAndSend(html, player);
	}
	
	protected void separateAndSend(String html, L2PcInstance acha)
	{
		if (html == null)
		{
			return;
		}
		if (html.length() < 8180)
		{
			acha.sendPacket(new ShowBoard(html, "101"));
			acha.sendPacket(new ShowBoard(null, "102"));
			acha.sendPacket(new ShowBoard(null, "103"));
		}
		else if (html.length() < (8180 * 2))
		{
			acha.sendPacket(new ShowBoard(html.substring(0, 8180), "101"));
			acha.sendPacket(new ShowBoard(html.substring(8180, html.length()), "102"));
			acha.sendPacket(new ShowBoard(null, "103"));
		}
		else if (html.length() < (8180 * 3))
		{
			acha.sendPacket(new ShowBoard(html.substring(0, 8180), "101"));
			acha.sendPacket(new ShowBoard(html.substring(8180, 8180 * 2), "102"));
			acha.sendPacket(new ShowBoard(html.substring(8180 * 2, html.length()), "103"));
		}
	}
}