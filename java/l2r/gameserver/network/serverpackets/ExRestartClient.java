package l2r.gameserver.network.serverpackets;

import gr.sr.network.handler.ServerTypeConfigs;

public class ExRestartClient extends L2GameServerPacket
{
	public static final ExRestartClient STATIC_PACKET = new ExRestartClient();
	
	private ExRestartClient()
	{
	}
	
	@Override
	protected void writeImpl()
	{
		writeC(0xFE);
		
		switch (ServerTypeConfigs.SERVER_TYPE)
		{
			case IL:
				writeH(0x47);
				break;
			case GF:
			case EPILOGUE:
			case FREYA:
			case H5:
				writeH(0x48);
				break;
			case GC:
			case SL:
				writeH(0x49);
				break;
		}
	}
}