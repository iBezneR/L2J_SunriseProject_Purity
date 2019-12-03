/*
 * Copyright (C) L2J Sunrise
 * This file is part of L2J Sunrise.
 */
package handlers.effecthandlers;

import l2r.gameserver.model.actor.L2Character;
import l2r.gameserver.model.effects.EffectTemplate;
import l2r.gameserver.model.effects.L2Effect;
import l2r.gameserver.model.effects.L2EffectType;
import l2r.gameserver.model.stats.Env;

/**
 * @author vGodFather
 */
public class DispelOne extends L2Effect
{
	private final boolean _randomEffects;
	
	public DispelOne(Env env, EffectTemplate template)
	{
		super(env, template);
		
		_randomEffects = template.getParameters().getBoolean("ordered", false);
	}
	
	@Override
	public L2EffectType getEffectType()
	{
		return L2EffectType.DISPEL;
	}
	
	@Override
	public boolean isInstant()
	{
		return true;
	}
	
	@Override
	public boolean onStart()
	{
		L2Character target = getEffected();
		if ((target == null) || target.isDead())
		{
			return false;
		}
		
		if (_randomEffects)
		{
			L2Effect buff = null;
			for (L2Effect e : target.getAllEffects())
			{
				if ((e != null) && !e.getSkill().canBeDispeled() && e.getSkill().isDance())
				{
					continue;
				}
				buff = e;
				break;
			}
			
			if (buff != null)
			{
				buff.exit();
				return true;
			}
			
			for (L2Effect e : target.getAllEffects())
			{
				if ((e != null) && !e.getSkill().canBeDispeled())
				{
					continue;
				}
				buff = e;
				break;
			}
			
			if (buff != null)
			{
				buff.exit();
				return true;
			}
		}
		else
		{
			for (L2Effect e : target.getAllEffects())
			{
				if (!e.getSkill().canBeDispeled())
				{
					continue;
				}
				e.exit();
				break;
			}
		}
		return true;
	}
}
