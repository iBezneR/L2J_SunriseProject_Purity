package l2r.gameserver.network.serverpackets;

import gr.sr.network.handler.ServerTypeConfigs;
import l2r.gameserver.enums.ZoneIdType;
import l2r.gameserver.model.actor.L2Summon;
import l2r.gameserver.model.actor.instance.L2PetInstance;
import l2r.gameserver.model.actor.instance.L2ServitorInstance;
import l2r.gameserver.model.skills.VisualEffect;
import l2r.gameserver.taskmanager.AttackStanceTaskManager;

import java.util.Set;

public class PetInfo extends L2GameServerPacket
{
	private final L2Summon _summon;
	private final int _x, _y, _z, _heading;
	private final boolean _isSummoned;
	private final int _val;
	private final int _mAtkSpd, _pAtkSpd;
	private final int _runSpd, _walkSpd;
	private final int _swimRunSpd, _swimWalkSpd;
	private final int _flyRunSpd, _flyWalkSpd;
	private final double _moveMultiplier;
	private final int _maxHp, _maxMp;
	private int _maxFed, _curFed;
	private int _statusMask = 0;
	
	public PetInfo(L2Summon summon, int val)
	{
		_summon = summon;
		_isSummoned = summon.isShowSummonAnimation();
		_x = summon.getX();
		_y = summon.getY();
		_z = summon.getZ();
		_heading = summon.getHeading();
		_mAtkSpd = summon.getMAtkSpd();
		_pAtkSpd = (int) summon.getPAtkSpd();
		_moveMultiplier = summon.getMovementSpeedMultiplier();
		_runSpd = (int) Math.round(summon.getRunSpeed() / _moveMultiplier);
		_walkSpd = (int) Math.round(summon.getWalkSpeed() / _moveMultiplier);
		_swimRunSpd = (int) Math.round(summon.getSwimRunSpeed() / _moveMultiplier);
		_swimWalkSpd = (int) Math.round(summon.getSwimWalkSpeed() / _moveMultiplier);
		_flyRunSpd = summon.isFlying() ? _runSpd : 0;
		_flyWalkSpd = summon.isFlying() ? _walkSpd : 0;
		_maxHp = summon.getMaxHp();
		_maxMp = summon.getMaxMp();
		_val = val;
		if (summon.isPet())
		{
			final L2PetInstance pet = (L2PetInstance) _summon;
			_curFed = pet.getCurrentFed(); // how fed it is
			_maxFed = pet.getMaxFed(); // max fed it can be
		}
		else if (summon.isServitor())
		{
			final L2ServitorInstance sum = (L2ServitorInstance) _summon;
			_curFed = sum.getTimeRemaining();
			_maxFed = sum.getTotalLifeTime();
		}
		
		if (summon.isBetrayed())
		{
			_statusMask |= 0x01; // Auto attackable status
		}
		_statusMask |= 0x02; // can be chatted with
		
		if (summon.isRunning())
		{
			_statusMask |= 0x04;
		}
		if (AttackStanceTaskManager.getInstance().hasAttackStanceTask(summon))
		{
			_statusMask |= 0x08;
		}
		if (summon.isDead())
		{
			_statusMask |= 0x10;
		}
		if (summon.isMountable())
		{
			_statusMask |= 0x20;
		}
	}
	
