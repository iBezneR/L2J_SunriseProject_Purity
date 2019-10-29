package l2r.gameserver.network.serverpackets;

import java.util.List;

import l2r.gameserver.data.xml.impl.HennaData;
import l2r.gameserver.model.actor.instance.L2PcInstance;
import l2r.gameserver.model.items.L2Henna;

import gr.sr.network.handler.ServerTypeConfigs;

public class HennaEquipList extends L2GameServerPacket
{
	private final L2PcInstance _player;
	private final List<L2Henna> _hennaEquipList;
	
	public HennaEquipList(L2PcInstance player)
	{
		_player = player;
		_hennaEquipList = HennaData.getInstance().getHennaList(player.getClassId());
	}
	
	public HennaEquipList(L2PcInstance player, List<L2Henna> list)
	{
		_player = player;
		_hennaEquipList = list;
	}
	
	@Override
	protected final void writeImpl()
	{
		switch (ServerTypeConfigs.SERVER_TYPE)
		{
			case IL:
				writeC(0xE2);
				break;
			case GF:
			case EPILOGUE:
			case FREYA:
			case H5:
			case GC:
			case SL:
				writeC(0xEE);
				break;
		}
		
		switch (ServerTypeConfigs.SERVER_TYPE)
		{
			case IL:
				writeD((int) _player.getAdena());
				break;
			case GF:
			case EPILOGUE:
			case FREYA:
			case H5:
			case GC:
			case SL:
				writeQ(_player.getAdena());
				break;
		}
		
		writeD(3); // available equip slot
		writeD(_hennaEquipList.size());
		
		for (L2Henna henna : _hennaEquipList)
		{
			// Player must have at least one dye in inventory
			// to be able to see the Henna that can be applied with it.
			if ((_player.getInventory().getItemByItemId(henna.getDyeItemId())) != null)
			{
				switch (ServerTypeConfigs.SERVER_TYPE)
				{
					case IL:
						writeD(henna.getDyeId()); // dye Id
						writeD(henna.getDyeItemId()); // itemid of dye
						writeD(henna.getWearCount()); // amount of dye require
						writeD(henna.getWearFee()); // amount of adena require
						writeD(1); // meet the requirement or not
						break;
					case GF:
					case EPILOGUE:
					case FREYA:
					case H5:
					case GC:
					case SL:
						writeD(henna.getDyeId()); // dye Id
						writeD(henna.getDyeItemId()); // item Id of the dye
						writeQ(henna.getWearCount()); // amount of dyes required
						writeQ(henna.getWearFee()); // amount of Adena required
						writeD(henna.isAllowedClass(_player.getClassId()) ? 0x01 : 0x00); // meet the requirement or not
						break;
				}
				
				switch (ServerTypeConfigs.SERVER_TYPE)
				{
					case GC:
					case SL:
						writeD(0x00);
						break;
				}
			}
		}
	}
}
