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

public final class ItemList extends AbstractItemPacket
{
	private final L2PcInstance _activeChar;
	private final List<L2ItemInstance> _items = new ArrayList<>();
	private final boolean _showWindow;
	private int length;
	private final List<L2ItemInstance> questItems = new ArrayList<>();
	
	public ItemList(L2PcInstance cha, boolean showWindow)
	{
		_activeChar = cha.getActingPlayer();
		final L2ItemInstance[] items = cha.getInventory().getItems();
		_showWindow = showWindow;
		
		for (int i = 0; i < items.length; i++)
		{
			if ((items[i] != null) && !items[i].isQuestItem())
			{
				_items.add(items[i]); // add to questinv
			}
		}
	}
	
	@Override
	protected final void writeImpl()
	{
		switch (ServerTypeConfigs.SERVER_TYPE)
		{
			case IL:
				writeC(0x1B);
				break;
			case GF:
			case EPILOGUE:
			case FREYA:
			case H5:
			case GC:
			case SL:
				writeC(0x11);
				break;
		}

		writeH(_showWindow ? 0x01 : 0x00);
		writeH(_items.size());
		
		for (L2ItemInstance temp : _items)
		{
			writeItem(temp);
		}
		writeInventoryBlock(_activeChar.getInventory());
	}
}
