/*
 * Copyright (C) 2004-2014 L2J DataPack
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
package custom.npctopc;

import l2r.gameserver.data.sql.NpcTable;
import l2r.gameserver.enums.Race;
import l2r.gameserver.handler.IAdminCommandHandler;
import l2r.gameserver.model.L2Object;
import l2r.gameserver.model.L2World;
import l2r.gameserver.model.actor.instance.L2NpcInstance;
import l2r.gameserver.model.actor.instance.L2PcInstance;
import l2r.gameserver.model.actor.templates.L2NpcTemplate;
import l2r.gameserver.model.effects.AbnormalEffect;
import l2r.gameserver.network.serverpackets.AbstractNpcInfo.NpcInfo;
import l2r.gameserver.network.serverpackets.NpcHtmlMessage;

import java.util.StringTokenizer;

/**
 * EditChar admin command implementation.
 */
public class AdminEditNpcToPc implements IAdminCommandHandler
{
	private static final String[] ADMIN_COMMANDS =
	{
		"admin_edit_npctopc",
		"admin_save_npctopc",
		"admin_reloadnpctopc",
		"admin_updatenpctopc"
	};
	
	@Override
	public boolean useAdminCommand(String command, L2PcInstance activeChar)
	{
		if (!activeChar.isGM())
		{
			return false;
		}
		if (command.startsWith("admin_edit_npctopc"))
		{
			StringTokenizer st = new StringTokenizer(command.substring(19), " ");
			int npcId = Integer.parseInt(st.nextToken());
			int page = 0;
			if (st.hasMoreTokens())
			{
				page = Integer.parseInt(st.nextToken());
			}
			showMainHtml(activeChar, npcId, page);
		}
		else if (command.startsWith("admin_save_npctopc"))
		{
			StringTokenizer st = new StringTokenizer(command.substring(19), ";");
			int npcId = Integer.parseInt(st.nextToken());
			String name = st.nextToken().substring(1);
			String value = st.nextToken().substring(1);
			value = value.substring(0, value.length() - 1);
			if ((name.equalsIgnoreCase("Title") || name.equalsIgnoreCase("Name")) && value.equalsIgnoreCase("NULL"))
			{
				value = "";
			}
			int pageId = Integer.parseInt(st.nextToken().replaceAll(" ", ""));
			if (name.equalsIgnoreCase("NameColor") || name.equalsIgnoreCase("TitleColor"))
			{
				if (value.length() != 6)
				{
					activeChar.sendMessage("Color must have 6 letters!");
					showMainHtml(activeChar, npcId, 0);
					return true;
				}
			}
			NpcToPcManager.getInstance().setInfo(npcId, name, value);
			for (L2Object obj : L2World.getInstance().getVisibleObjects())
			{
				if (obj instanceof L2NpcInstance)
				{
					L2NpcInstance npc = (L2NpcInstance) obj;
					if (npc.getId() == npcId)
					{
						npc.broadcastPacket(new NpcInfo(npc, null));
					}
				}
			}
			showMainHtml(activeChar, npcId, pageId);
		}
		else if (command.startsWith("admin_reloadnpctopc"))
		{
			NpcToPcManager.getInstance().init();
			activeChar.sendMessage("Npc to Pc data were successfully restored from XML!");
		}
		else if (command.startsWith("admin_updatenpctopc"))
		{
			NpcToPcManager.getInstance().rewriteToXml();
			activeChar.sendMessage("Npc to Pc data were successfully stored into XML!");
		}
		return true;
	}
	
