package l2r.gameserver.network.serverpackets;

import gr.sr.network.handler.ServerTypeConfigs;

public class ExRequestChangeNicknameColor extends L2GameServerPacket
{
	private final int _itemObjectId;
	
	public ExRequestChangeNicknameColor(int itemObjectId)
	{
		_itemObjectId = itemObjectId;
	}
	
	@Override
	protected final void writeImpl()
	{
		switch (ServerTypeConfigs.SERVER_TYPE)
		{
			case IL:
				return;
		}
		
		writeC(0xFE);
		
		switch (ServerTypeConfigs.SERVER_TYPE)
		{
			case GF:
			case EPILOGUE:
			case FREYA:
			case H5:
				writeH(0x83);
				break;
			case GC:
			case SL:
				writeH(0x84);
				break;
		}
		
		writeD(_itemObjectId);
	}
}