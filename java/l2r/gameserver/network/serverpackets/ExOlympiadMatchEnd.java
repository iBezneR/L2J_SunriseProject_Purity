package l2r.gameserver.network.serverpackets;

import gr.sr.network.handler.ServerTypeConfigs;

public class ExOlympiadMatchEnd extends L2GameServerPacket
{
	public static final ExOlympiadMatchEnd STATIC_PACKET = new ExOlympiadMatchEnd();
	
	private ExOlympiadMatchEnd()
	{
	}
	
	@Override
	protected final void writeImpl()
	{
		writeC(0xFE);
		
		switch (ServerTypeConfigs.SERVER_TYPE)
		{
			case IL:
				writeH(0x2C);
				break;
			case GF:
			case EPILOGUE:
			case FREYA:
			case H5:
			case GC:
			case SL:
				writeH(0x2D);
				break;
		}
	}
}