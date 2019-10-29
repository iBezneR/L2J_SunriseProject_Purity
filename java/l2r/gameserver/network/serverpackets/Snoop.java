package l2r.gameserver.network.serverpackets;

import gr.sr.network.handler.ServerTypeConfigs;

public class Snoop extends L2GameServerPacket
{
	private final int _convoId;
	private final String _name;
	private final int _type;
	private final String _speaker;
	private final String _msg;
	
	public Snoop(int id, String name, int type, String speaker, String msg)
	{
		_convoId = id;
		_name = name;
		_type = type;
		_speaker = speaker;
		_msg = msg;
	}
	
	@Override
	protected void writeImpl()
	{
		switch (ServerTypeConfigs.SERVER_TYPE)
		{
			case IL:
				writeC(0xD5);
				break;
			case GF:
			case EPILOGUE:
			case FREYA:
			case H5:
			case GC:
			case SL:
				writeC(0xDB);
				break;
		}
		
		writeD(_convoId);
		writeS(_name);
		writeD(0x00); // ??
		writeD(_type);
		writeS(_speaker);
		writeS(_msg);
	}
}