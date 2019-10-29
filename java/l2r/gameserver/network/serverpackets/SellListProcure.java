package l2r.gameserver.network.serverpackets;

import gr.sr.network.handler.ServerTypeConfigs;
import l2r.gameserver.instancemanager.CastleManorManager;
import l2r.gameserver.model.CropProcure;
import l2r.gameserver.model.actor.instance.L2PcInstance;
import l2r.gameserver.model.items.instance.L2ItemInstance;

import java.util.HashMap;
import java.util.Map;

public class SellListProcure extends L2GameServerPacket
{
	private final long _money;
	private final Map<L2ItemInstance, Long> _sellList = new HashMap<>();
	
	public SellListProcure(L2PcInstance player, int castleId)
	{
		_money = player.getAdena();
		for (CropProcure c : CastleManorManager.getInstance().getCropProcure(castleId, false))
		{
			final L2ItemInstance item = player.getInventory().getItemByItemId(c.getId());
			if ((item != null) && (c.getAmount() > 0))
			{
				_sellList.put(item, c.getAmount());
			}
		}
	}
	
	@Override
	protected final void writeImpl()
	{
		switch (ServerTypeConfigs.SERVER_TYPE)
		{
			case IL:
				writeC(0xE9);
				
				writeD((int) _money); // money
				writeD(0x00); // lease ?
				writeH(_sellList.size()); // list size
				
				for (L2ItemInstance item : _sellList.keySet())
				{
					long count = _sellList.get(item);
					writeH(item.getItem().getType1());
					writeD(item.getObjectId());
					writeD(item.getDisplayId());
					writeD((int) count); // count
					writeH(item.getItem().getType2());
					writeH(0); // unknown
					writeD(0); // price, u shouldnt get any adena for crops, only raw materials
				}
				break;
			case GF:
			case EPILOGUE:
			case FREYA:
			case H5:
			case GC:
			case SL:
				writeC(0xEF);
				
				writeQ(_money); // money
				writeD(0x00); // lease ?
				writeH(_sellList.size()); // list size
				
				for (L2ItemInstance item : _sellList.keySet())
				{
					writeH(item.getItem().getType1());
					writeD(item.getObjectId());
					writeD(item.getDisplayId());
					writeQ(_sellList.get(item)); // count
					writeH(item.getItem().getType2());
					writeH(0); // unknown
					writeQ(0); // price, u shouldnt get any adena for crops, only raw materials
				}
				break;
		}
	}
}
