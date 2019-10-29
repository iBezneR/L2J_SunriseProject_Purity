package l2r.gameserver.network.serverpackets;

import gr.sr.network.handler.ServerTypeConfigs;
import l2r.gameserver.model.items.instance.L2ItemInstance;

public class DropItem extends L2GameServerPacket
{
	private final L2ItemInstance _item;
	private final int _charObjId;
	
	public DropItem(L2ItemInstance item, int playerObjId)
	{
		_item = item;
		_charObjId = playerObjId;
	}
	
	@Override
	protected final void writeImpl()
	{
		switch (ServerTypeConfigs.SERVER_TYPE)
		{
			case IL:
				writeC(0x0C);
				break;
			case GF:
			case EPILOGUE:
			case FREYA:
			case H5:
			case GC:
			case SL:
				writeC(0x16);
				break;
		}
		
		writeD(_charObjId);
		writeD(_item.getObjectId());
		writeD(_item.getDisplayId());
		
		writeD(_item.getX());
		writeD(_item.getY());
		writeD(_item.getZ());
		
		// only show item count if it is a stackable item
		switch (ServerTypeConfigs.SERVER_TYPE)
		{
			case IL:
			case GF:
			case EPILOGUE:
			case FREYA:
			case H5:
				writeD(_item.isStackable() ? 0x01 : 0x00);
				break;
			case GC:
			case SL:
				writeC(_item.isStackable() ? 0x01 : 0x00);
				break;
		}
		
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
		
		switch (ServerTypeConfigs.SERVER_TYPE)
		{
			case IL:
			case GF:
			case EPILOGUE:
			case FREYA:
			case H5:
				writeD(0x01); // unknown
				break;
			case GC:
			case SL:
				writeC(0x00);
				// writeD(0x01); if above C == true (1) then packet.readD()
				
				writeC(_item.getEnchantLevel()); // Grand Crusade
				writeC(_item.getAugmentation() != null ? 1 : 0); // Grand Crusade
				writeC(0/* _item.getSpecialAbilities().size() */); // Grand Crusade
				break;
		}
	}
}
