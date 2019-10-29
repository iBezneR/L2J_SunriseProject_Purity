package l2r.gameserver.network.serverpackets;

import gr.sr.network.handler.ServerTypeConfigs;

public final class ChooseInventoryItem extends L2GameServerPacket
{
	private final int _itemId;
	
	public ChooseInventoryItem(int itemId)
	{
		_itemId = itemId;
	}
	
	@Override
	protected final void writeImpl()
	{
		switch (ServerTypeConfigs.SERVER_TYPE)
		{
			case IL:
				writeC(0x6F);
				break;
			case GF:
			case EPILOGUE:
			case FREYA:
			case H5:
			case GC:
			case SL:
				writeC(0x7C);
				break;
		}
		
		writeD(_itemId);
	}
}
