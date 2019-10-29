package l2r.gameserver.network.serverpackets;

import gr.sr.network.handler.ServerTypeConfigs;

public class ExChangeNpcState extends L2GameServerPacket
{
	private final int _objId;
	private final int _state;
	
	public ExChangeNpcState(int objId, int state)
	{
		_objId = objId;
		_state = state;
	}
	
	@Override
	protected void writeImpl()
	{
		switch (ServerTypeConfigs.SERVER_TYPE)
		{
			case IL:
			case GF:
			case EPILOGUE:
				return;
		}
		
		writeC(0xFE);
		
		switch (ServerTypeConfigs.SERVER_TYPE)
		{
			case FREYA:
				writeH(0xBD);
				break;
			case H5:
				writeH(0xBE);
				break;
			case GC:
			case SL:
				writeH(0xBF);
				break;
		}
		
		writeD(_objId);
		writeD(_state);
	}
}
