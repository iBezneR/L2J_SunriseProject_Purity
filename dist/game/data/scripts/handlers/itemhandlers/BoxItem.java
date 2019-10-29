/*
 * Copyright (C) 2004-2013 L2J DataPack
 * 
 * This file is part of L2J DataPack.
 * 
 * L2J DataPack is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * L2J DataPack is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package handlers.itemhandlers;

import l2r.gameserver.data.xml.impl.ItemData;
import l2r.gameserver.handler.IItemHandler;
import l2r.gameserver.model.actor.L2Playable;
import l2r.gameserver.model.actor.instance.L2PcInstance;
import l2r.gameserver.model.items.L2EtcItem;
import l2r.gameserver.model.items.instance.L2ItemInstance;
import l2r.gameserver.network.SystemMessageId;
import l2r.util.Rnd;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;

/**
 * @author FBIagent 11/12/2006
 */
public class BoxItem implements IItemHandler
{
	private static Logger _log = LoggerFactory.getLogger(ItemData.class);
	
	@Override
	public boolean useItem(L2Playable playable, L2ItemInstance item, boolean forceUse)
	{
		if (!playable.isPlayer())
		{
			playable.sendPacket(SystemMessageId.ITEM_NOT_FOR_PETS);
			return false;
		}
		
		final L2PcInstance activeChar = playable.getActingPlayer();
		final L2EtcItem etcitem = (L2EtcItem) item.getItem();
		final ArrayList<int[]> items = etcitem.getBoxItems(activeChar.getLevel());
		if (items == null)
		{
			_log.info("No extractable data defined for " + etcitem + " or character level " + activeChar.getLevel());
			return false;
		}
		
		// destroy item
		if (!activeChar.destroyItem("Extract", item.getObjectId(), 1, activeChar, true))
		{
			return false;
		}
		
		for (int it[] : items)
		{
			final int min = it[1];
			final int max = it[2];
			
			int createItemAmount = (max == min) ? min : (Rnd.get(min, max));
			if (createItemAmount == 0)
			{
				continue;
			}
			
			if (item.isStackable() || (createItemAmount == 1))
			{
				activeChar.addItem("Extract", it[0], createItemAmount, activeChar, true);
			}
			else
			{
				while (createItemAmount > 0)
				{
					activeChar.addItem("Extract", it[0], 1, activeChar, true);
					createItemAmount--;
				}
			}
		}
		return true;
	}
}
