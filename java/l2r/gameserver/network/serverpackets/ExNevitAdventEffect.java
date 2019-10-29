package l2r.gameserver.network.serverpackets;

import gr.sr.network.handler.ServerTypeConfigs;

public class ExNevitAdventEffect extends L2GameServerPacket
{
	private final int _timeLeft;
	
	public ExNevitAdventEffect(int timeLeft)
	{
		_timeLeft = timeLeft;
	}
	
	@Override
	protected void writeImpl()
	{
		switch (ServerTypeConfigs.SERVER_TYPE)
		{
			case IL:
			case GF:
			case EPILOGUE:
			case FREYA:
				return;
		}
		
		writeC(0xFE);
		
		switch (ServerTypeConfigs.SERVER_TYPE)
		{
			case H5:
				writeH(0xE0);
				break;
			case GC:
			case SL:
				writeH(0xE4);
				break;
		}
		
		writeD(_timeLeft);
	}
}
