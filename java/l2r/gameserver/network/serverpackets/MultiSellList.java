package l2r.gameserver.network.serverpackets;

import static l2r.gameserver.data.xml.impl.MultisellData.PAGE_SIZE;

import l2r.gameserver.model.multisell.Entry;
import l2r.gameserver.model.multisell.Ingredient;
import l2r.gameserver.model.multisell.ListContainer;

import gr.sr.network.handler.ServerTypeConfigs;

public final class MultiSellList extends AbstractItemPacket
{
	private int _size;
	private int _index;
	private final ListContainer _list;
	private final boolean _finished;
	
	public MultiSellList(ListContainer list, int index)
	{
		_list = list;
		_index = index;
		_size = list.getEntries().size() - index;
		if (_size > PAGE_SIZE)
		{
			_finished = false;
			_size = PAGE_SIZE;
		}
		else
		{
			_finished = true;
		}
	}
	
	@Override
	protected void writeImpl()
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
				writeC(0xD0);
				break;
		}
		
		switch (ServerTypeConfigs.SERVER_TYPE)
		{
			case GC:
			case SL:
				writeC(0x00); // Helios
				break;
		}
		
		writeD(_list.getListId()); // list id
		
		switch (ServerTypeConfigs.SERVER_TYPE)
		{
			case GC:
			case SL:
				writeC(0x00); // Helios
				break;
		}
		
		writeD(1 + (_index / PAGE_SIZE)); // page started from 1
		writeD(_finished ? 1 : 0); // finished
		writeD(PAGE_SIZE); // size of pages
		writeD(_size); // list length
		
		switch (ServerTypeConfigs.SERVER_TYPE)
		{
			case GC:
			case SL:
				writeC(0x00); // Grand crusade
				writeC(0x00); // _list.isChanceMultisell() new multisell window
				writeD(0x20); // Helios - Always 32
				break;
		}
		
		Entry ent;
		while (_size-- > 0)
		{
			ent = _list.getEntries().get(_index++);
			
			switch (ServerTypeConfigs.SERVER_TYPE)
			{
				case IL:
					writeD(ent.getEntryId());
					writeD(0x00); // C6
					writeD(0x00); // C6
					writeC(1);
					break;
				case GF:
				case EPILOGUE:
				case FREYA:
				case H5:
				case GC:
				case SL:
					writeD(ent.getEntryId());
					writeC(ent.isStackable() ? 1 : 0);
					
					writeH(0x00); // enchant level
					
					writeD(0x00); // augment id
					writeD(0x00); // mana
					
					writeItemElemental(null);
					writeItemEnsoulOptions(null);
					break;
			}
			
			writeH(ent.getProducts().size());
			writeH(ent.getIngredients().size());
			
			for (Ingredient ing : ent.getProducts())
			{
				switch (ServerTypeConfigs.SERVER_TYPE)
				{
					case IL:
						writeH(ing.getId());
						writeD(ing.getTemplate() != null ? ing.getTemplate().getBodyPart() : 0);
						writeH(ing.getTemplate() != null ? ing.getTemplate().getType2() : 65535);
						writeD((int) ing.getItemCount());
						break;
					case GF:
					case EPILOGUE:
					case FREYA:
					case H5:
						writeD(ing.getId());
						writeD(ing.getTemplate() != null ? ing.getTemplate().getBodyPart() : 0);
						writeH(ing.getTemplate() != null ? ing.getTemplate().getType2() : 65535);
						writeQ(ing.getItemCount());
						break;
					case GC:
					case SL:
						writeD(ing.getId());
						writeQ(ing.getTemplate() != null ? ing.getTemplate().getBodyPart() : 0);
						writeH(ing.getTemplate() != null ? ing.getTemplate().getType2() : 65535);
						writeQ(ing.getItemCount());
						break;
				}
				
				writeH(ing.getItemInfo() != null ? ing.getItemInfo().getEnchantLevel() : ing.getEnchantLevel()); // enchant level
				
				switch (ServerTypeConfigs.SERVER_TYPE)
				{
					case IL:
						writeD(0x00); // C6
						writeD(0x00); // C6
						continue;
					case GF:
					case EPILOGUE:
					case FREYA:
					case H5:
						writeD(ing.getItemInfo() != null ? ing.getItemInfo().getAugmentId() : 0); // augment id
						writeD(ing.getItemInfo() != null ? ing.getItemInfo().getMana() : 0); // mana
						break;
					case GC:
					case SL:
						writeD(0x00); // chance
						
						writeD(ing.getItemInfo() != null ? ing.getItemInfo().getAugmentId() : 0); // augment id
						writeD(ing.getItemInfo() != null ? ing.getItemInfo().getAugmentId() : 0); // augment id
						break;
				}
				
				writeH(ing.getItemInfo() != null ? ing.getItemInfo().getElementId() : 0); // attack element
				writeH(ing.getItemInfo() != null ? ing.getItemInfo().getElementPower() : 0); // element power
				writeH(ing.getItemInfo() != null ? ing.getItemInfo().getElementals()[0] : 0); // fire
				writeH(ing.getItemInfo() != null ? ing.getItemInfo().getElementals()[1] : 0); // water
				writeH(ing.getItemInfo() != null ? ing.getItemInfo().getElementals()[2] : 0); // wind
				writeH(ing.getItemInfo() != null ? ing.getItemInfo().getElementals()[3] : 0); // earth
				writeH(ing.getItemInfo() != null ? ing.getItemInfo().getElementals()[4] : 0); // holy
				writeH(ing.getItemInfo() != null ? ing.getItemInfo().getElementals()[5] : 0); // dark
				
				writeItemEnsoulOptions(null);
			}
			
			for (Ingredient ing : ent.getIngredients())
			{
				switch (ServerTypeConfigs.SERVER_TYPE)
				{
					case IL:
						writeH(ing.getId());
						writeH(ing.getTemplate() != null ? ing.getTemplate().getType2() : 65535);
						writeD((int) ing.getItemCount());
						break;
					case GF:
					case EPILOGUE:
					case FREYA:
					case H5:
					case GC:
					case SL:
						writeD(ing.getId());
						writeH(ing.getTemplate() != null ? ing.getTemplate().getType2() : 65535);
						writeQ(ing.getItemCount());
						break;
				}
				
				writeH(ing.getItemInfo() != null ? ing.getItemInfo().getEnchantLevel() : 0); // enchant level
				
				switch (ServerTypeConfigs.SERVER_TYPE)
				{
					case IL:
						writeD(0x00); // C6
						writeD(0x00); // C6
						continue;
					case GF:
					case EPILOGUE:
					case FREYA:
					case H5:
						writeD(ing.getItemInfo() != null ? ing.getItemInfo().getAugmentId() : 0); // augment id
						writeD(ing.getItemInfo() != null ? ing.getItemInfo().getMana() : 0); // mana
						break;
					case GC:
					case SL:
						writeD(ing.getItemInfo() != null ? ing.getItemInfo().getAugmentId() : 0); // augment id
						writeD(ing.getItemInfo() != null ? ing.getItemInfo().getAugmentId() : 0); // augment id
						break;
				}
				
				writeH(ing.getItemInfo() != null ? ing.getItemInfo().getElementId() : 0); // attack element
				writeH(ing.getItemInfo() != null ? ing.getItemInfo().getElementPower() : 0); // element power
				writeH(ing.getItemInfo() != null ? ing.getItemInfo().getElementals()[0] : 0); // fire
				writeH(ing.getItemInfo() != null ? ing.getItemInfo().getElementals()[1] : 0); // water
				writeH(ing.getItemInfo() != null ? ing.getItemInfo().getElementals()[2] : 0); // wind
				writeH(ing.getItemInfo() != null ? ing.getItemInfo().getElementals()[3] : 0); // earth
				writeH(ing.getItemInfo() != null ? ing.getItemInfo().getElementals()[4] : 0); // holy
				writeH(ing.getItemInfo() != null ? ing.getItemInfo().getElementals()[5] : 0); // dark
				
				writeItemEnsoulOptions(null);
			}
		}
	}
}
