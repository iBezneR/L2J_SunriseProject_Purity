package l2r.gameserver.network.serverpackets;

import gr.sr.network.handler.ServerTypeConfigs;

public class ExDuelStart extends L2GameServerPacket
{
	public static final ExDuelStart PLAYER_DUEL = new ExDuelStart(false);
	public static final ExDuelStart PARTY_DUEL = new ExDuelStart(true);
	
	private final boolean _partyDuel;
	
	public ExDuelStart(boolean partyDuel)
	{
		_partyDuel = partyDuel;
	}
	
	@Override
	protected void writeImpl()
	{
		writeC(0xFE);
		
		switch (ServerTypeConfigs.SERVER_TYPE)
		{
			case IL:
				writeH(0x4D);
				break;
			case GF:
			case EPILOGUE:
			case FREYA:
			case H5:
				writeH(0x4E);
				break;
			case GC:
			case SL:
				writeH(0x4F);
				break;
		}
		
		writeD(_partyDuel ? 1 : 0);
	}
}
