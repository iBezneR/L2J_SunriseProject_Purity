package l2r.gameserver.network.serverpackets;

import gr.sr.network.handler.ServerTypeConfigs;

public final class CSShowComBoard extends L2GameServerPacket
{
	private final byte[] _html;
	
	public CSShowComBoard(final byte[] html)
	{
		_html = html;
	}
	
	@Override
	protected void writeImpl()
	{
		switch (ServerTypeConfigs.SERVER_TYPE)
		{
			case IL:
			case GF:
			case EPILOGUE:
			case FREYA:
			case H5:
			case GC:
			case SL:
				writeC(0x7B);
				break;
		}
		
		writeC(0x01); // c4 1 to show community 00 to hide
		writeB(_html);
	}
}
