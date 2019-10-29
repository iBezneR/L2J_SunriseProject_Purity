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

public class ExBrExtraUserInfo extends L2GameServerPacket
{
	private final int _charObjId;
	
	public ExBrExtraUserInfo(L2PcInstance player)
	{
		_charObjId = player.getObjectId();
		_invisible = player.isInvisible();
	}
	
	@Override
	protected final void writeImpl()
	{
		switch (ServerTypeConfigs.SERVER_TYPE)
		{
			case IL:
			case GC:
			case SL:
				return;
		}
		
		writeC(0xFE);
		
		switch (ServerTypeConfigs.SERVER_TYPE)
		{
			case GF:
				writeH(0xAC);
				break;
			case EPILOGUE:
				writeH(0xBE);
				break;
			case FREYA:
				writeH(0xCF);
				break;
			case H5:
				writeH(0xDA);
				break;
		}
		
		writeD(_charObjId); // object ID of Player
		// writeD(_val); // event effect id
		// writeC(0x00); // Event flag, added only if event is active
	}
}