	private void showMainHtml(L2PcInstance activeChar, int npcId, int pageId)
	{
		String content = getContent(pageId);
		L2NpcTemplate npc = NpcTable.getInstance().getTemplate(npcId);
		NpcToPcManager manager = NpcToPcManager.getInstance();
		content = content.replace("%npcId%", npcId + "");
		if (pageId == 0)
		{
			content = content.replace("%enabled%", manager.npcExist(npcId) ? "True" : "False");
			content = content.replace("%name%", manager.getString(npcId, "Name").equals("") ? npc.getName() : manager.getString(npcId, "Name"));
			content = content.replace("%nameColor%", manager.getString(npcId, "NameColor").equals("") ? "FFFFFF" : manager.getString(npcId, "NameColor").toUpperCase());
			content = content.replace("%title%", manager.getString(npcId, "Title").equals("") ? "" : manager.getString(npcId, "Title"));
			content = content.replace("%titleColor%", manager.getString(npcId, "TitleColor").equals("") ? "FFFFFF" : manager.getString(npcId, "TitleColor").toUpperCase());
			int raceId = manager.getInt(npcId, "Race");
			String race = "Human";
			if (raceId == 0)
			{
				race = "Human";
			}
			else if (raceId == 1)
			{
				race = "Elf";
			}
			else if (raceId == 2)
			{
				race = "DarkElf";
			}
			else if (raceId == 3)
			{
				race = "Orc";
			}
			else if (raceId == 4)
			{
				race = "Dwarf";
			}
			else if (raceId == 5)
			{
				race = "Kamael";
			}
			content = content.replace("%race%", race);
			//
			if (raceId > 3)
			{
				content = content.replace("%raceType%", "");
			}
			else
			{
				String raceTypes = (manager.getBoolean(npcId, "RaceType") ? "Mage" : "Fighter") + ";" + (manager.getBoolean(npcId, "RaceType") ? "Fighter" : "Mage");
				content = content.replace("%raceType%", "<tr><td>Race Type (" + (manager.getBoolean(npcId, "RaceType") ? "Mage" : "Fighter") + "):</td><td><combobox var=\"rt\" list=\"" + raceTypes + "\" width=70></td><td><button value=\"Set\" width=35 action=\"bypass -h admin_save_npctopc " + npcId + "; RaceType; $rt ; 0\" height=15 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_CT1.Button_DF\"></td></tr>");
			}
			int genderId = manager.getInt(npcId, "Gender");
			String gender = "Not Exist";
			if (genderId == 0)
			{
				gender = "Male";
			}
			else if (genderId == 1)
			{
				gender = "Female";
			}
			content = content.replace("%gender%", gender);
			content = content.replace("%rhand%", manager.getInt(npcId, "RHand") + "");
			content = content.replace("%lhand%", manager.getInt(npcId, "LHand") + "");
			content = content.replace("%enchant%", manager.getInt(npcId, "Enchant") + "");
			content = content.replace("%gloves%", manager.getInt(npcId, "Gloves") + "");
			content = content.replace("%chest%", manager.getInt(npcId, "Chest") + "");
			content = content.replace("%legs%", manager.getInt(npcId, "Legs") + "");
			content = content.replace("%boots%", manager.getInt(npcId, "Feet") + "");
			content = content.replace("%cloak%", manager.getInt(npcId, "Cloak") + "");
			String isEnabled = (manager.getBoolean(npcId, "IsEnabled") ? "True" : "False") + ";" + (manager.getBoolean(npcId, "IsEnabled") ? "False" : "True");
			content = content.replace("%enableds%", isEnabled);

			String races = Race.values()[manager.getInt(npcId, "Race")].name() + ";";
			for (Race r : Race.values())
			{
				if (r.ordinal() == raceId)
				{
					continue;
				}
				races += r.name() + ";";
			}
			races = races.substring(0, races.length() - 1);
			content = content.replace("%races%", races);
			
			String genders = gender + ";" + (genderId == 1 ? "Male" : "Female");
			content = content.replace("%genders%", genders);
		}
		else if (pageId == 1) {
			content = content.replace("%hair1%", manager.getInt(npcId, "Hair1") + "");
			content = content.replace("%hair2%", manager.getInt(npcId, "Hair2") + "");
			content = content.replace("%useAug%", manager.getInt(npcId, "UseAugmentation") != 0 ? "True" : "False");
			content = content.replace("%pvpflag%", manager.getInt(npcId, "PvPFlag") + "");
			content = content.replace("%karma%", manager.getInt(npcId, "Karma") + "");
			int mountTypeId = manager.getInt(npcId, "MountType");
			String mountType = "No Mount";
			if (mountTypeId == 1)
			{
				mountType = "Strider";
			}
			else if (mountTypeId == 2)
			{
				mountType = "Wywern";
			}
			content = content.replace("%mountType%", mountType);
			if (mountTypeId > 0)
			{
				content = content.replace("%mountNpcId%", "<tr><td>Mount Npc Id (" + manager.getInt(npcId, "MountNpcId") + "):</td><td><edit var=\"mni\" width=70></td><td><button value=\"Set\" width=35 action=\"bypass -h admin_save_npctopc " + npcId + "; MountNpcId; $mni ; 1\" height=15 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_CT1.Button_DF\"></td></tr>");
			}
			else
			{
				content = content.replace("%mountNpcId%", "");
			}
			content = content.replace("%hairStyle%", manager.getInt(npcId, "HairStyle") + "");
			content = content.replace("%hairColor%", manager.getInt(npcId, "HairColor") + "");
			content = content.replace("%face%", manager.getInt(npcId, "Face") + "");
			int circleId = manager.getInt(npcId, "Circle");
			String circle = "No Team";
			if (circleId == 1)
			{
				circle = "Blue";
			}
			else if (circleId == 2)
			{
				circle = "Red";
			}
			content = content.replace("%circle%", circle);
			content = content.replace("%isSitting%", manager.getBoolean(npcId, "IsSitting") ? "True" : "False");
			content = content.replace("%isRunning%", manager.getBoolean(npcId, "IsRunning") ? "True" : "False");
			content = content.replace("%isInCombat%", manager.getBoolean(npcId, "IsInCombat") ? "True" : "False");
			content = content.replace("%isAlikeDead%", manager.getBoolean(npcId, "IsAlikeDead") ? "True" : "False");
			String useAugs = (manager.getInt(npcId, "UseAugmentation") != 0 ? "True" : "False") + ";" + (manager.getInt(npcId, "UseAugmentation") != 0 ? "False" : "True");
			content = content.replace("%useAugs%", useAugs);

			String pvpFlags = manager.getInt(npcId, "PvPFlag") + ";";
			for (int i = 0; i < 3; i++)
			{
				if (manager.getInt(npcId, "PvPFlag") == i)
				{
					continue;
				}
				pvpFlags += i + ";";
			}
			pvpFlags = pvpFlags.substring(0, pvpFlags.length() - 1);
			content = content.replace("%pvpFlags%", pvpFlags);
			
			String mountTypes = mountType + ";";
			String mTypes[] =
			{
				"No Mount",
				"Strider",
				"Wywern"
			};
			for (int i = 0; i < 3; i++)
			{
				if (mountTypeId == i)
				{
					continue;
				}
				mountTypes += mTypes[i] + ";";
			}
			mountTypes = mountTypes.substring(0, mountTypes.length() - 1);
			content = content.replace("%mountTypes%", mountTypes);

			String teams = circle + ";";
			String tTypes[] =
			{
				"No Team",
				"Blue",
				"Red"
			};
			for (int i = 0; i < 3; i++)
			{
				if (circleId == i)
				{
					continue;
				}
				teams += tTypes[i] + ";";
			}
			teams = teams.substring(0, teams.length() - 1);
			content = content.replace("%teams%", teams);

			String isSitting = (manager.getBoolean(npcId, "IsSitting") ? "True" : "False") + ";" + (manager.getBoolean(npcId, "IsSitting") ? "False" : "True");
			content = content.replace("%isSittings%", isSitting);
			
			String isRunning = (manager.getBoolean(npcId, "IsRunning") ? "True" : "False") + ";" + (manager.getBoolean(npcId, "IsRunning") ? "False" : "True");
			content = content.replace("%isRunnings%", isRunning);
			
			String isInCombat = (manager.getBoolean(npcId, "IsInCombat") ? "True" : "False") + ";" + (manager.getBoolean(npcId, "IsInCombat") ? "False" : "True");
			content = content.replace("%isInCombats%", isInCombat);

			String isAlikeDead = (manager.getBoolean(npcId, "IsAlikeDead") ? "True" : "False") + ";" + (manager.getBoolean(npcId, "IsAlikeDead") ? "False" : "True");
			content = content.replace("%isAlikeDeads%", isAlikeDead);
		}
		else if (pageId == 2) {
			content = content.replace("%privStore%", manager.getInt(npcId, "PrivateStore") + "");
			String cubic = "";
			for (int id : manager.getIntegerArray(npcId, "Cubics"))
			{
				cubic += id + ",";
			}
			if (cubic.equals(""))
			{
				cubic = "No Cubics";
			}
			cubic = cubic.substring(0, cubic.length() - 1);
			content = content.replace("%cubics%", cubic);
			content = content.replace("%isInParty%", manager.getBoolean(npcId, "IsInPartyChatRoom") ? "True" : "False");
			content = content.replace("%rec%", manager.getInt(npcId, "Recommendations") + "");
			content = content.replace("%noble%", manager.getBoolean(npcId, "IsNoble") ? "True" : "False");
			content = content.replace("%hero%", manager.getBoolean(npcId, "IsHero") ? "True" : "False");
			content = content.replace("%fishing%", manager.getBoolean(npcId, "IsFishing") ? "True" : "False");
			if (manager.getBoolean(npcId, "IsFishing"))
			{
				content = content.replace("%fishX%", "<tr><td>Fishing X (" + manager.getInt(npcId, "FishingX") + "):</td><td><edit var=\"fx\" width=70></td><td><button value=\"Set\" width=35 action=\"bypass -h admin_save_npctopc " + npcId + "; FishingX; $fx ; 2\" height=15 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_CT1.Button_DF\"></td></tr>");
				content = content.replace("%fishY%", "<tr><td>Fishing Y (" + manager.getInt(npcId, "FishingY") + "):</td><td><edit var=\"fy\" width=70></td><td><button value=\"Set\" width=35 action=\"bypass -h admin_save_npctopc " + npcId + "; FishingY; $fy ; 2\" height=15 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_CT1.Button_DF\"></td></tr>");
				content = content.replace("%fishZ%", "<tr><td>Fishing Z (" + manager.getInt(npcId, "FishingZ") + "):</td><td><edit var=\"fz\" width=70></td><td><button value=\"Set\" width=35 action=\"bypass -h admin_save_npctopc " + npcId + "; FishingZ; $fz ; 2\" height=15 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_CT1.Button_DF\"></td></tr>");
			}
			else
			{
				content = content.replace("%fishX%", "");
				content = content.replace("%fishY%", "");
				content = content.replace("%fishZ%", "");
			}
			content = content.replace("%clanId%", manager.getInt(npcId, "ClanId") + "");
			if (manager.getInt(npcId, "ClanId") > 0)
			{
				content = content.replace("%pClass%", "<tr><td>Pledge Class (" + manager.getInt(npcId, "PledgeClass") + "):</td><td><edit var=\"pc\" width=70></td><td><button value=\"Set\" width=35 action=\"bypass -h admin_save_npctopc " + npcId + "; PledgeClass; $pc ; 2\" height=15 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_CT1.Button_DF\"></td></tr>");
				content = content.replace("%pType%", "<tr><td>Pledge Type (" + manager.getInt(npcId, "PledgeType") + "):</td><td><edit var=\"pt\" width=70></td><td><button value=\"Set\" width=35 action=\"bypass -h admin_save_npctopc " + npcId + "; PledgeType; $pt ; 2\" height=15 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_CT1.Button_DF\"></td></tr>");
			}
			else
			{
				content = content.replace("%pClass%", "");
				content = content.replace("%pType%", "");
			}
			String isInParty = (manager.getBoolean(npcId, "IsInPartyChatRoom") ? "True" : "False") + ";" + (manager.getBoolean(npcId, "IsInPartyChatRoom") ? "False" : "True");
			content = content.replace("%isInPartys%", isInParty);
			
			String noble = (manager.getBoolean(npcId, "IsNoble") ? "True" : "False") + ";" + (manager.getBoolean(npcId, "IsNoble") ? "False" : "True");
			content = content.replace("%nobles%", noble);
			
			String hero = (manager.getBoolean(npcId, "IsHero") ? "True" : "False") + ";" + (manager.getBoolean(npcId, "IsHero") ? "False" : "True");
			content = content.replace("%heros%", hero);

			String fishing = (manager.getBoolean(npcId, "IsFishing") ? "True" : "False") + ";" + (manager.getBoolean(npcId, "IsFishing") ? "False" : "True");
			content = content.replace("%fishings%", fishing);
		}
		else if (pageId == 3)
		{
			String aEffects = "";
			for (String str : manager.getStringArray(npcId, "RealAbnormalEffects"))
			{
				aEffects += str + ",";
			}
			if (aEffects.length() > 0)
			{
				aEffects = aEffects.substring(0, aEffects.length() - 1);
			}
			if (aEffects.equalsIgnoreCase(""))
			{
				aEffects = "Empty";
			}
			content = content.replace("%aEffects%", aEffects);
			String abnormalEffects = "";
			for (AbnormalEffect aEffect : AbnormalEffect.values())
			{
				if (aEffect.name().startsWith("S_") || aEffect.name().startsWith("E_"))
					continue;
				abnormalEffects += aEffect.getName() + ", ";
			}
			if (abnormalEffects.length() > 0)
			{
				abnormalEffects = abnormalEffects.substring(0, abnormalEffects.length() - 1);
			}
			content = content.replace("%abnormalEffects%", abnormalEffects);
			
			String sEffects = "";
			for (String str : manager.getStringArray(npcId, "RealSpecialEffects"))
			{
				sEffects += str + ",";
			}
			if (sEffects.length() > 0)
			{
				sEffects = sEffects.substring(0, sEffects.length() - 1);
			}
			if (sEffects.equalsIgnoreCase(""))
			{
				sEffects = "Empty";
			}
			content = content.replace("%sEffects%", sEffects);
			String specialEffects = "";
			for (AbnormalEffect aEffect : AbnormalEffect.values())
			{
				if (!aEffect.name().startsWith("S_"))
					continue;
				specialEffects += aEffect.getName() + ", ";
			}
			if (specialEffects.length() > 0)
			{
				specialEffects = specialEffects.substring(0, specialEffects.length() - 1);
			}
			content = content.replace("%specialEffects%", specialEffects);
		}
		
		NpcHtmlMessage html = new NpcHtmlMessage(0);
		html.setHtml(content);
		activeChar.sendPacket(html);
	}
	
