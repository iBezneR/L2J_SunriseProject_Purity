package l2r.gameserver.network.serverpackets;

import gr.sr.network.handler.ServerTypeConfigs;
import l2r.gameserver.model.actor.instance.L2PcInstance;
import l2r.gameserver.model.items.instance.L2ItemInstance;

import java.util.ArrayList;
import java.util.List;

public final class WareHouseWithdrawalList extends AbstractItemPacket
{
	public static final int PRIVATE = 1;
	public static final int CLAN = 4;
	public static final int CASTLE = 3; // not sure
	public static final int FREIGHT = 1;
	private L2PcInstance _activeChar;
	private long _playerAdena;
	private L2ItemInstance[] _items;
	/**
	 * <ul>
	 * <li>0x01-Private Warehouse</li>
	 * <li>0x02-Clan Warehouse</li>
	 * <li>0x03-Castle Warehouse</li>
	 * <li>0x04-Warehouse</li>
	 * </ul>
	 */
	private int _whType;
	
	private int _invSize;
	private final List<Integer> _itemsStackable = new ArrayList<>();
	
	public WareHouseWithdrawalList(L2PcInstance player, int type)
	{
		_activeChar = player;
		_whType = type;
		
		_playerAdena = _activeChar.getAdena();
		if (_activeChar.getActiveWarehouse() == null)
		{
			// Something went wrong!
			_log.warn("error while sending withdraw request to: " + _activeChar.getName());
			return;
		}
		
		_items = _activeChar.getActiveWarehouse().getItems();
		
		_invSize = player.getInventory().getSize();
		for (L2ItemInstance item : _items)
		{
			if (item.isStackable())
			{
				_itemsStackable.add(item.getDisplayId());
			}
		}
	}
	
	@Override
	protected final void writeImpl()
	{
		switch (ServerTypeConfigs.SERVER_TYPE)
		{
			case IL:
			case GF:
			case EPILOGUE:
			case FREYA:
			case H5:
			case GC:
			case SL:
				writeC(0x42);
				break;
		}
		
		writeH(_whType);
		
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
		
		writeH(_items.length);
		
		switch (ServerTypeConfigs.SERVER_TYPE)
		{
			case GC:
			case SL:
				writeH(_itemsStackable.size());
				for (int itemId : _itemsStackable)
				{
					writeD(itemId);
				}
				writeD(_invSize);
				break;
		}
		
		for (L2ItemInstance item : _items)
		{
			writeItem(item);
			writeD(item.getObjectId());
		}
	}
}
