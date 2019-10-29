package l2r.gameserver.network.serverpackets;

import gr.sr.network.handler.ServerTypeConfigs;

public final class RestartResponse extends L2GameServerPacket
{
	private static final RestartResponse STATIC_PACKET_TRUE = new RestartResponse(true);
	private static final RestartResponse STATIC_PACKET_FALSE = new RestartResponse(false);
	
	public static final RestartResponse valueOf(boolean result)
	{
		return result ? STATIC_PACKET_TRUE : STATIC_PACKET_FALSE;
	}
	
	private final boolean _result;
	
	public RestartResponse(boolean result)
	{
		_result = result;
	}
	
	@Override
	protected final void writeImpl()
	{
		switch (ServerTypeConfigs.SERVER_TYPE)
		{
			case IL:
				writeC(0x5F);
				break;
			case GF:
			case EPILOGUE:
			case FREYA:
			case H5:
			case GC:
			case SL:
				writeC(0x71);
				break;
		}
		
		writeD(_result ? 1 : 0);
	}
}
