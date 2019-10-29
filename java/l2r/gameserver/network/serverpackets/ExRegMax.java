package l2r.gameserver.network.serverpackets;

import gr.sr.network.handler.ServerTypeConfigs;

public class ExRegMax extends L2GameServerPacket
{
	private final double _max;
	private final int _count;
	private final int _time;
	
	public ExRegMax(double max, int count, int time)
	{
		_max = max;
		_count = count;
		_time = time;
	}
	
	@Override
	protected void writeImpl()
	{
		writeC(0xFE);
		
		switch (ServerTypeConfigs.SERVER_TYPE)
		{
			case IL:
			case GF:
			case EPILOGUE:
			case FREYA:
			case H5:
			case GC:
			case SL:
				writeH(0x01);
				break;
		}
		
		writeD(1);
		writeD(_count);
		writeD(_time);
		writeF(_max);
	}
}
