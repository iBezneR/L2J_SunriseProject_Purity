package l2r.gameserver.network.serverpackets;

import gr.sr.network.handler.ServerTypeConfigs;

public class ExShowQuestMark extends L2GameServerPacket
{
	private final int _questId;
	private final int _questState;
	
	public ExShowQuestMark(int questId, int questState)
	{
		_questId = questId;
		_questState = questState;
	}
	
	@Override
	protected void writeImpl()
	{
		writeC(0xFE);
		
		switch (ServerTypeConfigs.SERVER_TYPE)
		{
			case IL:
				writeH(0x1A);
				break;
			case GF:
			case EPILOGUE:
			case FREYA:
			case H5:
			case GC:
			case SL:
				writeH(0x21);
				break;
		}
		
		writeD(_questId);
		
		switch (ServerTypeConfigs.SERVER_TYPE)
		{
			case GC:
			case SL:
				writeD(_questState);
				break;
		}
	}
}
