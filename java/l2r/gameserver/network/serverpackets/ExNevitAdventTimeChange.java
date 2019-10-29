package l2r.gameserver.network.serverpackets;

import gr.sr.network.handler.ServerTypeConfigs;
import l2r.gameserver.model.entity.NevitSystem;

/**
 * @author vGodFather
 */
public class ExNevitAdventTimeChange extends L2GameServerPacket
{
	private final boolean _paused;
	private final int _time;
	
	public ExNevitAdventTimeChange(int time, boolean paused)
	{
		// we must set time here
		_time = (time >= NevitSystem.ADVENT_TIME) ? 16000 : time;
		_paused = paused;
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
				writeH(0xE1);
				break;
			case GC:
			case SL:
				writeH(0xE5);
				break;
		}
		
		// state 0 - pause 1 - started
		writeC(_paused ? 0x00 : 0x01);
		// left time in ms max is 16000 its 4m and state is automatically changed to quit
		writeD(_time);
	}
}
