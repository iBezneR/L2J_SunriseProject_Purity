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
import l2r.gameserver.model.L2Object;

import java.util.ArrayList;

public final class StatusUpdate extends L2GameServerPacket
{
	public static final int LEVEL = 0x01;
	public static final int EXP = 0x02;
	public static final int STR = 0x03;
	public static final int DEX = 0x04;
	public static final int CON = 0x05;
	public static final int INT = 0x06;
	public static final int WIT = 0x07;
	public static final int MEN = 0x08;
	
	public static final int CUR_HP = 0x09;
	public static final int MAX_HP = 0x0a;
	public static final int CUR_MP = 0x0b;
	public static final int MAX_MP = 0x0c;
	
	public static final int SP = 0x0d;
	public static final int CUR_LOAD = 0x0e;
	public static final int MAX_LOAD = 0x0f;
	
	public static final int P_ATK = 0x11;
	public static final int ATK_SPD = 0x12;
	public static final int P_DEF = 0x13;
	public static final int EVASION = 0x14;
	public static final int ACCURACY = 0x15;
	public static final int CRITICAL = 0x16;
	public static final int M_ATK = 0x17;
	public static final int CAST_SPD = 0x18;
	public static final int M_DEF = 0x19;
	public static final int PVP_FLAG = 0x1a;
	public static final int KARMA = 0x1b;
	
	public static final int CUR_CP = 0x21;
	public static final int MAX_CP = 0x22;
	
	private final int _objectId;
	private final ArrayList<Attribute> _attributes = new ArrayList<>();
	
	private int _casterObjectId = 0;
	private final boolean _isPlayable;
	private boolean _isVisible = false;
	
	static class Attribute
	{
		public int id;
		public int value;
		
		Attribute(int pId, int pValue)
		{
			id = pId;
			value = pValue;
		}
	}
	
	public StatusUpdate(L2Object object)
	{
		_objectId = object.getObjectId();
		_casterObjectId = object.getObjectId();
		_isPlayable = object.isPlayable();
	}
	
	public void addAttribute(int id, int level)
	{
		_attributes.add(new Attribute(id, level));
		
		if (_isPlayable)
		{
			switch (id)
			{
				case CUR_HP:
				case CUR_MP:
				case CUR_CP:
				{
					_isVisible = true;
				}
			}
		}
	}
	
	public void addCaster(L2Object object)
	{
		_casterObjectId = object.getObjectId();
	}
	
	public boolean hasAttributes()
	{
		return !_attributes.isEmpty();
	}
	
	@Override
	protected final void writeImpl()
	{
		switch (ServerTypeConfigs.SERVER_TYPE)
		{
			case IL:
				writeC(0x0E);
				break;
			case GF:
			case EPILOGUE:
			case FREYA:
			case H5:
			case GC:
			case SL:
				writeC(0x18);
				break;
		}
		
		writeD(_objectId);
		
		switch (ServerTypeConfigs.SERVER_TYPE)
		{
			case GC:
			case SL:
				writeD(_isVisible ? _casterObjectId : 0x00);
				writeC(_isVisible ? 0x01 : 0x00);
				break;
		}
		
		switch (ServerTypeConfigs.SERVER_TYPE)
		{
			case IL:
			case GF:
			case EPILOGUE:
			case FREYA:
			case H5:
				writeD(_attributes.size());
				break;
			case GC:
			case SL:
				writeC(_attributes.size());
				break;
		}
		
		for (Attribute temp : _attributes)
		{
			switch (ServerTypeConfigs.SERVER_TYPE)
			{
				case IL:
				case GF:
				case EPILOGUE:
				case FREYA:
				case H5:
					writeD(temp.id);
					break;
				case GC:
				case SL:
					writeC(temp.id);
					break;
			}
			
			writeD(temp.value);
		}
	}
}
