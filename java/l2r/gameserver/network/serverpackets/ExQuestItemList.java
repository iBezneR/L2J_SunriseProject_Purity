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
package l2r.gameserver.network.serverpackets;

import gr.sr.network.handler.ServerTypeConfigs;
import l2r.gameserver.model.actor.instance.L2PcInstance;
import l2r.gameserver.model.items.instance.L2ItemInstance;

import java.util.ArrayList;
import java.util.List;

public class ExQuestItemList extends AbstractItemPacket
{
	private final L2PcInstance _activeChar;
	private final List<L2ItemInstance> _items = new ArrayList<>();

	public ExQuestItemList(L2PcInstance activeChar)
	{
		_activeChar = activeChar;
		final L2ItemInstance[] items = activeChar.getInventory().getItems();
		
		for (int i = 0; i < items.length; i++)
		{
			if ((items[i] != null) && items[i].isQuestItem())
			{
				_items.add(items[i]); // add to questinv
				items[i] = null; // remove from list
			}
		}
	}
	
	@Override
	protected void writeImpl()
	{
		switch (ServerTypeConfigs.SERVER_TYPE)
		{
			case IL:
			case GF:
			case EPILOGUE:
				return;
		}
		
		writeC(0xFE);
		
		switch (ServerTypeConfigs.SERVER_TYPE)
		{
			case FREYA:
				writeH(0xC5);
				break;
			case H5:
				writeH(0xC6);
				break;
			case GC:
			case SL:
				writeH(0xC7);
				break;
		}

		writeH(_items.size());
		for (L2ItemInstance item : _items)
		{
			writeItem(item);
		}
		writeInventoryBlock(_activeChar.getInventory());
	}
}
