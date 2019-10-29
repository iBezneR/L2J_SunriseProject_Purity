package handlers;

import gr.sr.handler.ABLoader;
import vehicles.*;

/**
 * @author L2jSunrise Team
 * @Website www.l2jsunrise.com
 */
public final class VehiclesLoader extends ABLoader
{
	private final Class<?>[] SCRIPTS =
	{
		BoatGiranTalking.class,
		BoatGludinRune.class,
		BoatInnadrilTour.class,
		BoatRunePrimeval.class,
		BoatTalkingGludin.class
	};
	
	public VehiclesLoader()
	{
		loadScripts();
	}
	
	@Override
	public Class<?>[] getScripts()
	{
		return SCRIPTS;
	}
}
