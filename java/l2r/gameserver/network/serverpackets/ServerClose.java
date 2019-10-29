package l2r.gameserver.network.serverpackets;

import gr.sr.network.handler.ServerTypeConfigs;

public class ServerClose extends L2GameServerPacket
{
	public static final ServerClose STATIC_PACKET = new ServerClose();
	
	private ServerClose()
	{
	}
	
	@Override
	protected void writeImpl()
	{
		switch (ServerTypeConfigs.SERVER_TYPE)
		{
			case IL:
				writeC(0x26);
				break;
			case GF:
			case EPILOGUE:
			case FREYA:
			case H5:
			case GC:
			case SL:
				writeC(0x20);
				break;
		}
	}
}
