package l2r.gameserver.network.serverpackets;

import gr.sr.network.handler.ServerTypeConfigs;

public class PledgeShowMemberListDelete extends L2GameServerPacket
{
	private final String _player;
	
	public PledgeShowMemberListDelete(String playerName)
	{
		_player = playerName;
	}
	
	@Override
	protected final void writeImpl()
	{
		switch (ServerTypeConfigs.SERVER_TYPE)
		{
			case IL:
				writeC(0x56);
				break;
			case GF:
			case EPILOGUE:
			case FREYA:
			case H5:
			case GC:
			case SL:
				writeC(0x5D);
				break;
		}
		
		writeS(_player);
	}
}
