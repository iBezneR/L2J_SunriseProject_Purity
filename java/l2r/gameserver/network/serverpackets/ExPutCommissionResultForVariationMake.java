package l2r.gameserver.network.serverpackets;

import gr.sr.network.handler.ServerTypeConfigs;

public class ExPutCommissionResultForVariationMake extends L2GameServerPacket
{
	private final int _gemstoneObjId;
	private final int _itemId;
	private final long _gemstoneCount;
	private final int _unk1;
	private final int _unk2;
	private final int _unk3;
	
	public ExPutCommissionResultForVariationMake(int gemstoneObjId, long count, int itemId)
	{
		_gemstoneObjId = gemstoneObjId;
		_itemId = itemId;
		_gemstoneCount = count;
		_unk1 = 0;
		_unk2 = 0;
		_unk3 = 1;
	}
	
	@Override
	protected void writeImpl()
	{
		writeC(0xFE);
		
		switch (ServerTypeConfigs.SERVER_TYPE)
		{
			case IL:
				writeH(0x54);
				break;
			case GF:
			case EPILOGUE:
			case FREYA:
			case H5:
				writeH(0x55);
				break;
			case GC:
			case SL:
				writeH(0x56);
				break;
		}
		
		switch (ServerTypeConfigs.SERVER_TYPE)
		{
			case IL:
				break;
			case GF:
			case EPILOGUE:
			case FREYA:
			case H5:
				writeD(_gemstoneObjId);
				writeD(_itemId);
				writeQ(_gemstoneCount);
				writeD(_unk1);
				writeD(_unk2);
				writeD(_unk3);
				break;
			case GC:
			case SL:
				writeD(_gemstoneObjId);
				writeD(_itemId);
				writeQ(_gemstoneCount);
				writeQ(_unk1);
				writeD(_unk2);
				break;
		}
	}
}
