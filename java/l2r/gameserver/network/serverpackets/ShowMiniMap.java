package l2r.gameserver.network.serverpackets;

import gr.sr.network.handler.ServerTypeConfigs;
import l2r.gameserver.SevenSigns;

public class ShowMiniMap extends L2GameServerPacket
{
	private final int _mapId;
	
	public ShowMiniMap(int mapId)
	{
		_mapId = mapId;
	}
	
	@Override
	protected final void writeImpl()
	{
		switch (ServerTypeConfigs.SERVER_TYPE)
		{
			case IL:
				writeC(0x9D);
				break;
			case GF:
			case EPILOGUE:
			case FREYA:
			case H5:
			case GC:
			case SL:
				writeC(0xA3);
				break;
		}
		
		writeD(_mapId);
		writeC(SevenSigns.getInstance().getCurrentPeriod());
	}
}
