package l2r.gameserver.network.serverpackets;

import gr.sr.network.handler.ServerTypeConfigs;

public class ShowTownMap extends L2GameServerPacket
{
	private final String _texture;
	private final int _x;
	private final int _y;
	
	public ShowTownMap(String texture, int x, int y)
	{
		_texture = texture;
		_x = x;
		_y = y;
	}
	
	@Override
	protected final void writeImpl()
	{
		switch (ServerTypeConfigs.SERVER_TYPE)
		{
			case IL:
				writeC(0xDE);
				break;
			case GF:
			case EPILOGUE:
			case FREYA:
			case H5:
			case GC:
			case SL:
				writeC(0xEA);
				break;
		}
		
		writeS(_texture);
		writeD(_x);
		writeD(_y);
	}
}
