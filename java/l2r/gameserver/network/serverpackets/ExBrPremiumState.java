package l2r.gameserver.network.serverpackets;

import gr.sr.network.handler.ServerTypeConfigs;

public class ExBrPremiumState extends L2GameServerPacket
{
	private final int _objId;
	private final int _state;
	
	public ExBrPremiumState(int id, int state)
	{
		_objId = id;
		_state = state;
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
				writeH(0xAB);
				break;
			case EPILOGUE:
				writeH(0xBC);
				break;
			case FREYA:
				writeH(0xCD);
				break;
			case H5:
				writeH(0xD9);
				break;
			case GC:
			case SL:
				writeH(0xDA);
				break;
		}
		
		writeD(_objId);
		writeC(_state);
	}
}
