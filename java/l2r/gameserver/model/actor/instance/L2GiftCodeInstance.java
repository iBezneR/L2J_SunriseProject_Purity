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
package l2r.gameserver.model.actor.instance;

import l2r.gameserver.custom.GiftCodesManager;
import l2r.gameserver.enums.InstanceType;
import l2r.gameserver.model.actor.L2Npc;
import l2r.gameserver.model.actor.templates.L2NpcTemplate;
import l2r.gameserver.network.serverpackets.ActionFailed;
import l2r.gameserver.network.serverpackets.NpcHtmlMessage;

import java.util.StringTokenizer;

/**
 * @author NightMarez
 */
public final class L2GiftCodeInstance extends L2Npc
{
	/**
	 * @param template
	 */
	public L2GiftCodeInstance(L2NpcTemplate template)
	{
		super(template);
		setInstanceType(InstanceType.L2GiftCodeInstance);
	}
	
	@Override
	public void onBypassFeedback(L2PcInstance player, String command)
	{
		player.sendPacket(ActionFailed.STATIC_PACKET);
		
		StringTokenizer st = new StringTokenizer(command, " ");
		String actualCommand = st.nextToken(); // Get actual command
		if (actualCommand.equalsIgnoreCase("check"))
		{
			if (st.countTokens() <= 0)
			{
				return;
			}
			String giftCode = st.nextToken();
			GiftCodesManager.getInstance().giveGift(player, giftCode);
		}
		super.onBypassFeedback(player, command);
	}
	
	@Override
	public void showChatWindow(L2PcInstance player)
	{
		String filename = "data/html/giftcode/main.htm";
		
		NpcHtmlMessage html = new NpcHtmlMessage(getObjectId());
		html.setFile(player, player.getHtmlPrefix(), filename);
		html.replace("%objectId%", String.valueOf(getObjectId()));
		html.replace("%npcname%", getName());
		player.sendPacket(html);
	}
}