	@Override
	protected final void writeImpl()
	{
		switch (ServerTypeConfigs.SERVER_TYPE)
		{
			case IL:
				writeC(0xB1);
				break;
			case GF:
			case EPILOGUE:
			case FREYA:
			case H5:
			case GC:
			case SL:
				writeC(0xB2);
				break;
		}
		
		switch (ServerTypeConfigs.SERVER_TYPE)
		{
			case GC:
			case SL:
				writeC(_summon.getSummonType());
				writeD(_summon.getObjectId());
				writeD(_summon.getTemplate().getDisplayId() + 1000000);
				
				writeD(_summon.getX());
				writeD(_summon.getY());
				writeD(_summon.getZ());
				writeD(_summon.getHeading());
				
				writeD(_summon.getStat().getMAtkSpd());
				writeD((int) _summon.getStat().getPAtkSpd());
				
				writeH(_runSpd);
				writeH(_walkSpd);
				writeH(_swimRunSpd);
				writeH(_swimWalkSpd);
				writeH(_flyRunSpd);
				writeH(_flyWalkSpd);
				writeH(_flyRunSpd);
				writeH(_flyWalkSpd);
				
				writeF(_moveMultiplier);
				writeF(_summon.getAttackSpeedMultiplier()); // attack speed multiplier
				writeF(_summon.getTemplate().getfCollisionRadius());
				writeF(_summon.getTemplate().getfCollisionHeight());
				
				writeD(_summon.getWeapon()); // right hand weapon
				writeD(_summon.getArmor()); // body armor
				writeD(0x00); // left hand weapon
				
				writeC(_summon.isShowSummonAnimation() ? 0x02 : _val); // 0=teleported 1=default 2=summoned
				writeD(-1); // High Five NPCString ID
				if (_summon.isPet())
				{
					writeS(_summon.getName()); // Pet name.
				}
				else
				{
					writeS(_summon.getTemplate().isUsingServerSideName() ? _summon.getName() : ""); // Summon name.
				}
				writeD(-1); // High Five NPCString ID
				writeS(_summon.getTitle()); // owner name
				
				writeC(_summon.getPvpFlag()); // confirmed
				writeD(_summon.getKarma()); // confirmed
				
				writeD(_curFed); // how fed it is
				writeD(_maxFed); // max fed it can be
				writeD((int) _summon.getCurrentHp()); // current hp
				writeD(_summon.getMaxHp()); // max hp
				writeD((int) _summon.getCurrentMp()); // current mp
				writeD(_summon.getMaxMp()); // max mp
				
				writeQ(_summon.getStat().getSp()); // sp
				writeC(_summon.getLevel()); // lvl
				writeQ(_summon.getStat().getExp());
				
				if (_summon.getExpForThisLevel() > _summon.getStat().getExp())
				{
					writeQ(_summon.getStat().getExp()); // 0% absolute value
				}
				else
				{
					writeQ(_summon.getExpForThisLevel()); // 0% absolute value
				}
				
				writeQ(_summon.getExpForNextLevel()); // 100% absoulte value
				
				writeD(_summon.isPet() ? _summon.getInventory().getTotalWeight() : 0); // weight
				writeD(_summon.getMaxLoad()); // max weight it can carry
				writeD((int) _summon.getPAtk(null)); // patk
				writeD((int) _summon.getPDef(null)); // pdef
				writeD(_summon.getAccuracy()); // accuracy
				writeD(_summon.getEvasionRate(null)); // evasion
				writeD(_summon.getCriticalHit(null, null)); // critical
				writeD((int) _summon.getMAtk(null, null)); // matk
				writeD((int) _summon.getMDef(null, null)); // mdef
				
				// writeD(_activeChar.getMagicAccuracy()); // magic accuracy
				writeD(100);
				
				// writeD(_activeChar.getMagicEvasionRate()); // magic evasion
				writeD(100);
				
				writeD(_summon.getMCriticalHit(null, null)); // mcritical
				writeD((int) _summon.getStat().getMoveSpeed()); // speed
				writeD((int) _summon.getPAtkSpd()); // atkspeed
				writeD(_summon.getMAtkSpd()); // casting speed
				
				writeC(0); // TODO: Check me, might be ride status
				writeC(_summon.getTeam().getId()); // Confirmed
				writeC(_summon.getSoulShotsPerHit()); // How many soulshots this servitor uses per hit - Confirmed
				writeC(_summon.getSpiritShotsPerHit()); // How many spiritshots this servitor uses per hit - - Confirmed
				
				writeD(0x00); // TODO: Find me
				writeD(_summon.getFormId()); // Transformation ID - Confirmed
				
				writeC(0x00); // _summon.getOwner().getSummonPoints() Used Summon Points
				writeC(0x00); // _summon.getOwner().getMaxSummonPoints() Maximum Summon Points
				
				Set<Integer> _abnormalVisualEffects = _summon._abnormalEffects;
				writeH(_abnormalVisualEffects.size() + (_summon.isInvisible() ? 1 : 0));
				for (Integer abnormalVisualEffect : _abnormalVisualEffects)
				{
					writeH(abnormalVisualEffect);
				}
				if (_summon.isInvisible())
				{
					writeH(VisualEffect.STEALTH.getId());
				}
				
				writeC(_statusMask);
				return;
		}
		
		writeD(_summon.getSummonType());
		writeD(_summon.getObjectId());
		writeD(_summon.getTemplate().getDisplayId() + 1000000);
		writeD(0); // 1=attackable
		
		writeD(_x);
		writeD(_y);
		writeD(_z);
		writeD(_heading);
		writeD(0);
		writeD(_mAtkSpd);
		writeD(_pAtkSpd);
		writeD(_runSpd);
		writeD(_walkSpd);
		writeD(_swimRunSpd);
		writeD(_swimWalkSpd);
		writeD(_flyRunSpd);
		writeD(_flyWalkSpd);
		writeD(_flyRunSpd);
		writeD(_flyWalkSpd);
		writeF(_moveMultiplier);
		writeF(_summon.getAttackSpeedMultiplier()); // attack speed multiplier
		writeF(_summon.getTemplate().getfCollisionRadius());
		writeF(_summon.getTemplate().getfCollisionHeight());
		writeD(_summon.getWeapon()); // right hand weapon
		writeD(_summon.getArmor()); // body armor
		writeD(0x00); // left hand weapon
		writeC(_summon.getOwner() != null ? 1 : 0); // when pet is dead and player exit game, pet doesn't show master name
		writeC(_summon.isRunning() ? 1 : 0); // running=1 (it is always 1, walking mode is calculated from multiplier)
		writeC(_summon.isInCombat() ? 1 : 0); // attacking 1=true
		writeC(_summon.isAlikeDead() ? 1 : 0); // dead 1=true
		writeC(_isSummoned ? 2 : _val); // 0=teleported 1=default 2=summoned
		
		switch (ServerTypeConfigs.SERVER_TYPE)
		{
			case H5:
				writeD(-1); // High Five NPCString ID
				break;
		}
		
		if (_summon.isPet())
		{
			writeS(_summon.getName()); // Pet name.
		}
		else
		{
			writeS(_summon.getTemplate().isUsingServerSideName() ? _summon.getName() : ""); // Summon name.
		}
		
		switch (ServerTypeConfigs.SERVER_TYPE)
		{
			case H5:
				writeD(-1); // High Five NPCString ID
				break;
		}
		
		writeS(_summon.getTitle()); // owner name
		writeD(1);
		writeD(_summon.getPvpFlag()); // 0 = white,2= purpleblink, if its greater then karma = purple
		writeD(_summon.getKarma()); // karma
		writeD(_curFed); // how fed it is
		writeD(_maxFed); // max fed it can be
		writeD((int) _summon.getCurrentHp());// current hp
		writeD(_maxHp);// max hp
		writeD((int) _summon.getCurrentMp());// current mp
		writeD(_maxMp);// max mp
		writeD(_summon.getStat().getSp()); // sp
		writeD(_summon.getLevel());// lvl
		writeQ(_summon.getStat().getExp());
		
		if (_summon.getExpForThisLevel() > _summon.getStat().getExp())
		{
			writeQ(_summon.getStat().getExp());// 0% absolute value
		}
		else
		{
			writeQ(_summon.getExpForThisLevel());// 0% absolute value
		}
		
		writeQ(_summon.getExpForNextLevel());// 100% absoulte value
		writeD(_summon.isPet() ? _summon.getInventory().getTotalWeight() : 0);// weight
		writeD(_summon.getMaxLoad());// max weight it can carry
		writeD((int) _summon.getPAtk(null));// patk
		writeD((int) _summon.getPDef(null));// pdef
		writeD((int) _summon.getMAtk(null, null));// matk
		writeD((int) _summon.getMDef(null, null));// mdef
		writeD(_summon.getAccuracy());// accuracy
		writeD(_summon.getEvasionRate(null));// evasion
		writeD(_summon.getCriticalHit(null, null));// critical
		writeD((int) _summon.getMoveSpeed());// speed
		writeD((int) _summon.getPAtkSpd());// atkspeed
		writeD(_summon.getMAtkSpd());// casting speed
		
		writeD(_summon.getAbnormalEffect());// c2 abnormal visual effect... bleed=1; poison=2; poison & bleed=3; flame=4;
		writeH(_summon.isMountable() ? 1 : 0);// c2 ride button
		
		writeC(_summon.isInsideZone(ZoneIdType.WATER) ? 1 : _summon.isFlying() ? 2 : 0); // c2
		
		// Following all added in C4.
		writeH(0); // ??
		writeC(_summon.getTeam().getId());
		writeD(_summon.getSoulShotsPerHit()); // How many soulshots this servitor uses per hit
		writeD(_summon.getSpiritShotsPerHit()); // How many spiritshots this servitor uses per hit
		
		switch (ServerTypeConfigs.SERVER_TYPE)
		{
			case IL:
				return;
		}
		
		writeD(_summon.getFormId());// CT1.5 Pet form and skills
		writeD(_summon.getSpecialEffect());
	}
}
