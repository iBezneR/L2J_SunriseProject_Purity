package l2r.gameserver.network.serverpackets;

import gr.sr.network.handler.ServerTypeConfigs;

public class ExShowAdventurerGuideBook extends L2GameServerPacket
{
	public static final ExShowAdventurerGuideBook STATIC_PACKET = new ExShowAdventurerGuideBook();
	
	private ExShowAdventurerGuideBook()
	{
	}
	
	@Override
	protected void writeImpl()
	{
		writeC(0xFE);
		
		switch (ServerTypeConfigs.SERVER_TYPE)
		{
			case IL:
				writeH(0x37);
				break;
			case GF:
			case EPILOGUE:
			case FREYA:
			case H5:
			case GC:
			case SL:
				writeH(0x38);
				break;
		}
	}
}
