package l2r.gameserver.network.serverpackets;

import gr.sr.network.handler.ServerTypeConfigs;
import l2r.Config;
import l2r.gameserver.model.actor.instance.L2PcInstance;
import l2r.gameserver.model.stats.Stats;

public class ExStorageMaxCount extends L2GameServerPacket
{
	private final L2PcInstance _activeChar;
	private final int _inventory;
	private final int _warehouse;
	private final int _clan;
	private final int _privateSell;
	private final int _privateBuy;
	private final int _receipeD;
	private final int _recipe;
	private final int _inventoryExtraSlots;
	private final int _inventoryQuestItems;
	
	public ExStorageMaxCount(L2PcInstance character)
	{
		_activeChar = character;
		_inventory = _activeChar.getInventoryLimit();
		_warehouse = _activeChar.getWareHouseLimit();
		_privateSell = _activeChar.getPrivateSellStoreLimit();
		_privateBuy = _activeChar.getPrivateBuyStoreLimit();
		_clan = Config.WAREHOUSE_SLOTS_CLAN;
		_receipeD = _activeChar.getDwarfRecipeLimit();
		_recipe = _activeChar.getCommonRecipeLimit();
		_inventoryExtraSlots = (int) _activeChar.getStat().calcStat(Stats.INV_LIM, 0, null, null);
		_inventoryQuestItems = Config.INVENTORY_MAXIMUM_QUEST_ITEMS;
	}
	
	@Override
	protected void writeImpl()
	{
		writeC(0xFE);
		
		switch (ServerTypeConfigs.SERVER_TYPE)
		{
			case IL:
				writeH(0x2E);
				break;
			case GF:
			case EPILOGUE:
			case FREYA:
			case H5:
			case GC:
			case SL:
				writeH(0x2F);
				break;
		}
		
		writeD(_inventory);
		writeD(_warehouse);
		writeD(_clan);
		writeD(_privateSell);
		writeD(_privateBuy);
		writeD(_receipeD);
		writeD(_recipe);
		writeD(_inventoryExtraSlots); // Belt inventory slots increase count
		writeD(_inventoryQuestItems);
		
		switch (ServerTypeConfigs.SERVER_TYPE)
		{
			case GC:
			case SL:
				writeD(40); // TODO: Find me!
				writeD(40); // TODO: Find me!
				break;
		}
	}
}