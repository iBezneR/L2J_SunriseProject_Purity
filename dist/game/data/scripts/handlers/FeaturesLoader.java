package handlers;

import features.SkillTransfer.SkillTransfer;
import gr.sr.handler.ABLoader;

/**
 * @author L2jSunrise Team
 * @Website www.l2jsunrise.com
 */
public final class FeaturesLoader extends ABLoader
{
	private final Class<?>[] SCRIPTS =
	{
		SkillTransfer.class,
	};
	
	public FeaturesLoader()
	{
		loadScripts();
	}
	
	@Override
	public Class<?>[] getScripts()
	{
		return SCRIPTS;
	}
}
