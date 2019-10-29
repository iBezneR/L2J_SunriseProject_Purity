package l2r.gameserver.network.serverpackets;

import gr.sr.network.handler.ServerTypeConfigs;
import l2r.gameserver.SevenSigns;

public class SSQInfo extends L2GameServerPacket
{
	private int _state = 0;
	
	public SSQInfo()
	{
		int compWinner = SevenSigns.getInstance().getCabalHighestScore();
		
		if (SevenSigns.getInstance().isSealValidationPeriod())
		{
			if (compWinner == SevenSigns.CABAL_DAWN)
			{
				_state = 2;
			}
			else if (compWinner == SevenSigns.CABAL_DUSK)
			{
				_state = 1;
			}
		}
	}
	
	public SSQInfo(int state)
	{
		_state = state;
	}
	
	@Override
	protected final void writeImpl()
	{
		switch (ServerTypeConfigs.SERVER_TYPE)
		{
			case GC:
			case SL:
				return;
		}
		
		switch (ServerTypeConfigs.SERVER_TYPE)
		{
			case IL:
				writeC(0xF8);
				break;
			case GF:
			case EPILOGUE:
			case FREYA:
			case H5:
				writeC(0x73);
				break;
		}
		
		writeH(256 + _state);
	}
}
