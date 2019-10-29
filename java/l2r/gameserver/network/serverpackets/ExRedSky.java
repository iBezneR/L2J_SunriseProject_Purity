package l2r.gameserver.network.serverpackets;

import gr.sr.network.handler.ServerTypeConfigs;

public class ExRedSky extends L2GameServerPacket
{
	private final int _duration;
	
	public ExRedSky(int duration)
	{
		_duration = duration;
	}
	
	@Override
	protected void writeImpl()
	{
		writeC(0xFE);
		
		switch (ServerTypeConfigs.SERVER_TYPE)
		{
			case IL:
				writeH(0x40);
				break;
			case GF:
			case EPILOGUE:
			case FREYA:
			case H5:
				writeH(0x41);
				break;
			case GC:
			case SL:
				writeH(0x42);
				break;
		}
		
		writeD(_duration);
	}
}
