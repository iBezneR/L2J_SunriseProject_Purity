package l2r.gameserver.network.serverpackets;

import gr.sr.network.handler.ServerTypeConfigs;

public class ExNoticePostSent extends L2GameServerPacket
{
	private static final ExNoticePostSent STATIC_PACKET_TRUE = new ExNoticePostSent(true);
	private static final ExNoticePostSent STATIC_PACKET_FALSE = new ExNoticePostSent(false);
	
	public static final ExNoticePostSent valueOf(boolean result)
	{
		return result ? STATIC_PACKET_TRUE : STATIC_PACKET_FALSE;
	}
	
	private final boolean _showAnim;
	
	public ExNoticePostSent(boolean showAnimation)
	{
		_showAnim = showAnimation;
	}
	
	@Override
	protected void writeImpl()
	{
		switch (ServerTypeConfigs.SERVER_TYPE)
		{
			case IL:
			case GF:
				return;
		}
		
		writeC(0xFE);
		
		switch (ServerTypeConfigs.SERVER_TYPE)
		{
			case EPILOGUE:
			case FREYA:
			case H5:
				writeH(0xB4);
				break;
			case GC:
			case SL:
				writeH(0xB5);
				break;
		}
		
		writeD(_showAnim ? 0x01 : 0x00);
	}
}
