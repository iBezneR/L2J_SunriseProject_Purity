package l2r.gameserver.network.serverpackets;

import gr.sr.network.handler.ServerTypeConfigs;

public class ExShowVariationMakeWindow extends L2GameServerPacket
{
	public static final ExShowVariationMakeWindow STATIC_PACKET = new ExShowVariationMakeWindow();
	
	public ExShowVariationMakeWindow()
	{
	}
	
	@Override
	protected void writeImpl()
	{
		writeC(0xFE);
		
		switch (ServerTypeConfigs.SERVER_TYPE)
		{
			case IL:
				writeH(0x50);
				break;
			case GF:
			case EPILOGUE:
			case FREYA:
			case H5:
				writeH(0x51);
				break;
			case GC:
			case SL:
				writeH(0x52);
				break;
		}
	}
}
