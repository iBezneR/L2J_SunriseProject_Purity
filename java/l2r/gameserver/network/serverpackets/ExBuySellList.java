package l2r.gameserver.network.serverpackets;

import gr.sr.network.handler.ServerTypeConfigs;
import l2r.gameserver.model.actor.instance.L2PcInstance;
import l2r.gameserver.model.items.instance.L2ItemInstance;

public class ExBuySellList extends AbstractItemPacket
{
	private L2ItemInstance[] _sellList = null;
	private L2ItemInstance[] _refundList = null;
	private final boolean _done;
	private final int _inventorySlots;
	
	public ExBuySellList(L2PcInstance player, double taxRate, boolean done)
	{
		_sellList = player.getInventory().getAvailableItems(false, false, false);
		_inventorySlots = player.getInventory().getItemsWithoutQuest().size();
		if (player.hasRefund())
		{
			_refundList = player.getRefund().getItems();
		}
		_done = done;
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
		
		writeD(0x01);
		
		switch (ServerTypeConfigs.SERVER_TYPE)
		{
			case GC:
			case SL:
				writeD(_inventorySlots);
				break;
		}
		
		if ((_sellList != null) && (_sellList.length > 0))
		{
			writeH(_sellList.length);
			for (L2ItemInstance item : _sellList)
			{
				writeItem(item);
				writeQ(item.getItem().getReferencePrice() / 2);
			}
		}
		else
		{
			writeH(0x00);
		}
		
		if ((_refundList != null) && (_refundList.length > 0))
		{
			writeH(_refundList.length);
			int i = 0;
			for (L2ItemInstance item : _refundList)
			{
				writeItem(item);
				writeD(i++);
				writeQ((item.getItem().getReferencePrice() / 2) * item.getCount());
			}
		}
		else
		{
			writeH(0x00);
		}
		
		writeC(_done ? 0x01 : 0x00);
	}
}