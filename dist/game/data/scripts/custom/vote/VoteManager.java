/* This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2, or (at your option)
 * any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA
 * 02111-1307, USA.
 *
 * http://www.gnu.org/copyleft/gpl.html
 */
package custom.vote;

import custom.erengine.*;
import l2r.gameserver.handler.AdminCommandHandler;
import l2r.gameserver.model.actor.instance.L2PcInstance;
import l2r.gameserver.network.serverpackets.ExShowScreenMessage;
import l2r.util.Rnd;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author Erlandys
 */
public class VoteManager
{
	private final static Logger _log = Logger.getLogger(VoteManager.class.getName());
	public static int VOTE_TIME = 30;
	public static boolean ENABLE_IDLE_TIME = true;
	public static int IDLE_TIME = 2;
	public static String COMMUNITY_BOARD_TAB = "_bbshome";
	public static boolean WHOLE_COMMUNITY = false;
	HashMap<Integer, IVote> _votes;
	HashMap<String, Long> _bannedAccounts;
	String _locationOfHandlers = "l2r.gameserver.handler.vote";
	public static String SERVER_NAME = "Erlandys";
	public static String VOTE_LINKS_INFORMATION_1 = "You can vote by writing this link into the browser or";
	public static String VOTE_LINKS_INFORMATION_2 = "open <font color=\"c6c859\">http://Erlandys/</font> and on the left side you will see vote links.";
	public static boolean SHOW_ONLINE = false;
	public static int FAKE_ONLINE = 0;
	public static boolean FAKE_ONLINE_IN_PERCENTS = false;
	public static boolean SHOW_ONLINE_FOR_GMS = true;
	public static boolean USE_VOTE_BUTTON = false;
	
	public VoteManager()
	{
		_votes = new HashMap<>();
		_bannedAccounts = new HashMap<>();
		generateSQL();
		generateXMLFile();
		loadXML();
		AdminCommandHandler.getInstance().registerHandler(new AdminVote());
	}
	
