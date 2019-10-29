package handlers.loader;

import gr.sr.handler.ABLoader;

import handlers.*;

/**
 * @author L2jSunrise Team
 * @Website www.l2jsunrise.com
 */
public final class GlobalLoader extends ABLoader
{
	private final Class<?>[] SCRIPTS =
	{
		BloodAltarsLoader.class,
		ConquerableHallsLoader.class,
		CustomsLoader.class,
		EventsLoader.class,
		FeaturesLoader.class,
		GraciaLoader.class,
		GroupTemplatesLoader.class,
		GrandBossLoader.class,
		IndividualLoader.class,
		HellboundLoader.class,
		InstanceLoader.class,
		MasterHandler.class,
		ModifiersLoader.class,
		NpcLoader.class,
		QuestLoader.class,
		SunriseNpcsLoader.class,
		TeleportersLoader.class,
		VehiclesLoader.class,
		VillageMastersLoader.class,
		ZonesLoader.class,
	};
	
	public GlobalLoader()
	{
		loadScripts();
	}
	
	@Override
	public Class<?>[] getScripts()
	{
		return SCRIPTS;
	}
	
	public static void main(String[] args)
	{
		new GlobalLoader();
	}
}
