package l2r.gameserver.network.serverpackets;

import gr.sr.network.handler.ServerTypeConfigs;

/**
 * @author vGodFather
 */
public class ExNevitAdventPointInfoPacket extends L2GameServerPacket
{
	private final int _points;
	
	public ExNevitAdventPointInfoPacket(int points)
	{
		_points = points;
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
				writeH(0xDF);
				break;
			case GC:
			case SL:
				writeH(0xE3);
				break;
		}
		
		writeD(_points); // 72 = 1%, max 7200 = 100%
	}
}
