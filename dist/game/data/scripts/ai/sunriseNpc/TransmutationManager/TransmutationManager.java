package ai.sunriseNpc.TransmutationManager;

import ai.npc.AbstractNpcAI;
import l2r.gameserver.data.xml.impl.ExperienceData;
import l2r.gameserver.model.actor.L2Npc;
import l2r.gameserver.model.actor.instance.L2PcInstance;
import l2r.gameserver.model.quest.QuestState;
import l2r.gameserver.network.serverpackets.NpcHtmlMessage;

/**
 * @author Sybar
 */
public class TransmutationManager extends AbstractNpcAI
{
    // Transmutation Specialist ID
    private final static int NPC = 50086;

    // Level drop after Sacrifice
    private final static int LEVEL_DROP_LOW = 6;
    private final static int LEVEL_DROP_MEDIUM = 4;
    private final static int LEVEL_DROP_HIGH = 2;

    // Empower 'to' Levels
    private final static int LEVEL_INCREASE_20 = 20;
    private final static int LEVEL_INCREASE_40 = 40;
    private final static int LEVEL_INCREASE_76 = 76;

    // Sacrifice Cost
    private final static int COST_ITEM_ID = 40904;
    private final static int COST_ITEM_AMOUNT_LOW = 500;
    private final static int COST_ITEM_AMOUNT_MEDIUM = 2000;
    private final static int COST_ITEM_AMOUNT_HIGH = 5000;
    private final static int COST_SP_LOW = 500;
    private final static int COST_SP_MEDIUM = 500;
    private final static int COST_SP_HIGH = 500;
    // Transmutation Stones
    private final static int TRANSMUTATION_STONE_LOW_ID = 40356;
    private final static int TRANSMUTATION_STONE_MEDIUM_ID = 40357;
    private final static int TRANSMUTATION_STONE_HIGH_ID = 40358;

    public TransmutationManager()
    {
        super(TransmutationManager.class.getSimpleName(), "ai/sunriseNpc");
        addStartNpc(NPC);
        addFirstTalkId(NPC);
        addTalkId(NPC);
    }

