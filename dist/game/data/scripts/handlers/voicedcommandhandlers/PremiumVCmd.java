/*
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see <http://www.gnu.org/licenses/>.
 */

package handlers.voicedcommandhandlers;

import gr.sr.configsEngine.configs.impl.PremiumServiceConfigs;
import gr.sr.premiumEngine.PremiumHandler;
import l2r.Config;
import l2r.gameserver.handler.IVoicedCommandHandler;
import l2r.gameserver.model.actor.instance.L2PcInstance;
import l2r.gameserver.network.serverpackets.NpcHtmlMessage;

import java.text.SimpleDateFormat;
import java.util.Map;

public class PremiumVCmd implements IVoicedCommandHandler
{
	private static final String[] VOICED_COMMANDS =
	{
		"premium"
	};

	@Override
	public boolean useVoicedCommand(String command, L2PcInstance activeChar, String target)
	{
		if (command.startsWith("premium"))
		{
			SimpleDateFormat format = new SimpleDateFormat("MM-dd-yyyy hh:mm a");
//			 get default buff scheme amount
//			int normalBuffScheme = BufferConfigs.MAX_SCHEME_PROFILES;
//
//			 get pr buff scheme amount
//			int prBuffScheme = PremiumServiceConfigs.PREMIUM_MAX_SCHEME;

			// get pr drop map
			Map<Integer, Float> prSpecialDropMap = PremiumServiceConfigs.PR_RATE_DROP_ITEMS_ID;

			// get normal drop map
			Map<Integer, Float> normalSpecialDropMap = Config.RATE_DROP_AMOUNT_MULTIPLIER;

			// get pr adena multiplier from pr map (57 is adena item id)
			float PrAdenaMultiplier = prSpecialDropMap.get(57);

			// get normal adena multiplier from normal map (57 is adena item id)
			float normalAdenaMultiplier = normalSpecialDropMap.get(57);

			// get pr drop rate
			float PrAdenaDrop = (PrAdenaMultiplier * normalAdenaMultiplier);

			// NEXT STEP EXPLAINED FOR NEWBIES TO LEARN:
			// This is how to get % difference to show bonus in pr drop rate compared to normal.
			// we need to subtract 1 here because the default PR multiplier is 1 + the percent you specify.
			// (example: PrRateDropItemsById=1.10 in PremiumService.ini is 1.10 x your drop rate multiplier in Rates.ini)
			// so if your adena multiplier was 35, your premium rate would be 1.10 x 35
			// we found this amount in the previous step, so now we just divide by normal drop rate to get % (which is 1.10, or 110%)
			// and we subtract our 1x default pr multiplier, to give us .10, or 10%)
			// then multiply by 100 to give us 10% instead of .10
			int PrAdenaDropDiff = Math.round(((PrAdenaDrop / normalAdenaMultiplier) - 1) * 100);

			// PcBang bonus for Patrons (Premium)
			double pcBangPatronBonus = Config.PCB_BONUS_FOR_PREMIUM;
			int pcBangPatronBonusPercent = (int) Math.round((pcBangPatronBonus - 1) * 100);

			// Converts 'float' into 'int'
			float premiumXpFloat = PremiumServiceConfigs.PREMIUM_RATE_XP;
			int premiumXp = Math.round(premiumXpFloat);
			float premiumSpFloat = PremiumServiceConfigs.PREMIUM_RATE_SP;
			int premiumSp = Math.round(premiumSpFloat);

			String playerName = activeChar.getName();

			if (!activeChar.isPremium())
			{
				NpcHtmlMessage preReply = new NpcHtmlMessage();
				StringBuilder html3 = new StringBuilder("<html noscrollbar><body><title>Normal Account</title><center>");
				html3.append("<table border=0 width=285  height=350 background=\"L2UI_CH3.refinewnd_back_Pattern\">");
				html3.append("<tr><td><br><center>Your Account Type: <font color=\"LEVEL\">Normal<br></font></td></tr>");
				html3.append("<tr><td><center><font color=\"LEVEL\">You currently don't receive any bonuses!</font><br1></td></tr>");
				html3.append("<tr><td><center><font color=\"LEVEL\">Become a Patron to receive following perks:</font><br1></td></tr>");
				html3.append("<tr><td><center>XP Rate: <font color=\"LEVEL\">" + premiumXp + "<br1></font></center></td></tr>");
				html3.append("<tr><td><center>SP Rate: <font color=\"LEVEL\">" + premiumSp + "<br1></font></center></td></tr>");
				html3.append("<tr><td><center><font color=\"LEVEL\">1 Hour Longer Buffs</font></center></td></tr>");
				html3.append("<tr><td><center><font color=\"LEVEL\">+" + pcBangPatronBonusPercent + "% Extra PcBang Points</font></center></td></tr>");
				html3.append("<tr><td><center><font color=\"LEVEL\">+ 20% Extra Dwarven Skill Tokens</font></center></td></tr>");
				html3.append("<tr><td><center><font color=\"LEVEL\">+ 35% More Crafting Materials<br1> from Drop and Spoil</font></center></td></tr>");
				html3.append("<tr><td><center><font color=\"LEVEL\">+" + PrAdenaDropDiff + "% Adena Drop Amount</font></center></td></tr>");
				html3.append("<tr><td><center><font color=\"LEVEL\">Exclusive Access to Rim Kamaloka Solo Instance<br></font></center></td></tr>");
				html3.append("<tr><td><center><font color=\"LEVEL\"><center>Patron Account Information<br1></font></center></td></tr>");
				html3.append("<tr><td><center><font color=\"70FFCA\">1.Patron account cannot be transferred.<br1></font></center></td></tr>");
				html3.append("<tr><td><center><font color=\"70FFCA\">2.Patron status is account wide.<br1></font></center></td></tr>");
				html3.append("<tr><td><center><font color=\"70FFCA\">3.Does not affect Party members.<br><br></font></center></td></tr>");
				html3.append("</table>");
				html3.append("</center></body></html>");

				preReply.setHtml(html3.toString());
				activeChar.sendPacket(preReply);
			}
			else
			{
				long _end_prem_date;
				_end_prem_date = PremiumHandler.getPremServiceData(activeChar.getAccountName());
				NpcHtmlMessage preReply = new NpcHtmlMessage();

				StringBuilder html3 = new StringBuilder("<html noscrollbar><body><title>Patron Status</title><center>");
				html3.append("<table border=0 width=285  height=350 background=\"L2UI_CH3.refinewnd_back_Pattern\" >");
				html3.append("<tr><td><br><center><font color=\"00A5FF\"> Thank you " + playerName + "<br1> for being a <font color=\"LEVEL\">Patron</font></font><br1></td></tr>");
				//html3.append("<tr><td><center><font color=\"00A5FF\">Thank you for supporting us!</font><br></td></tr>");
				html3.append("<tr><td><center>XP Rate: <font color=\"LEVEL\">" + premiumXp + "<br1></font></center></td></tr>");
				html3.append("<tr><td><center>SP Rate: <font color=\"LEVEL\">" + premiumSp + "<br1></font></center></td></tr>");
				html3.append("<tr><td><center><font color=\"LEVEL\">1 Hour Longer Buffs</font></center></td></tr>");
				html3.append("<tr><td><center><font color=\"LEVEL\">+" + pcBangPatronBonusPercent + "% Extra PcBang Points</font></center></td></tr>");
				html3.append("<tr><td><center><font color=\"LEVEL\">+ 20% Extra Dwarven Skill Tokens</font></center></td></tr>");
				html3.append("<tr><td><center><font color=\"LEVEL\">+ 35% More Crafting Materials<br1> from Drop and Spoil</font></center></td></tr>");
				html3.append("<tr><td><center><font color=\"LEVEL\">+" + PrAdenaDropDiff + "% Adena Drop Amount</font></center></td></tr>");
				html3.append("<tr><td><center><font color=\"LEVEL\">Exclusive Access to Rim Kamaloka Solo Instance<br></font></center></td></tr>");
				html3.append("<tr><td><center>Expires: <font color=\"00A5FF\">" + String.valueOf(format.format(_end_prem_date)) + "</font></center></td></tr>");
				html3.append("<tr><td><center>Current Date: <font color=\"70FFCA\">" + String.valueOf(format.format(System.currentTimeMillis())) + "<br><br></font></center></td></tr>");
				html3.append("<tr><td><center><font color=\"LEVEL\"><center>Patron Account Information<br1></font></center></td></tr>");
				html3.append("<tr><td><center><font color=\"70FFCA\">1.Patron account cannot be transferred.<br1></font></center></td></tr>");
				html3.append("<tr><td><center><font color=\"70FFCA\">2.Patron status is account wide.<br1></font></center></td></tr>");
				html3.append("<tr><td><center><font color=\"70FFCA\">3.Does not affect Party members.<br><br></font></center></td></tr>");
				html3.append("</table>");
				html3.append("</center></body></html>");

				preReply.setHtml(html3.toString());
				activeChar.sendPacket(preReply);
			}
		}

		return true;
	}

	@Override
	public String[] getVoicedCommandList()
	{
		return VOICED_COMMANDS;
	}
}
