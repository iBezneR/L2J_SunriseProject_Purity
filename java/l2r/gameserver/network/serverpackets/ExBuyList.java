package l2r.gameserver.network.serverpackets;

import gr.sr.network.handler.ServerTypeConfigs;
import l2r.Config;
import l2r.gameserver.model.actor.instance.L2PcInstance;
import l2r.gameserver.model.buylist.L2BuyList;
import l2r.gameserver.model.buylist.Product;

import java.util.Collection;

/**
 * @author vGodFather
 */
public final class ExBuyList extends AbstractItemPacket
{
	private final int _listId;
	private final Collection<Product> _list;
	private final long _money;
	private double _taxRate = 0;
	private boolean _loadAll = true;
	private final int _inventorySlots;
	
	public ExBuyList(L2PcInstance player)
	{
		_listId = -1;
		_list = null;
		_money = player.getAdena();
		_taxRate = 0;
		_loadAll = false;
		_inventorySlots = player.getInventory().getItemsWithoutQuest().size();
	}
	
	public ExBuyList(L2BuyList list, L2PcInstance player, double taxRate)
	{
		_listId = list.getListId();
		_list = list.getProducts();
		_money = player.getAdena();
		_taxRate = taxRate;
		_loadAll = true;
		_inventorySlots = player.getInventory().getItemsWithoutQuest().size();
	}
	
	@Override
	protected final void writeImpl()
	{
		switch (ServerTypeConfigs.SERVER_TYPE)
		{
			case IL:
				writeC(0x11);
				break;
			case GF:
			case EPILOGUE:
			case FREYA:
			case H5:
				writeC(0xFE);
				writeH(0xB7);
				break;
			case GC:
			case SL:
				writeC(0xFE);
				writeH(0xB8);
				break;
		}
		
		writeD(0x00);
		
		switch (ServerTypeConfigs.SERVER_TYPE)
		{
			case IL:
				writeD((int) _money); // current money
				break;
			case GF:
			case EPILOGUE:
			case FREYA:
			case H5:
			case GC:
			case SL:
				writeQ(_money); // current money
				break;
		}
		
		if (_loadAll)
		{
			writeD(_listId);
			
			switch (ServerTypeConfigs.SERVER_TYPE)
			{
				case GC:
				case SL:
					writeD(_inventorySlots);
					break;
			}
			
			writeH(_list.size());
			
			for (Product product : _list)
			{
				if ((product.getCount() > 0) || !product.hasLimitedStock())
				{
					writeItem(product);
					
					if ((product.getId() >= 3960) && (product.getId() <= 4026))
					{
						writeQ((long) (product.getPrice() * Config.RATE_SIEGE_GUARDS_PRICE * (1 + _taxRate)));
					}
					else
					{
						writeQ((long) (product.getPrice() * (1 + _taxRate)));
					}
				}
			}
		}
		else
		{
			writeD(-1);
			writeH(0);
		}
	}
}
