package l2r.gameserver.network.serverpackets;

import gr.sr.network.handler.ServerTypeConfigs;

public final class SurrenderPledgeWar extends L2GameServerPacket
{
	private final String _pledgeName;
	private final String _playerName;
	
	public SurrenderPledgeWar(String pledge, String charName)
	{
		_pledgeName = pledge;
		_playerName = charName;
	}
	
	@Override
	protected final void writeImpl()
	{
		switch (ServerTypeConfigs.SERVER_TYPE)
		{
			case IL:
				writeC(0x69);
				break;
			case GF:
			case EPILOGUE:
			case FREYA:
			case H5:
			case GC:
			case SL:
				writeC(0x67);
				break;
		}
		
		writeS(_pledgeName);
		writeS(_playerName);
	}
}