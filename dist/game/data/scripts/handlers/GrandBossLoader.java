package handlers;

import ai.grandboss.Antharas.Antharas;
import ai.grandboss.Baium.Baium;
import ai.grandboss.*;
import ai.grandboss.Sailren.Sailren;
import ai.grandboss.Valakas.Valakas;
import gr.sr.handler.ABLoader;

/**
 * @author L2jSunrise Team
 * @Website www.l2jsunrise.com
 */
public final class GrandBossLoader extends ABLoader
{
	private final Class<?>[] SCRIPTS =
	{
		Beleth.class,
		Core.class,
		Orfen.class,
		QueenAnt.class,
		VanHalter.class,
		Antharas.class,
		Baium.class,
		Sailren.class,
		Valakas.class,
	};
	
	public GrandBossLoader()
	{
		loadScripts();
	}
	
	@Override
	public Class<?>[] getScripts()
	{
		return SCRIPTS;
	}
}
