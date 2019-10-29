package l2r.gameserver.network.serverpackets;

import gr.sr.network.handler.ServerTypeConfigs;

public class ExOpenMPCC extends L2GameServerPacket
{
	public static final ExOpenMPCC STATIC_PACKET = new ExOpenMPCC();
	
	private ExOpenMPCC()
	{
	}
	
	@Override
	protected void writeImpl()
	{
		writeC(0xFE);
		
		switch (ServerTypeConfigs.SERVER_TYPE)
		{
			case IL:
				writeH(0x25);
				break;
			case GF:
			case EPILOGUE:
			case FREYA:
			case H5:
			case GC:
			case SL:
				writeH(0x12);
				break;
		}
	}
}
