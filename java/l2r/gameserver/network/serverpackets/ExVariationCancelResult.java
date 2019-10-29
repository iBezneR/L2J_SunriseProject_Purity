package l2r.gameserver.network.serverpackets;

import gr.sr.network.handler.ServerTypeConfigs;

public class ExVariationCancelResult extends L2GameServerPacket
{
	private final int _result;
	
	public ExVariationCancelResult(int result)
	{
		_result = result;
	}
	
	@Override
	protected void writeImpl()
	{
		writeC(0xFE);
		
		switch (ServerTypeConfigs.SERVER_TYPE)
		{
			case IL:
				writeH(0x57);
				break;
			case GF:
			case EPILOGUE:
			case FREYA:
			case H5:
				writeH(0x58);
				break;
			case GC:
			case SL:
				writeH(0x59);
				break;
		}
		
		writeD(_result);
	}
}