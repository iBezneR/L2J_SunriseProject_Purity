package l2r.gameserver.network.serverpackets;

import gr.sr.network.handler.ServerTypeConfigs;
import l2r.Config;

public final class KeyPacket extends L2GameServerPacket
{
	private final byte[] _key;
	private final int _id;
	
	public KeyPacket(byte[] key, int id)
	{
		_key = key;
		_id = id;
	}
	
	@Override
	public void writeImpl()
	{
		switch (ServerTypeConfigs.SERVER_TYPE)
		{
			case IL:
				writeC(0x00);
				writeC(0x01);
				writeB(_key);
				writeD(0x01);
				writeD(0x01);
				break;
			case GF:
			case EPILOGUE:
			case FREYA:
			case H5:
			case GC:
			case SL:
				writeC(0x2E);
				writeC(_id); // 0 - wrong protocol, 1 - protocol ok
				for (int i = 0; i < 8; i++)
				{
					writeC(_key[i]); // key
				}
				writeD(0x01);
				writeD(Config.SERVER_ID); // server id
				writeC(0x01);
				writeD(0x00); // obfuscation key
				break;
		}
		
		switch (ServerTypeConfigs.SERVER_TYPE)
		{
			case GC:
				writeC(0); // classic : 1, normal: 0
			case SL:
				writeC(0); // classic : 1, normal: 0
				break;
		}
	}
}
