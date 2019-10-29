package l2r.gameserver.network.serverpackets;

import gr.sr.network.handler.ServerTypeConfigs;

public class ExPlayScene extends L2GameServerPacket
{
	public static final ExPlayScene STATIC_PACKET = new ExPlayScene();
	
	private ExPlayScene()
	{
	}
	
	@Override
	protected void writeImpl()
	{
		writeC(0xFE);
		
		switch (ServerTypeConfigs.SERVER_TYPE)
		{
			case IL:
				writeH(0x5B);
				break;
			case GF:
			case EPILOGUE:
			case FREYA:
			case H5:
				writeH(0x5C);
				break;
			case GC:
			case SL:
				writeH(0x5D);
				break;
		}
	}
}
