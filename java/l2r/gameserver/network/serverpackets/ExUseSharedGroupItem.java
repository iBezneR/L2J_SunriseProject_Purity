package l2r.gameserver.network.serverpackets;

import gr.sr.network.handler.ServerTypeConfigs;

public class ExUseSharedGroupItem extends L2GameServerPacket
{
	private final int _itemId;
	private final int _grpId;
	private final int _remainingTime;
	private final int _totalTime;
	
	public ExUseSharedGroupItem(int itemId, int grpId, long remainingTime, int totalTime)
	{
		_itemId = itemId;
		_grpId = grpId;
		_remainingTime = (int) (remainingTime / 1000);
		_totalTime = totalTime / 1000;
	}
	
	@Override
	protected void writeImpl()
	{
		writeC(0xFE);
		
		switch (ServerTypeConfigs.SERVER_TYPE)
		{
			case IL:
				writeH(0x49);
				break;
			case GF:
			case EPILOGUE:
			case FREYA:
			case H5:
				writeH(0x4A);
				break;
			case GC:
			case SL:
				writeH(0x4B);
				break;
		}
		
		writeD(_itemId);
		writeD(_grpId);
		writeD(_remainingTime);
		writeD(_totalTime);
	}
}
