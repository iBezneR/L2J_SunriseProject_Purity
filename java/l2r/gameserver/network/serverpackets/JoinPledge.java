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

public final class JoinPledge extends L2GameServerPacket
{
	private final int _pledgeId;
	
	public JoinPledge(int pledgeId)
	{
		_pledgeId = pledgeId;
	}
	
	@Override
	protected final void writeImpl()
	{
		switch (ServerTypeConfigs.SERVER_TYPE)
		{
			case IL:
				writeC(0x33);
				break;
			case GF:
			case EPILOGUE:
			case FREYA:
			case H5:
			case GC:
			case SL:
				writeC(0x2D);
				break;
		}
		
		writeD(_pledgeId);
	}
}
