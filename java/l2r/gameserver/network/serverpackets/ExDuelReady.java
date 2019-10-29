package l2r.gameserver.network.serverpackets;

import gr.sr.network.handler.ServerTypeConfigs;

public class ExDuelReady extends L2GameServerPacket
{
	public static final ExDuelReady PLAYER_DUEL = new ExDuelReady(false);
	public static final ExDuelReady PARTY_DUEL = new ExDuelReady(true);
	
	private final boolean _partyDuel;
	
	public ExDuelReady(boolean partyDuel)
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
				writeH(0x4C);
				break;
			case GF:
			case EPILOGUE:
			case FREYA:
			case H5:
				writeH(0x4D);
				break;
			case GC:
			case SL:
				writeH(0x4E);
				break;
		}
		
		writeD(_partyDuel ? 1 : 0);
	}
}
