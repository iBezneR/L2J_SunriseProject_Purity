package l2r.gameserver.network.serverpackets;

import gr.sr.network.handler.ServerTypeConfigs;
import l2r.Config;
import l2r.gameserver.enums.PcCondOverride;
import l2r.gameserver.model.actor.instance.L2PcInstance;
import l2r.gameserver.model.items.instance.L2ItemInstance;

public final class TradeStart extends AbstractItemPacket
{
	private final L2PcInstance _activeChar;
	private final L2ItemInstance[] _itemList;
	private final int _mask = 0;
	
	public TradeStart(L2PcInstance player)
	{
		_activeChar = player;
		_itemList = _activeChar.getInventory().getAvailableItems(true, (_activeChar.canOverrideCond(PcCondOverride.ITEM_CONDITIONS) && Config.GM_TRADE_RESTRICTED_ITEMS), false);
	}
	
	@Override
	protected final void writeImpl()
	{
		if ((_activeChar.getActiveTradeList() == null) || (_activeChar.getActiveTradeList().getPartner() == null))
		{
			return;
		}
		
		switch (ServerTypeConfigs.SERVER_TYPE)
		{
			case IL:
				writeC(0x1E);
				break;
			case GF:
			case EPILOGUE:
			case FREYA:
			case H5:
			case GC:
			case SL:
				writeC(0x14);
				break;
		}
		
		writeD(_activeChar.getActiveTradeList().getPartner().getObjectId());
		
		switch (ServerTypeConfigs.SERVER_TYPE)
		{
			case GC:
			case SL:
				writeC(_mask); // some kind of mask
				writeC(_activeChar.getActiveTradeList().getPartner().getLevel());
				break;
		}
		
		writeH(_itemList.length);
		
		for (L2ItemInstance item : _itemList)
		{
			writeItem(item);
		}
	}
}
