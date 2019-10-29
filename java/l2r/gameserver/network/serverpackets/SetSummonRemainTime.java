package l2r.gameserver.network.serverpackets;

import gr.sr.network.handler.ServerTypeConfigs;

public final class SetSummonRemainTime extends L2GameServerPacket
{
	private final int _maxTime;
	private final int _remainingTime;
	
	public SetSummonRemainTime(int maxTime, int remainingTime)
	{
		_remainingTime = remainingTime;
		_maxTime = maxTime;
	}
	
	@Override
	protected final void writeImpl()
	{
		switch (ServerTypeConfigs.SERVER_TYPE)
		{
			case IL:
				writeC(0xD1);
				break;
			case GF:
			case EPILOGUE:
			case FREYA:
			case H5:
			case GC:
			case SL:
				writeC(0xD1);
				break;
		}
		
		writeD(_maxTime);
		writeD(_remainingTime);
	}
}
