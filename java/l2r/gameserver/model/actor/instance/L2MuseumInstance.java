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

import l2r.gameserver.communitybbs.BoardsManager;
import l2r.gameserver.model.actor.templates.L2NpcTemplate;
import l2r.gameserver.network.serverpackets.ActionFailed;

/**
 * @author Erlandys
 */
public final class L2MuseumInstance extends L2NpcInstance
{
	/**
	 * @param template
	 * @param playerObjectId
	 * @param type
	 */
	public L2MuseumInstance(L2NpcTemplate template, int playerObjectId, int type)
	{
		super(template);
	}
	
	@Override
	public void onBypassFeedback(L2PcInstance player, String command)
	{
		player.sendPacket(ActionFailed.STATIC_PACKET);
		
		super.onBypassFeedback(player, command);
	}
	
	@Override
	public void showChatWindow(L2PcInstance player)
	{
		BoardsManager.getInstance().handleCommands(player.getClient(), "_bbsmuseum");
	}
}