package l2r.gameserver.network.serverpackets;

import gr.sr.network.handler.ServerTypeConfigs;
import l2r.gameserver.model.L2ClanMember;
import l2r.gameserver.model.actor.instance.L2PcInstance;

public final class PledgeShowMemberListAdd extends L2GameServerPacket
{
	private final String _name;
	private final int _lvl;
	private final int _classId;
	private final int _isOnline;
	private final int _pledgeType;
	
	public PledgeShowMemberListAdd(L2PcInstance player)
	{
		_name = player.getName();
		_lvl = player.getLevel();
		_classId = player.getClassId().getId();
		_isOnline = (player.isOnline() ? player.getObjectId() : 0);
		_pledgeType = player.getPledgeType();
	}
	
	public PledgeShowMemberListAdd(L2ClanMember cm)
	{
		_name = cm.getName();
		_lvl = cm.getLevel();
		_classId = cm.getClassId();
		_isOnline = (cm.isOnline() ? cm.getObjectId() : 0);
		_pledgeType = cm.getPledgeType();
	}
	
	@Override
	protected final void writeImpl()
	{
		switch (ServerTypeConfigs.SERVER_TYPE)
		{
			case IL:
				writeC(0x55);
				break;
			case GF:
			case EPILOGUE:
			case FREYA:
			case H5:
			case GC:
			case SL:
				writeC(0x5C);
				break;
		}
		
		writeS(_name);
		writeD(_lvl);
		writeD(_classId);
		writeD(0x00);
		writeD(0x01);
		writeD(_isOnline); // 1 = online 0 = offline
		writeD(_pledgeType);
	}
}