    @Override
    public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
    {
        String htmltext = event;
        final QuestState st = getQuestState(player, true);

        // Sacrifice
        // Player must reach level requirement
        if (event.equalsIgnoreCase("sacrifice"))
        {
            if ((player.getLevel() >= 40) & (player.getLevel() <= 52))
            {
                if (player.destroyItemByItemId("TakePayment", COST_ITEM_ID, COST_ITEM_AMOUNT_LOW, player, true))
                {
                    player.setExp(player.getStat().getExpForLevel(player.getLevel()));
                    player.removeExpAndSp(player.getExp() - ExperienceData.getInstance().getExpForLevel(player.getLevel() - LEVEL_DROP_LOW), COST_SP_LOW);
                    player.sendMessage("Your have sacrificed a portion of your soul!");
                    st.giveItems(TRANSMUTATION_STONE_LOW_ID, 1);
                    return "sacrifice-done.htm";
                }
            }
            if ((player.getLevel() >= 53) & (player.getLevel() <= 75))
            {
                if (player.destroyItemByItemId("TakePayment", COST_ITEM_ID, COST_ITEM_AMOUNT_MEDIUM, player, true))
                {
                    player.setExp(player.getStat().getExpForLevel(player.getLevel()));
                    player.removeExpAndSp(player.getExp() - ExperienceData.getInstance().getExpForLevel(player.getLevel() - LEVEL_DROP_MEDIUM), COST_SP_MEDIUM);
                    player.sendMessage("Your have sacrificed a portion of your soul!");
                    st.giveItems(TRANSMUTATION_STONE_MEDIUM_ID, 1);
                    return "sacrifice-done.htm";
                }
            }
            if ((player.getLevel() >= 76))
            {
                if (player.destroyItemByItemId("TakePayment", COST_ITEM_ID, COST_ITEM_AMOUNT_HIGH, player, true))
                {
                    player.setExp(player.getStat().getExpForLevel(player.getLevel()));
                    player.removeExpAndSp(player.getExp() - ExperienceData.getInstance().getExpForLevel(player.getLevel() - LEVEL_DROP_HIGH), COST_SP_HIGH);
                    player.sendMessage("Your have sacrificed a portion of your soul!");
                    st.giveItems(TRANSMUTATION_STONE_HIGH_ID, 1);
                    return "sacrifice-done.htm";
                }
            }
            else
            {
                player.sendMessage("You must be at least level 40 to sacrifice!");
                return "sacrifice-nolevel.htm";
            }
        }

        // Empower
        if (event.equalsIgnoreCase("empower-20"))
        {
            if ((player.getLevel() >= 1) & (player.getLevel() <= 19))
            {
                if ((player.destroyItemByItemId("TakePayment", TRANSMUTATION_STONE_LOW_ID, 1, player, true)))
                {
                    if ((LEVEL_INCREASE_20 >= 1) && (LEVEL_INCREASE_20 <= ExperienceData.getInstance().getMaxLevel()))
                    {
                        long pXp = player.getExp();
                        long tXp = ExperienceData.getInstance().getExpForLevel(LEVEL_INCREASE_20);

                        if (pXp > tXp)
                        {
                            player.removeExpAndSp(pXp - tXp, 0);
                        }
                        else if (pXp < tXp)
                        {
                            player.addExpAndSp(tXp - pXp, 0);
                        }
                    }
                    player.giveAvailableSkills(false, false);
                    player.sendMessage("You have been empowered! Your level has increased to 20 and all available skills have been granted.");
                    return "empower-done.htm";
                }
                else
                {
                    player.sendMessage("You don't have enough items to pay for my services. Check all requirements and try again.");
                    return "use-stone.htm";
                }
            }
            else
            {
                player.sendMessage("You must be between level 20 and 39 to use this function.");
                return "use-stone.htm";
            }
        }

        if (event.equalsIgnoreCase("empower-40"))
        {
            if ((player.getLevel() >= 20) & (player.getLevel() <= 39))
            {
                if ((player.destroyItemByItemId("TakePayment", TRANSMUTATION_STONE_MEDIUM_ID, 1, player, true)))
                {
                    if ((LEVEL_INCREASE_40 >= 1) && (LEVEL_INCREASE_40 <= ExperienceData.getInstance().getMaxLevel()))
                    {
                        long pXp = player.getExp();
                        long tXp = ExperienceData.getInstance().getExpForLevel(LEVEL_INCREASE_40);

                        if (pXp > tXp)
                        {
                            player.removeExpAndSp(pXp - tXp, 0);
                        }
                        else if (pXp < tXp)
                        {
                            player.addExpAndSp(tXp - pXp, 0);
                        }
                    }
                    player.giveAvailableSkills(false, false);
                    player.sendMessage("You have been empowered! Your level has increased to 40 and all available skills have been granted.");
                    return "empower-done.htm";
                }
                else
                {
                    player.sendMessage("You don't have enough items to pay for my services. Check all requirements and try again.");
                    return "use-stone.htm";
                }
            }
            else
            {
                player.sendMessage("You must be between level 20 and 39 to use this function.");
                return "use-stone.htm";
            }
        }
        if (event.equalsIgnoreCase("empower-76"))
        {
            if ((player.getLevel() >= 40) & (player.getLevel() <= 75))
            {
                if ((player.destroyItemByItemId("TakePayment", TRANSMUTATION_STONE_HIGH_ID, 1, player, true)))
                {
                    if ((LEVEL_INCREASE_76 >= 1) && (LEVEL_INCREASE_76 <= ExperienceData.getInstance().getMaxLevel()))
                    {
                        long pXp = player.getExp();
                        long tXp = ExperienceData.getInstance().getExpForLevel(LEVEL_INCREASE_76);

                        if (pXp > tXp)
                        {
                            player.removeExpAndSp(pXp - tXp, 0);
                        }
                        else if (pXp < tXp)
                        {
                            player.addExpAndSp(tXp - pXp, 0);
                        }
                    }
                    player.giveAvailableSkills(false, false);
                    player.sendMessage("You have been empowered! Your level has increased to 60 and all available skills have been granted.");
                    return "empower-done.htm";
                }
                else
                {
                    player.sendMessage("You don't have enough items to pay for my services. Check all requirements and try again.");
                    return "use-stone.htm";
                }
            }
            else
            {
                player.sendMessage("You must be between level 40 and 75 to use this function.");
                return "use-stone.htm";
            }
        }
        return htmltext;
    }

    @Override
    public String onFirstTalk(L2Npc npc, L2PcInstance player)
    {
        return "main.htm";
    }

    private NpcHtmlMessage getHtmlPacket(L2PcInstance player, L2Npc npc, String htmlFile)
    {
        final NpcHtmlMessage packet = new NpcHtmlMessage(npc.getObjectId());
        packet.setHtml(getHtm(player, player.getHtmlPrefix(), htmlFile));
        return packet;
    }


}