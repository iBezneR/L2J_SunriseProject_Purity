package l2r.gameserver.network.serverpackets;

import gr.sr.network.handler.ServerTypeConfigs;
import l2r.gameserver.model.items.instance.L2ItemInstance;

public class PetItemList extends AbstractItemPacket
{
	private final L2ItemInstance[] _items;
	
	public PetItemList(L2ItemInstance[] items)
	{
		_items = items;
	}
	
	@Override
	protected final void writeImpl()
	{
		switch (ServerTypeConfigs.SERVER_TYPE)
		{
			case IL:
				writeC(0xB2);
				break;
			case GF:
			case EPILOGUE:
			case FREYA:
			case H5:
			case GC:
			case SL:
				writeC(0xB3);
				break;
		}
		
		writeH(_items.length);
		
		for (L2ItemInstance item : _items)
		{
			writeItem(item);
		}
	}
}
