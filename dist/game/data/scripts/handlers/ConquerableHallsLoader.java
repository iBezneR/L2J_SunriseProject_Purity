package handlers;

import conquerablehalls.DevastatedCastle.DevastatedCastle;
import conquerablehalls.FortressOfResistance.FortressOfResistance;
import conquerablehalls.FortressOfTheDead.FortressOfTheDead;
import conquerablehalls.RainbowSpringsChateau.RainbowSpringsChateau;
import gr.sr.handler.ABLoader;

/**
 * @author L2jSunrise Team
 * @Website www.l2jsunrise.com
 */
public final class ConquerableHallsLoader extends ABLoader
{
	private final Class<?>[] SCRIPTS =
	{
		// BanditStronghold.class,
		// WildBeastReserve.class,
		DevastatedCastle.class,
		FortressOfResistance.class,
		FortressOfTheDead.class,
		RainbowSpringsChateau.class,
	};
	
	public ConquerableHallsLoader()
	{
		loadScripts();
	}
	
	@Override
	public Class<?>[] getScripts()
	{
		return SCRIPTS;
	}
}
