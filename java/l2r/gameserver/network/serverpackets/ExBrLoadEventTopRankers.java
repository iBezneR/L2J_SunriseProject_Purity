package l2r.gameserver.network.serverpackets;

import gr.sr.network.handler.ServerTypeConfigs;

public class ExBrLoadEventTopRankers extends L2GameServerPacket
{
	private final int _eventId;
	private final int _day;
	private final int _count;
	private final int _bestScore;
	private final int _myScore;
	
	public ExBrLoadEventTopRankers(int eventId, int day, int count, int bestScore, int myScore)
	{
		_eventId = eventId;
		_day = day;
		_count = count;
		_bestScore = bestScore;
		_myScore = myScore;
	}
	
	@Override
	protected final void writeImpl()
	{
		switch (ServerTypeConfigs.SERVER_TYPE)
		{
			case IL:
			case GF:
				return;
		}
		
		writeC(0xFE);
		
		switch (ServerTypeConfigs.SERVER_TYPE)
		{
			case EPILOGUE:
				writeH(0xC1);
				break;
			case FREYA:
				writeH(0xBC);
				break;
			case H5:
				writeH(0xBD);
				break;
			case GC:
			case SL:
				writeH(0xBE);
				break;
		}
		
		writeD(_eventId);
		writeD(_day);
		writeD(_count);
		writeD(_bestScore);
		writeD(_myScore);
	}
}
