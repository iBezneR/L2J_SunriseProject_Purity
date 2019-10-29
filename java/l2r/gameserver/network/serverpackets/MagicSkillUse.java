package l2r.gameserver.network.serverpackets;

import gr.sr.network.handler.ServerTypeConfigs;
import l2r.gameserver.data.xml.impl.ActionData;
import l2r.gameserver.data.xml.impl.SkillData;
import l2r.gameserver.model.Location;
import l2r.gameserver.model.actor.L2Character;
import l2r.gameserver.model.interfaces.IPositionable;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * MagicSkillUse server packet implementation.
 * @author vGodFather
 */
public class MagicSkillUse extends L2GameServerPacket
{
	private final L2Character _activeChar;
	private final L2Character _target;
	private final int _skillId;
	private final int _skillLevel;
	private final int _hitTime;
	private final int _reuseGroup;
	private final int _reuseDelay;
	private final List<Integer> _unknown = Collections.emptyList();
	private final List<Location> _groundLocations;
	private final int _actionId; // If skill is called from RequestActionUse, use that ID.
	
	public MagicSkillUse(L2Character cha, L2Character target, int skillId, int skillLevel, int skillTime, int reuseDelay)
	{
		_activeChar = cha;
		_target = target;
		_skillId = skillId;
		_skillLevel = skillLevel;
		_hitTime = skillTime;
		_reuseGroup = (_skillId * 1000) + _skillLevel;// _skillId;// reuseGroup;
		_reuseDelay = reuseDelay;
		_groundLocations = cha.isPlayer() && (cha.getActingPlayer().getCurrentSkillWorldPosition() != null) ? Arrays.asList(cha.getActingPlayer().getCurrentSkillWorldPosition()) : Collections.<Location> emptyList();
		
		final int actionId = _activeChar.isSummon() ? ActionData.getInstance().getSkillActionId(_skillId) : -1;
		_actionId = actionId;
	}
	
	public MagicSkillUse(L2Character cha, int skillId, int skillLevel, int skillTime, int reuseDelay)
	{
		this(cha, cha, skillId, skillLevel, skillTime, reuseDelay);
	}
	
	@Override
	protected final void writeImpl()
	{
		switch (ServerTypeConfigs.SERVER_TYPE)
		{
			case IL:
				writeC(0x2F);
				break;
			case GF:
			case EPILOGUE:
			case FREYA:
			case H5:
			case GC:
			case SL:
				writeC(0x48);
				break;
		}
		
		switch (ServerTypeConfigs.SERVER_TYPE)
		{
			case GC:
			case SL:
				writeD(0); // Casting bar type: 0 - default, 1 - default up, 2 - blue, 3 - green, 4 - red.
				break;
		}
		
		writeD(_activeChar.getObjectId());
		writeD(_target.getObjectId());
		writeD(_skillId);
		
		switch (ServerTypeConfigs.SERVER_TYPE)
		{
			case IL:
			case GF:
			case EPILOGUE:
			case FREYA:
			case H5:
				writeD(_skillLevel);
				break;
			case GC:
			case SL:
				writeD(_skillLevel > 100 ? SkillData.getInstance().getMaxLevel(_skillId) : _skillLevel);
				break;
		}
		
		writeD(_hitTime);
		
		switch (ServerTypeConfigs.SERVER_TYPE)
		{
			case GC:
			case SL:
				writeD(_reuseGroup);
				break;
		}
		
		writeD(_reuseDelay);
		writeLoc(_activeChar);
		
		writeH(_unknown.size()); // TODO: Implement me!
		for (int unknown : _unknown)
		{
			writeH(unknown);
		}
		writeH(_groundLocations.size());
		for (IPositionable target : _groundLocations)
		{
			writeD(target.getX());
			writeD(target.getY());
			writeD(target.getZ());
		}
		
		writeLoc(_target);
		
		switch (ServerTypeConfigs.SERVER_TYPE)
		{
			case GC:
			case SL:
				writeD(_actionId >= 0 ? 0x01 : 0x00); // 1 when ID from RequestActionUse is used
				writeD(_actionId >= 0 ? _actionId : 0); // ID from RequestActionUse. Used to set cooldown on summon skills.
				break;
		}
	}
}