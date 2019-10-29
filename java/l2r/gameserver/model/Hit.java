package l2r.gameserver.model;

import gr.sr.network.handler.ServerTypeConfigs;
import gr.sr.network.handler.types.ServerType;

public class Hit
{
	public static final int HITFLAG_MISS = ServerTypeConfigs.SERVER_TYPE == ServerType.GC ? 0x01 : 0x80;
	public static final int HITFLAG_SHLD = ServerTypeConfigs.SERVER_TYPE == ServerType.GC ? 0x02 : 0x40;
	public static final int HITFLAG_CRIT = ServerTypeConfigs.SERVER_TYPE == ServerType.GC ? 0x04 : 0x20;
	public static final int HITFLAG_USESS = ServerTypeConfigs.SERVER_TYPE == ServerType.GC ? 0x08 : 0x10;
	
	private final int _targetId;
	private final int _damage;
	private final int _ssGrade;
	private int _flags = 0;
	
	public Hit(L2Object target, int damage, boolean miss, boolean crit, byte shld, boolean soulshot, int ssGrade)
	{
		_targetId = target.getObjectId();
		_damage = damage;
		_ssGrade = ssGrade;
		
		if (miss)
		{
			_flags |= HITFLAG_MISS;
			return;
		}
		
		if (soulshot)
		{
			_flags |= HITFLAG_USESS;
		}
		
		if (crit)
		{
			_flags |= HITFLAG_CRIT;
		}
		
		if (shld > 0)
		{
			_flags |= HITFLAG_SHLD;
		}
	}
	
	public int getTargetId()
	{
		return _targetId;
	}
	
	public int getDamage()
	{
		return _damage;
	}
	
	public int getFlags()
	{
		return _flags;
	}
	
	public int getGrade()
	{
		return _ssGrade;
	}
}
