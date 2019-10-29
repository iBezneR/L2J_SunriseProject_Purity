package l2r.gameserver.network.serverpackets;

import gr.sr.network.handler.ServerTypeConfigs;

public class ExOlympiadMode extends L2GameServerPacket
{
	private final int _mode;
	
	public ExOlympiadMode(int mode)
	{
		_mode = mode;
	}
	
	@Override
	protected final void writeImpl()
	{
		writeC(0xFE);
		
		switch (ServerTypeConfigs.SERVER_TYPE)
		{
			case IL:
				writeH(0x2B);
				break;
			case GF:
			case EPILOGUE:
			case FREYA:
			case H5:
				writeH(0x7C);
				break;
			case GC:
			case SL:
				writeH(0x7D);
				break;
		}
		
		writeC(_mode);
	}
}
