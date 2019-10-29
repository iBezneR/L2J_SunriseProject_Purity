/*
 * Copyright (C) 2004-2018 L2J DataPack
 * 
 * This file is part of L2J DataPack.
 * 
 * L2J DataPack is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * L2J DataPack is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package ai.group_template;

import ai.npc.AbstractNpcAI;
import l2r.gameserver.model.actor.L2Npc;
import l2r.gameserver.model.actor.instance.L2ChestInstance;
import l2r.gameserver.model.actor.instance.L2PcInstance;
import l2r.gameserver.model.holders.ItemChanceHolder;
import l2r.gameserver.model.holders.SkillHolder;
import l2r.gameserver.model.skills.L2Skill;
import l2r.gameserver.network.SystemMessageId;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Treasure Chest AI.
 * @author ivantotov
 */
public final class TreasureChest extends AbstractNpcAI
{
	private static final String TIMER_1 = "5001";
	private static final String TIMER_2 = "5002";
	private static final int MAX_SPAWN_TIME = 14400000;
	private static final int ATTACK_SPAWN_TIME = 5000;
	private static final int PLAYER_LEVEL_THRESHOLD = 78;
	private static final int MAESTROS_KEY_SKILL_ID = 22271;
	private static final SkillHolder[] TREASURE_BOMBS = new SkillHolder[]
	{
		new SkillHolder(4143, 1),
		new SkillHolder(4143, 2),
		new SkillHolder(4143, 3),
		new SkillHolder(4143, 4),
		new SkillHolder(4143, 5),
		new SkillHolder(4143, 6),
		new SkillHolder(4143, 7),
		new SkillHolder(4143, 8),
		new SkillHolder(4143, 9),
		new SkillHolder(4143, 10),
	};
	
	private static final Map<Integer, List<ItemChanceHolder>> DROPS = new HashMap<>();
	
