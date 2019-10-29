package l2r.gameserver.network.serverpackets;

import gr.sr.network.handler.ServerTypeConfigs;
import l2r.gameserver.model.items.instance.L2ItemInstance;

public final class SpawnItem extends L2GameServerPacket
{
	private final L2ItemInstance _item;
	
	public SpawnItem(L2ItemInstance item)
	{
		_item = item;
	}
	
	@Override
	protected final void writeImpl()
	{
		switch (ServerTypeConfigs.SERVER_TYPE)
		{
			case IL:
				writeC(0x0B);
				break;
			case GF:
			case EPILOGUE:
			case FREYA:
			case H5:
			case GC:
			case SL:
				writeC(0x05);
				break;
		}
		
		writeD(_item.getObjectId());
		writeD(_item.getDisplayId());
		writeD(_item.getX());
		writeD(_item.getY());
		writeD(_item.getZ());
		// only show item count if it is a stackable item
		writeD(_item.isStackable() ? 0x01 : 0x00);
		
		switch (ServerTypeConfigs.SERVER_TYPE)
		{
			case IL:
				writeD((int) _item.getCount());
				break;
			case GF:
			case EPILOGUE:
			case FREYA:
			case H5:
			case GC:
			case SL:
				writeQ(_item.getCount());
				break;
		}
		
		writeD(0x00); // c2
		
		switch (ServerTypeConfigs.SERVER_TYPE)
		{
			case GF:
			case EPILOGUE:
			case FREYA:
			case H5:
				writeD(0x00); // freya unk
				break;
		}
		
		switch (ServerTypeConfigs.SERVER_TYPE)
		{
			case GC:
			case SL:
				writeC(_item.getEnchantLevel()); // Grand Crusade
				writeC(_item.getAugmentation() != null ? 1 : 0); // Grand Crusade
				writeC(0 /* _item.getSpecialAbilities().size() */); // Grand Crusade
				break;
		}
	}
}
