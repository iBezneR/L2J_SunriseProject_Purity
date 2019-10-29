package l2r.gameserver.network.serverpackets;

import gr.sr.network.handler.ServerTypeConfigs;
import l2r.gameserver.model.items.L2Item;
import l2r.gameserver.model.items.instance.L2ItemInstance;

public final class EquipUpdate extends L2GameServerPacket
{
	private final L2ItemInstance _item;
	private final int _change;
	
	public EquipUpdate(L2ItemInstance item, int change)
	{
		_item = item;
		_change = change;
	}
	
	@Override
	protected final void writeImpl()
	{
		switch (ServerTypeConfigs.SERVER_TYPE)
		{
			case GC:
			case SL:
				break;
		}
		
		switch (ServerTypeConfigs.SERVER_TYPE)
		{
			case IL:
			case GF:
			case EPILOGUE:
			case FREYA:
			case H5:
				writeC(0x4B);
				break;
		}
		
		int bodypart = 0;
		writeD(_change);
		writeD(_item.getObjectId());
		switch (_item.getItem().getBodyPart())
		{
			case L2Item.SLOT_L_EAR:
				bodypart = 0x01;
				break;
			case L2Item.SLOT_R_EAR:
				bodypart = 0x02;
				break;
			case L2Item.SLOT_NECK:
				bodypart = 0x03;
				break;
			case L2Item.SLOT_R_FINGER:
				bodypart = 0x04;
				break;
			case L2Item.SLOT_L_FINGER:
				bodypart = 0x05;
				break;
			case L2Item.SLOT_HEAD:
				bodypart = 0x06;
				break;
			case L2Item.SLOT_R_HAND:
				bodypart = 0x07;
				break;
			case L2Item.SLOT_L_HAND:
				bodypart = 0x08;
				break;
			case L2Item.SLOT_GLOVES:
				bodypart = 0x09;
				break;
			case L2Item.SLOT_CHEST:
				bodypart = 0x0a;
				break;
			case L2Item.SLOT_LEGS:
				bodypart = 0x0b;
				break;
			case L2Item.SLOT_FEET:
				bodypart = 0x0c;
				break;
			case L2Item.SLOT_BACK:
				bodypart = 0x0d;
				break;
			case L2Item.SLOT_LR_HAND:
				bodypart = 0x0e;
				break;
			case L2Item.SLOT_HAIR:
				bodypart = 0x0f;
				break;
			case L2Item.SLOT_BELT:
				bodypart = 0x10;
				break;
		}
		writeD(bodypart);
	}
}
