package l2r.gameserver.network.serverpackets;

import gr.sr.network.handler.ServerTypeConfigs;

public class ExNoticePostArrived extends L2GameServerPacket
{
	private static final ExNoticePostArrived STATIC_PACKET_TRUE = new ExNoticePostArrived(true);
	private static final ExNoticePostArrived STATIC_PACKET_FALSE = new ExNoticePostArrived(false);
	
	public static final ExNoticePostArrived valueOf(boolean result)
	{
		return result ? STATIC_PACKET_TRUE : STATIC_PACKET_FALSE;
	}
	
	private final boolean _showAnim;
	
	public ExNoticePostArrived(boolean showAnimation)
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
				writeH(0xA9);
				break;
			case GC:
			case SL:
				writeH(0xAA);
				break;
		}
		
		writeD(_showAnim ? 0x01 : 0x00);
	}
}
