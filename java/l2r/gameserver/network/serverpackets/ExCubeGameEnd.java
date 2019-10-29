package l2r.gameserver.network.serverpackets;

import gr.sr.network.handler.ServerTypeConfigs;

public class ExCubeGameEnd extends L2GameServerPacket
{
	boolean _isRedTeamWin;
	
	public ExCubeGameEnd(boolean isRedTeamWin)
	{
		_isRedTeamWin = isRedTeamWin;
	}
	
	@Override
	protected void writeImpl()
	{
		switch (ServerTypeConfigs.SERVER_TYPE)
		{
			case IL:
				return;
		}
		
		writeC(0xFE);
		
		switch (ServerTypeConfigs.SERVER_TYPE)
		{
			case GF:
			case EPILOGUE:
			case FREYA:
			case H5:
				writeH(0x98);
				break;
			case GC:
			case SL:
				writeH(0x99);
				break;
		}
		
		writeD(0x01);
		
		writeD(_isRedTeamWin ? 0x01 : 0x00);
		
		switch (ServerTypeConfigs.SERVER_TYPE)
		{
			case GC:
			case SL:
				writeD(0x00); // TODO: Find me!
				break;
		}
	}
}