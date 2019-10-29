package l2r.gameserver.network.serverpackets;

import gr.sr.network.handler.ServerTypeConfigs;

public class ExAutoSoulShot extends L2GameServerPacket
{
	private final int _itemId;
	private final boolean _enable;
	private final int _type;
	
	public ExAutoSoulShot(int itemId, boolean enabled, int type)
	{
		_itemId = itemId;
		_enable = enabled;
		_type = type;
	}
	
	public ExAutoSoulShot(int itemId, int type)
	{
		this(itemId, false, type);
	}
	
	@Override
	protected final void writeImpl()
	{
		writeC(0xFE);
		
		switch (ServerTypeConfigs.SERVER_TYPE)
		{
			case IL:
				writeH(0x0C);
				break;
			case GF:
			case EPILOGUE:
			case FREYA:
			case H5:
			case GC:
			case SL:
				writeH(0x0C);
				break;
		}
		
		writeD(_itemId);
		
		switch (ServerTypeConfigs.SERVER_TYPE)
		{
			case GC:
			case SL:
				writeD(_enable ? 0x01 : 0x00);
				break;
		}
		
		writeD(_type);
	}
}
