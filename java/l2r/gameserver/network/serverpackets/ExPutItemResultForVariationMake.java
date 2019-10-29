package l2r.gameserver.network.serverpackets;

import gr.sr.network.handler.ServerTypeConfigs;

public class ExPutItemResultForVariationMake extends L2GameServerPacket
{
	private final int _itemObjId;
	private final int _itemId;
	
	public ExPutItemResultForVariationMake(int itemObjId, int itemId)
	{
		_itemObjId = itemObjId;
		_itemId = itemId;
	}
	
	@Override
	protected void writeImpl()
	{
		writeC(0xFE);
		
		switch (ServerTypeConfigs.SERVER_TYPE)
		{
			case IL:
				writeH(0x52);
				break;
			case GF:
			case EPILOGUE:
			case FREYA:
			case H5:
				writeH(0x53);
				break;
			case GC:
			case SL:
				writeH(0x54);
				break;
		}
		
		writeD(_itemObjId);
		writeD(_itemId);
		writeD(0x01);
	}
}
