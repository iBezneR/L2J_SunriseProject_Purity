package l2r.gameserver.network.serverpackets;

import gr.sr.network.handler.ServerTypeConfigs;
import l2r.gameserver.model.actor.instance.L2PcInstance;
import l2r.gameserver.model.items.instance.L2ItemInstance;

public class ExReplyPostItemList extends AbstractItemPacket
{
	L2PcInstance _activeChar;
	private final L2ItemInstance[] _itemList;
	
	public ExReplyPostItemList(L2PcInstance activeChar)
	{
		_activeChar = activeChar;
		_itemList = _activeChar.getInventory().getAvailableItems(true, false, false);
	}
	
	@Override
	protected void writeImpl()
	{
		writeC(0xFE);
		
		switch (ServerTypeConfigs.SERVER_TYPE)
		{
			case IL:
			case GF:
			case EPILOGUE:
			case FREYA:
			case H5:
				writeH(0xB2);
				break;
			case GC:
			case SL:
				writeH(0xB3);
				break;
		}
		
		writeD(_itemList.length);
		for (L2ItemInstance item : _itemList)
		{
			writeItem(item);
		}
	}
}
