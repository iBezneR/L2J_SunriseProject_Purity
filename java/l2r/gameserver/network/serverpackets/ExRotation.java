package l2r.gameserver.network.serverpackets;

import gr.sr.network.handler.ServerTypeConfigs;

public class ExRotation extends L2GameServerPacket
{
	private final int _charId, _heading;
	
	public ExRotation(int charId, int heading)
	{
		_charId = charId;
		_heading = heading;
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
				writeH(0xC0);
				break;
			case H5:
				writeH(0xC1);
				break;
			case GC:
			case SL:
				writeH(0xC2);
				break;
		}
		
		writeD(_charId);
		writeD(_heading);
	}
}
