package l2r.gameserver.network.serverpackets;

import gr.sr.network.handler.ServerTypeConfigs;
import l2r.gameserver.model.actor.instance.L2PcInstance;
import l2r.gameserver.model.actor.instance.L2PetInstance;
import l2r.gameserver.model.items.instance.L2ItemInstance;

public class GMViewItemList extends AbstractItemPacket
{
	private final L2ItemInstance[] _items;
	private final int _limit;
	private final String _playerName;
	
	public GMViewItemList(L2PcInstance cha)
	{
		_items = cha.getInventory().getItems();
		_playerName = cha.getName();
		_limit = cha.getInventoryLimit();
	}
	
	public GMViewItemList(L2PetInstance cha)
	{
		_items = cha.getInventory().getItems();
		_playerName = cha.getName();
		_limit = cha.getInventoryLimit();
	}
	
	@Override
	protected final void writeImpl()
	{
		switch (ServerTypeConfigs.SERVER_TYPE)
		{
			case IL:
				writeC(0x94);
				break;
			case GF:
			case EPILOGUE:
			case FREYA:
			case H5:
			case GC:
			case SL:
				writeC(0x9A);
				break;
		}
		
		writeS(_playerName);
		writeD(_limit); // inventory limit
		writeH(0x01); // show window ??
		writeH(_items.length);
		
		for (L2ItemInstance item : _items)
		{
			writeItem(item);
		}
	}
}
