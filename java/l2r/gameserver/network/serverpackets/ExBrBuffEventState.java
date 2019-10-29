package l2r.gameserver.network.serverpackets;

import gr.sr.network.handler.ServerTypeConfigs;

public class ExBrBuffEventState extends L2GameServerPacket
{
	private final int _type; // 1 - %, 2 - npcId
	private final int _value; // depending on type: for type 1 - % value; for type 2 - 20573-20575
	private final int _state; // 0-1
	private final int _endtime; // only when type 2 as unix time in seconds from 1970
	
	public ExBrBuffEventState(int type, int value, int state, int endtime)
	{
		_type = type;
		_value = value;
		_state = state;
		_endtime = endtime;
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
				writeH(0xAE);
				break;
			case EPILOGUE:
				writeH(0xBF);
				break;
			case FREYA:
				writeH(0xD0);
				break;
			case H5:
				writeH(0xDB);
				break;
			case GC:
			case SL:
				writeH(0xDC);
				break;
		}
		
		writeD(_type);
		writeD(_value);
		writeD(_state);
		writeD(_endtime);
	}
}