	static
	{
		DROPS.put(18265, Arrays.asList( // Treasure Chest
		new ItemChanceHolder(736, 2703, 7), // Scroll of Escape
		new ItemChanceHolder(1061, 2365, 4), // Major Healing Potion
		new ItemChanceHolder(737, 3784, 4), // Scroll of Resurrection
		new ItemChanceHolder(10260, 1136, 1), // Haste Potion
		new ItemChanceHolder(10261, 1136, 1), // Accuracy Juice
		new ItemChanceHolder(10262, 1136, 1), // Critical Damage Juice
		new ItemChanceHolder(10263, 1136, 1), // Critical Rate Juice
		new ItemChanceHolder(10264, 1136, 1), // Casting Spd. Juice
		new ItemChanceHolder(10265, 1136, 1), // Evasion Juice
		new ItemChanceHolder(10266, 1136, 1), // M. Atk. Juice
		new ItemChanceHolder(10267, 1136, 1), // P. Atk. Potion
		new ItemChanceHolder(10268, 1136, 1), // Wind Walk Juice
		new ItemChanceHolder(5593, 2365, 6), // SP Scroll (Low-grade)
		new ItemChanceHolder(5594, 1136, 1), // SP Scroll (Mid-grade)
		new ItemChanceHolder(10269, 1136, 1), // P. Def. Juice
		new ItemChanceHolder(10131, 4919, 1), // Transformation Scroll: Onyx Beast
		new ItemChanceHolder(10132, 4919, 1), // Transformation Scroll: Doom Wraith
		new ItemChanceHolder(10133, 4919, 1), // Transformation Scroll: Grail Apostle
		new ItemChanceHolder(1538, 3279, 1), // Blessed Scroll of Escape
		new ItemChanceHolder(3936, 1230, 1), // Blessed Scroll of Resurrection
		new ItemChanceHolder(68, 2617, 1), // Falchion
		new ItemChanceHolder(21747, 320, 1))); // Novice Adventurer's Treasure Sack
		
		DROPS.put(18266, Arrays.asList( // Treasure Chest
		new ItemChanceHolder(736, 3159, 7), // Scroll of Escape
		new ItemChanceHolder(1061, 2764, 4), // Major Healing Potion
		new ItemChanceHolder(737, 4422, 4), // Scroll of Resurrection
		new ItemChanceHolder(10260, 1327, 1), // Haste Potion
		new ItemChanceHolder(10261, 1327, 1), // Accuracy Juice
		new ItemChanceHolder(10262, 1327, 1), // Critical Damage Juice
		new ItemChanceHolder(10263, 1327, 1), // Critical Rate Juice
		new ItemChanceHolder(10264, 1327, 1), // Casting Spd. Juice
		new ItemChanceHolder(10265, 1327, 1), // Evasion Juice
		new ItemChanceHolder(10266, 1327, 1), // M. Atk. Juice
		new ItemChanceHolder(10267, 1327, 1), // P. Atk. Potion
		new ItemChanceHolder(10268, 1327, 1), // Wind Walk Juice
		new ItemChanceHolder(5593, 2764, 6), // SP Scroll (Low-grade)
		new ItemChanceHolder(5594, 1327, 1), // SP Scroll (Mid-grade)
		new ItemChanceHolder(10269, 1327, 1), // P. Def. Juice
		new ItemChanceHolder(10131, 5749, 1), // Transformation Scroll: Onyx Beast
		new ItemChanceHolder(10132, 5749, 1), // Transformation Scroll: Doom Wraith
		new ItemChanceHolder(10133, 5749, 1), // Transformation Scroll: Grail Apostle
		new ItemChanceHolder(1538, 3833, 1), // Blessed Scroll of Escape
		new ItemChanceHolder(3936, 1438, 1), // Blessed Scroll of Resurrection
		new ItemChanceHolder(68, 3058, 1), // Falchion
		new ItemChanceHolder(21747, 374, 1))); // Novice Adventurer's Treasure Sack
		
		DROPS.put(18267, Arrays.asList( // Treasure Chest
		new ItemChanceHolder(736, 3651, 7), // Scroll of Escape
		new ItemChanceHolder(1061, 3194, 4), // Major Healing Potion
		new ItemChanceHolder(737, 5111, 4), // Scroll of Resurrection
		new ItemChanceHolder(10260, 1534, 1), // Haste Potion
		new ItemChanceHolder(10261, 1534, 1), // Accuracy Juice
		new ItemChanceHolder(10262, 1534, 1), // Critical Damage Juice
		new ItemChanceHolder(10263, 1534, 1), // Critical Rate Juice
		new ItemChanceHolder(10264, 1534, 1), // Casting Spd. Juice
		new ItemChanceHolder(10265, 1534, 1), // Evasion Juice
		new ItemChanceHolder(10266, 1534, 1), // M. Atk. Juice
		new ItemChanceHolder(10267, 1534, 1), // P. Atk. Potion
		new ItemChanceHolder(10268, 1534, 1), // Wind Walk Juice
		new ItemChanceHolder(5593, 3194, 6), // SP Scroll (Low-grade)
		new ItemChanceHolder(5594, 1534, 1), // SP Scroll (Mid-grade)
		new ItemChanceHolder(10269, 1534, 1), // P. Def. Juice
		new ItemChanceHolder(10131, 6644, 1), // Transformation Scroll: Onyx Beast
		new ItemChanceHolder(10132, 6644, 1), // Transformation Scroll: Doom Wraith
		new ItemChanceHolder(10133, 6644, 1), // Transformation Scroll: Grail Apostle
		new ItemChanceHolder(1538, 4429, 1), // Blessed Scroll of Escape
		new ItemChanceHolder(3936, 1661, 1), // Blessed Scroll of Resurrection
		new ItemChanceHolder(68, 3534, 1), // Falchion
		new ItemChanceHolder(21747, 463, 1))); // Novice Adventurer's Treasure Sack
		
		DROPS.put(18268, Arrays.asList( // Treasure Chest
		new ItemChanceHolder(736, 4200, 7), // Scroll of Escape
		new ItemChanceHolder(1061, 3675, 4), // Major Healing Potion
		new ItemChanceHolder(737, 5879, 4), // Scroll of Resurrection
		new ItemChanceHolder(10260, 1764, 1), // Haste Potion
		new ItemChanceHolder(10261, 1764, 1), // Accuracy Juice
		new ItemChanceHolder(10262, 1764, 1), // Critical Damage Juice
		new ItemChanceHolder(10263, 1764, 1), // Critical Rate Juice
		new ItemChanceHolder(10264, 1764, 1), // Casting Spd. Juice
		new ItemChanceHolder(10265, 1764, 1), // Evasion Juice
		new ItemChanceHolder(10266, 1764, 1), // M. Atk. Juice
		new ItemChanceHolder(10267, 1764, 1), // P. Atk. Potion
		new ItemChanceHolder(10268, 1764, 1), // Wind Walk Juice
		new ItemChanceHolder(5593, 3675, 6), // SP Scroll (Low-grade)
		new ItemChanceHolder(5594, 1764, 1), // SP Scroll (Mid-grade)
		new ItemChanceHolder(10269, 1764, 1), // P. Def. Juice
		new ItemChanceHolder(10134, 5095, 1), // Transformation Scroll: Unicorn
		new ItemChanceHolder(10135, 5095, 1), // Transformation Scroll: Lilim Knight
		new ItemChanceHolder(10136, 5095, 1), // Transformation Scroll: Golem Guardian
		new ItemChanceHolder(1538, 5095, 1), // Blessed Scroll of Escape
		new ItemChanceHolder(3936, 1911, 1), // Blessed Scroll of Resurrection
		new ItemChanceHolder(69, 1543, 1), // Bastard Sword
		new ItemChanceHolder(21747, 498, 1))); // Novice Adventurer's Treasure Sack
		
		DROPS.put(18269, Arrays.asList( // Treasure Chest
		new ItemChanceHolder(736, 5010, 7), // Scroll of Escape
		new ItemChanceHolder(1061, 4383, 4), // Major Healing Potion
		new ItemChanceHolder(737, 7013, 4), // Scroll of Resurrection
		new ItemChanceHolder(10260, 2104, 1), // Haste Potion
		new ItemChanceHolder(10261, 2104, 1), // Accuracy Juice
		new ItemChanceHolder(10262, 2104, 1), // Critical Damage Juice
		new ItemChanceHolder(10263, 2104, 1), // Critical Rate Juice
		new ItemChanceHolder(10264, 2104, 1), // Casting Spd. Juice
		new ItemChanceHolder(10265, 2104, 1), // Evasion Juice
		new ItemChanceHolder(10266, 2104, 1), // M. Atk. Juice
		new ItemChanceHolder(10267, 2104, 1), // P. Atk. Potion
		new ItemChanceHolder(10268, 2104, 1), // Wind Walk Juice
		new ItemChanceHolder(5593, 4383, 6), // SP Scroll (Low-grade)
		new ItemChanceHolder(5594, 2104, 1), // SP Scroll (Mid-grade)
		new ItemChanceHolder(10269, 2104, 1), // P. Def. Juice
		new ItemChanceHolder(10134, 6078, 1), // Transformation Scroll: Unicorn
		new ItemChanceHolder(10135, 6078, 1), // Transformation Scroll: Lilim Knight
		new ItemChanceHolder(10136, 6078, 1), // Transformation Scroll: Golem Guardian
		new ItemChanceHolder(1538, 6078, 1), // Blessed Scroll of Escape
		new ItemChanceHolder(3936, 2280, 1), // Blessed Scroll of Resurrection
		new ItemChanceHolder(69, 1840, 1), // Bastard Sword
		new ItemChanceHolder(21747, 593, 1))); // Novice Adventurer's Treasure Sack
		
		DROPS.put(18270, Arrays.asList( // Treasure Chest
		new ItemChanceHolder(736, 5894, 7), // Scroll of Escape
		new ItemChanceHolder(1061, 5157, 4), // Major Healing Potion
		new ItemChanceHolder(737, 8252, 4), // Scroll of Resurrection
		new ItemChanceHolder(10260, 2476, 1), // Haste Potion
		new ItemChanceHolder(10261, 2476, 1), // Accuracy Juice
		new ItemChanceHolder(10262, 2476, 1), // Critical Damage Juice
		new ItemChanceHolder(10263, 2476, 1), // Critical Rate Juice
		new ItemChanceHolder(10264, 2476, 1), // Casting Spd. Juice
		new ItemChanceHolder(10265, 2476, 1), // Evasion Juice
		new ItemChanceHolder(10266, 2476, 1), // M. Atk. Juice
		new ItemChanceHolder(10267, 2476, 1), // P. Atk. Potion
		new ItemChanceHolder(10268, 2476, 1), // Wind Walk Juice
		new ItemChanceHolder(5593, 5157, 6), // SP Scroll (Low-grade)
		new ItemChanceHolder(5594, 2476, 1), // SP Scroll (Mid-grade)
		new ItemChanceHolder(10269, 2476, 1), // P. Def. Juice
		new ItemChanceHolder(10134, 7152, 1), // Transformation Scroll: Unicorn
		new ItemChanceHolder(10135, 7152, 1), // Transformation Scroll: Lilim Knight
		new ItemChanceHolder(10136, 7152, 1), // Transformation Scroll: Golem Guardian
		new ItemChanceHolder(1538, 7152, 1), // Blessed Scroll of Escape
		new ItemChanceHolder(3936, 2682, 1), // Blessed Scroll of Resurrection
		new ItemChanceHolder(69, 2165, 1), // Bastard Sword
		new ItemChanceHolder(21747, 698, 1))); // Novice Adventurer's Treasure Sack
		
		DROPS.put(18271, Arrays.asList( // Treasure Chest
		new ItemChanceHolder(736, 6879, 7), // Scroll of Escape
		new ItemChanceHolder(1061, 6019, 4), // Major Healing Potion
		new ItemChanceHolder(737, 9630, 4), // Scroll of Resurrection
		new ItemChanceHolder(10260, 2889, 1), // Haste Potion
		new ItemChanceHolder(10261, 2889, 1), // Accuracy Juice
		new ItemChanceHolder(10262, 2889, 1), // Critical Damage Juice
		new ItemChanceHolder(10263, 2889, 1), // Critical Rate Juice
		new ItemChanceHolder(10264, 2889, 1), // Casting Spd. Juice
		new ItemChanceHolder(10265, 2889, 1), // Evasion Juice
		new ItemChanceHolder(10266, 2889, 1), // M. Atk. Juice
		new ItemChanceHolder(10267, 2889, 1), // P. Atk. Potion
		new ItemChanceHolder(10268, 2889, 1), // Wind Walk Juice
		new ItemChanceHolder(5593, 6019, 6), // SP Scroll (Low-grade)
		new ItemChanceHolder(5594, 2889, 1), // SP Scroll (Mid-grade)
		new ItemChanceHolder(10269, 2889, 1), // P. Def. Juice
		new ItemChanceHolder(10134, 8346, 1), // Transformation Scroll: Unicorn
		new ItemChanceHolder(10135, 8346, 1), // Transformation Scroll: Lilim Knight
		new ItemChanceHolder(10136, 8346, 1), // Transformation Scroll: Golem Guardian
		new ItemChanceHolder(1538, 8346, 1), // Blessed Scroll of Escape
		new ItemChanceHolder(3936, 3130, 1), // Blessed Scroll of Resurrection
		new ItemChanceHolder(69, 2527, 1), // Bastard Sword
		new ItemChanceHolder(21747, 815, 1))); // Novice Adventurer's Treasure Sack
		
		DROPS.put(18272, Arrays.asList( // Treasure Chest
		new ItemChanceHolder(736, 6668, 5), // Scroll of Escape
		new ItemChanceHolder(1061, 4168, 4), // Major Healing Potion
		new ItemChanceHolder(737, 2223, 3), // Scroll of Resurrection
		new ItemChanceHolder(1539, 6668, 5), // Major Healing Potion
		new ItemChanceHolder(8625, 3334, 2), // Elixir of Life (B-grade)
		new ItemChanceHolder(8631, 2874, 2), // Elixir of Mind (B-grade)
		new ItemChanceHolder(8637, 5557, 3), // Elixir of CP (B-grade)
		new ItemChanceHolder(8636, 5557, 4), // Elixir of CP (C-grade)
		new ItemChanceHolder(8630, 3832, 2), // Elixir of Mind (C-grade)
		new ItemChanceHolder(8624, 4631, 2), // Elixir of Life (C-grade)
		new ItemChanceHolder(10260, 5129, 1), // Haste Potion
		new ItemChanceHolder(10261, 5129, 1), // Accuracy Juice
		new ItemChanceHolder(10262, 5129, 1), // Critical Damage Juice
		new ItemChanceHolder(10263, 5129, 1), // Critical Rate Juice
		new ItemChanceHolder(10264, 5129, 1), // Casting Spd. Juice
		new ItemChanceHolder(10265, 5129, 1), // Evasion Juice
		new ItemChanceHolder(10266, 5129, 1), // M. Atk. Juice
		new ItemChanceHolder(10267, 5129, 1), // P. Atk. Potion
		new ItemChanceHolder(10268, 5129, 1), // Wind Walk Juice
		new ItemChanceHolder(5593, 7124, 9), // SP Scroll (Low-grade)
		new ItemChanceHolder(5594, 6411, 2), // SP Scroll (Mid-grade)
		new ItemChanceHolder(5595, 642, 1), // SP Scroll (High-grade)
		new ItemChanceHolder(10269, 5129, 1), // P. Def. Juice
		new ItemChanceHolder(10137, 5418, 1), // Transformation Scroll: Inferno Drake
		new ItemChanceHolder(10138, 5418, 1), // Transformation Scroll: Dragon Bomber
		new ItemChanceHolder(1538, 7223, 1), // Blessed Scroll of Escape
		new ItemChanceHolder(3936, 2709, 1), // Blessed Scroll of Resurrection
		new ItemChanceHolder(5577, 2167, 1), // Red Soul Crystal - Stage 11
		new ItemChanceHolder(5578, 2167, 1), // Green Soul Crystal - Stage 11
		new ItemChanceHolder(5579, 2167, 1), // Blue Soul Crystal - Stage 11
		new ItemChanceHolder(70, 1250, 1), // Claymore
		new ItemChanceHolder(21747, 940, 1))); // Novice Adventurer's Treasure Sack
		
		DROPS.put(18273, Arrays.asList( // Treasure Chest
		new ItemChanceHolder(736, 7662, 5), // Scroll of Escape
		new ItemChanceHolder(1061, 4789, 4), // Major Healing Potion
		new ItemChanceHolder(737, 2554, 3), // Scroll of Resurrection
		new ItemChanceHolder(1539, 7662, 5), // Major Healing Potion
		new ItemChanceHolder(8625, 3831, 2), // Elixir of Life (B-grade)
		new ItemChanceHolder(8631, 3303, 2), // Elixir of Mind (B-grade)
		new ItemChanceHolder(8637, 6385, 3), // Elixir of CP (B-grade)
		new ItemChanceHolder(8636, 6385, 4), // Elixir of CP (C-grade)
		new ItemChanceHolder(8630, 4404, 2), // Elixir of Mind (C-grade)
		new ItemChanceHolder(8624, 5321, 2), // Elixir of Life (C-grade)
		new ItemChanceHolder(10260, 5894, 1), // Haste Potion
		new ItemChanceHolder(10261, 5894, 1), // Accuracy Juice
		new ItemChanceHolder(10262, 5894, 1), // Critical Damage Juice
		new ItemChanceHolder(10263, 5894, 1), // Critical Rate Juice
		new ItemChanceHolder(10264, 5894, 1), // Casting Spd. Juice
		new ItemChanceHolder(10265, 5894, 1), // Evasion Juice
		new ItemChanceHolder(10266, 5894, 1), // M. Atk. Juice
		new ItemChanceHolder(10267, 5894, 1), // P. Atk. Potion
		new ItemChanceHolder(10268, 5894, 1), // Wind Walk Juice
		new ItemChanceHolder(5593, 8186, 9), // SP Scroll (Low-grade)
		new ItemChanceHolder(5594, 7367, 2), // SP Scroll (Mid-grade)
		new ItemChanceHolder(5595, 737, 1), // SP Scroll (High-grade)
		new ItemChanceHolder(10269, 5894, 1), // P. Def. Juice
		new ItemChanceHolder(10137, 6226, 1), // Transformation Scroll: Inferno Drake
		new ItemChanceHolder(10138, 6226, 1), // Transformation Scroll: Dragon Bomber
		new ItemChanceHolder(1538, 8301, 1), // Blessed Scroll of Escape
		new ItemChanceHolder(3936, 3113, 1), // Blessed Scroll of Resurrection
		new ItemChanceHolder(5577, 2491, 1), // Red Soul Crystal - Stage 11
		new ItemChanceHolder(5578, 2491, 1), // Green Soul Crystal - Stage 11
		new ItemChanceHolder(5579, 2491, 1), // Blue Soul Crystal - Stage 11
		new ItemChanceHolder(70, 1437, 1), // Claymore
		new ItemChanceHolder(21747, 1080, 1))); // Novice Adventurer's Treasure Sack
		
		DROPS.put(18274, Arrays.asList( // Treasure Chest
		new ItemChanceHolder(736, 8719, 5), // Scroll of Escape
		new ItemChanceHolder(1061, 5450, 4), // Major Healing Potion
		new ItemChanceHolder(737, 2907, 3), // Scroll of Resurrection
		new ItemChanceHolder(1539, 8719, 5), // Major Healing Potion
		new ItemChanceHolder(8625, 4360, 2), // Elixir of Life (B-grade)
		new ItemChanceHolder(8631, 3759, 2), // Elixir of Mind (B-grade)
		new ItemChanceHolder(8637, 7266, 3), // Elixir of CP (B-grade)
		new ItemChanceHolder(8636, 7266, 4), // Elixir of CP (C-grade)
		new ItemChanceHolder(8630, 5011, 2), // Elixir of Mind (C-grade)
		new ItemChanceHolder(8624, 6055, 2), // Elixir of Life (C-grade)
		new ItemChanceHolder(10260, 6707, 1), // Haste Potion
		new ItemChanceHolder(10261, 6707, 1), // Accuracy Juice
		new ItemChanceHolder(10262, 6707, 1), // Critical Damage Juice
		new ItemChanceHolder(10263, 6707, 1), // Critical Rate Juice
		new ItemChanceHolder(10264, 6707, 1), // Casting Spd. Juice
		new ItemChanceHolder(10265, 6707, 1), // Evasion Juice
		new ItemChanceHolder(10266, 6707, 1), // M. Atk. Juice
		new ItemChanceHolder(10267, 6707, 1), // P. Atk. Potion
		new ItemChanceHolder(10268, 6707, 1), // Wind Walk Juice
		new ItemChanceHolder(5593, 9315, 9), // SP Scroll (Low-grade)
		new ItemChanceHolder(5594, 8384, 2), // SP Scroll (Mid-grade)
		new ItemChanceHolder(5595, 839, 1), // SP Scroll (High-grade)
		new ItemChanceHolder(10269, 6707, 1), // P. Def. Juice
		new ItemChanceHolder(21180, 7084, 1), // Transformation Scroll: Heretic - Event
		new ItemChanceHolder(21181, 5668, 1), // Transformation Scroll: Veil Master - Event
		new ItemChanceHolder(1538, 9446, 1), // Blessed Scroll of Escape
		new ItemChanceHolder(3936, 3542, 1), // Blessed Scroll of Resurrection
		new ItemChanceHolder(5577, 2834, 1), // Red Soul Crystal - Stage 11
		new ItemChanceHolder(5578, 2834, 1), // Green Soul Crystal - Stage 11
		new ItemChanceHolder(5579, 2834, 1), // Blue Soul Crystal - Stage 11
		new ItemChanceHolder(135, 481, 1), // Samurai Long Sword
		new ItemChanceHolder(21747, 1229, 1))); // Novice Adventurer's Treasure Sack
		
		DROPS.put(18275, Arrays.asList( // Treasure Chest
		new ItemChanceHolder(736, 9881, 5), // Scroll of Escape
		new ItemChanceHolder(1061, 6176, 4), // Major Healing Potion
		new ItemChanceHolder(737, 3294, 3), // Scroll of Resurrection
		new ItemChanceHolder(1539, 9881, 5), // Major Healing Potion
		new ItemChanceHolder(8625, 4941, 2), // Elixir of Life (B-grade)
		new ItemChanceHolder(8631, 4259, 2), // Elixir of Mind (B-grade)
		new ItemChanceHolder(8637, 8234, 3), // Elixir of CP (B-grade)
		new ItemChanceHolder(8636, 8234, 4), // Elixir of CP (C-grade)
		new ItemChanceHolder(8630, 5679, 2), // Elixir of Mind (C-grade)
		new ItemChanceHolder(8624, 6862, 2), // Elixir of Life (C-grade)
		new ItemChanceHolder(10260, 7601, 1), // Haste Potion
		new ItemChanceHolder(10261, 7601, 1), // Accuracy Juice
		new ItemChanceHolder(10262, 7601, 1), // Critical Damage Juice
		new ItemChanceHolder(10263, 7601, 1), // Critical Rate Juice
		new ItemChanceHolder(10264, 7601, 1), // Casting Spd. Juice
		new ItemChanceHolder(10265, 7601, 1), // Evasion Juice
		new ItemChanceHolder(10266, 7601, 1), // M. Atk. Juice
		new ItemChanceHolder(10267, 7601, 1), // P. Atk. Potion
		new ItemChanceHolder(10268, 7601, 1), // Wind Walk Juice
		new ItemChanceHolder(5593, 10557, 9), // SP Scroll (Low-grade)
		new ItemChanceHolder(5594, 9501, 2), // SP Scroll (Mid-grade)
		new ItemChanceHolder(5595, 951, 1), // SP Scroll (High-grade)
		new ItemChanceHolder(10269, 7601, 1), // P. Def. Juice
		new ItemChanceHolder(21180, 8028, 1), // Transformation Scroll: Heretic - Event
		new ItemChanceHolder(21181, 6423, 1), // Transformation Scroll: Veil Master - Event
		new ItemChanceHolder(1538, 10704, 1), // Blessed Scroll of Escape
		new ItemChanceHolder(3936, 4014, 1), // Blessed Scroll of Resurrection
		new ItemChanceHolder(5577, 3212, 1), // Red Soul Crystal - Stage 11
		new ItemChanceHolder(5578, 3212, 1), // Green Soul Crystal - Stage 11
		new ItemChanceHolder(5579, 3212, 1), // Blue Soul Crystal - Stage 11
		new ItemChanceHolder(135, 546, 1), // Samurai Long Sword
		new ItemChanceHolder(21747, 1393, 1))); // Novice Adventurer's Treasure Sack
		
		DROPS.put(18276, Arrays.asList( // Treasure Chest
		new ItemChanceHolder(736, 7727, 8), // Scroll of Escape
		new ItemChanceHolder(1061, 7727, 4), // Major Healing Potion
		new ItemChanceHolder(737, 4121, 3), // Scroll of Resurrection
		new ItemChanceHolder(8625, 6182, 2), // Elixir of Life (B-grade)
		new ItemChanceHolder(8631, 5329, 2), // Elixir of Mind (B-grade)
		new ItemChanceHolder(8637, 7727, 4), // Elixir of CP (B-grade)
		new ItemChanceHolder(8638, 8242, 3), // Elixir of CP (A-grade)
		new ItemChanceHolder(8632, 4293, 2), // Elixir of Mind (A-grade)
		new ItemChanceHolder(8626, 4945, 2), // Elixir of Life (A-grade)
		new ItemChanceHolder(10260, 4451, 1), // Haste Potion
		new ItemChanceHolder(10261, 4451, 1), // Accuracy Juice
		new ItemChanceHolder(10262, 4451, 1), // Critical Damage Juice
		new ItemChanceHolder(10263, 4451, 1), // Critical Rate Juice
		new ItemChanceHolder(10264, 4451, 1), // Casting Spd. Juice
		new ItemChanceHolder(10265, 4451, 1), // Evasion Juice
		new ItemChanceHolder(10266, 4451, 1), // M. Atk. Juice
		new ItemChanceHolder(10267, 4451, 1), // P. Atk. Potion
		new ItemChanceHolder(10268, 4451, 1), // Wind Walk Juice
		new ItemChanceHolder(5594, 5563, 2), // SP Scroll (Mid-grade)
		new ItemChanceHolder(5595, 557, 1), // SP Scroll (High-grade)
		new ItemChanceHolder(10269, 4451, 1), // P. Def. Juice
		new ItemChanceHolder(8736, 6439, 1), // Mid-grade Life Stone - Lv. 55
		new ItemChanceHolder(8737, 5563, 1), // Mid-grade Life Stone - Lv. 58
		new ItemChanceHolder(8738, 4636, 1), // Mid-grade Life Stone - Lv. 61
		new ItemChanceHolder(21182, 5786, 1), // Transformation Scroll: Saber Tooth Tiger - Event
		new ItemChanceHolder(21183, 4822, 1), // Transformation Scroll: Oel Mahum - Event
		new ItemChanceHolder(1538, 4822, 2), // Blessed Scroll of Escape
		new ItemChanceHolder(3936, 3616, 1), // Blessed Scroll of Resurrection
		new ItemChanceHolder(9648, 670, 1), // Transformation Sealbook: Onyx Beast
		new ItemChanceHolder(9649, 804, 1), // Transformation Sealbook: Doom Wraith
		new ItemChanceHolder(5580, 145, 1), // Red Soul Crystal - Stage 12
		new ItemChanceHolder(5581, 145, 1), // Green Soul Crystal - Stage 12
		new ItemChanceHolder(5582, 145, 1), // Blue Soul Crystal - Stage 12
		new ItemChanceHolder(142, 217, 1), // Keshanberk
		new ItemChanceHolder(21748, 92, 1))); // Experienced Adventurer's Treasure Sack
		
		DROPS.put(18277, Arrays.asList( // Treasure Chest
		new ItemChanceHolder(736, 8657, 8), // Scroll of Escape
		new ItemChanceHolder(1061, 8657, 4), // Major Healing Potion
		new ItemChanceHolder(737, 4617, 3), // Scroll of Resurrection
		new ItemChanceHolder(8625, 6926, 2), // Elixir of Life (B-grade)
		new ItemChanceHolder(8631, 5971, 2), // Elixir of Mind (B-grade)
		new ItemChanceHolder(8637, 8657, 4), // Elixir of CP (B-grade)
		new ItemChanceHolder(8638, 9234, 3), // Elixir of CP (A-grade)
		new ItemChanceHolder(8632, 4810, 2), // Elixir of Mind (A-grade)
		new ItemChanceHolder(8626, 5541, 2), // Elixir of Life (A-grade)
		new ItemChanceHolder(10260, 4987, 1), // Haste Potion
		new ItemChanceHolder(10261, 4987, 1), // Accuracy Juice
		new ItemChanceHolder(10262, 4987, 1), // Critical Damage Juice
		new ItemChanceHolder(10263, 4987, 1), // Critical Rate Juice
		new ItemChanceHolder(10264, 4987, 1), // Casting Spd. Juice
		new ItemChanceHolder(10265, 4987, 1), // Evasion Juice
		new ItemChanceHolder(10266, 4987, 1), // M. Atk. Juice
		new ItemChanceHolder(10267, 4987, 1), // P. Atk. Potion
		new ItemChanceHolder(10268, 4987, 1), // Wind Walk Juice
		new ItemChanceHolder(5594, 6233, 2), // SP Scroll (Mid-grade)
		new ItemChanceHolder(5595, 624, 1), // SP Scroll (High-grade)
		new ItemChanceHolder(10269, 4987, 1), // P. Def. Juice
		new ItemChanceHolder(8736, 7214, 1), // Mid-grade Life Stone - Lv. 55
		new ItemChanceHolder(8737, 6233, 1), // Mid-grade Life Stone - Lv. 58
		new ItemChanceHolder(8738, 5195, 1), // Mid-grade Life Stone - Lv. 61
		new ItemChanceHolder(21183, 5402, 1), // Transformation Scroll: Oel Mahum - Event
		new ItemChanceHolder(21184, 5402, 1), // Transformation Scroll: Doll Blader - Event
		new ItemChanceHolder(1538, 5402, 2), // Blessed Scroll of Escape
		new ItemChanceHolder(3936, 4052, 1), // Blessed Scroll of Resurrection
		new ItemChanceHolder(9648, 751, 1), // Transformation Sealbook: Onyx Beast
		new ItemChanceHolder(9649, 901, 1), // Transformation Sealbook: Doom Wraith
		new ItemChanceHolder(5580, 163, 1), // Red Soul Crystal - Stage 12
		new ItemChanceHolder(5581, 163, 1), // Green Soul Crystal - Stage 12
		new ItemChanceHolder(5582, 163, 1), // Blue Soul Crystal - Stage 12
		new ItemChanceHolder(79, 161, 1), // Damascus Sword
		new ItemChanceHolder(21748, 103, 1))); // Experienced Adventurer's Treasure Sack
		
		DROPS.put(18278, Arrays.asList( // Treasure Chest
		new ItemChanceHolder(736, 9646, 8), // Scroll of Escape
		new ItemChanceHolder(1061, 9646, 4), // Major Healing Potion
		new ItemChanceHolder(737, 5145, 3), // Scroll of Resurrection
		new ItemChanceHolder(8625, 7717, 2), // Elixir of Life (B-grade)
		new ItemChanceHolder(8631, 6652, 2), // Elixir of Mind (B-grade)
		new ItemChanceHolder(8637, 9646, 4), // Elixir of CP (B-grade)
		new ItemChanceHolder(8638, 10289, 3), // Elixir of CP (A-grade)
		new ItemChanceHolder(8632, 5359, 2), // Elixir of Mind (A-grade)
		new ItemChanceHolder(8626, 6173, 2), // Elixir of Life (A-grade)
		new ItemChanceHolder(10260, 5556, 1), // Haste Potion
		new ItemChanceHolder(10261, 5556, 1), // Accuracy Juice
		new ItemChanceHolder(10262, 5556, 1), // Critical Damage Juice
		new ItemChanceHolder(10263, 5556, 1), // Critical Rate Juice
		new ItemChanceHolder(10264, 5556, 1), // Casting Spd. Juice
		new ItemChanceHolder(10265, 5556, 1), // Evasion Juice
		new ItemChanceHolder(10266, 5556, 1), // M. Atk. Juice
		new ItemChanceHolder(10267, 5556, 1), // P. Atk. Potion
		new ItemChanceHolder(10268, 5556, 1), // Wind Walk Juice
		new ItemChanceHolder(5594, 6945, 2), // SP Scroll (Mid-grade)
		new ItemChanceHolder(5595, 695, 1), // SP Scroll (High-grade)
		new ItemChanceHolder(10269, 5556, 1), // P. Def. Juice
		new ItemChanceHolder(8736, 8038, 1), // Mid-grade Life Stone - Lv. 55
		new ItemChanceHolder(8737, 6945, 1), // Mid-grade Life Stone - Lv. 58
		new ItemChanceHolder(8738, 5788, 1), // Mid-grade Life Stone - Lv. 61
		new ItemChanceHolder(21183, 6019, 1), // Transformation Scroll: Oel Mahum - Event
		new ItemChanceHolder(21184, 6019, 1), // Transformation Scroll: Doll Blader - Event
		new ItemChanceHolder(1538, 6019, 2), // Blessed Scroll of Escape
		new ItemChanceHolder(3936, 4514, 1), // Blessed Scroll of Resurrection
		new ItemChanceHolder(9648, 836, 1), // Transformation Sealbook: Onyx Beast
		new ItemChanceHolder(9649, 1004, 1), // Transformation Sealbook: Doom Wraith
		new ItemChanceHolder(5580, 181, 1), // Red Soul Crystal - Stage 12
		new ItemChanceHolder(5581, 181, 1), // Green Soul Crystal - Stage 12
		new ItemChanceHolder(5582, 181, 1), // Blue Soul Crystal - Stage 12
		new ItemChanceHolder(79, 179, 1), // Damascus Sword
		new ItemChanceHolder(21748, 115, 1))); // Experienced Adventurer's Treasure Sack
		
		DROPS.put(18279, Arrays.asList( // Treasure Chest
		new ItemChanceHolder(8627, 5714, 2), // Elixir of Life (S-grade)
		new ItemChanceHolder(8633, 5102, 2), // Elixir of Mind (S-grade)
		new ItemChanceHolder(8639, 5714, 5), // Elixir of CP (S-grade)
		new ItemChanceHolder(8638, 5714, 6), // Elixir of CP (A-grade)
		new ItemChanceHolder(8632, 5953, 2), // Elixir of Mind (A-grade)
		new ItemChanceHolder(8626, 4572, 3), // Elixir of Life (A-grade)
		new ItemChanceHolder(729, 96, 1), // Scroll: Enchant Weapon (A-grade)
		new ItemChanceHolder(730, 715, 1), // Scroll: Enchant Armor (A-grade)
		new ItemChanceHolder(1540, 4286, 4), // Quick Healing Potion
		new ItemChanceHolder(10260, 1929, 3), // Haste Potion
		new ItemChanceHolder(10261, 1929, 3), // Accuracy Juice
		new ItemChanceHolder(10262, 1929, 3), // Critical Damage Juice
		new ItemChanceHolder(10263, 1929, 3), // Critical Rate Juice
		new ItemChanceHolder(10264, 1929, 3), // Casting Spd. Juice
		new ItemChanceHolder(10265, 1929, 3), // Evasion Juice
		new ItemChanceHolder(10266, 1929, 3), // M. Atk. Juice
		new ItemChanceHolder(10267, 1929, 3), // P. Atk. Potion
		new ItemChanceHolder(10268, 1929, 3), // Wind Walk Juice
		new ItemChanceHolder(5595, 724, 1), // SP Scroll (High-grade)
		new ItemChanceHolder(9898, 724, 1), // SP Scroll (Top-grade)
		new ItemChanceHolder(10269, 1929, 3), // P. Def. Juice
		new ItemChanceHolder(8739, 4822, 1), // Mid-grade Life Stone - Lv. 64
		new ItemChanceHolder(8740, 4018, 1), // Mid-grade Life Stone - Lv. 67
		new ItemChanceHolder(8741, 3349, 1), // Mid-grade Life Stone - Lv. 70
		new ItemChanceHolder(8742, 3014, 1), // Mid-grade Life Stone - Lv. 76
		new ItemChanceHolder(21180, 9117, 1), // Transformation Scroll: Heretic - Event
		new ItemChanceHolder(21181, 7294, 1), // Transformation Scroll: Veil Master - Event
		new ItemChanceHolder(21182, 7294, 1), // Transformation Scroll: Saber Tooth Tiger - Event
		new ItemChanceHolder(1538, 6078, 2), // Blessed Scroll of Escape
		new ItemChanceHolder(3936, 4559, 1), // Blessed Scroll of Resurrection
		new ItemChanceHolder(9654, 845, 1), // Transformation Sealbook: Inferno Drake
		new ItemChanceHolder(9655, 845, 1), // Transformation Sealbook: Dragon Bomber
		new ItemChanceHolder(5580, 183, 1), // Red Soul Crystal - Stage 12
		new ItemChanceHolder(5581, 183, 1), // Green Soul Crystal - Stage 12
		new ItemChanceHolder(5582, 183, 1), // Blue Soul Crystal - Stage 12
		new ItemChanceHolder(80, 130, 1), // Tallum Blade
		new ItemChanceHolder(21748, 128, 1))); // Experienced Adventurer's Treasure Sack
		
		DROPS.put(18280, Arrays.asList( // Treasure Chest
		new ItemChanceHolder(8627, 6323, 2), // Elixir of Life (S-grade)
		new ItemChanceHolder(8633, 5646, 2), // Elixir of Mind (S-grade)
		new ItemChanceHolder(8639, 6323, 5), // Elixir of CP (S-grade)
		new ItemChanceHolder(8638, 6323, 6), // Elixir of CP (A-grade)
		new ItemChanceHolder(8632, 6587, 2), // Elixir of Mind (A-grade)
		new ItemChanceHolder(8626, 5059, 3), // Elixir of Life (A-grade)
		new ItemChanceHolder(729, 106, 1), // Scroll: Enchant Weapon (A-grade)
		new ItemChanceHolder(730, 791, 1), // Scroll: Enchant Armor (A-grade)
		new ItemChanceHolder(1540, 4742, 4), // Quick Healing Potion
		new ItemChanceHolder(10260, 2134, 3), // Haste Potion
		new ItemChanceHolder(10261, 2134, 3), // Accuracy Juice
		new ItemChanceHolder(10262, 2134, 3), // Critical Damage Juice
		new ItemChanceHolder(10263, 2134, 3), // Critical Rate Juice
		new ItemChanceHolder(10264, 2134, 3), // Casting Spd. Juice
		new ItemChanceHolder(10265, 2134, 3), // Evasion Juice
		new ItemChanceHolder(10266, 2134, 3), // M. Atk. Juice
		new ItemChanceHolder(10267, 2134, 3), // P. Atk. Potion
		new ItemChanceHolder(10268, 2134, 3), // Wind Walk Juice
		new ItemChanceHolder(5595, 801, 1), // SP Scroll (High-grade)
		new ItemChanceHolder(9898, 801, 1), // SP Scroll (Top-grade)
		new ItemChanceHolder(10269, 2134, 3), // P. Def. Juice
		new ItemChanceHolder(8739, 5335, 1), // Mid-grade Life Stone - Lv. 64
		new ItemChanceHolder(8740, 4446, 1), // Mid-grade Life Stone - Lv. 67
		new ItemChanceHolder(8741, 3705, 1), // Mid-grade Life Stone - Lv. 70
		new ItemChanceHolder(8742, 3335, 1), // Mid-grade Life Stone - Lv. 76
		new ItemChanceHolder(21180, 10088, 1), // Transformation Scroll: Heretic - Event
		new ItemChanceHolder(21181, 8070, 1), // Transformation Scroll: Veil Master - Event
		new ItemChanceHolder(21182, 8070, 1), // Transformation Scroll: Saber Tooth Tiger - Event
		new ItemChanceHolder(1538, 6725, 2), // Blessed Scroll of Escape
		new ItemChanceHolder(3936, 5044, 1), // Blessed Scroll of Resurrection
		new ItemChanceHolder(9654, 935, 1), // Transformation Sealbook: Inferno Drake
		new ItemChanceHolder(9655, 935, 1), // Transformation Sealbook: Dragon Bomber
		new ItemChanceHolder(5580, 202, 1), // Red Soul Crystal - Stage 12
		new ItemChanceHolder(5581, 202, 1), // Green Soul Crystal - Stage 12
		new ItemChanceHolder(5582, 202, 1), // Blue Soul Crystal - Stage 12
		new ItemChanceHolder(80, 144, 1), // Tallum Blade
		new ItemChanceHolder(21748, 141, 1))); // Experienced Adventurer's Treasure Sack
		
		DROPS.put(18281, Arrays.asList( // Treasure Chest
		new ItemChanceHolder(8627, 6967, 2), // Elixir of Life (S-grade)
		new ItemChanceHolder(8633, 6220, 2), // Elixir of Mind (S-grade)
		new ItemChanceHolder(8639, 6967, 5), // Elixir of CP (S-grade)
		new ItemChanceHolder(8638, 6967, 6), // Elixir of CP (A-grade)
		new ItemChanceHolder(8632, 7257, 2), // Elixir of Mind (A-grade)
		new ItemChanceHolder(8626, 5573, 3), // Elixir of Life (A-grade)
		new ItemChanceHolder(729, 117, 1), // Scroll: Enchant Weapon (A-grade)
		new ItemChanceHolder(730, 871, 1), // Scroll: Enchant Armor (A-grade)
		new ItemChanceHolder(1540, 5225, 4), // Quick Healing Potion
		new ItemChanceHolder(10260, 2352, 3), // Haste Potion
		new ItemChanceHolder(10261, 2352, 3), // Accuracy Juice
		new ItemChanceHolder(10262, 2352, 3), // Critical Damage Juice
		new ItemChanceHolder(10263, 2352, 3), // Critical Rate Juice
		new ItemChanceHolder(10264, 2352, 3), // Casting Spd. Juice
		new ItemChanceHolder(10265, 2352, 3), // Evasion Juice
		new ItemChanceHolder(10266, 2352, 3), // M. Atk. Juice
		new ItemChanceHolder(10267, 2352, 3), // P. Atk. Potion
		new ItemChanceHolder(10268, 2352, 3), // Wind Walk Juice
		new ItemChanceHolder(5595, 882, 1), // SP Scroll (High-grade)
		new ItemChanceHolder(9898, 882, 1), // SP Scroll (Top-grade)
		new ItemChanceHolder(10269, 2352, 3), // P. Def. Juice
		new ItemChanceHolder(8739, 5878, 1), // Mid-grade Life Stone - Lv. 64
		new ItemChanceHolder(8740, 4899, 1), // Mid-grade Life Stone - Lv. 67
		new ItemChanceHolder(8741, 4082, 1), // Mid-grade Life Stone - Lv. 70
		new ItemChanceHolder(8742, 3674, 1), // Mid-grade Life Stone - Lv. 76
		new ItemChanceHolder(21183, 7410, 1), // Transformation Scroll: Oel Mahum - Event
		new ItemChanceHolder(21184, 7410, 1), // Transformation Scroll: Doll Blader - Event
		new ItemChanceHolder(21185, 3705, 1), // Transformation Scroll: Zaken - Event
		new ItemChanceHolder(1538, 7410, 2), // Blessed Scroll of Escape
		new ItemChanceHolder(3936, 5558, 1), // Blessed Scroll of Resurrection
		new ItemChanceHolder(9654, 1030, 1), // Transformation Sealbook: Inferno Drake
		new ItemChanceHolder(9655, 1030, 1), // Transformation Sealbook: Dragon Bomber
		new ItemChanceHolder(5908, 112, 1), // Red Soul Crystal: Stage 13
		new ItemChanceHolder(5911, 112, 1), // Green Soul Crystal - Stage 13
		new ItemChanceHolder(5914, 112, 1), // Blue Soul Crystal: Stage 13
		new ItemChanceHolder(6364, 52, 1), // Forgotten Blade
		new ItemChanceHolder(21748, 156, 1))); // Experienced Adventurer's Treasure Sack
		
		DROPS.put(18282, Arrays.asList( // Treasure Chest
		new ItemChanceHolder(8627, 7649, 2), // Elixir of Life (S-grade)
		new ItemChanceHolder(8633, 6829, 2), // Elixir of Mind (S-grade)
		new ItemChanceHolder(8639, 7649, 5), // Elixir of CP (S-grade)
		new ItemChanceHolder(8638, 7649, 6), // Elixir of CP (A-grade)
		new ItemChanceHolder(8632, 7968, 2), // Elixir of Mind (A-grade)
		new ItemChanceHolder(8626, 6119, 3), // Elixir of Life (A-grade)
		new ItemChanceHolder(729, 128, 1), // Scroll: Enchant Weapon (A-grade)
		new ItemChanceHolder(730, 957, 1), // Scroll: Enchant Armor (A-grade)
		new ItemChanceHolder(1540, 5737, 4), // Quick Healing Potion
		new ItemChanceHolder(10260, 2582, 3), // Haste Potion
		new ItemChanceHolder(10261, 2582, 3), // Accuracy Juice
		new ItemChanceHolder(10262, 2582, 3), // Critical Damage Juice
		new ItemChanceHolder(10263, 2582, 3), // Critical Rate Juice
		new ItemChanceHolder(10264, 2582, 3), // Casting Spd. Juice
		new ItemChanceHolder(10265, 2582, 3), // Evasion Juice
		new ItemChanceHolder(10266, 2582, 3), // M. Atk. Juice
		new ItemChanceHolder(10267, 2582, 3), // P. Atk. Potion
		new ItemChanceHolder(10268, 2582, 3), // Wind Walk Juice
		new ItemChanceHolder(5595, 968, 1), // SP Scroll (High-grade)
		new ItemChanceHolder(9898, 968, 1), // SP Scroll (Top-grade)
		new ItemChanceHolder(10269, 2582, 3), // P. Def. Juice
		new ItemChanceHolder(8739, 6454, 1), // Mid-grade Life Stone - Lv. 64
		new ItemChanceHolder(8740, 5378, 1), // Mid-grade Life Stone - Lv. 67
		new ItemChanceHolder(8741, 4482, 1), // Mid-grade Life Stone - Lv. 70
		new ItemChanceHolder(8742, 4034, 1), // Mid-grade Life Stone - Lv. 76
		new ItemChanceHolder(21183, 8136, 1), // Transformation Scroll: Oel Mahum - Event
		new ItemChanceHolder(21184, 8136, 1), // Transformation Scroll: Doll Blader - Event
		new ItemChanceHolder(21185, 4068, 1), // Transformation Scroll: Zaken - Event
		new ItemChanceHolder(1538, 8136, 2), // Blessed Scroll of Escape
		new ItemChanceHolder(3936, 6102, 1), // Blessed Scroll of Resurrection
		new ItemChanceHolder(9654, 1130, 1), // Transformation Sealbook: Inferno Drake
		new ItemChanceHolder(9655, 1130, 1), // Transformation Sealbook: Dragon Bomber
		new ItemChanceHolder(5908, 123, 1), // Red Soul Crystal: Stage 13
		new ItemChanceHolder(5911, 123, 1), // Green Soul Crystal - Stage 13
		new ItemChanceHolder(5914, 123, 1), // Blue Soul Crystal: Stage 13
		new ItemChanceHolder(6364, 58, 1), // Forgotten Blade
		new ItemChanceHolder(21748, 171, 1))); // Experienced Adventurer's Treasure Sack
		
		DROPS.put(18283, Arrays.asList( // Treasure Chest
		new ItemChanceHolder(8627, 8366, 2), // Elixir of Life (S-grade)
		new ItemChanceHolder(8633, 7470, 2), // Elixir of Mind (S-grade)
		new ItemChanceHolder(8639, 8366, 5), // Elixir of CP (S-grade)
		new ItemChanceHolder(8638, 8366, 6), // Elixir of CP (A-grade)
		new ItemChanceHolder(8632, 8715, 2), // Elixir of Mind (A-grade)
		new ItemChanceHolder(8626, 6693, 3), // Elixir of Life (A-grade)
		new ItemChanceHolder(729, 140, 1), // Scroll: Enchant Weapon (A-grade)
		new ItemChanceHolder(730, 1046, 1), // Scroll: Enchant Armor (A-grade)
		new ItemChanceHolder(1540, 6275, 4), // Quick Healing Potion
		new ItemChanceHolder(10260, 2824, 3), // Haste Potion
		new ItemChanceHolder(10261, 2824, 3), // Accuracy Juice
		new ItemChanceHolder(10262, 2824, 3), // Critical Damage Juice
		new ItemChanceHolder(10263, 2824, 3), // Critical Rate Juice
		new ItemChanceHolder(10264, 2824, 3), // Casting Spd. Juice
		new ItemChanceHolder(10265, 2824, 3), // Evasion Juice
		new ItemChanceHolder(10266, 2824, 3), // M. Atk. Juice
		new ItemChanceHolder(10267, 2824, 3), // P. Atk. Potion
		new ItemChanceHolder(10268, 2824, 3), // Wind Walk Juice
		new ItemChanceHolder(5595, 1059, 1), // SP Scroll (High-grade)
		new ItemChanceHolder(9898, 1059, 1), // SP Scroll (Top-grade)
		new ItemChanceHolder(10269, 2824, 3), // P. Def. Juice
		new ItemChanceHolder(8739, 7059, 1), // Mid-grade Life Stone - Lv. 64
		new ItemChanceHolder(8740, 5883, 1), // Mid-grade Life Stone - Lv. 67
		new ItemChanceHolder(8741, 4902, 1), // Mid-grade Life Stone - Lv. 70
		new ItemChanceHolder(8742, 4412, 1), // Mid-grade Life Stone - Lv. 76
		new ItemChanceHolder(21183, 8898, 1), // Transformation Scroll: Oel Mahum - Event
		new ItemChanceHolder(21184, 8898, 1), // Transformation Scroll: Doll Blader - Event
		new ItemChanceHolder(21185, 4449, 1), // Transformation Scroll: Zaken - Event
		new ItemChanceHolder(1538, 8898, 2), // Blessed Scroll of Escape
		new ItemChanceHolder(3936, 6674, 1), // Blessed Scroll of Resurrection
		new ItemChanceHolder(9654, 1236, 1), // Transformation Sealbook: Inferno Drake
		new ItemChanceHolder(9655, 1236, 1), // Transformation Sealbook: Dragon Bomber
		new ItemChanceHolder(5908, 134, 1), // Red Soul Crystal: Stage 13
		new ItemChanceHolder(5911, 134, 1), // Green Soul Crystal - Stage 13
		new ItemChanceHolder(5914, 134, 1), // Blue Soul Crystal: Stage 13
		new ItemChanceHolder(6364, 63, 1), // Forgotten Blade
		new ItemChanceHolder(21748, 187, 1))); // Experienced Adventurer's Treasure Sack
		
		DROPS.put(18284, Arrays.asList( // Treasure Chest
		new ItemChanceHolder(8627, 6836, 2), // Elixir of Life (S-grade)
		new ItemChanceHolder(8633, 6103, 2), // Elixir of Mind (S-grade)
		new ItemChanceHolder(8639, 10000, 4), // Elixir of CP (S-grade)
		new ItemChanceHolder(9546, 821, 1), // Fire Stone
		new ItemChanceHolder(9547, 821, 1), // Water Stone
		new ItemChanceHolder(9548, 821, 1), // Earth Stone
		new ItemChanceHolder(9549, 821, 1), // Wind Stone
		new ItemChanceHolder(9550, 821, 1), // Dark Stone
		new ItemChanceHolder(9551, 821, 1), // Holy Stone
		new ItemChanceHolder(959, 42, 1), // Scroll: Enchant Weapon (S-grade)
		new ItemChanceHolder(960, 411, 1), // Scroll: Enchant Armor (S-grade)
		new ItemChanceHolder(14701, 2051, 2), // Superior Quick Healing Potion
		new ItemChanceHolder(10260, 3076, 3), // Haste Potion
		new ItemChanceHolder(10261, 3076, 3), // Accuracy Juice
		new ItemChanceHolder(10262, 3076, 3), // Critical Damage Juice
		new ItemChanceHolder(10263, 3076, 3), // Critical Rate Juice
		new ItemChanceHolder(10264, 3076, 3), // Casting Spd. Juice
		new ItemChanceHolder(10265, 3076, 3), // Evasion Juice
		new ItemChanceHolder(10266, 3076, 3), // M. Atk. Juice
		new ItemChanceHolder(10267, 3076, 3), // P. Atk. Potion
		new ItemChanceHolder(10268, 3076, 3), // Wind Walk Juice
		new ItemChanceHolder(5595, 577, 2), // SP Scroll (High-grade)
		new ItemChanceHolder(9898, 231, 1), // SP Scroll (Top-grade)
		new ItemChanceHolder(17185, 116, 1), // Scroll: 10,000 SP
		new ItemChanceHolder(10269, 3076, 3), // P. Def. Juice
		new ItemChanceHolder(9574, 4006, 1), // Mid-grade Life Stone - Lv. 80
		new ItemChanceHolder(10484, 3338, 1), // Mid-grade Life Stone - Lv. 82
		new ItemChanceHolder(14167, 2783, 1), // Mid-grade Life Stone - Lv. 84
		new ItemChanceHolder(21185, 2539, 1), // Transformation Scroll: Zaken - Event
		new ItemChanceHolder(21186, 1524, 1), // Transformation Scroll: Anakim - Event
		new ItemChanceHolder(21187, 2177, 1), // Transformation Scroll: Venom - Event
		new ItemChanceHolder(21188, 2177, 1), // Transformation Scroll: Gordon - Event
		new ItemChanceHolder(21189, 2177, 1), // Transformation Scroll: Ranku - Event
		new ItemChanceHolder(21190, 2177, 1), // Transformation Scroll: Kechi - Event
		new ItemChanceHolder(21191, 2177, 1), // Transformation Scroll: Demon Prince - Event
		new ItemChanceHolder(9552, 191, 1), // Fire Crystal
		new ItemChanceHolder(9553, 191, 1), // Water Crystal
		new ItemChanceHolder(9554, 191, 1), // Earth Crystal
		new ItemChanceHolder(9555, 191, 1), // Wind Crystal
		new ItemChanceHolder(9556, 191, 1), // Dark Crystal
		new ItemChanceHolder(9557, 191, 1), // Holy Crystal
		new ItemChanceHolder(6622, 3047, 1), // Lesser Giant's Codex
		new ItemChanceHolder(9627, 191, 1), // Lesser Giant's Codex - Mastery
		new ItemChanceHolder(1538, 5078, 2), // Blessed Scroll of Escape
		new ItemChanceHolder(3936, 3809, 1), // Blessed Scroll of Resurrection
		new ItemChanceHolder(9570, 39, 1), // Red Soul Crystal - Stage 14
		new ItemChanceHolder(9572, 39, 1), // Green Soul Crystal - Stage 14
		new ItemChanceHolder(9571, 39, 1), // Blue Soul Crystal - Stage 14
		new ItemChanceHolder(9442, 21, 1), // Dynasty Sword
		new ItemChanceHolder(21749, 25, 1))); // Great Adventurer's Treasure Sack
		
		DROPS.put(18285, Arrays.asList( // Treasure Chest
		new ItemChanceHolder(8627, 7420, 2), // Elixir of Life (S-grade)
		new ItemChanceHolder(8633, 6625, 2), // Elixir of Mind (S-grade)
		new ItemChanceHolder(8639, 10000, 4), // Elixir of CP (S-grade)
		new ItemChanceHolder(9546, 891, 1), // Fire Stone
		new ItemChanceHolder(9547, 891, 1), // Water Stone
		new ItemChanceHolder(9548, 891, 1), // Earth Stone
		new ItemChanceHolder(9549, 891, 1), // Wind Stone
		new ItemChanceHolder(9550, 891, 1), // Dark Stone
		new ItemChanceHolder(9551, 891, 1), // Holy Stone
		new ItemChanceHolder(959, 45, 1), // Scroll: Enchant Weapon (S-grade)
		new ItemChanceHolder(960, 446, 1), // Scroll: Enchant Armor (S-grade)
		new ItemChanceHolder(14701, 2226, 2), // Superior Quick Healing Potion
		new ItemChanceHolder(10260, 3339, 3), // Haste Potion
		new ItemChanceHolder(10261, 3339, 3), // Accuracy Juice
		new ItemChanceHolder(10262, 3339, 3), // Critical Damage Juice
		new ItemChanceHolder(10263, 3339, 3), // Critical Rate Juice
		new ItemChanceHolder(10264, 3339, 3), // Casting Spd. Juice
		new ItemChanceHolder(10265, 3339, 3), // Evasion Juice
		new ItemChanceHolder(10266, 3339, 3), // M. Atk. Juice
		new ItemChanceHolder(10267, 3339, 3), // P. Atk. Potion
		new ItemChanceHolder(10268, 3339, 3), // Wind Walk Juice
		new ItemChanceHolder(5595, 627, 2), // SP Scroll (High-grade)
		new ItemChanceHolder(9898, 251, 1), // SP Scroll (Top-grade)
		new ItemChanceHolder(17185, 126, 1), // Scroll: 10,000 SP
		new ItemChanceHolder(10269, 3339, 3), // P. Def. Juice
		new ItemChanceHolder(9574, 4348, 1), // Mid-grade Life Stone - Lv. 80
		new ItemChanceHolder(10484, 3623, 1), // Mid-grade Life Stone - Lv. 82
		new ItemChanceHolder(14167, 3021, 1), // Mid-grade Life Stone - Lv. 84
		new ItemChanceHolder(21185, 2756, 1), // Transformation Scroll: Zaken - Event
		new ItemChanceHolder(21186, 1654, 1), // Transformation Scroll: Anakim - Event
		new ItemChanceHolder(21187, 2363, 1), // Transformation Scroll: Venom - Event
		new ItemChanceHolder(21188, 2363, 1), // Transformation Scroll: Gordon - Event
		new ItemChanceHolder(21189, 2363, 1), // Transformation Scroll: Ranku - Event
		new ItemChanceHolder(21190, 2363, 1), // Transformation Scroll: Kechi - Event
		new ItemChanceHolder(21191, 2363, 1), // Transformation Scroll: Demon Prince - Event
		new ItemChanceHolder(9552, 207, 1), // Fire Crystal
		new ItemChanceHolder(9553, 207, 1), // Water Crystal
		new ItemChanceHolder(9554, 207, 1), // Earth Crystal
		new ItemChanceHolder(9555, 207, 1), // Wind Crystal
		new ItemChanceHolder(9556, 207, 1), // Dark Crystal
		new ItemChanceHolder(9557, 207, 1), // Holy Crystal
		new ItemChanceHolder(6622, 3308, 1), // Lesser Giant's Codex
		new ItemChanceHolder(9627, 207, 1), // Lesser Giant's Codex - Mastery
		new ItemChanceHolder(1538, 5512, 2), // Blessed Scroll of Escape
		new ItemChanceHolder(3936, 4134, 1), // Blessed Scroll of Resurrection
		new ItemChanceHolder(10480, 21, 1), // Red Soul Crystal - Stage 15
		new ItemChanceHolder(10482, 21, 1), // Green Soul Crystal - Stage 15
		new ItemChanceHolder(10481, 21, 1), // Blue Soul Crystal - Stage 15
		new ItemChanceHolder(10215, 16, 1), // Icarus Sawsword
		new ItemChanceHolder(21749, 27, 1))); // Great Adventurer's Treasure Sack
		
		DROPS.put(18286, Arrays.asList( // Treasure Chest
		new ItemChanceHolder(8627, 8005, 2), // Elixir of Life (S-grade)
		new ItemChanceHolder(8633, 7147, 2), // Elixir of Mind (S-grade)
		new ItemChanceHolder(8639, 10000, 4), // Elixir of CP (S-grade)
		new ItemChanceHolder(9546, 961, 1), // Fire Stone
		new ItemChanceHolder(9547, 961, 1), // Water Stone
		new ItemChanceHolder(9548, 961, 1), // Earth Stone
		new ItemChanceHolder(9549, 961, 1), // Wind Stone
		new ItemChanceHolder(9550, 961, 1), // Dark Stone
		new ItemChanceHolder(9551, 961, 1), // Holy Stone
		new ItemChanceHolder(959, 49, 1), // Scroll: Enchant Weapon (S-grade)
		new ItemChanceHolder(960, 481, 1), // Scroll: Enchant Armor (S-grade)
		new ItemChanceHolder(14701, 2402, 2), // Superior Quick Healing Potion
		new ItemChanceHolder(10260, 3602, 3), // Haste Potion
		new ItemChanceHolder(10261, 3602, 3), // Accuracy Juice
		new ItemChanceHolder(10262, 3602, 3), // Critical Damage Juice
		new ItemChanceHolder(10263, 3602, 3), // Critical Rate Juice
		new ItemChanceHolder(10264, 3602, 3), // Casting Spd. Juice
		new ItemChanceHolder(10265, 3602, 3), // Evasion Juice
		new ItemChanceHolder(10266, 3602, 3), // M. Atk. Juice
		new ItemChanceHolder(10267, 3602, 3), // P. Atk. Potion
		new ItemChanceHolder(10268, 3602, 3), // Wind Walk Juice
		new ItemChanceHolder(5595, 676, 2), // SP Scroll (High-grade)
		new ItemChanceHolder(9898, 271, 1), // SP Scroll (Top-grade)
		new ItemChanceHolder(17185, 136, 1), // Scroll: 10,000 SP
		new ItemChanceHolder(10269, 3602, 3), // P. Def. Juice
		new ItemChanceHolder(9574, 4690, 1), // Mid-grade Life Stone - Lv. 80
		new ItemChanceHolder(10484, 3909, 1), // Mid-grade Life Stone - Lv. 82
		new ItemChanceHolder(14167, 3259, 1), // Mid-grade Life Stone - Lv. 84
		new ItemChanceHolder(21185, 2973, 1), // Transformation Scroll: Zaken - Event
		new ItemChanceHolder(21186, 1784, 1), // Transformation Scroll: Anakim - Event
		new ItemChanceHolder(21187, 2549, 1), // Transformation Scroll: Venom - Event
		new ItemChanceHolder(21188, 2549, 1), // Transformation Scroll: Gordon - Event
		new ItemChanceHolder(21189, 2549, 1), // Transformation Scroll: Ranku - Event
		new ItemChanceHolder(21190, 2549, 1), // Transformation Scroll: Kechi - Event
		new ItemChanceHolder(21191, 2549, 1), // Transformation Scroll: Demon Prince - Event
		new ItemChanceHolder(9552, 223, 1), // Fire Crystal
		new ItemChanceHolder(9553, 223, 1), // Water Crystal
		new ItemChanceHolder(9554, 223, 1), // Earth Crystal
		new ItemChanceHolder(9555, 223, 1), // Wind Crystal
		new ItemChanceHolder(9556, 223, 1), // Dark Crystal
		new ItemChanceHolder(9557, 223, 1), // Holy Crystal
		new ItemChanceHolder(6622, 3568, 1), // Lesser Giant's Codex
		new ItemChanceHolder(9627, 223, 1), // Lesser Giant's Codex - Mastery
		new ItemChanceHolder(1538, 5946, 2), // Blessed Scroll of Escape
		new ItemChanceHolder(3936, 4460, 1), // Blessed Scroll of Resurrection
		new ItemChanceHolder(13071, 12, 1), // Red Soul Crystal - Stage 16
		new ItemChanceHolder(13073, 12, 1), // Green Soul Crystal - Stage 16
		new ItemChanceHolder(13072, 12, 1), // Blue Soul Crystal - Stage 16
		new ItemChanceHolder(13457, 13, 1), // Vesper Cutter
		new ItemChanceHolder(21749, 29, 1))); // Great Adventurer's Treasure Sack
	}
	
