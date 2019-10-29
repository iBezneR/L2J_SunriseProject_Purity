package l2r.gameserver.network.serverpackets;

import gr.sr.network.handler.ServerTypeConfigs;
import l2r.gameserver.model.actor.L2Playable;
import l2r.gameserver.model.actor.instance.L2PcInstance;

import java.util.ArrayList;
import java.util.List;

public final class RelationChanged extends L2GameServerPacket
{
	public static final int RELATION_PARTY1 = 0x00001; // party member
	public static final int RELATION_PARTY2 = 0x00002; // party member
	public static final int RELATION_PARTY3 = 0x00004; // party member
	public static final int RELATION_PARTY4 = 0x00008; // party member (for information, see L2PcInstance.getRelation())
	public static final int RELATION_PARTYLEADER = 0x00010; // true if is party leader
	public static final int RELATION_HAS_PARTY = 0x00020; // true if is in party
	public static final int RELATION_CLAN_MEMBER = 0x00040; // true if is in clan
	public static final int RELATION_LEADER = 0x00080; // true if is clan leader
	public static final int RELATION_CLAN_MATE = 0x00100; // true if is in same clan
	public static final int RELATION_INSIEGE = 0x00200; // true if in siege
	public static final int RELATION_ATTACKER = 0x00400; // true when attacker
	public static final int RELATION_ALLY = 0x00800; // blue siege icon, cannot have if red
	public static final int RELATION_ENEMY = 0x01000; // true when red icon, doesn't matter with blue
	public static final int RELATION_MUTUAL_WAR = 0x04000; // double fist
	public static final int RELATION_1SIDED_WAR = 0x08000; // single fist
	public static final int RELATION_ALLY_MEMBER = 0x10000; // clan is in alliance
	public static final int RELATION_TERRITORY_WAR = 0x80000; // show Territory War icon
	
	protected static class Relation
	{
		int _objId, _relation, _autoAttackable, _karma, _pvpFlag;
	}
	
	private Relation _singled;
	private List<Relation> _multi;
	private byte _mask = 0x00;
	
	// Masks
	public static final byte SEND_ONE = 0x00;
	public static final byte SEND_DEFAULT = 0x01;
	public static final byte SEND_MULTI = 0x04;
	
	public RelationChanged(L2Playable activeChar, int relation, boolean autoattackable)
	{
		_mask |= SEND_ONE;
		
		_singled = new Relation();
		_singled._objId = activeChar.getObjectId();
		_singled._relation = relation;
		_singled._autoAttackable = autoattackable ? 1 : 0;
		_singled._karma = activeChar.getKarma();
		_singled._pvpFlag = activeChar.getPvpFlag();
		_invisible = activeChar.isInvisible();
	}
	
	public RelationChanged(L2Playable activeChar, int relation, L2PcInstance attacker)
	{
		_singled = new Relation();
		_singled._objId = activeChar.getObjectId();
		_singled._relation = relation;
		_singled._autoAttackable = activeChar.isAutoAttackable(attacker) ? 1 : 0;
		_singled._karma = activeChar.getKarma();
		_singled._pvpFlag = activeChar.getPvpFlag();
		_invisible = activeChar.isInvisible();
	}

	public static void sendRelationChanged(L2PcInstance target, L2PcInstance attacker)
	{
		if ((target == null) || (attacker == null))
		{
			return;
		}

		int currentRelation = target.getRelation(attacker);

		attacker.sendPacket(new RelationChanged(target, currentRelation, attacker));
		if (target.getSummon() != null)
		{
			attacker.sendPacket(new RelationChanged(target.getSummon(), currentRelation, attacker));
		}
	}

	public RelationChanged()
	{
		_mask |= SEND_MULTI;
		_multi = new ArrayList<>();
	}
	
	public void addRelation(L2Playable activeChar, int relation, boolean autoattackable)
	{
		if (activeChar.isInvisible())
		{
			throw new IllegalArgumentException("Cannot add insivisble character to multi relation packet");
		}
		Relation r = new Relation();
		r._objId = activeChar.getObjectId();
		r._relation = relation;
		r._autoAttackable = autoattackable ? 1 : 0;
		r._karma = activeChar.getKarma();
		r._pvpFlag = activeChar.getPvpFlag();
		_multi.add(r);
	}
	
	@Override
	protected final void writeImpl()
	{
		switch (ServerTypeConfigs.SERVER_TYPE)
		{
			case IL:
			case GF:
			case EPILOGUE:
			case FREYA:
			case H5:
			case GC:
			case SL:
				writeC(0xCE);
				break;
		}
		
		switch (ServerTypeConfigs.SERVER_TYPE)
		{
			case IL:
			case GF:
			case EPILOGUE:
			case FREYA:
			case H5:
				writeD(1);
				break;
			case GC:
			case SL:
				writeC(_mask);
				break;
		}
		
		if (_multi == null)
		{
			writeRelation(_singled);
		}
		else
		{
			switch (ServerTypeConfigs.SERVER_TYPE)
			{
				case IL:
				case GF:
				case EPILOGUE:
				case FREYA:
				case H5:
					writeD(_multi.size());
					break;
				case GC:
				case SL:
					writeH(_multi.size());
					break;
			}
		}
	}
	
	private void writeRelation(Relation relation)
	{
		writeD(relation._objId);
		
		switch (ServerTypeConfigs.SERVER_TYPE)
		{
			case IL:
			case GF:
			case EPILOGUE:
			case FREYA:
			case H5:
				writeD(relation._relation);
				writeD(relation._autoAttackable);
				writeD(relation._karma);
				writeD(relation._pvpFlag);
				break;
			case GC:
			case SL:
				if ((_mask & SEND_DEFAULT) == 0)
				{
					writeD(relation._relation);
					writeC(relation._autoAttackable);
					writeD(relation._karma);
					writeC(relation._pvpFlag);
				}
				break;
		}
	}
}
