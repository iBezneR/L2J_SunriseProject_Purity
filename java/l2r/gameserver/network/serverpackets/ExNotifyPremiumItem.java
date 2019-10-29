package l2r.gameserver.network.serverpackets;

import gr.sr.network.handler.ServerTypeConfigs;

public class ExNotifyPremiumItem extends L2GameServerPacket
{
	public static final ExNotifyPremiumItem STATIC_PACKET = new ExNotifyPremiumItem();
	
	@Override
	protected void writeImpl()
	{
		switch (ServerTypeConfigs.SERVER_TYPE)
		{
			case IL:
				return;
		}
		
		writeC(0xFE);
		
		switch (ServerTypeConfigs.SERVER_TYPE)
		{
			case GF:
			case EPILOGUE:
			case FREYA:
			case H5:
				writeH(0x85);
				break;
			case GC:
			case SL:
				writeH(0x86);
				break;
		}
	}
}
