/*
 * Copyright (C) L2J Sunrise
 * This file is part of L2J Sunrise
 */
package l2r.gameserver.network.serverpackets;

import gr.sr.network.handler.ServerTypeConfigs;
import l2r.gameserver.model.Elementals;
import l2r.gameserver.model.ItemInfo;
import l2r.gameserver.model.TradeItem;
import l2r.gameserver.model.buylist.Product;
import l2r.gameserver.model.itemcontainer.PcInventory;
import l2r.gameserver.model.items.instance.L2ItemInstance;
import l2r.gameserver.network.handlers.types.ItemListType;

/**
 * @author vGodFather
 */
public abstract class AbstractItemPacket extends AbstractMaskPacket<ItemListType>
{
	private static final byte[] MASKS =
	{
		0x00
	};
	
	@Override
	protected byte[] getMasks()
	{
		return MASKS;
	}
	
	protected void writeItem(TradeItem item, boolean isTrade)
	{
		writeItem(new ItemInfo(item), isTrade);
	}
	
	protected void writeItem(TradeItem item)
	{
		writeItem(new ItemInfo(item), false);
	}
	
	protected void writeItem(L2ItemInstance item)
	{
		writeItem(new ItemInfo(item));
	}
	
	protected void writeItem(Product item)
	{
		writeItem(new ItemInfo(item));
	}
	
	protected void writeItem(ItemInfo item)
	{
		writeItem(item, false);
	}
	
	protected void writeItem(ItemInfo item, boolean isTrade)
	{
		switch (ServerTypeConfigs.SERVER_TYPE)
		{
			case GC:
			case SL:
				final int mask = calculateMask(item);
				writeC(mask);
				writeD(item.getObjectId()); // ObjectId
				writeD(item.getItem().getDisplayId()); // ItemId
				writeC(item.getItem().isQuestItem() || (item.getEquipped() == 1) ? 0xFF : item.getLocation()); // T1
				writeQ(item.getCount()); // Quantity
				writeC(item.getItem().getType2()); // Item Type 2 : 00-weapon, 01-shield/armor, 02-ring/earring/necklace, 03-questitem, 04-adena, 05-item
				writeC(item.getCustomType1()); // Filler (always 0)
				writeH(item.getEquipped()); // Equipped : 00-No, 01-yes
				writeQ(item.getItem().getBodyPart()); // Slot : 0006-lr.ear, 0008-neck, 0030-lr.finger, 0040-head, 0100-l.hand, 0200-gloves, 0400-chest, 0800-pants, 1000-feet, 4000-r.hand, 8000-r.hand
				writeC(item.getEnchant()); // Enchant level (pet level shown in control item)
				writeC(0x01); // TODO : Find me
				writeD(item.getMana());
				writeD(item.getTime());
				writeC(1); // item.isAvailable() ? 1 : 0 GOD Item enabled = 1 disabled (red) = 0
				if (containsMask(mask, ItemListType.AUGMENT_BONUS))
				{
					writeItemAugment(item);
				}
				if (containsMask(mask, ItemListType.ELEMENTAL_ATTRIBUTE))
				{
					writeItemElemental(item);
				}
				if (containsMask(mask, ItemListType.ENCHANT_EFFECT))
				{
					writeItemEnchantEffect(item);
				}
				// if (containsMask(mask, ItemListType.VISUAL_ID))
				// {
				// writeD(item.getVisualId()); // Item remodel visual ID
				// }
				// if (containsMask(mask, ItemListType.SOUL_CRYSTAL))
				// {
				// writeItemEnsoulOptions(item);
				// }
				return;
		}
		
		switch (ServerTypeConfigs.SERVER_TYPE)
		{
			case IL:
			case GF:
			case EPILOGUE:
				writeH(item.getItem().getType1()); // Item Type 1
				break;
		}
		
		writeD(item.getObjectId());
		writeD(item.getItem().getDisplayId());
		
		if (!isTrade)
		{
			switch (ServerTypeConfigs.SERVER_TYPE)
			{
				case GF:
				case EPILOGUE:
				case FREYA:
				case H5:
					writeD(item.getLocation());
					break;
			}
		}
		
		switch (ServerTypeConfigs.SERVER_TYPE)
		{
			case IL:
				writeD((int) item.getCount());
				break;
			case GF:
			case EPILOGUE:
			case FREYA:
			case H5:
				writeQ(item.getCount());
				break;
		}
		
		writeH(item.getItem().getType2()); // item type2
		writeH(item.getCustomType1()); // item type3
		
		if (!isTrade)
		{
			writeH(item.getEquipped());
		}
		
		writeD(item.getItem().getBodyPart());
		writeH(item.getEnchant()); // enchant level
		writeH(item.getCustomType2()); // item type3
		
		if (!isTrade)
		{
			writeD(item.getAugmentationBonus());
			writeD(item.getMana());
		}
		
		switch (ServerTypeConfigs.SERVER_TYPE)
		{
			case IL:
				return;
		}
		
		if (!isTrade)
		{
			switch (ServerTypeConfigs.SERVER_TYPE)
			{
				case FREYA:
				case H5:
					writeD(item.getTime());
					break;
			}
		}
		
		writeItemElemental(item);
		
		switch (ServerTypeConfigs.SERVER_TYPE)
		{
			case GF:
			case EPILOGUE:
				writeD(item.getTime());
				break;
		}
		
		switch (ServerTypeConfigs.SERVER_TYPE)
		{
			case EPILOGUE:
			case FREYA:
			case H5:
				writeItemEnchantEffect(item);
				break;
		}
	}
	
