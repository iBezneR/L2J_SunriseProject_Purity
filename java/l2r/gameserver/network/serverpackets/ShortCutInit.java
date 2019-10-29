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
import l2r.gameserver.model.Shortcut;
import l2r.gameserver.model.actor.instance.L2PcInstance;

public final class ShortCutInit extends L2GameServerPacket
{
	private Shortcut[] _shortCuts;
	
	public ShortCutInit(L2PcInstance activeChar)
	{
		if (activeChar == null)
		{
			return;
		}
		
		_shortCuts = activeChar.getAllShortCuts();
	}
	
	@Override
	protected final void writeImpl()
	{
		switch (ServerTypeConfigs.SERVER_TYPE)
		{
			case IL:
			case GF:
			case EPILOGUE:
			case FREYA:
			case H5:
			case GC:
			case SL:
				writeC(0x45);
				break;
		}
		
		writeD(_shortCuts.length);
		
		for (Shortcut sc : _shortCuts)
		{
			writeD(sc.getType().ordinal());
			writeD(sc.getSlot() + (sc.getPage() * 12));
			
			switch (sc.getType())
			{
				case ITEM:
				{
					writeD(sc.getId());
					writeD(0x01);
					writeD(sc.getSharedReuseGroup());
					writeD(0x00);
					writeD(0x00);
					
					switch (ServerTypeConfigs.SERVER_TYPE)
					{
						case IL:
						case GF:
						case EPILOGUE:
						case FREYA:
						case H5:
							writeH(0x00);
							writeH(0x00);
							break;
						case GC:
						case SL:
							writeQ(0x00); // augment id
							writeD(0x00); // Visual id
							break;
					}
					break;
				}
				case SKILL:
				{
					writeD(sc.getId());
					
					switch (ServerTypeConfigs.SERVER_TYPE)
					{
						case IL:
						case GF:
						case EPILOGUE:
						case FREYA:
						case H5:
							writeD(sc.getLevel());
							break;
						case GC:
						case SL:
							writeH(sc.getLevel());
							writeH(sc.getSubLevel());
							writeD(sc.getSharedReuseGroup());
							break;
					}
					
					writeC(0x00); // C5
					writeD(0x01); // C6
					break;
				}
				case ACTION:
				case MACRO:
				case RECIPE:
				case BOOKMARK:
				{
					writeD(sc.getId());
					writeD(0x01); // C6
					break;
				}
			}
		}
	}
}
