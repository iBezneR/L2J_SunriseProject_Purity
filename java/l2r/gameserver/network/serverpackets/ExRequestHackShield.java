package l2r.gameserver.network.serverpackets;

import gr.sr.network.handler.ServerTypeConfigs;

public class ExRequestHackShield extends L2GameServerPacket
{
	public static final ExRequestHackShield STATIC_PACKET = new ExRequestHackShield();
	
	private ExRequestHackShield()
	{
	}
	
	@Override
	protected void writeImpl()
	{
		writeC(0xFE);
		
		switch (ServerTypeConfigs.SERVER_TYPE)
		{
			case IL:
				writeH(0x48);
				break;
			case GF:
			case EPILOGUE:
			case FREYA:
			case H5:
				writeH(0x49);
				break;
			case GC:
			case SL:
				writeH(0x4A);
				break;
		}
	}
}
