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
package custom.vote;

import custom.erengine.ErBonus;
import custom.erengine.ErObject;
import custom.erengine.ErReward;
import l2r.gameserver.communitybbs.Managers.BaseBBSManager;
import l2r.gameserver.data.xml.impl.ItemData;
import l2r.gameserver.model.L2World;
import l2r.gameserver.model.actor.instance.L2PcInstance;
import l2r.gameserver.model.items.L2Item;

import java.util.ArrayList;
import java.util.StringTokenizer;

/**
 * @author Erlandys
 */
public class VoteBBSManager extends BaseBBSManager
{
	@Override
	public void cbByPass(String command, L2PcInstance activeChar)
	{
		StringTokenizer st = new StringTokenizer(command, ";");
		st.nextToken();
		boolean startVoting = false;
		IVote type = null;
		int rewardId = -1;
		
		if (st.hasMoreTokens())
		{
			startVoting = st.nextToken().equalsIgnoreCase("vote");
		}
		if (st.hasMoreTokens())
		{
			type = VoteManager.getInstance().getVoteType(Integer.parseInt(st.nextToken()));
		}
		if (st.hasMoreTokens())
		{
			rewardId = Integer.parseInt(st.nextToken());
		}
		if (type == null)
		{
			type = VoteManager.getInstance().getRandomVoteType();
		}
		boolean isVoting = type.isVoting();
		if (startVoting)
		{
			isVoting = VoteManager.getInstance().startVoting(activeChar, type.getId(), rewardId);
		}
		VotingPlayer vp = null;
		try
		{
			vp = (VotingPlayer) activeChar.getClass().getMethod("getVotingPlayer").invoke(activeChar);
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		if (vp == null)
		{
			return;
		}
		if (vp.isVoting())
		{
			isVoting = true;
		}
		boolean hasVoted = vp.hasVotedToday(type.getId());
		boolean isBanned = VoteManager.getInstance().isBanned(activeChar.getAccountName());
		String html = "<html><body><center><br><br>";
		if (VoteManager.SHOW_ONLINE)
		{
			int count = (L2World.getInstance().getPlayers().size());
			if (!activeChar.isGM())
			{
				count = VoteManager.FAKE_ONLINE_IN_PERCENTS ? (int) ((count / 100.0) * VoteManager.FAKE_ONLINE) : count + VoteManager.FAKE_ONLINE;
			}
			html += "<font color=\"888888\" name=\"CreditTextNormal\">Total online count: " + count + "</font><br>";
		}
		else if (VoteManager.SHOW_ONLINE_FOR_GMS && activeChar.isGM())
		{
			int count = (L2World.getInstance().getPlayers().size());
			html += "<font color=\"888888\" name=\"CreditTextNormal\">Total online count: " + count + "</font><br>";
		}
		int i = 0;
		for (IVote vote : VoteManager.getInstance().getAllVoteTypes())
		{
			if ((i % 5) == 0)
			{
				html += "<table><tr>";
			}
			html += "<td width=115 align=center>";
			if (vote.getId() == type.getId())
			{
				html += "<table background=\"L2UI_CT1.Button_DF_Disable\" width=106 height=21><tr><td width=108 align=center><font color=e6dcbe>" + vote.getVoteName() + "</font></td></tr></table>";
			}
			else
			{
				html += "<button value=\"" + vote.getVoteName() + "\" action=\"" + (vote.getId() == type.getId() ? "" : "bypass _bbsvote;type;" + vote.getId()) + "\" width=106 height=21 fore=\"L2UI_CT1.Button_DF\" back=\"L2UI_CT1.Button_DF_Down\"/>";
			}
			html += "</td>";
			i++;
			if ((i % 5) == 0)
			{
				html += "</tr></table>";
			}
		}
		if ((i % 5) != 0)
		{
			html += "</tr></table>";
		}
		html += "<br>";
		if (hasVoted)
		{
			html += "<center><font name=\"CreditTextNormal\" color=\"D2B48C\">Left: " + getLeftTime((int) (vp.getNextVote(type.getId()) - System.currentTimeMillis()) / 1000, true) + "</font></center><br>";
		}
		if (isBanned)
		{
			html += "<center><font name=\"CreditTextNormal\" color=\"D2B48C\">You will be able to vote after: " + getLeftTime(VoteManager.getInstance().banWillBeLiftedAfter(activeChar.getAccountName()), true) + "</font></center><br>";
		}
		html += "<br><br>";
		html += "<table background=\"L2UI_CT1.Windows_DF_Drawer_Bg\"><tr><td align=\"center\" width=\"670\">";
		html += "<img src=\"L2UI.SquareBlank\" width=\"1\" height=\"8\"/>";
		html += "<table><tr><td width=650 align=center><font color=A0522D name=\"ScreenMessageSmall\">Rewards</font></td></tr></table>";
		html += "<img src=L2UI.SquareBlank width=632 height=8/>";
		html += "<img src=L2UI.SquareGray width=632 height=2/>";
		html += "<img src=L2UI.SquareBlank width=632 height=1/>";
		
		int ii = 0;
		for (int id : type.getRewardIds())
		{
			ArrayList<ErReward> rewards = type.getReward(id);
			ArrayList<ErBonus> bonuses = type.getBonus(id);
			int size = rewards.size() + bonuses.size();
			if (size < 1)
			{
				continue;
			}
			int c = 0;
			
			for (ErReward reward : rewards)
			{
				html += getReward(activeChar, reward, c, size, id, type.getId());
				c++;
			}
			for (ErBonus bonus : bonuses)
			{
				html += getReward(activeChar, bonus, c, size, id, type.getId());
				c++;
			}
			if ((c % 2) == 0)
			{
				html += "<img src=\"L2UI.SquareGray\" width=\"632\" height=\"1\"/>";
			}
			else
			{
				html += "<img src=\"L2UI.SquareGray\" width=\"357\" height=\"1\"/>";
			}
			html += "<center>";
			boolean showFalse = isBanned || hasVoted || isVoting;
			String bypass = "bypass _bbsvote;vote;" + type.getId() + ";" + id;
			String icons = "";
			
			html += "<table background=\"L2UI_CT1.Windows_DF_Drawer_Bg_Darker\"><tr><td align=\"center\" width=\"357\">";
			
			html += "<img src=L2UI.SquareBlank width=1 height=3/>";
			
			if (VoteManager.USE_VOTE_BUTTON)
			{
				icons = showFalse ? "fore=\"L2UI_CH3.Btn1_normalDisable\" back=\"L2UI_CH3.Btn1_normalDisable\"" : "fore=\"L2UI_CH3.Btn1_normal\" back=\"L2UI_CH3.Btn1_normalOn\"";
				if (showFalse)
				{
					html += "<table background=\"L2UI_CT1.Button_DF_Disable\" width=106 height=21><tr><td width=108 align=center><font color=e6dcbe>Vote</font></td></tr></table>";
				}
				else
				{
					html += "<button value=\"Vote\" action=\"" + bypass + "\" width=\"106\" height=\"21\" fore=\"L2UI_CT1.Button_DF\" back=\"L2UI_CT1.Button_DF_Down\"/>";
				}
			}
			else
			{
				icons = showFalse ? "fore=\"L2UI_CT1.MiniMap_DF_MinusBtn_Red\" back=\"L2UI_CT1.MiniMap_DF_MinusBtn_Red_Down\"" : "fore=\"L2UI_CT1.MiniMap_DF_PlusBtn_Blue\" back=\"L2UI_CT1.MiniMap_DF_PlusBtn_Blue_Down\"";
				html += "<button value=\"\" action=\"" + bypass + "\" width=\"22\" height=\"22\" " + icons + "/>";
			}
			
			html += "<img src=L2UI.SquareBlank width=1 height=3/>";
			
			html += "</td></tr></table>";
			
			html += "<img src=\"L2UI.SquareBlank\" width=\"1\" height=\"5\" />";
			html += "</center>";
			if ((ii + 1) < type.getRewardIds().size())
			{
				html += "<img src=L2UI.SquareGray width=632 height=2/>";
				html += "<img src=L2UI.SquareBlank width=632 height=1/>";
			}
			html += "<center>";
			ii++;
		}
		html += "<img src=\"L2UI.SquareBlank\" width=\"1\" height=\"3\"/>";
		html += "</td></tr></table>";
		html += "<br><br>";
		html += "<table background=\"L2UI_CT1.Windows_DF_Drawer_Bg_Darker\"><tr><td align=\"center\" width=\"470\">";
		html += "<img src=\"L2UI.SquareBlank\" width=\"1\" height=\"7\">";
		html += "<font color=\"c6c859\" name=\"CreditTextNormal\">" + type.getLink() + "</font><br1>";
		html += "<font name=\"CreditTextNormal\" color=\"D2B48C\">" + VoteManager.VOTE_LINKS_INFORMATION_1 + "<br1>";
		html += VoteManager.VOTE_LINKS_INFORMATION_2 + "</font><br1>";
		html += "<img src=\"L2UI.SquareBlank\" width=\"1\" height=\"5\">";
		html += "</td></tr></table>";
		html += "</center><br></body></html>";
		separateAndSend(html, activeChar);
	}
	
	private String getReward(L2PcInstance player, ErObject object, int c, int size, int id, int typeId)
	{
		boolean isReward = object instanceof ErReward;
		String html = "";
		String icon = "";
		icon += "<td fixwidth=\"35\" align=\"center\">";
		icon += "<img src=\"" + object.getIcon() + "\" width=\"32\" height=\"32\"/>";
		icon += "<img src=\"L2UI.SquareBlank\" width=\"1\" height=\"5\" />";
		icon += "</td>";
		String name = "";
		String description = "";
		name += "<td fixwidth=\"151\" align=\"" + ((c % 2) == 0 ? "left" : "right") + "\">";
		description += "<td fixwidth=\"156\" align=\"" + ((c % 2) == 0 ? "right" : "left") + "\">";
		name += "<font color=\"D2B48C\" name=\"CreditTextNormal\">";
		description += "<font color=\"D2B48C\" name=\"CreditTextNormal\">";
		switch (object.getType())
		{
			case Experience:
				name += "Experience";
				description += isReward ? "Give experience" : "Increase XP by";
				break;
			case SkillPoints:
				name += "Skill Points";
				description += isReward ? "Give skill points" : "Increase SP by";
				break;
			case ClanPoints:
				name += "Clan Points";
				description += isReward ? "Give clan points" : "Inc. clan pts. by";
				break;
			case ArmorEnchant:
				name += "Armors Enchantment";
				description += "Increases ench. rate by";
				break;
			case WeaponEnchant:
				name += "Weapons Enchantment";
				description += "Increases ench. rate by";
				break;
			case SkillsEnchant:
				name += "Skills Enchantment";
				description += "Increases ench. rate by";
				break;
			case Item:
				L2Item it = ItemData.getInstance().getTemplate(object.getItemId());
				if (it != null)
				{
					name += it.getName();
				}
				else
				{
					name += "Unknown";
				}
				if (isReward)
				{
					description += "Item reward";
				}
				else
				{
					description += ((ErBonus) object).isItemChance() ? "Incr. drop chance by" : "Incr. drop amount by";
				}
				break;
		}
		name += "</font></td>";
		description += "</font></td>";
		String count = "";
		count += "<td fixwidth=\"122\" align=\"" + ((c % 2) == 0 ? "right" : "left") + "\">";
		count += "<font color=\"b88747\" name=\"CreditTextNormal\">";
		String percent = !isReward && ((ErBonus) object).isAddingPercent() ? "%" : "";
		count += (object.getMinCount() == object.getMaxCount() ? object.getMinCount() : (object.getMinCount() + "-" + object.getMaxCount())) + percent;
		count += "</font></td>";
		String duration = "";
		duration += "<td fixwidth=\"151\" align=\"" + ((c % 2) == 0 ? "left" : "right") + "\">";
		duration += "<font color=\"a57940\" name=\"CreditTextNormal\">";
		duration += (player.getVotingPlayer().hasReward(typeId, id) ? "<font color=88160d name=\"CreditTextNormal\">(Taken)</font> " : "");
		if (isReward)
		{
			duration += "Instant";
		}
		else
		{
			duration += "Duration " + getLeftTime((int) ((ErBonus) object).getTime() / 1000, false);
		}
		duration += "<img src=\"L2UI.SquareBlank\" width=\"1\" height=\"5\" />";
		duration += "</font></td>";
		if ((c % 2) == 0)
		{
			if (c > 0)
			{
				html += "<img src=\"L2UI.SquareBlank\" width=\"632\" height=\"1\"/>";
				html += "<img src=\"L2UI.SquareGray\" width=\"632\" height=\"1\"/>";
				html += "<img src=\"L2UI.SquareBlank\" width=\"632\" height=\"1\"/>";
			}
			if ((c + 1) >= size)
			{
				html += "<center>";
			}
			html += "<table fixwidth=\"620\" bgcolor=\"111111\">";
			html += "<tr>";
			if ((c + 1) >= size)
			{
				html += icon;
			}
			html += "<td fixwidth=\"273\">";
			html += "<table>";
			html += "<tr>";
			html += name;
			html += description;
			html += "</tr>";
			html += "<tr>";
			html += duration;
			html += count;
			html += "</tr>";
			html += "</table>";
			html += "</td>";
			if ((c + 1) < size)
			{
				html += icon;
			}
			html += "<td fixwidth=\"4\"></td>";
			if ((c + 1) >= size)
			{
				html += icon;
				html += "</tr>";
				html += "</table>";
				html += "</center>";
			}
		}
		else
		{
			html += icon;
			html += "<td fixwidth=\"273\">";
			html += "<table>";
			html += "<tr>";
			html += description;
			html += name;
			html += "</tr>";
			html += "<tr>";
			html += count;
			html += duration;
			html += "</tr>";
			html += "</table>";
			html += "</td>";
			html += "</tr>";
			html += "</table>";
		}
		return html;
	}
	
	public String getLeftTime(int time, boolean leftTime)
	{
		String text = "";
		int days = time / 86400;
		int hours = (time % 86400) / 3600;
		int minutes = (time % 3600) / 60;
		int seconds = time % 60;
		if (days > 0)
		{
			text += days + "d.";
			if (hours > 0)
			{
				text += " " + hours + "h.";
			}
			return text;
		}
		if (leftTime)
		{
			return ((hours < 10) ? "0" + hours : hours) + ":" + ((minutes < 10) ? "0" + minutes : minutes) + ":" + ((seconds < 10) ? "0" + seconds : seconds);
		}
		if (hours > 0)
		{
			text += hours + "h.";
			if (minutes > 0)
			{
				text += " " + minutes + "mins.";
			}
			return text;
		}
		text += minutes + "mins. " + seconds + "secs.";
		return text;
	}
	
	@Override
	public void parsewrite(String url, String ar1, String ar2, String ar3, String ar4, String ar5, L2PcInstance activeChar)
	{
	}
	
	private static VoteBBSManager _instance = new VoteBBSManager();
	
	public static VoteBBSManager getInstance()
	{
		return _instance;
	}
	
}