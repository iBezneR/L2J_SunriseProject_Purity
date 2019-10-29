package l2r.gameserver.network.serverpackets;

import gr.sr.network.handler.ServerTypeConfigs;

public class ExCubeGameRequestReady extends L2GameServerPacket
{
	public ExCubeGameRequestReady()
	{
	
	}
	
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
				writeH(0x97);
				break;
			case GC:
			case SL:
				writeH(0x98);
				break;
		}
		
		writeD(0x04);
	}
}