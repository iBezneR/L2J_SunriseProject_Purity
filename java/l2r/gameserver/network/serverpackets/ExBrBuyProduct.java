package l2r.gameserver.network.serverpackets;

import gr.sr.network.handler.ServerTypeConfigs;

public class ExBrBuyProduct extends L2GameServerPacket
{
	public static final int RESULT_OK = 1;
	public static final int RESULT_NOT_ENOUGH_POINTS = -1;
	public static final int RESULT_WRONG_PRODUCT = -2;
	public static final int RESULT_INVENTORY_FULL = -4;
	public static final int RESULT_SALE_PERIOD_ENDED = -7;
	public static final int RESULT_WRONG_USER_STATE = -9;
	public static final int RESULT_WRONG_PRODUCT_ITEM = -10;
	
	private final int _result;
	
	public ExBrBuyProduct(int result)
	{
		_result = result;
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
				writeH(0xAA);
				break;
			case EPILOGUE:
				writeH(0xBB);
				break;
			case FREYA:
				writeH(0xCC);
				break;
			case H5:
				writeH(0xD8);
				break;
			case GC:
			case SL:
				writeH(0xD9);
				break;
		}
		
		writeD(_result);
	}
}