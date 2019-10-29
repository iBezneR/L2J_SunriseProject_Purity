package l2r.gameserver.network.serverpackets;

import gr.sr.network.handler.ServerTypeConfigs;

public class ExSearchOrc extends L2GameServerPacket
{
	public static final ExSearchOrc STATIC_PACKET = new ExSearchOrc();
	
	private ExSearchOrc()
	{
	}
	
	@Override
	protected void writeImpl()
	{
		writeC(0xFE);
		
		switch (ServerTypeConfigs.SERVER_TYPE)
		{
			case IL:
				writeH(0x44);
				break;
			case GF:
			case EPILOGUE:
			case FREYA:
			case H5:
				writeH(0x45);
				break;
			case GC:
			case SL:
				writeH(0x46);
				break;
		}
	}
}