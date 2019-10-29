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
import l2r.util.StringUtil;

import java.util.List;

public class ShowBoard extends L2GameServerPacket
{
	private final StringBuilder _htmlCode;
	
	public ShowBoard(String htmlCode, String id)
	{
		_htmlCode = StringUtil.startAppend(500, id, "\u0008", htmlCode);
	}
	
	public ShowBoard(List<String> arg)
	{
		_htmlCode = StringUtil.startAppend(500, "1002\u0008");
		for (String str : arg)
		{
			StringUtil.append(_htmlCode, str, " \u0008");
		}
	}
	
	@Override
	protected final void writeImpl()
	{
		switch (ServerTypeConfigs.SERVER_TYPE)
		{
			case IL:
				writeC(0x6E);
				break;
			case GF:
			case EPILOGUE:
			case FREYA:
			case H5:
			case GC:
			case SL:
				writeC(0x7B);
				break;
		}
		
		writeC(0x01); // c4 1 to show community 00 to hide
		writeS("bypass _bbshome"); // top - 01 Home Tab
		writeS("bypass _bbsgetfav"); // favorite - 02 Events
		// _bbslink - 03 Auction House
		writeS("bypass _bbsloc"); // region - 04 Museum
		writeS("bypass _bbsclan"); // clan - 05 Clan Tab
		writeS("bypass _bbsmemo"); // memo - 06 Voting
		writeS("bypass _bbsmail"); // mail - 07 NOT USED?
		writeS("bypass _bbsfriends"); // friends - 08 Friends
		writeS("bypass bbs_add_fav"); // add fav. - 09 Drop Database
		writeS(_htmlCode.toString());
	}
}
