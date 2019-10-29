package l2r.gameserver.network.serverpackets;

import gr.sr.configsEngine.configs.impl.CustomServerConfigs;
import gr.sr.network.handler.ServerTypeConfigs;
import l2r.gameserver.model.TradeItem;
import l2r.gameserver.model.actor.instance.L2PcInstance;
import l2r.gameserver.model.items.instance.L2ItemInstance;

public class PrivateStoreManageListBuy extends AbstractItemPacket
{
	private final int _objId;
	private final long _playerAdena;
	private final L2ItemInstance[] _itemList;
	private final TradeItem[] _buyList;
	
	public PrivateStoreManageListBuy(L2PcInstance player)
	{
		_objId = player.getObjectId();
		_playerAdena = CustomServerConfigs.ALTERNATE_PAYMODE_SHOPS ? player.getFAdena() : player.getAdena();
		_itemList = player.getInventory().getUniqueItems(false, true);
		_buyList = player.getBuyList().getItems();
	}
	
	@Override
	protected final void writeImpl()
	{
		switch (ServerTypeConfigs.SERVER_TYPE)
		{
			case IL:
				writeC(0xB7);
				break;
			case GF:
			case EPILOGUE:
			case FREYA:
			case H5:
			case GC:
			case SL:
				writeC(0xBD);
				break;
		}
		
		writeD(_objId);
		
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
		for (L2ItemInstance item : _itemList)
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
		
		writeD(_buyList.length); // count for any items already added for sell
		for (TradeItem item : _buyList)
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
					writeQ(item.getCount());
					break;
			}
		}
	}
}
