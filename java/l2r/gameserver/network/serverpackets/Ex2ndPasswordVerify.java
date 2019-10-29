package l2r.gameserver.network.serverpackets;

import gr.sr.network.handler.ServerTypeConfigs;

public class Ex2ndPasswordVerify extends L2GameServerPacket
{
	public static final int PASSWORD_OK = 0x00;
	public static final int PASSWORD_WRONG = 0x01;
	public static final int PASSWORD_BAN = 0x02;
	
	private final int _wrongTentatives, _mode;
	
	public Ex2ndPasswordVerify(int mode, int wrongTentatives)
	{
		_mode = mode;
		_wrongTentatives = wrongTentatives;
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
				writeH(0xE6);
				break;
			case GC:
			case SL:
				writeH(0x106);
				break;
		}
		
		writeD(_mode);
		writeD(_wrongTentatives);
	}
}
