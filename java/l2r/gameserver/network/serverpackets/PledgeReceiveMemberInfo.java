package l2r.gameserver.network.serverpackets;

import gr.sr.network.handler.ServerTypeConfigs;
import l2r.gameserver.model.L2ClanMember;

public class PledgeReceiveMemberInfo extends L2GameServerPacket
{
	private final L2ClanMember _member;
	
	public PledgeReceiveMemberInfo(L2ClanMember member)
	{
		_member = member;
	}
	
	@Override
	protected void writeImpl()
	{
		writeC(0xFE);
		
		switch (ServerTypeConfigs.SERVER_TYPE)
		{
			case IL:
				writeH(0x3D);
				break;
			case GF:
			case EPILOGUE:
			case FREYA:
			case H5:
				writeH(0x3E);
				break;
			case GC:
			case SL:
				writeH(0x3F);
				break;
		}
		
		writeD(_member.getPledgeType());
		writeS(_member.getName());
		writeS(_member.getTitle()); // title
		writeD(_member.getPowerGrade()); // power
		
		// clan or subpledge name
		if (_member.getPledgeType() != 0)
		{
			writeS((_member.getClan().getSubPledge(_member.getPledgeType())).getName());
		}
		else
		{
			writeS(_member.getClan().getName());
		}
		
		writeS(_member.getApprenticeOrSponsorName()); // name of this member's apprentice/sponsor
	}
}