	public void loadXML()
	{
		_votes.clear();
		try
		{
			File file = new File("data/Vote.xml");
			Document doc = null;
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			factory.setValidating(false);
			factory.setIgnoringComments(true);
			try
			{
				doc = factory.newDocumentBuilder().parse(file);
			}
			catch (Exception e)
			{
				_log.log(Level.WARNING, "Could not parse Vote.xml file: " + e.getMessage(), e);
				return;
			}
			HashMap<String, String> configs = new HashMap<>();
			Node n = doc.getFirstChild();
			for (Node d = n.getFirstChild(); d != null; d = d.getNextSibling())
			{
				if (d.getNodeName().equalsIgnoreCase("vote"))
				{
					int id = Integer.parseInt(d.getAttributes().getNamedItem("id").getNodeValue());
					String type = d.getAttributes().getNamedItem("type").getNodeValue();
					
					String link = d.getAttributes().getNamedItem("linkForPlayers").getNodeValue();
					String linkToCheck = d.getAttributes().getNamedItem("linkForEngine").getNodeValue();
					boolean withIP = d.getAttributes().getNamedItem("withIP") == null ? false : Boolean.parseBoolean(d.getAttributes().getNamedItem("withIP").getNodeValue());
					int refreshesAfter = Integer.parseInt(d.getAttributes().getNamedItem("refreshesAfter").getNodeValue());
					HashMap<Integer, ArrayList<ErReward>> rewards = new HashMap<>();
					HashMap<Integer, ArrayList<ErBonus>> bonuses = new HashMap<>();
					ArrayList<Integer> rewardIds = new ArrayList<>();
					int voteRewardId = 0;
					for (Node c = d.getFirstChild(); c != null; c = c.getNextSibling())
					{
						if (c.getNodeName().equalsIgnoreCase("rewards"))
						{
							ArrayList<ErReward> r = new ArrayList<>();
							int rewardId = 0;
							ArrayList<ErBonus> b = new ArrayList<>();
							int bonusId = 0;
							for (Node h = c.getFirstChild(); h != null; h = h.getNextSibling())
							{
								if (h.getNodeName().equalsIgnoreCase("reward"))
								{
									int rew = ErReward.readReward(h, r, rewardId, false);
									if (rew == -1)
									{
										continue;
									}
									rewardId = rew;
								}
								else if (h.getNodeName().equalsIgnoreCase("bonus"))
								{
									int rew = ErBonus.readBonus(h, b, bonusId);
									if (rew == -1)
									{
										continue;
									}
									bonusId = rew;
								}
							}
							rewards.put(voteRewardId, r);
							bonuses.put(voteRewardId, b);
							rewardIds.add(voteRewardId);
							voteRewardId++;
						}
					}
					IVote vote = null;
					try
					{
						Constructor<?> c = Class.forName(_locationOfHandlers + "." + type).getConstructor(int.class, String.class, String.class, String.class, int.class, HashMap.class, HashMap.class, ArrayList.class, boolean.class);
						vote = (IVote) c.newInstance(id, type, link, linkToCheck, refreshesAfter, rewards, bonuses, rewardIds, withIP);
					}
					catch (ClassNotFoundException e)
					{
						_log.info(getClass().getSimpleName() + ": Vote handler was not found: \"" + _locationOfHandlers + "." + type + "\".");
						continue;
					}
					_votes.put(id, vote);
				}
				else if (d.getNodeName().equalsIgnoreCase("set"))
				{
					String name = d.getAttributes().getNamedItem("name").getNodeValue().toLowerCase();
					String val = d.getAttributes().getNamedItem("val").getNodeValue();
					configs.put(name, val);
				}
			}
			fixConfigs(configs);
			_log.info(getClass().getSimpleName() + ": Successfully loaded " + _votes.size() + " vote types.");
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
	
	void fixConfigs(HashMap<String, String> configs)
	{
		if (configs.containsKey("votetime"))
		{
			try
			{
				VOTE_TIME = Integer.parseInt(configs.get("votetime"));
			}
			catch (NumberFormatException e)
			{
				_log.info(getClass().getSimpleName() + ": VoteTime config is with not correct number, set to default[" + VOTE_TIME + "].");
			}
		}
		else
		{
			_log.info(getClass().getSimpleName() + ": Config for VoteTime was not found, set to default[" + VOTE_TIME + "].");
		}
		if (configs.containsKey("idletime"))
		{
			try
			{
				IDLE_TIME = Integer.parseInt(configs.get("idletime"));
			}
			catch (NumberFormatException e)
			{
				_log.info(getClass().getSimpleName() + ": IdleTime config is with not correct number, set to default[" + IDLE_TIME + "].");
			}
		}
		else
		{
			_log.info(getClass().getSimpleName() + ": Config for IdleTime was not found, set to default[" + IDLE_TIME + "].");
		}
		if (configs.containsKey("enableidletime"))
		{
			ENABLE_IDLE_TIME = configs.get("enableidletime").equalsIgnoreCase("true");
		}
		else
		{
			_log.info(getClass().getSimpleName() + ": Config for EnableIdleTime was not found, set to default[" + ENABLE_IDLE_TIME + "].");
		}
		if (configs.containsKey("tabincommunityboard"))
		{
			COMMUNITY_BOARD_TAB = configs.get("tabincommunityboard");
		}
		else
		{
			_log.info(getClass().getSimpleName() + ": Config for TabInCommunityBoard was not found, set to default[" + COMMUNITY_BOARD_TAB + "].");
		}
		if (configs.containsKey("wholecommunity"))
		{
			WHOLE_COMMUNITY = configs.get("wholecommunity").equalsIgnoreCase("true");
		}
		else
		{
			_log.info(getClass().getSimpleName() + ": Config for WholeCommunity was not found, set to default[" + WHOLE_COMMUNITY + "].");
		}
		if (configs.containsKey("votelinksinformation1"))
		{
			VOTE_LINKS_INFORMATION_1 = configs.get("votelinksinformation1");
		}
		else
		{
			_log.info(getClass().getSimpleName() + ": Config for VoteLinksInformation1 was not found, set to default[" + VOTE_LINKS_INFORMATION_1 + "].");
		}
		if (configs.containsKey("votelinksinformation2"))
		{
			VOTE_LINKS_INFORMATION_2 = configs.get("votelinksinformation2");
		}
		else
		{
			_log.info(getClass().getSimpleName() + ": Config for VoteLinksInformation2 was not found, set to default[" + VOTE_LINKS_INFORMATION_2 + "].");
		}
		if (configs.containsKey("showonline"))
		{
			SHOW_ONLINE = configs.get("showonline").equalsIgnoreCase("true");
		}
		else
		{
			_log.info(getClass().getSimpleName() + ": Config for ShowOnline was not found, set to default[" + SHOW_ONLINE + "].");
		}
		if (configs.containsKey("showonlineforgms"))
		{
			SHOW_ONLINE_FOR_GMS = configs.get("showonlineforgms").equalsIgnoreCase("true");
		}
		else
		{
			_log.info(getClass().getSimpleName() + ": Config for ShowOnlineForGMs was not found, set to default[" + SHOW_ONLINE_FOR_GMS + "].");
		}
		if (configs.containsKey("fakeonline"))
		{
			try
			{
				FAKE_ONLINE = Integer.parseInt(configs.get("idletime"));
			}
			catch (NumberFormatException e)
			{
				_log.info(getClass().getSimpleName() + ": FakeOnline config is with not correct number, set to default[" + FAKE_ONLINE + "].");
			}
		}
		else
		{
			_log.info(getClass().getSimpleName() + ": Config for FakeOnline was not found, set to default[" + FAKE_ONLINE + "].");
		}
		if (configs.containsKey("fakeonlineinpercents"))
		{
			FAKE_ONLINE_IN_PERCENTS = configs.get("fakeonlineinpercents").equalsIgnoreCase("true");
		}
		else
		{
			_log.info(getClass().getSimpleName() + ": Config for FakeOnlineInPercents was not found, set to default[" + FAKE_ONLINE_IN_PERCENTS + "].");
		}
		if (configs.containsKey("usevotebutton"))
		{
			USE_VOTE_BUTTON = configs.get("usevotebutton").equalsIgnoreCase("true");
		}
		else
		{
			_log.info(getClass().getSimpleName() + ": Config for UseVoteButton was not found, set to default[" + USE_VOTE_BUTTON + "].");
		}
	}
	
	public boolean startVoting(L2PcInstance player, int voteId, int rewardId)
	{
		VotingPlayer vp = null;
		try
		{
			vp = (VotingPlayer) player.getClass().getMethod("getVotingPlayer").invoke(player);
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		if (!_votes.containsKey(voteId))
		{
			player.sendMessage("This vote selection does not exist!");
			SendVotingMessage(player, "This vote selection does not exist!", ErSMPos.BOTTOM_RIGHT);
			return false;
		}
		if (vp != null)
		{
			if (vp.hasVotedToday(voteId))
			{
				player.sendMessage("You have already voted for this!");
				SendVotingMessage(player, "You have already voted for this!", ErSMPos.BOTTOM_RIGHT);
				return false;
			}
			if (vp.isVoting())
			{
				player.sendMessage("You are already voting for other top!");
				SendVotingMessage(player, "You are already voting for other top!", ErSMPos.BOTTOM_RIGHT);
				return false;
			}
		}
		if (_votes.get(voteId).isVoting())
		{
			player.sendMessage("Someone is voting right now!");
			SendVotingMessage(player, "Someone is voting right now!", ErSMPos.BOTTOM_RIGHT);
			return false;
		}
		if (isBanned(player.getAccountName()))
		{
			player.sendMessage("Try to vote later. You have voted earlier than " + IDLE_TIME + "mins!");
			SendVotingMessage(player, "Try to vote later, " + IDLE_TIME + "minutes haven't passed yet!", ErSMPos.BOTTOM_RIGHT);
			return false;
		}
		_votes.get(voteId).startVoting(player, rewardId);
		return true;
	}
	
	public static void SendVotingMessage(L2PcInstance player, String text)
	{
		SendVotingMessage(player, text, ErSMPos.TOP_CENTER);
	}
	
	public static void SendVotingMessage(L2PcInstance player, String text, ErSMPos location)
	{
		SendVotingMessage(player, text, location, 5000);
	}
	
	public static void SendVotingMessage(L2PcInstance player, String text, ErSMPos location, int time)
	{
		if (location != ErSMPos.BOTTOM_RIGHT)
		{
			showScreenMessage(player, text, time, location, false, true, false);
		}
		else
		{
			showScreenMessage(player, text, time, location, false, true, true);
		}
	}
	
	public IVote getRandomVoteType()
	{
		Collection<Integer> v = _votes.keySet();
		return _votes.get(v.toArray()[Rnd.get(0, v.size() - 1)]);
	}
	
	public IVote getVoteType(int type)
	{
		return _votes.containsKey(type) ? _votes.get(type) : null;
	}
	
	public Collection<IVote> getAllVoteTypes()
	{
		return _votes.values();
	}
	
	public boolean isBanned(String accountName)
	{
		if (!_bannedAccounts.containsKey(accountName) || !ENABLE_IDLE_TIME)
		{
			return false;
		}
		return _bannedAccounts.get(accountName) > System.currentTimeMillis();
	}
	
	public void addBan(String accountName)
	{
		if (!ENABLE_IDLE_TIME)
		{
			return;
		}
		_bannedAccounts.put(accountName, System.currentTimeMillis() + (IDLE_TIME * 60 * 1000));
	}
	
	public int banWillBeLiftedAfter(String accountName)
	{
		return (int) (_bannedAccounts.get(accountName) - System.currentTimeMillis()) / 1000;
	}
	
	public static void showScreenMessage(L2PcInstance player, String text, int time, ErSMPos position, boolean effect, boolean fade, boolean small)
	{
		player.sendPacket(new ExShowScreenMessage(1, 0, position.ordinal(), 0, small ? 1 : 0, 0, 0, effect, time, fade, text));
	}
	
	private void generateSQL()
	{
		String text = "";
		text += "CREATE TABLE er_character_votes (\n";
		text += "  account varchar(40) NOT NULL,\n";
		text += "  votes text NOT NULL,\n";
		text += "  PRIMARY KEY (account)\n";
		text += ") ENGINE=InnoDB DEFAULT CHARSET=utf8 ROW_FORMAT=DYNAMIC;";
		ErUtils.generateTable("VoteTableInitialised", text);
		
		text = "CREATE TABLE er_character_bonuses (\n";
		text += "  `id` int(10) unsigned NOT NULL AUTO_INCREMENT,\n";
		text += "  objectId int(10) NOT NULL,\n";
		text += "  `type` varchar(50) DEFAULT NULL,\n";
		text += "  itemId int(10) DEFAULT NULL,\n";
		text += "  count int(10) DEFAULT NULL,\n";
		text += "  itemTypeChance tinyint(1) DEFAULT NULL,\n";
		text += "  addingPercent tinyint(1) DEFAULT NULL,\n";
		text += "  expireDate bigint(15) DEFAULT NULL,\n";
		text += "  PRIMARY KEY (`id`)\n";
		text += ") ENGINE=InnoDB DEFAULT CHARSET=utf8;";
		ErUtils.generateTable("PlayerBonusesInitialised", text);
	}
	
	private void generateXMLFile()
	{
		if (ErGlobalVariables.getInstance().getBoolean("VoteXMLInitialized"))
		{
			return;
		}
		String text = "";
		text += "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n";
		text += "<list>\n";
		text += "	<!-- Available site types: HopZone / L2jLT / L2Network / L2TopCO / TopZone -->\n";
		text += "	<!-- You can add maximum 10 vote sites, but if you will need new one, you will have to make new handler with it's own parsing from website. -->\n";
		text += "	\n";
		text += "	<!-- If you use npc, leave this \"EMPTY\" (Npc instance -> L2EVoteManager) -->\n";
		text += "	<!-- This sets, the tab for community board (can be found in ShowBoard serverpacket). -->\n";
		text += "	<set name=\"TabInCommunityBoard\" val=\"_bbshome\" />\n";
		text += "	<!-- Enable / Disable whole community board to be only for vote. -->\n";
		text += "	<set name=\"WholeCommunity\" val=\"false\" />\n";
		text += "	<!-- This sets, how much seconds give to player, to vote for one of top sites. -->\n";
		text += "	<set name=\"VoteTime\" val=\"30\" />\n";
		text += "	<!-- Enable / Disable idle (ban) time, for x minutes, after successful/unsuccessful vote (to give some time to vote for others). -->\n";
		text += "	<set name=\"EnableIdleTime\" val=\"true\" />\n";
		text += "	<!-- Idle (ban) time for x minutes, after successful/unsuccessful vote (to give some time to vote for others). -->\n";
		text += "	<set name=\"IdleTime\" val=\"2\" />\n";
		text += "	<!-- First information line below rewards. -->\n";
		text += "	<set name=\"VoteLinksInformation1\" val=\"You can vote by writing this link into the browser or\" />\n";
		text += "	<!-- Second information line below rewards. -->\n";
		text += "	<set name=\"VoteLinksInformation2\" val=\"open http://www.l2consortio.com/ and on the left side you will see vote links.\" />\n";
		text += "	<!-- Show online count in vote manager. -->\n";
		text += "	<set name=\"ShowOnline\" val=\"false\" />\n";
		text += "	<!-- Show online count in vote manager only for GMs. -->\n";
		text += "	<set name=\"ShowOnlineForGMs\" val=\"true\" />\n";
		text += "	<!-- Show fake online in percents for ex: (online + 50% of online). -->\n";
		text += "	<set name=\"FakeOnlineInPercents\" val=\"true\" />\n";
		text += "	<!-- Show fake online added for normal online (For GMs online is real). -->\n";
		text += "	<set name=\"FakeOnline\" val=\"0\" />\n";
		text += "	<!-- Use button 'Vote' or use buttons as '+' or '-' -->\n";
		text += "	<set name=\"UseVoteButton\" val=\"true\" />\n";
		text += "\n";
		text += "	<!-- Explanations: -->\n";
		text += "	\n";
		text += "	<!-- vote part: -->\n";
		text += "	<!-- id must be unique (min 1, max 10). -->\n";
		text += "	<!-- type is the handler of vote site. -->\n";
		text += "	<!-- linkForPlayers is shown to players, where they can go to vote. Also you can write your text in here. -->\n";
		text += "	<!-- linkForEngine is the place where to check votes count. -->\n";
		text += "	\n";
		text += "	<!-- !!! WARNING !!! IF LINK CONTAINS '&' SYMBOL, YOU HAVE TO CHANGE IT TO '&amp;' TO WORK. -->\n";
		text += "	\n";
		text += "	<!-- refreshesAfter is the time in seconds, after how much time account can vote again. -->\n";
		text += "	<!-- withIP works only if you fix handler to accept with IP (just add withIp=\"true\"). -->\n";
		text += "	\n";
		text += "	<!-- rewards part: -->\n";
		text += "	<!-- Must be in <rewards> node (EACH NODE, EACH REWARDS SECTION!) -->\n";
		text += "	\n";
		text += "	<!-- REWARD: -->\n";
		text += "	<!-- id: is used only when you want to give ITEM as reward, then it is itemId -->\n";
		text += "	<!-- type: can be - \n";
		text += "		 Item\n";
		text += "		 ClanPoints\n";
		text += "		 SkillPoints\n";
		text += "		 Experience -->\n";
		text += "	<!-- min: describes minimum amount of item given to player -->\n";
		text += "	<!-- max: describes maximum amount of item given to player -->\n";
		text += "	<!-- chance: describes the chance of probability that item will be given to player -->\n";
		text += "	<!-- icon: shown icon next to reward -->\n";
		text += "	\n";
		text += "	<!-- BONUS: -->\n";
		text += "	<!-- id: is used only when you want to give ITEM as reward, then it is itemId -->\n";
		text += "	<!-- type: can be - \n";
		text += "		 Item\n";
		text += "		 ClanPoints\n";
		text += "		 SkillPoints\n";
		text += "		 Experience\n";
		text += "		 WeaponEnchant \n";
		text += "		 ArmorEnchant\n";
		text += "		 SkillsEnchant -->\n";
		text += "	<!-- addingType: can be -\n";
		text += "		 percent: bonus will be calculated with percents,\n";
		text += "		 for example: adena drop chance will be increased not by 10% from 60% to 70%, but 60% by 10%, so it will be 66%.\n";
		text += "		 amount: bonus will be added by amount -->\n";
		text += "	<!-- min: describes minimum amount of bonus given to player -->\n";
		text += "	<!-- max: describes maximum amount of bonus given to player -->\n";
		text += "	<!-- chance: describes the chance of probability that bonus will be given to player -->\n";
		text += "	<!-- itemBonusType: (is used only when adding bonus with Item type) can be -\n";
		text += "		 chance: to increase chance to drop that item\n";
		text += "		 drop: to increase amount of item to be dropped -->\n";
		text += "	<!-- time: (seconds) for how long bonus will be added -->\n";
		text += "	<!-- icon: shown icon next to reward -->\n";
		text += "	\n";
		text += "	<vote id=\"1\" type=\"HopZone\" linkForPlayers=\"http://vgw.hopzone.net/site/vote/79743/1\" linkForEngine=\"http://l2.hopzone.net/lineage2/details/79743/L2Saga\" refreshesAfter=\"86400\">\n";
		text += "		<rewards>\n";
		text += "			<bonus id=\"57\" type=\"Item\" itemBonusType=\"chance\" addingType=\"amount\" min=\"10\" max=\"10\" chance=\"100\" time=\"3600\" icon=\"icon.etc_adena_i00\"/>\n";
		text += "			<reward type=\"ClanPoints\" min=\"500\" max=\"1000\" chance=\"100\" icon=\"icon.etc_bloodpledge_point_i00\" />\n";
		text += "		</rewards>\n";
		text += "		<rewards>\n";
		text += "			<reward type=\"Experience\" min=\"500000\" max=\"1000000\" chance=\"100\" icon=\"icon.etc_blesscharm_slay_val_i00\"/>\n";
		text += "			<bonus type=\"SkillPoints\" addingType=\"amount\" min=\"10\" max=\"10\" chance=\"100\" time=\"3600\" icon=\"icon.etc_aidcharm_cancel_deathfire_i00\"/>\n";
		text += "		</rewards>\n";
		text += "	</vote>\n";
		text += "	<vote id=\"2\" type=\"L2jLT\" linkForPlayers=\"http://www.l2servers.com/servers/vote-22762.php\" linkForEngine=\"http://www.l2servers.com/servers/vote-22762.php\" refreshesAfter=\"86400\">\n";
		text += "		<rewards>\n";
		text += "			<reward type=\"Experience\" min=\"500000\" max=\"1000000\" chance=\"100\" icon=\"icon.etc_blesscharm_slay_val_i00\"/>\n";
		text += "			<reward type=\"ClanPoints\" min=\"500\" max=\"1000\" chance=\"100\" icon=\"icon.etc_bloodpledge_point_i00\" />\n";
		text += "		</rewards>\n";
		text += "		<rewards>\n";
		text += "			<reward type=\"Experience\" min=\"500000\" max=\"1000000\" chance=\"100\" icon=\"icon.etc_blesscharm_slay_val_i00\"/>\n";
		text += "			<bonus type=\"SkillPoints\" addingType=\"amount\" min=\"10\" max=\"10\" chance=\"100\" time=\"3600\" icon=\"icon.etc_aidcharm_cancel_deathfire_i00\"/>\n";
		text += "			<bonus type=\"SkillPoints\" addingType=\"percent\" min=\"10\" max=\"20\" chance=\"100\" time=\"3600\" icon=\"icon.etc_aidcharm_cancel_deathfire_i00\"/>\n";
		text += "			<reward type=\"ClanPoints\" min=\"500\" max=\"1000\" chance=\"100\" icon=\"icon.etc_bloodpledge_point_i00\" />\n";
		text += "			<reward id=\"57\" type=\"Item\" min=\"30\" max=\"40\" chance=\"100\" party=\"true\" icon=\"icon.etc_adena_i00\"/>\n";
		text += "		</rewards>\n";
		text += "	</vote>\n";
		text += "	<vote id=\"3\" type=\"L2Network\" linkForPlayers=\"http://l2network.eu/index.php?a=in&amp;u=adler1313\" linkForEngine=\"http://l2network.eu/details/adler1313/\" refreshesAfter=\"86400\">\n";
		text += "		<rewards>\n";
		text += "			<bonus id=\"57\" type=\"Item\" itemBonusType=\"chance\" addingType=\"amount\" min=\"10\" max=\"10\" chance=\"100\" time=\"3600\" icon=\"icon.etc_adena_i00\"/>\n";
		text += "			<reward type=\"ClanPoints\" min=\"500\" max=\"1000\" chance=\"100\" icon=\"icon.etc_bloodpledge_point_i00\" />\n";
		text += "		</rewards>\n";
		text += "		<rewards>\n";
		text += "			<bonus type=\"SkillPoints\" addingType=\"amount\" min=\"10\" max=\"10\" chance=\"100\" time=\"3600\" icon=\"icon.etc_aidcharm_cancel_deathfire_i00\"/>\n";
		text += "			<bonus type=\"ClanPoints\" addingType=\"amount\" min=\"10\" max=\"10\" chance=\"100\" time=\"3600\" icon=\"icon.etc_bloodpledge_point_i00\"/>\n";
		text += "		</rewards>\n";
		text += "	</vote>\n";
		text += "	<vote id=\"4\" type=\"L2TopCO\" linkForPlayers=\"http://www.l2top.co/vote/server/1589\" linkForEngine=\"http://l2top.co/index.php?page=server_info&amp;id=1589\" refreshesAfter=\"86400\">\n";
		text += "		<rewards>\n";
		text += "			<bonus id=\"57\" type=\"Item\" itemBonusType=\"chance\" addingType=\"amount\" min=\"10\" max=\"10\" chance=\"100\" time=\"3600\" icon=\"icon.etc_adena_i00\"/>\n";
		text += "			<reward type=\"ClanPoints\" min=\"500\" max=\"1000\" chance=\"100\" icon=\"icon.etc_bloodpledge_point_i00\" />\n";
		text += "		</rewards>\n";
		text += "	</vote>\n";
		text += "	<vote id=\"5\" type=\"TopZone\" linkForPlayers=\"http://l2topzone.com/vote/id/6084\" linkForEngine=\"http://l2topzone.com/lineage/server-info/6084/l2damage\" refreshesAfter=\"86400\">\n";
		text += "		<rewards>\n";
		text += "			<bonus id=\"57\" type=\"Item\" itemBonusType=\"drop\" addingType=\"percent\" min=\"10\" max=\"10\" chance=\"100\" time=\"3600\" icon=\"icon.etc_adena_i00\"/>\n";
		text += "			<bonus type=\"Experience\" addingType=\"amount\" min=\"10\" max=\"10\" chance=\"100\" time=\"3600\" icon=\"icon.etc_blesscharm_slay_val_i00\"/>\n";
		text += "		</rewards>\n";
		text += "	</vote>\n";
		text += "</list>\n";
		
		ErGlobalVariables.getInstance().setData("VoteXMLInitialized", true);
		ErUtils.generateFile("data/", "Vote", ".xml", text);
	}
	
	public static final VoteManager getInstance()
	{
		return SingletonHolder._instance;
	}
	
	private static class SingletonHolder
	{
		protected static final VoteManager _instance = new VoteManager();
	}
}
