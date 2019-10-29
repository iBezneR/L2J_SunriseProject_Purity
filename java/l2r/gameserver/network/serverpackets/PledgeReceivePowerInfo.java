package l2r.gameserver.network.serverpackets;

import gr.sr.network.handler.ServerTypeConfigs;
import l2r.gameserver.model.L2ClanMember;

public class PledgeReceivePowerInfo extends L2GameServerPacket
{
	private final L2ClanMember _member;
	
	public PledgeReceivePowerInfo(L2ClanMember member)
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
				writeH(0x3C);
				break;
			case GF:
			case EPILOGUE:
			case FREYA:
			case H5:
				writeH(0x3D);
				break;
			case GC:
			case SL:
				writeH(0x3E);
				break;
		}
		
		writeD(_member.getPowerGrade()); // power grade
		writeS(_member.getName());
		writeD(_member.getClan().getRankPrivs(_member.getPowerGrade()).getBitmask()); // privileges
	}
}
