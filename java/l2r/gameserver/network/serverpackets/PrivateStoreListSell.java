package l2r.gameserver.network.serverpackets;

import gr.sr.configsEngine.configs.impl.CustomServerConfigs;
import gr.sr.network.handler.ServerTypeConfigs;
import l2r.gameserver.model.TradeItem;
import l2r.gameserver.model.actor.instance.L2PcInstance;

public class PrivateStoreListSell extends AbstractItemPacket
{
	private final int _objId;
	private final long _playerAdena;
	private final boolean _packageSale;
	private final TradeItem[] _items;
	
	public PrivateStoreListSell(L2PcInstance player, L2PcInstance storePlayer)
	{
		_objId = storePlayer.getObjectId();
		_playerAdena = CustomServerConfigs.ALTERNATE_PAYMODE_SHOPS ? player.getFAdena() : player.getAdena();
		_items = storePlayer.getSellList().getItems();
		_packageSale = storePlayer.getSellList().isPackaged();
	}
	
	@Override
	protected final void writeImpl()
	{
		switch (ServerTypeConfigs.SERVER_TYPE)
		{
			case IL:
				writeC(0x9B);
				break;
			case GF:
			case EPILOGUE:
			case FREYA:
			case H5:
			case GC:
			case SL:
				writeC(0xA1);
				break;
		}
		
		writeD(_objId);
		writeD(_packageSale ? 1 : 0);
		
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
		
		writeD(_items.length);
		for (TradeItem item : _items)
		{
			switch (ServerTypeConfigs.SERVER_TYPE)
			{
				case IL:
					writeD(item.getItem().getType2());
					writeD(item.getObjectId());
					writeD(item.getItem().getId());
					writeD((int) item.getCount());
					writeH(0x00);
					writeH(item.getEnchant());
					writeH(0x00);
					writeD(item.getItem().getBodyPart());
					writeD((int) item.getPrice()); // your price
					writeD(item.getItem().getReferencePrice()); // store price
					break;
				case GF:
				case EPILOGUE:
				case FREYA:
				case H5:
				case GC:
				case SL:
					writeItem(item);
					
					writeQ(item.getPrice());
					writeQ(item.getItem().getReferencePrice() * 2);
					break;
			}
		}
	}
}
