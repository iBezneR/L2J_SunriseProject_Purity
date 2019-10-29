package ai.sunriseNpc.PremiumManager;

import ai.npc.AbstractNpcAI;
import gr.sr.configsEngine.configs.impl.CustomNpcsConfigs;
import gr.sr.premiumEngine.PremiumHandler;
import l2r.Config;
import l2r.L2DatabaseFactory;
import l2r.gameserver.model.actor.L2Npc;
import l2r.gameserver.model.actor.instance.L2PcInstance;
import l2r.gameserver.network.SystemMessageId;
import l2r.gameserver.network.serverpackets.NpcHtmlMessage;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Calendar;

/**
 * @author L2jSunrise Team
 * @Website www.l2jsunrise.com
 */
public final class PremiumManager extends AbstractNpcAI
{
    // Generic
    private final int NpcId = CustomNpcsConfigs.PREMIUM_NPC_ID;
    private final int ItemId = CustomNpcsConfigs.PREMIUM_ITEM_ID;

    // Patron Week
    private final int ItemIDForWeekPremium = Config.PREMIUM_ITEM_ID_WEEK_1;
    private final int ItemAmountForWeekPremium = Config.PREMIUM_ITEM_AMOUNT_WEEK_1;
    // Patron Month
    private final int ItemIDforPremium1 = Config.PREMIUM_ITEM_ID_MONTH_1;
    private final int ItemAmountforPremiumMonth1 = Config.PREMIUM_ITEM_AMOUNT_MONTH_1;

    // Not in Use
    private final int ItemAmountforPremium2 = CustomNpcsConfigs.PREMIUM_ITEM_AMOUNT_2;
    private final int ItemAmountforPremium3 = CustomNpcsConfigs.PREMIUM_ITEM_AMOUNT_3;

    public PremiumManager()
    {
        super(PremiumManager.class.getSimpleName(), "ai/sunriseNpc");
        addFirstTalkId(NpcId);
        addTalkId(NpcId);
        addStartNpc(NpcId);
    }

    @Override
    public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
    {
        if (!CustomNpcsConfigs.ENABLE_PREMIUM_MANAGER)
        {
            player.sendMessage("Patron Manager is disabled by admin");
            sendMainHtmlWindow(player, npc);
            return "";
        }

        if (player.getLevel() <= CustomNpcsConfigs.PREMIUM_REQUIRED_LEVEL)
        {
            player.sendMessage("Your level is too low to use this function, you must be at least " + String.valueOf(CustomNpcsConfigs.PREMIUM_REQUIRED_LEVEL + 1) + " level.");
            sendMainHtmlWindow(player, npc);
            return "";
        }

        if (event.startsWith("premium") || event.startsWith("week"))
        {
            if (player.isPremium())
            {
                player.sendMessage("You are already a Patron. Use .premium for more details!");
                sendMainHtmlWindow(player, npc);
                return "";
            }

            if (event.equalsIgnoreCase("week"))
            {
                if (player.destroyItemByItemId("premium", ItemIDForWeekPremium, ItemAmountForWeekPremium, player, true))
                {
                    addPremiumServices(1, player);
                    player.setPremiumService(true);
                    player.sendMessage("Congratulations, you have became a Patron!");
                } else
                {
                    player.sendPacket(SystemMessageId.NOT_ENOUGH_ITEMS);
                }
            }

            if (event.equalsIgnoreCase("premium1"))
            {
                if (player.destroyItemByItemId("premium", ItemIDforPremium1, ItemAmountforPremiumMonth1, player, true))
                {
                    PremiumHandler.addPremiumServices(1, player);
                    player.setPremiumService(true);
                    player.sendMessage("Congratulations, you have became a Patron!");
                } else
                {
                    player.sendPacket(SystemMessageId.NOT_ENOUGH_ITEMS);
                }
            }

            if (event.equalsIgnoreCase("premium2"))
            {
                if (player.destroyItemByItemId("premium", ItemId, ItemAmountforPremium2, player, true))
                {
                    PremiumHandler.addPremiumServices(2, player);
                    player.setPremiumService(true);
                    player.sendMessage("Congratulations, you have became a Patron!");
                } else
                {
                    player.sendPacket(SystemMessageId.NOT_ENOUGH_ITEMS);
                }
            }

            if (event.equalsIgnoreCase("premium3"))
            {
                if (player.destroyItemByItemId("premium", ItemId, ItemAmountforPremium3, player, true))
                {
                    PremiumHandler.addPremiumServices(3, player);
                    player.setPremiumService(true);
                    player.sendMessage("Congratulations, you have became a Patron!");
                } else
                {
                    player.sendPacket(SystemMessageId.NOT_ENOUGH_ITEMS);
                }
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
        html.replace("%item_amount_week1%", String.valueOf(ItemAmountForWeekPremium));
        html.replace("%item_amount_month1%", String.valueOf(ItemAmountforPremiumMonth1));
        //html.replace("%item_name%", ItemData.getInstance().getTemplate(ItemId).getName());
        //html.replace("%item_amount2%", String.valueOf(ItemAmountforPremium2));
        //html.replace("%item_amount3%", String.valueOf(ItemAmountforPremium3));

        player.sendPacket(html);
    }

    private NpcHtmlMessage getHtmlPacket(L2PcInstance player, L2Npc npc, String htmlFile)
    {
        final NpcHtmlMessage packet = new NpcHtmlMessage(npc.getObjectId());
        packet.setHtml(getHtm(player, player.getHtmlPrefix(), htmlFile));
        return packet;
    }

    //Used for Premium Weeks
    private static final String UPDATE_PREMIUMSERVICE = "INSERT INTO characters_premium (account_name,premium_service,enddate) values(?,?,?) ON DUPLICATE KEY UPDATE premium_service = ?, enddate = ?";

    public static void addPremiumServices(int weeks, L2PcInstance player)
    {
        addPremiumServices(weeks, player.getAccountName());

    }

    private static void addPremiumServices(int weeks, String accName)
    {
        Calendar finishtime = Calendar.getInstance();
        finishtime.setTimeInMillis(System.currentTimeMillis());
        finishtime.set(Calendar.HOUR_OF_DAY, 0);
        finishtime.set(Calendar.MINUTE, 0);
        finishtime.set(Calendar.SECOND, 0);
        finishtime.add(Calendar.WEEK_OF_YEAR, weeks);

        try (Connection con = L2DatabaseFactory.getInstance().getConnection())
        {
            PreparedStatement statement = con.prepareStatement(UPDATE_PREMIUMSERVICE);
            statement.setString(1, accName);
            statement.setInt(2, 1);
            statement.setLong(3, finishtime.getTimeInMillis());
            statement.setInt(4, 1);
            statement.setLong(5, finishtime.getTimeInMillis());
            statement.execute();

        } catch (SQLException e)
        {
            //_log.error(PremiumHandler.class.getSimpleName() + ": Could not increase data." + e);
        }
    }
}