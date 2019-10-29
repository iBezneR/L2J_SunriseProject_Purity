package l2r.gameserver.network.serverpackets;

import gr.sr.network.handler.ServerTypeConfigs;

public final class TutorialShowQuestionMark extends L2GameServerPacket
{
	private final int _markId;
	
	public TutorialShowQuestionMark(int blink)
	{
		_markId = blink;
	}
	
	@Override
	protected void writeImpl()
	{
		switch (ServerTypeConfigs.SERVER_TYPE)
		{
			case IL:
				writeC(0xA1);
				break;
			case GF:
			case EPILOGUE:
			case FREYA:
			case H5:
			case GC:
			case SL:
				writeC(0xA7);
				break;
		}
		
		switch (ServerTypeConfigs.SERVER_TYPE)
		{
			case GC:
			case SL:
				writeC(0x01); // marktype most of the cases 1
				break;
		}
		
		writeD(_markId);
	}
}