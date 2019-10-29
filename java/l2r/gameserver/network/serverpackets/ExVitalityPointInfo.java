package l2r.gameserver.network.serverpackets;

import gr.sr.network.handler.ServerTypeConfigs;

public class ExVitalityPointInfo extends L2GameServerPacket
{
	private final int _vitalityPoints;
	
	public ExVitalityPointInfo(int vitPoints)
	{
		_vitalityPoints = vitPoints;
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
				writeH(0xA0);
				break;
			case GC:
			case SL:
				writeH(0xA1);
				break;
		}
		
		writeD(_vitalityPoints);
	}
}
