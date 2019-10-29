package l2r.gameserver.network.serverpackets;

import gr.sr.network.handler.ServerTypeConfigs;
import l2r.gameserver.model.Elementals;
import l2r.gameserver.model.actor.instance.L2PcInstance;
import l2r.gameserver.model.items.instance.L2ItemInstance;

import java.util.HashSet;
import java.util.Set;

/**
 * @author vGodFather
 */
public class ExChooseInventoryAttributeItem extends L2GameServerPacket
{
	private final int _itemId;
	private final long _count;
	private final byte _atribute;
	private final int _level;
	private final Set<Integer> _items = new HashSet<>();
	
	public ExChooseInventoryAttributeItem(L2PcInstance activeChar, L2ItemInstance stone)
	{
		_itemId = stone.getDisplayId();
		_count = stone.getCount();
		_atribute = Elementals.getItemElement(_itemId);
		if (_atribute == Elementals.NONE)
		{
			throw new IllegalArgumentException("Undefined Atribute item: " + stone);
		}
		_level = Elementals.getMaxElementLevel(_itemId);
		
		// Register only items that can be put an attribute stone/crystal
		for (L2ItemInstance item : activeChar.getInventory().getItems())
		{
			if (item.isElementable())
			{
				_items.add(item.getObjectId());
			}
		}
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
			case EPILOGUE:
			case FREYA:
			case H5:
				writeH(0x62);
				break;
			case GC:
			case SL:
				writeH(0x63);
				break;
		}
		
		writeD(_itemId);
		
		switch (ServerTypeConfigs.SERVER_TYPE)
		{
			case GC:
			case SL:
				writeQ(_count);
				break;
		}
		
		// Structure for now
		// Must be 0x01 for stone/crystal attribute type
		writeD(_atribute == Elementals.FIRE ? 1 : 0); // Fire
		writeD(_atribute == Elementals.WATER ? 1 : 0); // Water
		writeD(_atribute == Elementals.WIND ? 1 : 0); // Wind
		writeD(_atribute == Elementals.EARTH ? 1 : 0); // Earth
		writeD(_atribute == Elementals.HOLY ? 1 : 0); // Holy
		writeD(_atribute == Elementals.DARK ? 1 : 0); // Unholy
		writeD(_level); // Item max attribute level
		
		switch (ServerTypeConfigs.SERVER_TYPE)
		{
			case GC:
			case SL:
				writeD(_items.size());
				_items.forEach(this::writeD);
				break;
		}
	}
}
