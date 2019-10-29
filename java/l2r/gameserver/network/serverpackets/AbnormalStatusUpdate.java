/*
 * Copyright (C) 2004-2016 L2J Server
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
import l2r.gameserver.model.effects.L2Effect;

import java.util.ArrayList;
import java.util.List;

public class AbnormalStatusUpdate extends L2GameServerPacket
{
	private final List<L2Effect> _effects = new ArrayList<>();
	
	public void addSkill(L2Effect info)
	{
		if (!info.getSkill().isHealingPotionSkill())
		{
			_effects.add(info);
		}
	}
	
	@Override
	protected final void writeImpl()
	{
		switch (ServerTypeConfigs.SERVER_TYPE)
		{
			case IL:
				writeC(0x7F);
				break;
			case GF:
			case EPILOGUE:
			case FREYA:
			case H5:
			case GC:
			case SL:
				writeC(0x85);
				break;
		}
		
		switch (ServerTypeConfigs.SERVER_TYPE)
		{
			case IL:
			case GF:
			case EPILOGUE:
			case FREYA:
			case H5:
				writeH(_effects.size());
				
				for (L2Effect info : _effects)
				{
					if ((info != null) && info.getInUse())
					{
						writeD(info.getSkill().getDisplayId());
						writeH(info.getSkill().getDisplayLevel());
						writeD(info.getRemainingTime());
					}
				}
				break;
			case GC:
			case SL:
				writeH(_effects.size());
				
				for (L2Effect info : _effects)
				{
					writeD(info.getSkill().getDisplayId());
					writeH(info.getSkill().getDisplayLevel());
					writeH(info.getSkill().getSubLevel());
					writeD(0/* info.getSkill().getAbnormalType().getClientId() */); // TODO
					writeOptionalD(info.getSkill().isAura() ? -1 : info.getRemainingTime());
				}
				break;
		}
	}
}
