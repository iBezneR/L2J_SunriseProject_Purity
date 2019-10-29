package l2r.gameserver.network.serverpackets;

import gr.sr.network.handler.ServerTypeConfigs;

public class ExAskCoupleAction extends L2GameServerPacket
{
	private final int _charObjId;
	private final int _actionId;
	
	public ExAskCoupleAction(int charObjId, int social)
	{
		_charObjId = charObjId;
		_actionId = social;
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
			case H5:
				writeH(0xBB);
				break;
			case GC:
			case SL:
				writeH(0xBC);
				break;
		}
		
		writeD(_actionId);
		writeD(_charObjId);
	}
}
