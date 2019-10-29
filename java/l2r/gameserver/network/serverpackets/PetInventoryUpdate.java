package l2r.gameserver.network.serverpackets;

import java.util.List;

import l2r.gameserver.model.ItemInfo;
import l2r.gameserver.model.items.instance.L2ItemInstance;

import gr.sr.network.handler.ServerTypeConfigs;

public class PetInventoryUpdate extends AbstractInventoryUpdate
{
	public PetInventoryUpdate()
	{
	}
	
	public PetInventoryUpdate(L2ItemInstance item)
	{
		super(item);
	}
	
	public PetInventoryUpdate(List<ItemInfo> items)
	{
		super(items);
	}
	
	@Override
	protected final void writeImpl()
	{
		switch (ServerTypeConfigs.SERVER_TYPE)
		{
			case IL:
				writeC(0xB3);
				break;
			case GF:
			case EPILOGUE:
			case FREYA:
			case H5:
			case GC:
			case SL:
				writeC(0xB4);
				break;
		}
		
		writeItems();
	}
}
