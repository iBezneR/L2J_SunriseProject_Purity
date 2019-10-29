package l2r.gameserver.network.serverpackets;

import gr.sr.network.handler.ServerTypeConfigs;

public class Ex2ndPasswordCheck extends L2GameServerPacket
{
	public static final int PASSWORD_NEW = 0x00;
	public static final int PASSWORD_PROMPT = 0x01;
	public static final int PASSWORD_OK = 0x02;
	
	private final int _windowType;
	
	public Ex2ndPasswordCheck(int windowType)
	{
		_windowType = windowType;
	}
	
	@Override
	protected void writeImpl()
	{
		switch (ServerTypeConfigs.SERVER_TYPE)
		{
			case IL:
			case GF:
			case EPILOGUE:
			case FREYA:
				return;
		}
		
		writeC(0xFE);
		
		switch (ServerTypeConfigs.SERVER_TYPE)
		{
			case H5:
				writeH(0xE5);
				break;
			case GC:
			case SL:
				writeH(0x105);
				break;
		}
		
		writeD(_windowType);
		writeD(0x00);
	}
}
