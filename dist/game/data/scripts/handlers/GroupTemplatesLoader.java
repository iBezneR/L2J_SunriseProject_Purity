package handlers;

import ai.group_template.*;
import ai.group_template.extra.*;
import gr.sr.handler.ABLoader;

/**
 * @author L2jSunrise Team
 * @Website www.l2jsunrise.com
 */
public final class GroupTemplatesLoader extends ABLoader
{
	private final Class<?>[] SCRIPTS =
	{
		BeastFarm.class,
		DenOfEvil.class,
		FeedableBeasts.class,
		FleeMonsters.class,
		FrozenLabyrinth.class,
		GiantsCave.class,
		HotSprings.class,
		IsleOfPrayer.class,
		MinionSpawnManager.class,
		MonasteryOfSilence.class,
		PlainsOfDion.class,
		PolymorphingAngel.class,
		PolymorphingOnAttack.class,
		PrisonGuards.class,
		RaidBossCancel.class,
		RandomSpawn.class,
		//RangeGuard.class,
		Sandstorms.class,
		SilentValley.class,
		SummonPc.class,
		TreasureChest.class,
		TurekOrcs.class,
		VarkaKetra.class,
		WarriorFishingBlock.class,
		
		// Extras
		BrekaOrcOverlord.class,
		CryptsOfDisgrace.class,
		FieldOfWhispersSilence.class,
		KarulBugbear.class,
		LuckyPig.class,
		Mutation.class,
		OlMahumGeneral.class,
		TimakOrcOverlord.class,
		TimakOrcTroopLeader.class,
		TomlanKamos.class,
		WarriorMonk.class,
		ZombieGatekeepers.class,
	};
	
	public GroupTemplatesLoader()
	{
		loadScripts();
	}

	@Override
	public Class<?>[] getScripts()
	{
		return SCRIPTS;
	}
}
