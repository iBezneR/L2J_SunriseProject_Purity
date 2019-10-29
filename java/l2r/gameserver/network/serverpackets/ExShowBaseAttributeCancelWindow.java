package l2r.gameserver.network.serverpackets;

import gr.sr.network.handler.ServerTypeConfigs;
import l2r.gameserver.model.actor.instance.L2PcInstance;
import l2r.gameserver.model.items.L2Weapon;
import l2r.gameserver.model.items.instance.L2ItemInstance;

public class ExShowBaseAttributeCancelWindow extends L2GameServerPacket
{
	private final L2ItemInstance[] _items;
	private long _price;
	
	public ExShowBaseAttributeCancelWindow(L2PcInstance player)
	{
		_items = player.getInventory().getElementItems();
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
				writeH(0x74);
				break;
			case GC:
			case SL:
				writeH(0x75);
				break;
		}
		
		writeD(_items.length);
		for (L2ItemInstance item : _items)
		{
			writeD(item.getObjectId());
			writeQ(getPrice(item));
		}
	}
	
	/**
	 * TODO: Update prices for Top/Mid/Low S80/S84
	 * @param item
	 * @return
	 */
	private long getPrice(L2ItemInstance item)
	{
		switch (item.getItem().getCrystalType())
		{
			case S:
				if (item.getItem() instanceof L2Weapon)
				{
					_price = 50000;
				}
				else
				{
					_price = 40000;
				}
				break;
			case S80:
				if (item.getItem() instanceof L2Weapon)
				{
					_price = 100000;
				}
				else
				{
					_price = 80000;
				}
				break;
			case S84:
				if (item.getItem() instanceof L2Weapon)
				{
					_price = 200000;
				}
				else
				{
					_price = 160000;
				}
				break;
		}
		
		return _price;
	}
}