	private String getContent(int id)
	{
		String text = "";
		if (id == 0)
		{
			text = "<html><title>Edit Npc to Pc template</title><body>";
			text += "<table width=270>";
			text += "<tr><td>Npc ID (%npcId%)</td><td></td><td></td></tr>";
			text += "<tr><td>Is Enabled (%enabled%):</td><td><combobox var=\"en\" list=\"%enableds%\" width=70></td><td><button value=\"Set\" width=35 action=\"bypass -h admin_save_npctopc %npcId%; IsEnabled; $en ; 0\" height=15 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_CT1.Button_DF\"></td></tr>";
			text += "<tr><td>Name (%name%):</td><td><edit var=\"n\" width=70></td><td><button value=\"Set\" width=35 action=\"bypass -h admin_save_npctopc %npcId%; Name; $n ; 0\" height=15 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_CT1.Button_DF\"></td></tr>";
			text += "<tr><td>Name Color (%nameColor%):</td><td><edit var=\"nC\" width=70></td><td><button value=\"Set\" width=35 action=\"bypass -h admin_save_npctopc %npcId%; NameColor; $nC ; 0\" height=15 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_CT1.Button_DF\"></td></tr>";
			text += "<tr><td>Title (%title%):</td><td><edit var=\"t\" width=70></td><td><button value=\"Set\" width=35 action=\"bypass -h admin_save_npctopc %npcId%; Title; $t ; 0\" height=15 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_CT1.Button_DF\"></td></tr>";
			text += "<tr><td>Title Color (%titleColor%):</td><td><edit var=\"tC\" width=70></td><td><button value=\"Set\" width=35 action=\"bypass -h admin_save_npctopc %npcId%; TitleColor; $tC ; 0\" height=15 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_CT1.Button_DF\"></td></tr>";
			text += "<tr><td>Race (%race%):</td><td><combobox var=\"r\" list=\"%races%\" width=70></td><td><button value=\"Set\" width=35 action=\"bypass -h admin_save_npctopc %npcId%; Race; $r ; 0\" height=15 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_CT1.Button_DF\"></td></tr>";
			text += "%raceType%";
			text += "<tr><td>Gender (%gender%):</td><td><combobox var=\"g\" list=\"%genders%\" width=70></td><td><button value=\"Set\" width=35 action=\"bypass -h admin_save_npctopc %npcId%; Gender; $g ; 0\" height=15 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_CT1.Button_DF\"></td></tr><tr><td>RHand Id (%rhand%):</td><td><edit var=\"rh\" width=70></td><td><button value=\"Set\" width=35 action=\"bypass -h admin_save_npctopc %npcId%; RHand; $rh ; 0\" height=15 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_CT1.Button_DF\"></td></tr>";
			text += "<tr><td>LHand Id (%lhand%):</td><td><edit var=\"lh\" width=70></td><td><button value=\"Set\" width=35 action=\"bypass -h admin_save_npctopc %npcId%; LHand; $lh ; 0\" height=15 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_CT1.Button_DF\"></td></tr>";
			text += "<tr><td>Enchant (%enchant%):</td><td><edit var=\"ech\" width=70></td><td><button value=\"Set\" width=35 action=\"bypass -h admin_save_npctopc %npcId%; Enchant; $ech ; 0\" height=15 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_CT1.Button_DF\"></td></tr>";
			text += "<tr><td>Gloves Id (%gloves%):</td><td><edit var=\"gl\" width=70></td><td><button value=\"Set\" width=35 action=\"bypass -h admin_save_npctopc %npcId%; Gloves; $gl ; 0\" height=15 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_CT1.Button_DF\"></td></tr>";
			text += "<tr><td>Chest Id (%chest%):</td><td><edit var=\"ch\" width=70></td><td><button value=\"Set\" width=35 action=\"bypass -h admin_save_npctopc %npcId%; Chest; $ch ; 0\" height=15 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_CT1.Button_DF\"></td></tr>";
			text += "<tr><td>Leggings Id (%legs%):</td><td><edit var=\"le\" width=70></td><td><button value=\"Set\" width=35 action=\"bypass -h admin_save_npctopc %npcId%; Legs; $le ; 0\" height=15 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_CT1.Button_DF\"></td></tr>";
			text += "<tr><td>Boots Id (%boots%):</td><td><edit var=\"bo\" width=70></td><td><button value=\"Set\" width=35 action=\"bypass -h admin_save_npctopc %npcId%; Feet; $bo ; 0\" height=15 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_CT1.Button_DF\"></td></tr>";
			text += "<tr><td>Cloak Id (%cloak%):</td><td><edit var=\"cloak\" width=70></td><td><button value=\"Set\" width=35 action=\"bypass -h admin_save_npctopc %npcId%; Cloak; $cloak ; 0\" height=15 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_CT1.Button_DF\"></td></tr>";
			text += "</table>";
			text += "<table width=270>";
			text += "<tr>";
			text += "	<td width=30></td>";
			text += "	<td>";
			text += "		<button value=\"Prev. Page\" width=65 action=\"\" height=15 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_CT1.Button_DF\">";
			text += "	</td>";
			text += "	<td></td>";
			text += "	<td>";
			text += "		<button value=\"Next Page\" width=65 action=\"bypass -h admin_edit_npctopc %npcId% 1\" height=21 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_CT1.Button_DF\">";
			text += "	</td>";
			text += "</tr>";
			text += "<tr>";
			text += "	<td></td>";
			text += "	<td></td>";
			text += "	<td>";
			text += "		<button value=\"Main Screen\" width=65 action=\"bypass -h admin_edit_npc %npcId%\" height=21 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_CT1.Button_DF\">";
			text += "	</td>";
			text += "	<td></td>";
			text += "	</tr>";
			text += "</table>";
			text += "</body></html>";
		}
		else if (id == 1)
		{
			text = "<html><title>Edit Npc to Pc template</title><body>";
			text += "<table width=270>";
			text += "<tr><td>Npc ID (%npcId%)</td><td></td><td></td></tr>";
			text += "<tr><td>Hair 1 Id (%hair1%):</td><td><edit var=\"h1\" width=70></td><td><button value=\"Set\" width=35 action=\"bypass -h admin_save_npctopc %npcId%; Hair1; $h1 ; 1\" height=15 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_CT1.Button_DF\"></td></tr>";
			text += "<tr><td>Hair 2 Id (%hair2%):</td><td><edit var=\"h2\" width=70></td><td><button value=\"Set\" width=35 action=\"bypass -h admin_save_npctopc %npcId%; Hair2; $h2 ; 1\" height=15 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_CT1.Button_DF\"></td></tr>";
			text += "<tr><td>Use Augmentation (%useAug%):</td><td><combobox var=\"uau\" list=\"%useAugs%\" width=70></td><td><button value=\"Set\" width=35 action=\"bypass -h admin_save_npctopc %npcId%; UseAugmentation; $uau ; 1\" height=15 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_CT1.Button_DF\"></td></tr>";
			text += "<tr><td>PvP Flag (%pvpflag%):</td><td><combobox var=\"pfl\" list=\"%pvpFlags%\" width=70></td><td><button value=\"Set\" width=35 action=\"bypass -h admin_save_npctopc %npcId%; PvPFlag; $pfl ; 1\" height=15 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_CT1.Button_DF\"></td></tr>";
			text += "<tr><td>Karma (%karma%):</td><td><edit var=\"ka\" width=70></td><td><button value=\"Set\" width=35 action=\"bypass -h admin_save_npctopc %npcId%; Karma; $ka ; 1\" height=15 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_CT1.Button_DF\"></td></tr>";
			text += "<tr><td>Mount Type (%mountType%):</td><td><combobox var=\"mty\" list=\"%mountTypes%\" width=70></td><td><button value=\"Set\" width=35 action=\"bypass -h admin_save_npctopc %npcId%; MountType; $mty ; 1\" height=15 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_CT1.Button_DF\"></td></tr>";
			text += "%mountNpcId%";
			text += "<tr><td>Hair Style (%hairStyle%):</td><td><edit var=\"hs\" width=70></td><td><button value=\"Set\" width=35 action=\"bypass -h admin_save_npctopc %npcId%; HairStyle; $hs ; 1\" height=15 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_CT1.Button_DF\"></td></tr>";
			text += "<tr><td>Hair Color (%hairColor%):</td><td><edit var=\"hc\" width=70></td><td><button value=\"Set\" width=35 action=\"bypass -h admin_save_npctopc %npcId%; HairColor; $hc ; 1\" height=15 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_CT1.Button_DF\"></td></tr>";
			text += "<tr><td>Face (%face%):</td><td><edit var=\"f\" width=70></td><td><button value=\"Set\" width=35 action=\"bypass -h admin_save_npctopc %npcId%; Face; $f ; 1\" height=15 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_CT1.Button_DF\"></td></tr>";
			text += "<tr><td>Team (%circle%):</td><td><combobox var=\"cir\" list=\"%teams%\" width=70></td><td><button value=\"Set\" width=35 action=\"bypass -h admin_save_npctopc %npcId%; Circle; $cir ; 1\" height=15 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_CT1.Button_DF\"></td></tr>";
			text += "<tr><td>Is Sitting (%isSitting%):</td><td><combobox var=\"is\" list=\"%isSittings%\" width=70></td><td><button value=\"Set\" width=35 action=\"bypass -h admin_save_npctopc %npcId%; IsSitting; $is ; 1\" height=15 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_CT1.Button_DF\"></td></tr>";
			text += "<tr><td>Is Running (%isRunning%):</td><td><combobox var=\"ir\" list=\"%isRunnings%\" width=70></td><td><button value=\"Set\" width=35 action=\"bypass -h admin_save_npctopc %npcId%; IsRunning; $ir ; 1\" height=15 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_CT1.Button_DF\"></td></tr>";
			text += "<tr><td>Is In Combat (%isInCombat%):</td><td><combobox var=\"iic\" list=\"%isInCombats%\" width=70></td><td><button value=\"Set\" width=35 action=\"bypass -h admin_save_npctopc %npcId%; IsInCombat; $iic ; 1\" height=15 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_CT1.Button_DF\"></td></tr>";
			text += "<tr><td>Is Alike Dead (%isAlikeDead%):</td><td><combobox var=\"iad\" list=\"%isAlikeDeads%\" width=70></td><td><button value=\"Set\" width=35 action=\"bypass -h admin_save_npctopc %npcId%; IsAlikeDead; $iad ; 1\" height=15 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_CT1.Button_DF\"></td></tr>";
			text += "</table>";
			text += "<table width=270>";
			text += "<tr>";
			text += "	<td width=30></td>";
			text += "	<td>";
			text += "		<button value=\"Prev. Page\" width=65 action=\"bypass -h admin_edit_npctopc %npcId%\" height=21 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_CT1.Button_DF\">";
			text += "	</td>";
			text += "	<td></td>";
			text += "	<td>";
			text += "		<button value=\"Next Page\" width=65 action=\"bypass -h admin_edit_npctopc %npcId% 2\" height=21 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_CT1.Button_DF\">";
			text += "	</td>";
			text += "</tr>";
			text += "<tr>";
			text += "	<td></td>";
			text += "	<td></td>";
			text += "	<td>";
			text += "		<button value=\"Main Screen\" width=65 action=\"bypass -h admin_edit_npc %npcId%\" height=21 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_CT1.Button_DF\">";
			text += "	</td>";
			text += "	<td></td>";
			text += "	</tr>";
			text += "</table>";
			text += "</body></html>";
		}
		else if (id == 2)
		{
			text = "<html><title>Edit Npc to Pc template</title><body>";
			text += "<table width=270>";
			text += "<tr><td>Npc ID (%npcId%)</td><td></td><td></td></tr>";
			text += "<tr><td>Private Store (%privStore%):</td><td><edit var=\"ps\" width=70></td><td><button value=\"Set\" width=35 action=\"bypass -h admin_save_npctopc %npcId%; PrivateStore; $ps ; 2\" height=15 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_CT1.Button_DF\"></td></tr>";
			text += "<tr><td>Cubics (%cubics%):</td><td><edit var=\"cu\" width=70></td><td><button value=\"Set\" width=35 action=\"bypass -h admin_save_npctopc %npcId%; Cubics; $cu ; 2\" height=15 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_CT1.Button_DF\"></td></tr>";
			text += "<tr><td>Is In Party CRoom (%isInParty%):</td><td><combobox var=\"iipcr\" list=\"%isInPartys%\" width=70></td><td><button value=\"Set\" width=35 action=\"bypass -h admin_save_npctopc %npcId%; IsInPartyChatRoom; $iipcr ; 2\" height=15 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_CT1.Button_DF\"></td></tr>";
			text += "<tr><td>Recommendations (%rec%):</td><td><edit var=\"rec\" width=70></td><td><button value=\"Set\" width=35 action=\"bypass -h admin_save_npctopc %npcId%; Recommendations; $rec ; 2\" height=15 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_CT1.Button_DF\"></td></tr>";
			text += "<tr><td>Is Noble (%noble%):</td><td><combobox var=\"no\" list=\"%nobles%\" width=70></td><td><button value=\"Set\" width=35 action=\"bypass -h admin_save_npctopc %npcId%; IsNoble; $no ; 2\" height=15 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_CT1.Button_DF\"></td></tr>";
			text += "<tr><td>Is Hero (%hero%):</td><td><combobox var=\"he\" list=\"%heros%\" width=70></td><td><button value=\"Set\" width=35 action=\"bypass -h admin_save_npctopc %npcId%; IsHero; $he ; 2\" height=15 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_CT1.Button_DF\"></td></tr>";
			text += "<tr><td>Is Fishing (%fishing%):</td><td><combobox var=\"if\" list=\"%fishings%\" width=70></td><td><button value=\"Set\" width=35 action=\"bypass -h admin_save_npctopc %npcId%; IsFishing; $if ; 2\" height=15 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_CT1.Button_DF\"></td></tr>";
			text += "%fishX%";
			text += "%fishY%";
			text += "%fishZ%";
			text += "<tr><td>Clan Id (%clanId%):</td><td><edit var=\"cid\" width=70></td><td><button value=\"Set\" width=35 action=\"bypass -h admin_save_npctopc %npcId%; ClanId; $cid ; 2\" height=15 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_CT1.Button_DF\"></td></tr>";
			text += "%pClass%";
			text += "%pType%";
			text += "</table>";
			text += "<table width=270>";
			text += "<tr>";
			text += "	<td width=30></td>";
			text += "	<td>";
			text += "		<button value=\"Prev. Page\" width=65 action=\"bypass -h admin_edit_npctopc %npcId% 1\" height=21 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_CT1.Button_DF\">";
			text += "	</td>";
			text += "	<td></td>";
			text += "	<td>";
			text += "		<button value=\"Next Page\" width=65 action=\"bypass -h admin_edit_npctopc %npcId% 3\" height=21 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_CT1.Button_DF\">";
			text += "	</td>";
			text += "</tr>";
			text += "<tr>";
			text += "	<td></td>";
			text += "	<td></td>";
			text += "	<td>";
			text += "		<button value=\"Main Screen\" width=65 action=\"bypass -h admin_edit_npc %npcId%\" height=21 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_CT1.Button_DF\">";
			text += "	</td>";
			text += "	<td></td>";
			text += "	</tr>";
			text += "</table>";
			text += "</body></html>";
		}
		else if (id == 3)
		{
			text = "<html><title>Edit Npc to Pc template</title><body>";
			text += "<table width=270>";
			text += "<tr><td>Npc ID (%npcId%)</td><td></td><td></td></tr>";
			text += "<tr><td>Abnormal Effects</td></tr>";
			text += "<tr><td>(%aEffects%):</td></tr>";
			text += "<tr>";
			text += "    <td><Multiedit var=\"ae\" width=200 height=60></td>";
			text += "    <td>";
			text += "        <img src=\"L2UI.SquareBlank\" width=40 height=20>";
			text += "        <button value=\"Set\" width=85 action=\"bypass -h admin_save_npctopc %npcId%; AbnormalEffects; $ae ; 3\" height=21 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_CT1.Button_DF\">";
			text += "    </td>";
			text += "</tr>";
			text += "<tr></tr>";
			text += "</table>";
			text += "<center>";
			text += "<br>";
			text += "Abnormal efffects:<br>";
			text += "<table width=\"270\">";
			text += "<tr>";
			text += "<td>";
			text += "%abnormalEffects%";
			text += "</td>";
			text += "</tr>";
			text += "</table>";
			text += "</center><br>";
			text += "<table width=270>";
			text += "<tr><td>Special Effects</td></tr>";
			text += "<tr><td>(%sEffects%):</td></tr>";
			text += "<tr>";
			text += "    <td><Multiedit var=\"se\" width=200 height=60></td>";
			text += "    <td>";
			text += "        <img src=\"L2UI.SquareBlank\" width=40 height=20>";
			text += "        <button value=\"Set\" width=85 action=\"bypass -h admin_save_npctopc %npcId%; SpecialEffects; $se ; 3\" height=21 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_CT1.Button_DF\">";
			text += "    </td>";
			text += "</tr>";
			text += "<tr></tr>";
			text += "</table>";
			text += "<center>";
			text += "<br>";
			text += "Special efffects:<br>";
			text += "<table width=\"270\">";
			text += "<tr>";
			text += "<td>";
			text += "%specialEffects%";
			text += "</td>";
			text += "</tr>";
			text += "</table>";
			text += "</center>";
			text += "<table width=270>";
			text += "<tr>";
			text += "	<td width=30></td>";
			text += "	<td>";
			text += "		<button value=\"Prev. Page\" width=65 action=\"bypass -h admin_edit_npctopc %npcId% 2\" height=21 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_CT1.Button_DF\">";
			text += "	</td>";
			text += "	<td></td>";
			text += "	<td>";
			text += "		<button value=\"Next Page\" width=65 action=\"\" height=15 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_CT1.Button_DF\">";
			text += "	</td>";
			text += "</tr>";
			text += "<tr>";
			text += "	<td></td>";
			text += "	<td></td>";
			text += "	<td>";
			text += "		<button value=\"Main Screen\" width=65 action=\"bypass -h admin_edit_npc %npcId%\" height=21 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_CT1.Button_DF\">";
			text += "	</td>";
			text += "	<td></td>";
			text += "	</tr>";
			text += "</table>";
			text += "</body></html>";
		}
		return text;
	}
	
	@Override
	public String[] getAdminCommandList()
	{
		return ADMIN_COMMANDS;
	}
}