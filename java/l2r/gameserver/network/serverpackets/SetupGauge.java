package l2r.gameserver.network.serverpackets;

import gr.sr.network.handler.ServerTypeConfigs;

public final class SetupGauge extends L2GameServerPacket
{
	public static final int BLUE = 0;
	public static final int RED = 1;
	public static final int CYAN = 2;
	public static final int GREEN = 3;
	
	private final int _dat1;
	private final int _time;
	private final int _time2;
	private int _charObjId;
	
	public SetupGauge(int dat1, int time)
	{
		_dat1 = dat1;// color 0-blue 1-red 2-cyan 3-green
		_time = time;
		_time2 = time;
	}
	
	public SetupGauge(int color, int currentTime, int maxTime)
	{
		_dat1 = color;// color 0-blue 1-red 2-cyan 3-green
		_time = currentTime;
		_time2 = maxTime;
	}
	
	@Override
	protected final void writeImpl()
	{
		switch (ServerTypeConfigs.SERVER_TYPE)
		{
			case IL:
				writeC(0x6D);
				break;
			case GF:
			case EPILOGUE:
			case FREYA:
			case H5:
			case GC:
			case SL:
				writeC(0x6B);
				break;
		}
		
		writeD(_charObjId);
		writeD(_dat1);
		writeD(_time);
		writeD(_time2);
	}
	
	@Override
	public void runImpl()
	{
		_charObjId = getClient().getActiveChar().getObjectId();
	}
}
