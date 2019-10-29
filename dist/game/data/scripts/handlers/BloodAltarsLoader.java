package handlers;

import ai.npc.BloodAltars.*;
import gr.sr.handler.ABLoader;

/**
 * @author L2jSunrise Team
 * @Website www.l2jsunrise.com
 */
public final class BloodAltarsLoader extends ABLoader
{
	private final Class<?>[] SCRIPTS =
	{
		AdenBloodAltar.class,
		DarkElfBloodAltar.class,
		DionBloodAltar.class,
		DwarenBloodAltar.class,
		ElvenBloodAltar.class,
		GiranBloodAltar.class,
		GludinBloodAltar.class,
		GludioBloodAltar.class,
		GoddardBloodAltar.class,
		HeineBloodAltar.class,
		KamaelBloodAltar.class,
		OrcBloodAltar.class,
		OrenBloodAltar.class,
		PrimevalBloodAltar.class,
		RuneBloodAltar.class,
		SchutgartBloodAltar.class,
		TalkingIslandBloodAltar.class,
	};
	
	public BloodAltarsLoader()
	{
		loadScripts();
	}
	
	@Override
	public Class<?>[] getScripts()
	{
		return SCRIPTS;
	}
}
