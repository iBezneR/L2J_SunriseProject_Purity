package l2r.gameserver.network.serverpackets;

import gr.sr.network.handler.ServerTypeConfigs;

public class ExDuelEnd extends L2GameServerPacket
{
	public static final ExDuelEnd PLAYER_DUEL = new ExDuelEnd(false);
	public static final ExDuelEnd PARTY_DUEL = new ExDuelEnd(true);
	
	private final int _partyDuel;
	
	private ExDuelEnd(boolean isPartyDuel)
	{
		_partyDuel = isPartyDuel ? 1 : 0;
	}
	
	@Override
	protected void writeImpl()
	{
		writeC(0xFE);
		
		switch (ServerTypeConfigs.SERVER_TYPE)
		{
			case IL:
				writeH(0x4E);
				break;
			case GF:
			case EPILOGUE:
			case FREYA:
			case H5:
				writeH(0x4F);
				break;
			case GC:
			case SL:
				writeH(0x50);
				break;
		}
		
		writeD(_partyDuel);
	}
}
