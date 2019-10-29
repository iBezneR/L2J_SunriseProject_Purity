package l2r.gameserver.network.serverpackets;

import gr.sr.network.handler.ServerTypeConfigs;

public final class LeaveWorld extends L2GameServerPacket
{
	public static final LeaveWorld STATIC_PACKET = new LeaveWorld();
	
	private LeaveWorld()
	{
	}
	
	@Override
	protected final void writeImpl()
	{
		switch (ServerTypeConfigs.SERVER_TYPE)
		{
			case IL:
				writeC(0x7E);
				break;
			case GF:
			case EPILOGUE:
			case FREYA:
			case H5:
			case GC:
			case SL:
				writeC(0x84);
				break;
		}
	}
}
