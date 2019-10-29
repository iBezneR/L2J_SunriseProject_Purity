/*
 * Copyright (C) 2004-2014 L2J DataPack
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
package l2r.gameserver.custom;

import l2r.gameserver.handler.IVoicedCommandHandler;
import l2r.gameserver.model.actor.instance.L2PcInstance;

public class GiftCode implements IVoicedCommandHandler
{
	private static final String[] VOICED_COMMANDS =
	{
		"giftcode"
	};
	
	@Override
	public boolean useVoicedCommand(String command, L2PcInstance activeChar, String params)
	{
		if (command.equals(VOICED_COMMANDS[0]))
		{
			if (params == null)
			{
				activeChar.sendMessage("Usage: .giftcode [code]");
				return true;
			}
			GiftCodesManager.getInstance().giveGift(activeChar, params);
		}
		return true;
	}
	
	@Override
	public String[] getVoicedCommandList()
	{
		return VOICED_COMMANDS;
	}
}