	public TreasureChest()
	{
		super(TreasureChest.class.getSimpleName(), "ai/group_template");
		
		addSpawnId(DROPS.keySet());
		addAttackId(DROPS.keySet());
	}
	
	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		switch (event)
		{
			case TIMER_1:
			case TIMER_2:
			{
				npc.deleteMe();
				break;
			}
		}
		return super.onAdvEvent(event, npc, player);
	}
	
	@Override
	public String onSpawn(L2Npc npc)
	{
		L2ChestInstance chest = (L2ChestInstance) npc;
		chest.disableCoreAI(true);
		chest.setIsNoRndWalk(true);
		chest.setMustRewardExpSp(false);
		chest.enableItemDrop(false);
		chest.resetInteract();
		
		npc.getVariables().set("MAESTRO_SKILL_USED", 0);
		startQuestTimer(TIMER_2, MAX_SPAWN_TIME, npc, null);
		return super.onSpawn(npc);
	}
	
	@Override
	public String onAttack(L2Npc npc, L2PcInstance attacker, int damage, boolean isSummon, L2Skill skill)
	{
		if (attacker.getLevel() < PLAYER_LEVEL_THRESHOLD)
		{
			npc.getVariables().set("MAX_LEVEL_DIFFERENCE", 6);
		}
		else
		{
			npc.getVariables().set("MAX_LEVEL_DIFFERENCE", 5);
		}
		
		if (npc.getVariables().getInt("MAESTRO_SKILL_USED") == 0)
		{
			if ((skill != null) && (skill.getId() == MAESTROS_KEY_SKILL_ID))
			{
				npc.getVariables().set("MAESTRO_SKILL_USED", 1);
				startQuestTimer(TIMER_1, ATTACK_SPAWN_TIME, npc, null);
				
				if ((npc.getLevel() - npc.getVariables().getInt("MAX_LEVEL_DIFFERENCE")) > attacker.getLevel())
				{
					addSkillCastDesire(npc, attacker, TREASURE_BOMBS[npc.getLevel() / 10], 1000000);
				}
				else
				{
					if (getRandom(100) < 10)
					{
						npc.doDie(null);
						
						final List<ItemChanceHolder> items = DROPS.get(npc.getId());
						if (items == null)
						{
							_log.warn("Tresure Chest ID " + npc.getId() + " doesn't have a drop list!");
						}
						else
						{
							for (ItemChanceHolder item : items)
							{
								if (getRandom(10000) < item.getChance())
								{
									npc.dropItem(attacker, item.getId(), item.getCount());
								}
							}
						}
					}
					else
					{
						addSkillCastDesire(npc, attacker, TREASURE_BOMBS[npc.getLevel() / 10], 1000000);
					}
				}
			}
			else
			{
				if (getRandom(100) < 30)
				{
					attacker.sendPacket(SystemMessageId.IF_YOU_HAVE_A_MAESTROS_KEY_YOU_CAN_USE_IT_TO_OPEN_THE_TREASURE_CHEST);
				}
			}
		}
		return super.onAttack(npc, attacker, damage, isSummon);
	}
}
