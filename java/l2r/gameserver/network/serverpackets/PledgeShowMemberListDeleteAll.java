package l2r.gameserver.network.serverpackets;

import gr.sr.network.handler.ServerTypeConfigs;

public class PledgeShowMemberListDeleteAll extends L2GameServerPacket
{
	public static final PledgeShowMemberListDeleteAll STATIC_PACKET = new PledgeShowMemberListDeleteAll();
	
	private PledgeShowMemberListDeleteAll()
	{
	
	}
	
	@Override
	protected final void writeImpl()
	{
		switch (ServerTypeConfigs.SERVER_TYPE)
		{
			case IL:
				writeC(0x82);
				break;
			case GF:
			case EPILOGUE:
			case FREYA:
			case H5:
			case GC:
			case SL:
				writeC(0x88);
				break;
		}
	}
}
