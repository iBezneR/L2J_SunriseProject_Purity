package l2r.gameserver.network.serverpackets;

import gr.sr.network.handler.ServerTypeConfigs;

public class ExNeedToChangeName extends L2GameServerPacket
{
	private final int _type, _subType;
	private final String _name;
	
	public ExNeedToChangeName(int type, int subType, String name)
	{
		super();
		_type = type;
		_subType = subType;
		_name = name;
	}
	
	@Override
	protected void writeImpl()
	{
		writeC(0xFE);
		
		switch (ServerTypeConfigs.SERVER_TYPE)
		{
			case IL:
				writeH(0x5C);
				break;
			case GF:
			case EPILOGUE:
			case FREYA:
			case H5:
				writeH(0x69);
				break;
			case GC:
			case SL:
				writeH(0x6A);
				break;
		}
		
		writeD(_type);
		writeD(_subType);
		writeS(_name);
	}
}
