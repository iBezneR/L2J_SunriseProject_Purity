package l2r.gameserver.network.serverpackets;

import gr.sr.network.handler.ServerTypeConfigs;
import l2r.gameserver.model.actor.L2Character;

public class SpecialCamera extends L2GameServerPacket
{
	private final int _id;
	private final int _force;
	private final int _angle1;
	private final int _angle2;
	private final int _time;
	private final int _duration;
	private final int _relYaw;
	private final int _relPitch;
	private final int _isWide;
	private final int _relAngle;
	private final int _unk;
	
	/**
	 * Special Camera packet constructor.
	 * @param creature the creature
	 * @param force
	 * @param angle1
	 * @param angle2
	 * @param time
	 * @param range
	 * @param duration
	 * @param relYaw
	 * @param relPitch
	 * @param isWide
	 * @param relAngle
	 */
	public SpecialCamera(L2Character creature, int force, int angle1, int angle2, int time, int range, int duration, int relYaw, int relPitch, int isWide, int relAngle)
	{
		this(creature, force, angle1, angle2, time, duration, range, relYaw, relPitch, isWide, relAngle, 0);
	}
	
	/**
	 * Special Camera Ex packet constructor.
	 * @param creature the creature
	 * @param talker
	 * @param force
	 * @param angle1
	 * @param angle2
	 * @param time
	 * @param duration
	 * @param relYaw
	 * @param relPitch
	 * @param isWide
	 * @param relAngle
	 */
	public SpecialCamera(L2Character creature, L2Character talker, int force, int angle1, int angle2, int time, int duration, int relYaw, int relPitch, int isWide, int relAngle)
	{
		this(creature, force, angle1, angle2, time, duration, 0, relYaw, relPitch, isWide, relAngle, 0);
	}
	
	/**
	 * Special Camera 3 packet constructor.
	 * @param creature the creature
	 * @param force
	 * @param angle1
	 * @param angle2
	 * @param time
	 * @param range
	 * @param duration
	 * @param relYaw
	 * @param relPitch
	 * @param isWide
	 * @param relAngle
	 * @param unk unknown post-C4 parameter
	 */
	public SpecialCamera(L2Character creature, int force, int angle1, int angle2, int time, int range, int duration, int relYaw, int relPitch, int isWide, int relAngle, int unk)
	{
		_id = creature.getObjectId();
		_force = force;
		_angle1 = angle1;
		_angle2 = angle2;
		_time = time;
		_duration = duration;
		_relYaw = relYaw;
		_relPitch = relPitch;
		_isWide = isWide;
		_relAngle = relAngle;
		_unk = unk;
	}
	
	public SpecialCamera(L2Character creature, int dist, int yaw, int pitch, int time, int duration)
	{
		_id = creature.getObjectId();
		_force = dist;
		_angle1 = yaw;
		_angle2 = pitch;
		_time = time;
		_duration = duration;
		_relYaw = 0;
		_relPitch = 0;
		_isWide = 0;
		_relAngle = 0;
		_unk = 0;
	}
	
	@Override
	public void writeImpl()
	{
		switch (ServerTypeConfigs.SERVER_TYPE)
		{
			case IL:
				writeC(0xC7);
				break;
			case GF:
			case EPILOGUE:
			case FREYA:
			case H5:
			case GC:
			case SL:
				writeC(0xD6);
				break;
		}
		
		writeD(_id);
		writeD(_force);
		writeD(_angle1);
		writeD(_angle2);
		writeD(_time);
		writeD(_duration);
		writeD(_relYaw);
		writeD(_relPitch);
		writeD(_isWide);
		writeD(_relAngle);
		writeD(_unk);
	}
}
