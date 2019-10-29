package l2r.gameserver.network.serverpackets;

import gr.sr.network.handler.ServerTypeConfigs;

public class ExSetCompassZoneCode extends L2GameServerPacket
{
	public static final int ALTEREDZONE = 0x08;
	public static final int SIEGEWARZONE1 = 0x0A;
	public static final int SIEGEWARZONE2 = 0x0B;
	public static final int PEACEZONE = 0x0C;
	public static final int SEVENSIGNSZONE = 0x0D;
	public static final int PVPZONE = 0x0E;
	public static final int GENERALZONE = 0x0F;
	public static final int CHAOTICZONE = 0x09;
	
	private final int _zoneType;
	
	public ExSetCompassZoneCode(int val)
	{
		_zoneType = val;
	}
	
	@Override
	protected void writeImpl()
	{
		writeC(0xFE);
		
		switch (ServerTypeConfigs.SERVER_TYPE)
		{
			case IL:
				writeH(0x32);
				break;
			case GF:
			case EPILOGUE:
			case FREYA:
			case H5:
			case GC:
			case SL:
				writeH(0x33);
				break;
		}
		
		writeD(_zoneType);
	}
}
