package l2r.gameserver.network.serverpackets;

import gr.sr.network.handler.ServerTypeConfigs;

public class ExShowQuestInfo extends L2GameServerPacket
{
	public static final ExShowQuestInfo STATIC_PACKET = new ExShowQuestInfo();
	
	private ExShowQuestInfo()
	{
	}
	
	@Override
	protected void writeImpl()
	{
		writeC(0xFE);
		
		switch (ServerTypeConfigs.SERVER_TYPE)
		{
			case IL:
				writeH(0x19);
				break;
			case GF:
			case EPILOGUE:
			case FREYA:
			case H5:
			case GC:
			case SL:
				writeH(0x20);
				break;
		}
	}
}
