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
import l2r.gameserver.model.actor.L2Summon;

/**
 * @author KenM
 */
public class ExPartyPetWindowUpdate extends L2GameServerPacket
{
	private final L2Summon _summon;
	
	public ExPartyPetWindowUpdate(L2Summon summon)
	{
		_summon = summon;
	}
	
	@Override
	protected void writeImpl()
	{
		switch (ServerTypeConfigs.SERVER_TYPE)
		{
			case IL:
				return;
		}
		
		writeC(0xFE);
		
		switch (ServerTypeConfigs.SERVER_TYPE)
		{
			case GF:
			case EPILOGUE:
			case FREYA:
			case H5:
			case GC:
			case SL:
				writeH(0x19);
				break;
		}
		
		writeD(_summon.getObjectId());
		writeD(_summon.getTemplate().getDisplayId() + 1000000);
		
		switch (ServerTypeConfigs.SERVER_TYPE)
		{
			case IL:
			case GF:
			case EPILOGUE:
			case FREYA:
			case H5:
				writeD(_summon.getSummonType());
				break;
			case GC:
			case SL:
				writeC(_summon.getSummonType());
				break;
		}
		
		writeD(_summon.getOwner().getObjectId());
		
		switch (ServerTypeConfigs.SERVER_TYPE)
		{
			case IL:
			case GF:
			case EPILOGUE:
			case FREYA:
			case H5:
				writeS(_summon.getName());
				break;
		}
		
		writeD((int) _summon.getCurrentHp());
		writeD(_summon.getMaxHp());
		writeD((int) _summon.getCurrentMp());
		writeD(_summon.getMaxMp());
		
		switch (ServerTypeConfigs.SERVER_TYPE)
		{
			case IL:
			case GF:
			case EPILOGUE:
			case FREYA:
			case H5:
				writeD(_summon.getLevel());
				break;
		}
	}
}
