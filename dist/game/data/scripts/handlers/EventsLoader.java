package handlers;

import events.CharacterBirthday.CharacterBirthday;
import events.NewEra.NewEra;
import events.SquashEvent.SquashEvent;
import gr.sr.handler.ABLoader;

/**
 * @author L2jSunrise Team
 * @Website www.l2jsunrise.com
 */
public final class EventsLoader extends ABLoader
{
	private final Class<?>[] SCRIPTS =
	{
		CharacterBirthday.class,
		
		// Disabled by default events
		// FreyaCelebration.class,
		// GiftOfVitality.class,
		// HeavyMedal.class,
		// LoveYourGatekeeper.class,
		// MasterOfEnchanting.class,
		// SavingSanta.class,
		SquashEvent.class,
		NewEra.class,
		// TheValentineEvent.class,
	};
	
	public EventsLoader()
	{
		loadScripts();
	}
	
	@Override
	public Class<?>[] getScripts()
	{
		return SCRIPTS;
	}
}
