package l2r.gameserver.network.serverpackets;

import gr.sr.network.handler.ServerTypeConfigs;
import l2r.Config;

public class ExPledgeCrestLarge extends L2GameServerPacket
{
	private final int _crestId;
	private final int _clanId;
	private final byte[] _data;
	private final int _chunkId;
	private static final int TOTAL_SIZE = 65664;
	
	public ExPledgeCrestLarge(int crestId, byte[] data, int clanId, int chunkId)
	{
		_crestId = crestId;
		_clanId = clanId;
		_data = data;
		_chunkId = chunkId;
	}
	
	@Override
	protected void writeImpl()
	{
		writeC(0xFE);
		
		switch (ServerTypeConfigs.SERVER_TYPE)
		{
			case IL:
				writeH(0x28);
				break;
			case GF:
			case EPILOGUE:
			case FREYA:
			case H5:
			case GC:
			case SL:
				writeH(0x1B);
				break;
		}
		
		switch (ServerTypeConfigs.SERVER_TYPE)
		{
			case GC:
			case SL:
				writeD(Config.SERVER_ID);
				writeD(_clanId);
				break;
		}
		
		switch (ServerTypeConfigs.SERVER_TYPE)
		{
			case IL:
			case GF:
			case EPILOGUE:
			case FREYA:
			case H5:
				writeD(0x00);
				break;
		}
		
		writeD(_crestId);
		
		switch (ServerTypeConfigs.SERVER_TYPE)
		{
			case GC:
			case SL:
				writeD(_chunkId);
				writeD(TOTAL_SIZE);
				break;
		}
		
		if (_data != null)
		{
			writeD(_data.length);
			writeB(_data);
		}
		else
		{
			writeD(0);
		}
	}
}