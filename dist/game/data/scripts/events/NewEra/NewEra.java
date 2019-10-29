package events.NewEra;

import l2r.gameserver.model.actor.L2Npc;
import l2r.gameserver.model.actor.instance.L2PcInstance;
import l2r.gameserver.model.event.LongTimeEvent;
import l2r.util.Rnd;

/**
 * @author vGodFather
 */
public class NewEra extends LongTimeEvent
{
	// Items
	private final static int letterL = 3882;
	private final static int letterI = 3881;
	private final static int letterN = 3883;
	private final static int letterE = 3877;
	private final static int letterA = 3875;
	private final static int letterG = 3879;
	private final static int letterII = 3888;
	private final static int letterT = 3887;
	private final static int letterH = 3880;
	private final static int letterR = 3885;
	private final static int letterO = 3884;
	
	private final static int EventNPC = 31854;
	
	public NewEra()
	{
		super(NewEra.class.getSimpleName(), "events");
		
		addStartNpc(EventNPC);
		addFirstTalkId(EventNPC);
		addTalkId(EventNPC);
	}
	
	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		String htmltext = "";
		int prize, l2day;
		
		if (event.equalsIgnoreCase("LINEAGEII"))
		{
			if ((getQuestItemsCount(player, letterL) >= 1) && (getQuestItemsCount(player, letterI) >= 1) && (getQuestItemsCount(player, letterN) >= 1) && (getQuestItemsCount(player, letterE) >= 2) && (getQuestItemsCount(player, letterA) >= 1) && (getQuestItemsCount(player, letterG) >= 1) && (getQuestItemsCount(player, letterII) >= 1))
			{
				takeItems(player, letterL, 1);
				takeItems(player, letterI, 1);
				takeItems(player, letterN, 1);
				takeItems(player, letterE, 2);
				takeItems(player, letterA, 1);
				takeItems(player, letterG, 1);
				takeItems(player, letterII, 1);
				
				prize = Rnd.get(1000);
				l2day = Rnd.get(10);
				
				if (prize <= 5)
				{
					giveItems(player, 6660, 1); // 1 - Ring of Ant Queen
				}
				else if (prize <= 10)
				{
					giveItems(player, 6662, 1); // 1 - Ring of Core
				}
				else if (prize <= 25)
				{
					giveItems(player, 8949, 1); // 1 - Fairy Antennae
				}
				else if (prize <= 50)
				{
					giveItems(player, 8950, 1); // 1 - Feathered Hat
				}
				else if (prize <= 75)
				{
					giveItems(player, 8947, 1); // 1 - Rabbit Ears
				}
				else if (prize <= 100)
				{
					giveItems(player, 729, 1); // 1 - Scroll Enchant Weapon A Grade
				}
				else if (prize <= 200)
				{
					giveItems(player, 947, 2); // 2 - Scroll Enchant Weapon B Grade
				}
				else if (prize <= 300)
				{
					giveItems(player, 951, 3); // 3 - Scroll Enchant Weapon C Grade
				}
				else if (prize <= 400)
				{
					giveItems(player, 3936, 1); // 1 - Blessed Scroll of Resurrection
				}
				else if (prize <= 500)
				{
					giveItems(player, 1538, 1); // 1 - Blessed Scroll of Escape
				}
				else if (l2day == 1)
				{
					giveItems(player, 3926, 3); // 3 - Random L2 Day Buff Scrolls 2 of the same type
				}
				else if (l2day == 2)
				{
					giveItems(player, 3927, 3); // 3 - Random L2 Day Buff Scrolls 2 of the same type
				}
				else if (l2day == 3)
				{
					giveItems(player, 3928, 3); // 3 - Random L2 Day Buff Scrolls 2 of the same type
				}
				else if (l2day == 4)
				{
					giveItems(player, 3929, 3); // 3 - Random L2 Day Buff Scrolls 2 of the same type
				}
				else if (l2day == 5)
				{
					giveItems(player, 3930, 3); // 3 - Random L2 Day Buff Scrolls 2 of the same type
				}
				else if (l2day == 6)
				{
					giveItems(player, 3931, 3); // 3 - Random L2 Day Buff Scrolls 2 of the same type
				}
				else if (l2day == 7)
				{
					giveItems(player, 3932, 3); // 3 - Random L2 Day Buff Scrolls 2 of the same type
				}
				else if (l2day == 8)
				{
					giveItems(player, 3933, 3); // 3 - Random L2 Day Buff Scrolls 2 of the same type
				}
				else if (l2day == 9)
				{
					giveItems(player, 3934, 3); // 3 - Random L2 Day Buff Scrolls 2 of the same type
				}
				else
				{
					giveItems(player, 3935, 3); // 3 - Random L2 Day Buff Scrolls 2 of the same type
				}
			}
			else
			{
				htmltext = "31854-03.htm";
			}
		}
		else if (event.equalsIgnoreCase("THRONE"))
		{
			if ((getQuestItemsCount(player, letterT) >= 1) && (getQuestItemsCount(player, letterH) >= 1) && (getQuestItemsCount(player, letterR) >= 1) && (getQuestItemsCount(player, letterO) >= 1) && (getQuestItemsCount(player, letterN) >= 1) && (getQuestItemsCount(player, letterE) >= 1))
			{
				takeItems(player, letterT, 1);
				takeItems(player, letterH, 1);
				takeItems(player, letterR, 1);
				takeItems(player, letterO, 1);
				takeItems(player, letterN, 1);
				takeItems(player, letterE, 1);
				
				prize = Rnd.get(1000);
				l2day = Rnd.get(10);
				
				if (prize <= 5)
				{
					giveItems(player, 6660, 1); // 1 - Ring of Ant Queen
				}
				else if (prize <= 10)
				{
					giveItems(player, 6662, 1); // 1 - Ring of Core
				}
				else if (prize <= 25)
				{
					giveItems(player, 8951, 1); // 1 - Artisans Goggles
				}
				else if (prize <= 50)
				{
					giveItems(player, 8948, 1); // 1 - Little Angel Wings
				}
				else if (prize <= 75)
				{
					giveItems(player, 947, 2); // 2 - Scroll Enchant Weapon B Grade
				}
				else if (prize <= 100)
				{
					giveItems(player, 951, 3); // 3 - Scroll Enchant Weapon C Grade
				}
				else if (prize <= 150)
				{
					giveItems(player, 955, 4); // 4 - Scroll Enchant Weapon D Grade
				}
				else if (prize <= 200)
				{
					giveItems(player, 3936, 1); // 1 - Blessed Scroll of Resurrection
				}
				else if (prize <= 300)
				{
					giveItems(player, 1538, 1); // 1 - Blessed Scroll of Escape
				}
				else if (l2day == 1)
				{
					giveItems(player, 3926, 2); // 2 - Random L2 Day Buff Scrolls 2 of the same type
				}
				else if (l2day == 2)
				{
					giveItems(player, 3927, 2); // 2 - Random L2 Day Buff Scrolls 2 of the same type
				}
				else if (l2day == 3)
				{
					giveItems(player, 3928, 2); // 2 - Random L2 Day Buff Scrolls 2 of the same type
				}
				else if (l2day == 4)
				{
					giveItems(player, 3929, 2); // 2 - Random L2 Day Buff Scrolls 2 of the same type
				}
				else if (l2day == 5)
				{
					giveItems(player, 3930, 2); // 2 - Random L2 Day Buff Scrolls 2 of the same type
				}
				else if (l2day == 6)
				{
					giveItems(player, 3931, 2); // 2 - Random L2 Day Buff Scrolls 2 of the same type
				}
				else if (l2day == 7)
				{
					giveItems(player, 3932, 2); // 2 - Random L2 Day Buff Scrolls 2 of the same type
				}
				else if (l2day == 8)
				{
					giveItems(player, 3933, 2); // 2 - Random L2 Day Buff Scrolls 2 of the same type
				}
				else if (l2day == 9)
				{
					giveItems(player, 3934, 2); // 2 - Random L2 Day Buff Scrolls 2 of the same type
				}
				else
				{
					giveItems(player, 3935, 2); // 2 - Random L2 Day Buff Scrolls 2 of the same type
				}
			}
			else
			{
				htmltext = "31854-03.htm";
			}
		}
		else if (event.equalsIgnoreCase("chat0"))
		{
			htmltext = "31854.htm";
		}
		else if (event.equalsIgnoreCase("chat1"))
		{
			htmltext = "31854-02.htm";
		}
		
		return htmltext;
	}
	
	@Override
	public String onFirstTalk(L2Npc npc, L2PcInstance player)
	{
		return "31854.htm";
	}
}