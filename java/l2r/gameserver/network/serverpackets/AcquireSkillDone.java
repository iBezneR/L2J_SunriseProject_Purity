/*
 * Copyright (C) L2J Sunrise
 * This file is part of L2J Sunrise.
 */
package l2r.gameserver.network.serverpackets;

import gr.sr.network.handler.ServerTypeConfigs;

/**
 * @author vGodFather
 */
public class AcquireSkillDone extends L2GameServerPacket
{
	public AcquireSkillDone()
	{
	
	}
	
	@Override
	protected final void writeImpl()
	{
		switch (ServerTypeConfigs.SERVER_TYPE)
		{
			case IL:
				writeC(0x8E);
				break;
			case GF:
			case EPILOGUE:
			case FREYA:
			case H5:
			case GC:
			case SL:
				writeC(0x94);
				break;
		}
	}
}