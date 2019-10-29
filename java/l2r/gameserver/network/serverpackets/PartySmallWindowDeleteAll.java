package l2r.gameserver.network.serverpackets;

import gr.sr.network.handler.ServerTypeConfigs;

public final class PartySmallWindowDeleteAll extends L2GameServerPacket
{
	public static final PartySmallWindowDeleteAll STATIC_PACKET = new PartySmallWindowDeleteAll();
	
	public PartySmallWindowDeleteAll()
	{
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
				writeC(0x50);
				break;
		}
	}
}