	protected void writeInventoryBlock(PcInventory inventory)
	{
		switch (ServerTypeConfigs.SERVER_TYPE)
		{
			case IL:
				return;
		}
		
		if (inventory.hasInventoryBlock())
		{
			writeH(inventory.getBlockItems().length);
			writeC(inventory.getBlockMode());
			for (int i : inventory.getBlockItems())
			{
				writeD(i);
			}
		}
		else
		{
			writeH(0x00);
		}
	}
	
	protected void writeItemAugment(ItemInfo item)
	{
		writeD(item == null ? 0x00 : item.getAugmentationBonus());
		writeD(item == null ? 0x00 : item.getAugmentationBonus());
	}
	
	protected void writeItemElemental(ItemInfo item)
	{
		writeH(item == null ? 0x00 : item.getAttackElementType());
		writeH(item == null ? 0x00 : item.getAttackElementPower());
		writeH(item == null ? 0x00 : item.getElementDefAttr(Elementals.FIRE));
		writeH(item == null ? 0x00 : item.getElementDefAttr(Elementals.WATER));
		writeH(item == null ? 0x00 : item.getElementDefAttr(Elementals.WIND));
		writeH(item == null ? 0x00 : item.getElementDefAttr(Elementals.EARTH));
		writeH(item == null ? 0x00 : item.getElementDefAttr(Elementals.HOLY));
		writeH(item == null ? 0x00 : item.getElementDefAttr(Elementals.DARK));
	}
	
	protected void writeItemEnchantEffect(ItemInfo item)
	{
		switch (ServerTypeConfigs.SERVER_TYPE)
		{
			case IL:
			case GF:
			case EPILOGUE:
			case FREYA:
			case H5:
				for (int op : item.getEnchantOptions())
				{
					writeH(op);
				}
				break;
			case GC:
			case SL:
				for (int op : item.getEnchantOptions())
				{
					writeD(op);
				}
				break;
		}
	}
	
	protected void writeItemEnsoulOptions(ItemInfo item)
	{
		switch (ServerTypeConfigs.SERVER_TYPE)
		{
			case IL:
			case EPILOGUE:
			case FREYA:
			case H5:
				return;
		}
		
		writeC(0); // Size of regular soul crystal options.
		writeC(0); // Size of special soul crystal options.
	}
	
	protected static int calculateMask(ItemInfo item)
	{
		int mask = 0;
		if (item.getAugmentationBonus() > 0)
		{
			mask |= ItemListType.AUGMENT_BONUS.getMask();
		}
		
		if ((item.getAttackElementType() >= 0) || (item.getElementDefAttr(Elementals.FIRE) > 0) || (item.getElementDefAttr(Elementals.WATER) > 0) || (item.getElementDefAttr(Elementals.WIND) > 0) || (item.getElementDefAttr(Elementals.EARTH) > 0) || (item.getElementDefAttr(Elementals.HOLY) > 0) || (item.getElementDefAttr(Elementals.DARK) > 0))
		{
			mask |= ItemListType.ELEMENTAL_ATTRIBUTE.getMask();
		}
		
		if (item.getEnchantOptions() != null)
		{
			for (int id : item.getEnchantOptions())
			{
				if (id > 0)
				{
					mask |= ItemListType.ENCHANT_EFFECT.getMask();
					break;
				}
			}
		}
		
		// if (item.getVisualId() > 0)
		// {
		// mask |= ItemListType.VISUAL_ID.getMask();
		// }
		
		// if (((item.getSoulCrystalOptions() != null) && !item.getSoulCrystalOptions().isEmpty()) || ((item.getSoulCrystalSpecialOptions() != null) && !item.getSoulCrystalSpecialOptions().isEmpty()))
		// {
		// mask |= ItemListType.SOUL_CRYSTAL.getMask();
		// }
		
		return mask;
	}
}
