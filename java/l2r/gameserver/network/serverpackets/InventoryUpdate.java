package l2r.gameserver.network.serverpackets;

import gr.sr.network.handler.ServerTypeConfigs;
import l2r.gameserver.model.ItemInfo;
import l2r.gameserver.model.items.instance.L2ItemInstance;

import java.util.List;

public class InventoryUpdate extends AbstractInventoryUpdate
{
	public InventoryUpdate()
	{
	}
	
	public InventoryUpdate(L2ItemInstance item)
	{
		super(item);
	}
	
	public InventoryUpdate(List<ItemInfo> items)
	{
		super(items);
	}
	
	@Override
	protected final void writeImpl()
	{
		switch (ServerTypeConfigs.SERVER_TYPE)
		{
			case IL:
				writeC(0x27);
				break;
			case GF:
			case EPILOGUE:
			case FREYA:
			case H5:
			case GC:
			case SL:
				writeC(0x21);
				break;
		}
		
		writeItems();
	}
}
