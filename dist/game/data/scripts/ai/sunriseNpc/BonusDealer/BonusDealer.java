package ai.sunriseNpc.BonusDealer;

import ai.npc.AbstractNpcAI;
import custom.erengine.ErObject.ErObjectType;
import l2r.gameserver.model.actor.L2Npc;
import l2r.gameserver.model.actor.instance.L2PcInstance;
import l2r.gameserver.network.SystemMessageId;
import l2r.gameserver.network.serverpackets.NpcHtmlMessage;

/**
 * @author Sybar
 * @Website www.dwarven-manufacture.com
 */
public final class BonusDealer extends AbstractNpcAI
{
    private static final int NpcId = 50085;
    private static final int PAYMENT_METHOD = 40904;
    private static final String PAYMENT_METHOD_DISPLAYED = "Loyality Gems";
    // XP BONUS
    private static final int EXPERIENCE_GLYPH = 40320;
    private static final int XP_PAYMENT_AMOUNT = 50;
    private static final int EXPERIENCE_BONUS = 50;
    // SP BONUS
    private static final int SKILL_POINTS_GLYPH = 40321;
    private static final int SP_PAYMENT_AMOUNT = 50;
    private static final int SKILL_POINTS_BONUS = 50;
    // ADENA BONUS
    private static final int ADENA_GLYPH = 40322;
    private static final int ADENA_PAYMENT_AMOUNT = 50;
    private static final int ADENA_BONUS = 25;

    public BonusDealer()
    {
        super(BonusDealer.class.getSimpleName(), "ai/sunriseNpc");
        addFirstTalkId(NpcId);
        addTalkId(NpcId);
        addStartNpc(NpcId);
    }

    @Override
    public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
    {

        // Bonus XP
        if (event.startsWith("addBonusExperience"))
        {
            //Check Players Inventory for specific item ID, if he hasn't got it then execute...
            if (hasQuestItems(player, EXPERIENCE_GLYPH))
            {
                player.sendMessage("You have already purchased this item. Wait until it disappears.");
            }
            else if (player.destroyItemByItemId("experience", PAYMENT_METHOD, XP_PAYMENT_AMOUNT, player, true))
            {
                long BonusDuration = 10800000; // In Milliseconds
                long bonusDuration = ((BonusDuration / 1000) / 60);
                player.getActingPlayer().getPlayerBonuses().addBonus(ErObjectType.Experience, EXPERIENCE_BONUS, true, BonusDuration, false);
                player.sendUserInfo(true);
                giveItems(player, EXPERIENCE_GLYPH, 1);
                player.sendMessage("You have bought an Glyph of Experience Points. It will disappear after " + bonusDuration + " hours.");
            }
            else
            {
                player.sendPacket(SystemMessageId.NOT_ENOUGH_ITEMS);
            }
        }

        // Bonus SP
        if (event.startsWith("addBonusSkillPoints"))
        {
            //Check Players Inventory for specific item ID, if he hasn't got it then execute...
            if (hasQuestItems(player, SKILL_POINTS_GLYPH))
            {
                player.sendMessage("You have already purchased this item. Wait until it disappears.");
            }
            else if (player.destroyItemByItemId("skillpoints", PAYMENT_METHOD, SP_PAYMENT_AMOUNT, player, true))
            {
                long BonusDuration = 10800000; // In Milliseconds
                long bonusDuration = ((BonusDuration / 1000) / 60);
                player.getActingPlayer().getPlayerBonuses().addBonus(ErObjectType.SkillPoints, SKILL_POINTS_BONUS, true, BonusDuration, false);
                player.sendUserInfo(true);
                giveItems(player, SKILL_POINTS_GLYPH, 1);
                player.sendMessage("You have bought Glyph of Skill Points. It will disappear after " + bonusDuration + " minutes.");
            }
            else
            {
                player.sendPacket(SystemMessageId.NOT_ENOUGH_ITEMS);
            }
        }

        // Bonus Adena
        if (event.startsWith("addBonusAdena"))
        {
            //Check Players Inventory for specific item ID, if he hasn't got it then execute...
            if (hasQuestItems(player, ADENA_GLYPH))
            {
                player.sendMessage("You have already purchased this item. Wait until it disappears.");
            }
            else if (player.destroyItemByItemId("extraadena", PAYMENT_METHOD, ADENA_PAYMENT_AMOUNT, player, true))
            {
                long BonusDuration = 10800000; // In Milliseconds
                long bonusDuration = ((BonusDuration / 1000) / 60);
                player.getActingPlayer().getPlayerBonuses().addBonus(57, ADENA_BONUS, true, false, BonusDuration, false);
                player.sendUserInfo(true);
                giveItems(player, ADENA_GLYPH, 1);
                player.sendMessage("You have bought Glyph of Wealth. It will disappear after " + bonusDuration + " minutes.");
            }
            else
            {
                player.sendPacket(SystemMessageId.NOT_ENOUGH_ITEMS);
            }
        }

        sendMainHtmlWindow(player, npc);
        return "";
    }

    @Override
    public String onFirstTalk(L2Npc npc, L2PcInstance player)
    {
        sendMainHtmlWindow(player, npc);
        return "";
    }

    private void sendMainHtmlWindow(L2PcInstance player, L2Npc npc)
    {
        final NpcHtmlMessage html = getHtmlPacket(player, npc, "main.htm");
        html.replace("%player%", player.getName());
        html.replace("%EXPERIENCE_BONUS%", String.valueOf(EXPERIENCE_BONUS));
        html.replace("%PAYMENT_METHOD%", String.valueOf(PAYMENT_METHOD_DISPLAYED));
        html.replace("%XP_PAYMENT_AMOUNT%", String.valueOf(XP_PAYMENT_AMOUNT));
        html.replace("%EXPERIENCE_BONUS%", String.valueOf(EXPERIENCE_BONUS));
        html.replace("%SP_PAYMENT_AMOUNT%", String.valueOf(SP_PAYMENT_AMOUNT));
        html.replace("%SKILL_POINTS_BONUS%", String.valueOf(SKILL_POINTS_BONUS));
        html.replace("%ADENA_PAYMENT_AMOUNT%", String.valueOf(ADENA_PAYMENT_AMOUNT));
        html.replace("%ADENA_BONUS%", String.valueOf(ADENA_BONUS));

        player.sendPacket(html);
    }

    private NpcHtmlMessage getHtmlPacket(L2PcInstance player, L2Npc npc, String htmlFile)
    {
        final NpcHtmlMessage packet = new NpcHtmlMessage(npc.getObjectId());
        packet.setHtml(getHtm(player, player.getHtmlPrefix(), htmlFile));
        return packet;
    }
}