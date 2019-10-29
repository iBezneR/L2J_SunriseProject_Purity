package l2r.gameserver.network.serverpackets;

import gr.sr.network.handler.ServerTypeConfigs;
import l2r.gameserver.model.actor.instance.L2PcInstance;
import l2r.gameserver.model.items.L2Henna;

public class HennaRemoveList extends L2GameServerPacket
{
	private final L2PcInstance _player;
	
	public HennaRemoveList(L2PcInstance player)
	{
		_player = player;
	}
	
	@Override
	protected final void writeImpl()
	{
		switch (ServerTypeConfigs.SERVER_TYPE)
		{
			case IL:
				writeC(0xE5);
				break;
			case GF:
			case EPILOGUE:
			case FREYA:
			case H5:
			case GC:
			case SL:
				writeC(0xE6);
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
		
		switch (ServerTypeConfigs.SERVER_TYPE)
		{
			case IL:
				writeD(0x03);
				writeD(_player.getHennaEx().getHennaList().length);
				break;
			case GF:
			case EPILOGUE:
			case FREYA:
			case H5:
				writeD(0x03);
				writeD(3 - _player.getHennaEx().getHennaEmptySlots());
				break;
			case GC:
			case SL:
				final boolean premiumSlotEnabled = _player.getHennaEx().getHenna(4) != null;
				writeD(premiumSlotEnabled ? 0x04 : 0x03); // seems to be max size
				writeD((premiumSlotEnabled ? 4 : 3) - _player.getHennaEx().getHennaEmptySlots()); // slots used
				break;
		}
		
		for (L2Henna henna : _player.getHennaEx().getHennaList())
		{
			if (henna != null)
			{
				switch (ServerTypeConfigs.SERVER_TYPE)
				{
					case IL:
						writeD(henna.getDyeId());
						writeD(henna.getDyeItemId());
						writeD(henna.getCancelCount());
						writeD(henna.getCancelFee());
						writeD(0x01);
						break;
					case GF:
					case EPILOGUE:
					case FREYA:
					case H5:
						writeD(henna.getDyeId());
						writeD(henna.getDyeItemId());
						writeD(henna.getCancelCount());
						writeD(0x00);
						writeD(henna.getCancelFee());
						writeD(0x00);
						writeD(0x01);
						break;
					case GC:
					case SL:
						writeD(henna.getDyeId());
						writeD(henna.getDyeItemId());
						writeQ(henna.getCancelCount());
						writeQ(henna.getCancelFee());
						writeD(0x00);
						writeD(0x00);
						break;
				}
			}
		}
	}
}
