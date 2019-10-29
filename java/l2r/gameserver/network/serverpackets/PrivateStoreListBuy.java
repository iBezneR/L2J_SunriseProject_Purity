package l2r.gameserver.network.serverpackets;

import gr.sr.configsEngine.configs.impl.CustomServerConfigs;
import gr.sr.network.handler.ServerTypeConfigs;
import l2r.gameserver.model.TradeItem;
import l2r.gameserver.model.actor.instance.L2PcInstance;

import java.util.List;

public class PrivateStoreListBuy extends AbstractItemPacket
{
	private final int _objId;
	private final long _playerAdena;
	private final List<TradeItem> _items;
	
	public PrivateStoreListBuy(L2PcInstance player, L2PcInstance storePlayer)
	{
		_objId = storePlayer.getObjectId();
		_playerAdena = CustomServerConfigs.ALTERNATE_PAYMODE_SHOPS ? player.getFAdena() : player.getAdena();
		storePlayer.getSellList().updateItems(); // Update SellList for case inventory content has changed
		_items = storePlayer.getBuyList().getAvailableItems(player.getInventory());
	}
	
	@Override
	protected final void writeImpl()
	{
		switch (ServerTypeConfigs.SERVER_TYPE)
		{
			case IL:
				writeC(0xB8);
				break;
			case GF:
			case EPILOGUE:
			case FREYA:
			case H5:
			case GC:
			case SL:
				writeC(0xBE);
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
		
		switch (ServerTypeConfigs.SERVER_TYPE)
		{
			case GC:
			case SL:
				writeD(0x00); // Viewer's item count?
				break;
		}
		
		writeD(_items.size());
		
		int slotNumber = 0;
		for (TradeItem item : _items)
		{
			switch (ServerTypeConfigs.SERVER_TYPE)
			{
				case IL:
					writeD(item.getObjectId());
					writeD(item.getItem().getId());
					writeH(item.getEnchant());
					writeD((int) item.getCount());
					
					writeD(item.getItem().getReferencePrice());
					writeH(0);
					
					writeD(item.getItem().getBodyPart());
					writeH(item.getItem().getType2());
					writeD((int) item.getPrice());// buyers price
					
					writeD((int) item.getStoreCount()); // maximum possible tradecount
					break;
				case GF:
				case EPILOGUE:
				case FREYA:
				case H5:
				case GC:
				case SL:
					slotNumber++;
					writeItem(item);
					writeD(slotNumber); // Slot in shop
					writeQ(item.getPrice());
					writeQ(item.getItem().getReferencePrice() * 2);
					writeQ(item.getStoreCount());
					break;
			}
		}
	}
}
