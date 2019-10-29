/*
 * Copyright (C) L2J Sunrise
 * This file is part of L2J Sunrise.
 */
package l2r.gameserver.network.serverpackets;

import gr.sr.network.handler.ServerTypeConfigs;

/**
 * @author vGodFather
 */
public final class ActionFailed extends L2GameServerPacket
{
	public static final ActionFailed STATIC_PACKET = new ActionFailed();
	
	private ActionFailed()
	{
	}
	
	@Override
	protected void writeImpl()
	{
		switch (ServerTypeConfigs.SERVER_TYPE)
		{
			case IL:
				writeC(0x25);
				break;
			case GF:
			case EPILOGUE:
			case FREYA:
			case H5:
			case GC:
			case SL:
				writeC(0x1F);
				break;
		}
		
		switch (ServerTypeConfigs.SERVER_TYPE)
		{
			case GC:
			case SL:
				writeD(0); // casting type
				break;
		}
	}
}
