package handlers;

import ai.individual.*;
import ai.individual.Venom.Venom;
import ai.individual.extra.*;
import ai.individual.extra.ToiRaids.Golkonda;
import ai.individual.extra.ToiRaids.Hallate;
import ai.individual.extra.ToiRaids.Kernon;
import gr.sr.handler.ABLoader;

/**
 * @author L2jSunrise Team
 * @Website www.l2jsunrise.com
 */
public final class IndividualLoader extends ABLoader
{
	private final Class<?>[] SCRIPTS =
	{
		Anais.class,
		Ballista.class,
		CrimsonHatuOtis.class,
		DarkWaterDragon.class,
		DivineBeast.class,
		DrChaos.class,
		Epidos.class,
		EvasGiftBox.class,
		FrightenedRagnaOrc.class,
		Gordon.class,
		GraveRobbers.class,
		QueenShyeed.class,
		RagnaOrcCommander.class,
		RagnaOrcHero.class,
		RagnaOrcSeer.class,
		SinEater.class,
		SinWardens.class,
		
		// Extras
		Aenkinel.class,
		Barakiel.class,
		BladeOtis.class,
		EtisEtina.class,
		FollowerOfAllosce.class,
		FollowerOfMontagnar.class,
		Gargos.class,
		Hellenark.class,
		HolyBrazier.class,
		KaimAbigore.class,
		Kechi.class,
		KelBilette.class,
		OlAriosh.class,
		SelfExplosiveKamikaze.class,
		ValakasMinions.class,
		VenomousStorace.class,
		WeirdBunei.class,
		WhiteAllosce.class,
		
		// Extra Toi Raids
		Golkonda.class,
		Hallate.class,
		Kernon.class,
		
		// Other
		Venom.class,
	};
	
	public IndividualLoader()
	{
		loadScripts();
	}
	
	@Override
	public Class<?>[] getScripts()
	{
		return SCRIPTS;
	}
}
