package l2r.gameserver.network.serverpackets;

import gr.sr.network.handler.ServerTypeConfigs;
import l2r.gameserver.model.L2Clan;
import l2r.gameserver.model.L2ClanMember;
import l2r.gameserver.model.actor.instance.L2PcInstance;

public final class PledgeShowMemberListUpdate extends L2GameServerPacket
{
	private final int _pledgeType;
	private int _hasSponsor;
	private final String _name;
	private final int _level;
	private final int _classId;
	private final int _objectId;
	private final boolean _isOnline;
	private final int _race;
	private final int _sex;
	
	public PledgeShowMemberListUpdate(L2PcInstance player)
	{
		_pledgeType = player.getPledgeType();
		if (_pledgeType == L2Clan.SUBUNIT_ACADEMY)
		{
			_hasSponsor = player.getSponsor() != 0 ? 1 : 0;
		}
		else
		{
			_hasSponsor = 0;
		}
		_name = player.getName();
		_level = player.getLevel();
		_classId = player.getClassId().getId();
		_race = player.getRace().ordinal();
		_sex = player.getAppearance().getSex() ? 1 : 0;
		_objectId = player.getObjectId();
		_isOnline = player.isOnline();
	}
	
	public PledgeShowMemberListUpdate(L2ClanMember member)
	{
		_name = member.getName();
		_level = member.getLevel();
		_classId = member.getClassId();
		_objectId = member.getObjectId();
		_isOnline = member.isOnline();
		_pledgeType = member.getPledgeType();
		_race = member.getRaceOrdinal();
		_sex = member.getSex() ? 1 : 0;
		if (_pledgeType == L2Clan.SUBUNIT_ACADEMY)
		{
			_hasSponsor = member.getSponsor() != 0 ? 1 : 0;
		}
		else
		{
			_hasSponsor = 0;
		}
	}
	
	@Override
	protected final void writeImpl()
	{
		switch (ServerTypeConfigs.SERVER_TYPE)
		{
			case IL:
				writeC(0x54);
				break;
			case GF:
			case EPILOGUE:
			case FREYA:
			case H5:
			case GC:
			case SL:
				writeC(0x5B);
				break;
		}
		
		writeS(_name);
		writeD(_level);
		writeD(_classId);
		writeD(_sex);
		writeD(_race);
		writeD(_isOnline ? _objectId : 0);
		writeD(_isOnline ? _pledgeType : 0);
		writeD(_hasSponsor);
		
		switch (ServerTypeConfigs.SERVER_TYPE)
		{
			case GC:
			case SL:
				writeC(_isOnline ? 1 : 0);
				break;
		}
	}
}
