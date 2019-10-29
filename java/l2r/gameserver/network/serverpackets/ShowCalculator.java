package l2r.gameserver.network.serverpackets;

import gr.sr.network.handler.ServerTypeConfigs;

public class ShowCalculator extends L2GameServerPacket
{
	private final int _calculatorId;
	
	public ShowCalculator(int calculatorId)
	{
		_calculatorId = calculatorId;
	}
	
	@Override
	protected final void writeImpl()
	{
		switch (ServerTypeConfigs.SERVER_TYPE)
		{
			case IL:
				writeC(0xDC);
				break;
			case GF:
			case EPILOGUE:
			case FREYA:
			case H5:
			case GC:
			case SL:
				writeC(0xE2);
				break;
		}
		
		writeD(_calculatorId);
	}
}