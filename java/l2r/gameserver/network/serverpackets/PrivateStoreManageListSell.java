package l2r.gameserver.network.serverpackets;

import gr.sr.configsEngine.configs.impl.CustomServerConfigs;
import gr.sr.network.handler.ServerTypeConfigs;
import l2r.gameserver.model.TradeItem;
import l2r.gameserver.model.actor.instance.L2PcInstance;

public class PrivateStoreManageListSell extends AbstractItemPacket
{
	private final int _objId;
	private final long _playerAdena;
	private final boolean _packageSale;
	private final TradeItem[] _itemList;
	private final TradeItem[] _sellList;
	
	public PrivateStoreManageListSell(L2PcInstance player, boolean isPackageSale)
	{
		_objId = player.getObjectId();
		_playerAdena = CustomServerConfigs.ALTERNATE_PAYMODE_SHOPS ? player.getFAdena() : player.getAdena();
		player.getSellList().updateItems();
		_packageSale = isPackageSale;
		_itemList = player.getInventory().getAvailableItems(player.getSellList());
		_sellList = player.getSellList().getItems();
	}
	
	@Override
	protected final void writeImpl()
	{
		switch (ServerTypeConfigs.SERVER_TYPE)
		{
			case IL:
				writeC(0x9A);
				break;
			case GF:
			case EPILOGUE:
			case FREYA:
			case H5:
			case GC:
			case SL:
				writeC(0xA0);
				break;
		}
		
		writeD(_objId);
		writeD(_packageSale ? 1 : 0); // Package sell
		
		switch (ServerTypeConfigs.SERVER_TYPE)
		{
			case IL:
				writeD((int) _playerAdena);
				break;
			case GF:
			case EPILOGUE:
			case FREYA:
			case H5:
			case GC:
			case SL:
				writeQ(_playerAdena);
				break;
		}
		
		writeD(_itemList.length); // for potential sells
		for (TradeItem item : _itemList)
		{
			writeItem(item);
			
			switch (ServerTypeConfigs.SERVER_TYPE)
			{
				case IL:
					writeD(item.getItem().getReferencePrice() * 2);
					break;
				case GF:
				case EPILOGUE:
				case FREYA:
				case H5:
				case GC:
				case SL:
					writeQ(item.getItem().getReferencePrice() * 2);
					break;
			}
		}
		
		writeD(_sellList.length); // count for any items already added for sell
		for (TradeItem item : _sellList)
		{
			writeItem(item);
			
			switch (ServerTypeConfigs.SERVER_TYPE)
			{
				case IL:
					writeD((int) item.getPrice());
					writeD(item.getItem().getReferencePrice() * 2);
					break;
				case GF:
				case EPILOGUE:
				case FREYA:
				case H5:
				case GC:
				case SL:
					writeQ(item.getPrice());
					writeQ(item.getItem().getReferencePrice() * 2);
					break;
			}
		}
	}
}
