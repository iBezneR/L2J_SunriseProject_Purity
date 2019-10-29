package handlers;

import ai.modifier.*;
import ai.modifier.dropEngine.FortressReward;
import gr.sr.handler.ABLoader;

/**
 * @author L2jSunrise Team
 * @Website www.l2jsunrise.com
 */
public final class ModifiersLoader extends ABLoader
{
	private final Class<?>[] SCRIPTS =
	{
		FlyingNpcs.class,
		NoChampionMobs.class,
		NoMovingNpcs.class,
		NonAttackingNpcs.class,
		NonLethalableNpcs.class,
		NonTalkingNpcs.class,
		NoRandomAnimation.class,
		NoRandomWalkMobs.class,
		RunningNpcs.class,
		SeeThroughSilentMove.class,
		
		// Drop Modifiers
		FortressReward.class,
	};
	
	public ModifiersLoader()
	{
		loadScripts();
	}
	
	@Override
	public Class<?>[] getScripts()
	{
		return SCRIPTS;
	}
}
