package l2r.gameserver.network.serverpackets;

import gr.sr.network.handler.ServerTypeConfigs;

public class Ex2ndPasswordAck extends L2GameServerPacket
{
	int _response;
	
	public static int SUCCESS = 0x00;
	public static int WRONG_PATTERN = 0x01;
	
	public Ex2ndPasswordAck(int response)
	{
		_response = response;
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
				writeH(0xE7);
				break;
			case GC:
			case SL:
				writeH(0x107);
				break;
		}
		
		writeC(0x00);
		writeD(_response == WRONG_PATTERN ? 0x01 : 0x00);
		writeD(0x00);
	}
}
