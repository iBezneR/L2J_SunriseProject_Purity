package l2r.gameserver.network.serverpackets;

import gr.sr.network.handler.ServerTypeConfigs;

public class ShowXMasSeal extends L2GameServerPacket
{
	private final int _item;
	
	public ShowXMasSeal(int item)
	{
		_item = item;
	}
	
	@Override
	protected void writeImpl()
	{
		switch (ServerTypeConfigs.SERVER_TYPE)
		{
			case IL:
				writeC(0xF2);
				break;
			case GF:
			case EPILOGUE:
			case FREYA:
			case H5:
			case GC:
			case SL:
				writeC(0xF8);
				break;
		}
		
		writeD(_item);
	}
}
