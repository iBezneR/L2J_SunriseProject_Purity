package l2r.gameserver.network.serverpackets;

import gr.sr.network.handler.ServerTypeConfigs;
import l2r.Config;
import l2r.gameserver.model.L2Clan;
import l2r.gameserver.model.L2Clan.SubPledge;
import l2r.gameserver.model.L2ClanMember;
import l2r.gameserver.model.actor.instance.L2PcInstance;

public class PledgeShowMemberListAll extends L2GameServerPacket
{
	private final L2Clan _clan;
	private final L2PcInstance _activeChar;
	private final L2ClanMember[] _members;
	private int _pledgeType;
	
	public PledgeShowMemberListAll(L2Clan clan, L2PcInstance activeChar)
	{
		_clan = clan;
		_activeChar = activeChar;
		_members = _clan.getMembers();
	}
	
	@Override
	protected final void writeImpl()
	{
		_pledgeType = 0;
		// FIXME: That's wrong on retail sends this whole packet few times (depending how much sub pledges it has)
		writePledge(0);
		
		for (SubPledge subPledge : _clan.getAllSubPledges())
		{
			_activeChar.sendPacket(new PledgeReceiveSubPledgeCreated(subPledge, _clan));
		}
		
		for (L2ClanMember m : _members)
		{
			if (m.getPledgeType() == 0)
			{
				continue;
			}
			_activeChar.sendPacket(new PledgeShowMemberListAdd(m));
		}
		
		// unless this is sent sometimes, the client doesn't recognise the player as the leader
		_activeChar.sendUserInfo(true);
	}
	
	private void writePledge(int mainOrSubpledge)
	{
		switch (ServerTypeConfigs.SERVER_TYPE)
		{
			case IL:
				writeC(0x53);
				break;
			case GF:
			case EPILOGUE:
			case FREYA:
			case H5:
			case GC:
			case SL:
				writeC(0x5A);
				break;
		}
		
		writeD(mainOrSubpledge);
		writeD(_clan.getId());
		
		switch (ServerTypeConfigs.SERVER_TYPE)
		{
			case GC:
			case SL:
				writeD(Config.SERVER_ID);
				break;
		}
		
		writeD(_pledgeType);
		writeS(_clan.getName());
		writeS(_clan.getLeaderName());
		
		writeD(_clan.getCrestId()); // crest id .. is used again
		writeD(_clan.getLevel());
		writeD(_clan.getCastleId());
		
		switch (ServerTypeConfigs.SERVER_TYPE)
		{
			case GC:
			case SL:
				writeD(0x00); // castle state
				break;
		}
		
		writeD(_clan.getHideoutId());
		writeD(_clan.getFortId());
		writeD(_clan.getRank());
		writeD(_clan.getReputationScore());
		writeD(0x00); // 0
		writeD(0x00); // 0
		writeD(_clan.getAllyId());
		writeS(_clan.getAllyName());
		writeD(_clan.getAllyCrestId());
		writeD(_clan.isAtWar() ? 1 : 0);// new c3
		writeD(0x00); // Territory castle ID
		writeD(_clan.getSubPledgeMembersCount(_pledgeType));
		
		for (L2ClanMember m : _members)
		{
			if (m.getPledgeType() != _pledgeType)
			{
				continue;
			}
			writeS(m.getName());
			writeD(m.getLevel());
			writeD(m.getClassId());
			L2PcInstance player;
			if ((player = m.getPlayerInstance()) != null)
			{
				writeD(player.getAppearance().getSex() ? 1 : 0); // no visible effect
				writeD(player.getRace().ordinal());// writeD(1);
			}
			else
			{
				writeD(0x01); // no visible effect
				writeD(0x01); // writeD(1);
			}
			writeD(m.isOnline() ? m.getObjectId() : 0); // objectId = online 0 = offline
			writeD(m.getSponsor() != 0 ? 1 : 0);
			
			switch (ServerTypeConfigs.SERVER_TYPE)
			{
				case GC:
				case SL:
					writeC(1); // m.getOnlineStatus()
					break;
			}
		}
	}
}
