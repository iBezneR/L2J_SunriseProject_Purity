/*
 * Copyright (C) 2004-2015 L2J Server
 * 
 * This file is part of L2J Server.
 * 
 * L2J Server is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * L2J Server is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package custom.npctopc;

import custom.erengine.ErGlobalVariables;
import custom.erengine.ErUtils;
import l2r.gameserver.ThreadPoolManager;
import l2r.gameserver.data.sql.NpcTable;
import l2r.gameserver.handler.AdminCommandHandler;
import l2r.gameserver.model.L2World;
import l2r.gameserver.model.actor.instance.L2PcInstance;
import l2r.gameserver.model.actor.templates.L2NpcTemplate;
import l2r.gameserver.model.base.ClassId;
import l2r.gameserver.model.effects.AbnormalEffect;
import l2r.gameserver.network.clientpackets.Say2;
import l2r.gameserver.network.serverpackets.CreatureSay;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

import javax.xml.parsers.DocumentBuilderFactory;
import java.io.*;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author Erlandas
 */
public class NpcToPcManager
{
	public static HashMap<Integer, HashMap<String, String>> pcs;
	public static HashMap<String, HashMap<Integer, HashMap<String, String>>> changes;
	private static final Logger _log = Logger.getLogger(NpcToPcManager.class.getName());
	
	public NpcToPcManager()
	{
		pcs = new HashMap<>();
		changes = new HashMap<>();
		changes.put("add", new HashMap<Integer, HashMap<String, String>>());
		changes.put("modify", new HashMap<Integer, HashMap<String, String>>());
		changes.put("remove", new HashMap<Integer, HashMap<String, String>>());
		initializeXMLFile();
		init();
		AdminCommandHandler.getInstance().registerHandler(new AdminEditNpcToPc());
		ThreadPoolManager.getInstance().scheduleGeneralAtFixedRate(new UpdateNpcToPc(), 30000, 600000);
	}
	
	public void init()
	{
		_log.info(getClass().getSimpleName() + ": Initializing");
		try
		{
			pcs.clear();
			File file = new File("data/Npc_to_Pc.xml");
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
				_log.log(Level.WARNING, "Could not parse Npc_to_Pc.xml file: " + e.getMessage(), e);
				return;
			}
			Node n = doc.getFirstChild();
			for (Node d = n.getFirstChild(); d != null; d = d.getNextSibling())
			{
				if (d.getNodeName().equalsIgnoreCase("pc"))
				{
					int npcId = Integer.parseInt(d.getAttributes().getNamedItem("npcId").getNodeValue());
					String isEnabled = d.getAttributes().getNamedItem("isEnabled").getNodeValue();
					HashMap<String, String> info = new HashMap<>();
					info.put("isenabled", isEnabled);
					for (Node h = d.getFirstChild(); h != null; h = h.getNextSibling())
					{
						if (h.getNodeName().equals("set"))
						{
							String name = h.getAttributes().getNamedItem("name").getNodeValue().toLowerCase();
							if (name.equalsIgnoreCase("ClassId"))
							{
								continue;
							}
							String value = h.getAttributes().getNamedItem("value").getNodeValue();
							if (name.equalsIgnoreCase("AbnormalEffects") || name.equalsIgnoreCase("SpecialEffects"))
							{
								info.put("real" + name, value);
							}
							value = doCheck(name, value);
							info.put(name, value);
						}
					}
					if (info.containsKey("classid"))
					{
						info.remove("classid");
					}
					info.put("classid", ClassId.getClassId(Integer.parseInt(info.get("race")), Boolean.parseBoolean(info.get("racetype"))).getId() + "");
					if (!info.containsKey("mountnpcid") && info.containsKey("mounttype") && (Integer.parseInt(info.get("mounttype")) > 0))
					{
						info.put("mountnpcid", (Integer.parseInt(info.get("mounttype")) == 1 ? "12526" : "12621"));
					}
					pcs.put(npcId, info);
					String types[] =
					{
						"Name",
						"NameColor",
						"Title",
						"TitleColor",
						"Race",
						"RaceType",
						"Gender",
						"HairStyle",
						"HairColor",
						"Face",
						"IsSitting"
					};
					L2NpcTemplate template = NpcTable.getInstance().getTemplate(npcId);
					String gender = "male";
					gender = gender.substring(0, 1).toUpperCase() + gender.substring(1, gender.length());
					for (String name : types)
					{
						if (info.containsKey(name))
						{
							continue;
						}
						if (name.equalsIgnoreCase("Name") && !pcs.get(npcId).containsKey("name"))
						{
							setInfo(npcId, "Name", template.getName());
						}
						if (name.equalsIgnoreCase("NameColor") && !pcs.get(npcId).containsKey("namecolor"))
						{
							setInfo(npcId, "NameColor", "FFFFFF");
						}
						if (name.equalsIgnoreCase("Title") && !pcs.get(npcId).containsKey("title"))
						{
							setInfo(npcId, "Title", "");
						}
						if (name.equalsIgnoreCase("TitleColor") && !pcs.get(npcId).containsKey("titlecolor"))
						{
							setInfo(npcId, "TitleColor", "FFFFFF");
						}
						if (name.equalsIgnoreCase("Race") && !pcs.get(npcId).containsKey("race"))
						{
							setInfo(npcId, "Race", "Human");
						}
						if (name.equalsIgnoreCase("RaceType") && !pcs.get(npcId).containsKey("racetype"))
						{
							setInfo(npcId, "RaceType", "Fighter");
						}
						if (name.equalsIgnoreCase("Gender") && !pcs.get(npcId).containsKey("gender"))
						{
							setInfo(npcId, "Gender", gender);
						}
						if (name.equalsIgnoreCase("HairStyle") && !pcs.get(npcId).containsKey("hairstyle"))
						{
							setInfo(npcId, "HairStyle", "0");
						}
						if (name.equalsIgnoreCase("HairColor") && !pcs.get(npcId).containsKey("haircolor"))
						{
							setInfo(npcId, "HairColor", "0");
						}
						if (name.equalsIgnoreCase("Face") && !pcs.get(npcId).containsKey("face"))
						{
							setInfo(npcId, "Face", "0");
						}
						if (name.equalsIgnoreCase("IsSitting") && !pcs.get(npcId).containsKey("issitting"))
						{
							setInfo(npcId, "IsSitting", "False");
						}
					}
				}
			}
			_log.info(getClass().getSimpleName() + ": Successfully loaded " + pcs.size() + " Pcs.");
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
	
	private String doCheck(String name, String value)
	{
		if (name.equalsIgnoreCase("Race"))
		{
			if (value.equalsIgnoreCase("Human"))
			{
				return "0";
			}
			else if (value.equalsIgnoreCase("Elf"))
			{
				return "1";
			}
			else if (value.equalsIgnoreCase("DarkElf") || value.equalsIgnoreCase("Dark_Elf"))
			{
				return "2";
			}
			else if (value.equalsIgnoreCase("Orc"))
			{
				return "3";
			}
			else if (value.equalsIgnoreCase("Dwarf"))
			{
				return "4";
			}
			else if (value.equalsIgnoreCase("Kamael"))
			{
				return "5";
			}
			return "0";
		}
		else if (name.equalsIgnoreCase("Gender"))
		{
			if (value.equalsIgnoreCase("Male"))
			{
				return "0";
			}
			else if (value.equalsIgnoreCase("Female"))
			{
				return "1";
			}
			return "0";
		}
		else if (name.equalsIgnoreCase("Circle"))
		{
			if (value.equalsIgnoreCase("Blue"))
			{
				return "1";
			}
			else if (value.equalsIgnoreCase("Red"))
			{
				return "2";
			}
			return "0";
		}
		else if (name.equalsIgnoreCase("UseAugmentation"))
		{
			if (value.equalsIgnoreCase("True"))
			{
				return "829037597";
			}
			return "0";
		}
		else if (name.equalsIgnoreCase("MountType"))
		{
			if (value.equalsIgnoreCase("Strider"))
			{
				return "1";
			}
			else if (value.equalsIgnoreCase("Wywern"))
			{
				return "2";
			}
			return "0";
		}
		else if (name.equalsIgnoreCase("AbnormalEffects") || name.equalsIgnoreCase("SpecialEffects"))
		{
			String val = "";
			for (String aName : value.split(","))
			{
				AbnormalEffect aEffect = AbnormalEffect.getByName(aName);
				if (aEffect == null)
				{
					continue;
				}
				val += aEffect.getMask() + ",";
			}
			value = val.substring(0, val.length() - 1);
		}
		else if (name.equalsIgnoreCase("RaceType"))
		{
			if (value.equalsIgnoreCase("Mage"))
			{
				return "true";
			}
			return "false";
		}
		return value;
	}
	
	public boolean npcExist(int npcId)
	{
		if (pcs.containsKey(npcId))
		{
			return Boolean.parseBoolean(pcs.get(npcId).get("isenabled").toLowerCase());
		}
		return false;
	}
	
	public String getString(int npcId, String name)
	{
		name = name.toLowerCase();
		if (pcs.containsKey(npcId) && pcs.get(npcId).containsKey(name))
		{
			return pcs.get(npcId).get(name);
		}
		return "";
	}
	
	public int getInt(int npcId, String name)
	{
		name = name.toLowerCase();
		try
		{
			if (pcs.containsKey(npcId) && pcs.get(npcId).containsKey(name))
			{
				return Integer.parseInt(pcs.get(npcId).get(name));
			}
		}
		catch (NumberFormatException nfe)
		{
			return 0;
		}
		return 0;
	}
	
	public boolean getBoolean(int npcId, String name)
	{
		name = name.toLowerCase();
		if (pcs.containsKey(npcId) && pcs.get(npcId).containsKey(name))
		{
			return Boolean.parseBoolean(pcs.get(npcId).get(name).toLowerCase());
		}
		return false;
	}
	
	public double getDouble(int npcId, String name)
	{
		name = name.toLowerCase();
		try
		{
			if (pcs.containsKey(npcId) && pcs.get(npcId).containsKey(name))
			{
				return Double.parseDouble(pcs.get(npcId).get(name));
			}
		}
		catch (NumberFormatException nfe)
		{
			return 0;
		}
		return 0;
	}
	
	public double getLong(int npcId, String name)
	{
		name = name.toLowerCase();
		try
		{
			if (pcs.containsKey(npcId) && pcs.get(npcId).containsKey(name))
			{
				return Long.parseLong(pcs.get(npcId).get(name));
			}
		}
		catch (NumberFormatException nfe)
		{
			return 0;
		}
		return 0;
	}
	
	public ArrayList<Integer> getIntegerArray(int npcId, String name)
	{
		name = name.toLowerCase();
		ArrayList<Integer> values = new ArrayList<>();
		if (!pcs.containsKey(npcId) || !pcs.get(npcId).containsKey(name))
		{
			return values;
		}
		for (String val : pcs.get(npcId).get(name).split(","))
		{
			try
			{
				values.add(Integer.parseInt(val));
			}
			catch (NumberFormatException nfe)
			{
				continue;
			}
		}
		return values;
	}
	
	public ArrayList<String> getStringArray(int npcId, String name)
	{
		name = name.toLowerCase();
		ArrayList<String> values = new ArrayList<>();
		if (!pcs.containsKey(npcId) || !pcs.get(npcId).containsKey(name))
		{
			return values;
		}
		for (String val : pcs.get(npcId).get(name).split(","))
		{
			values.add(val);
		}
		return values;
	}
	
	public ArrayList<Boolean> getBooleanArray(int npcId, String name, String type)
	{
		name = name.toLowerCase();
		ArrayList<Boolean> values = new ArrayList<>();
		if (!pcs.containsKey(npcId) || !pcs.get(npcId).containsKey(name))
		{
			return values;
		}
		for (String val : pcs.get(npcId).get(name).split(","))
		{
			values.add(Boolean.parseBoolean(val.toLowerCase()));
		}
		return values;
	}
	
	public ArrayList<Double> getDoubleArray(int npcId, String name)
	{
		name = name.toLowerCase();
		ArrayList<Double> values = new ArrayList<>();
		if (!pcs.containsKey(npcId) || !pcs.get(npcId).containsKey(name))
		{
			return values;
		}
		for (String val : pcs.get(npcId).get(name).split(","))
		{
			values.add(Double.parseDouble(val));
		}
		return values;
	}
	
	public ArrayList<Long> getLongArray(int npcId, String name)
	{
		name = name.toLowerCase();
		ArrayList<Long> values = new ArrayList<>();
		if (!pcs.containsKey(npcId) || !pcs.get(npcId).containsKey(name))
		{
			return values;
		}
		for (String val : pcs.get(npcId).get(name).split(","))
		{
			values.add(Long.parseLong(val));
		}
		return values;
	}
	
	public void setInfo(int npcId, String name, String value)
	{
		String type = "";
		if (pcs.containsKey(npcId) && pcs.get(npcId).containsKey(name.toLowerCase()))
		{
			if (value.equalsIgnoreCase("0"))
			{
				if (name.equalsIgnoreCase("RHand") || name.equalsIgnoreCase("LHand") || name.equalsIgnoreCase("Chest") || name.equalsIgnoreCase("Legs") || name.equalsIgnoreCase("Gloves") || name.equalsIgnoreCase("Feet") || name.equalsIgnoreCase("Hair1") || name.equalsIgnoreCase("Hair2") || name.equalsIgnoreCase("PvPFlag") || name.equalsIgnoreCase("Karma") || name.equalsIgnoreCase("MountType") || name.equalsIgnoreCase("Karma") || name.equalsIgnoreCase("MountType") || name.equalsIgnoreCase("MountNpcId") || name.equalsIgnoreCase("PrivateStore") || name.equalsIgnoreCase("Recommendations") || name.equalsIgnoreCase("FishingX") || name.equalsIgnoreCase("FishingY") || name.equalsIgnoreCase("FishingZ") || name.equalsIgnoreCase("ClanId") || name.equalsIgnoreCase("PledgeClass") || name.equalsIgnoreCase("PledgeType"))
				{
					type = "remove";
				}
				else
				{
					type = "modify";
				}
			}
			else if (value.equalsIgnoreCase("False"))
			{
				if (name.equalsIgnoreCase("UseAugmentation") || name.equalsIgnoreCase("isSitting") || name.equalsIgnoreCase("isInCombat") || name.equalsIgnoreCase("isInPartyChatRoom") || name.equalsIgnoreCase("IsNoble") || name.equalsIgnoreCase("IsHero") || name.equalsIgnoreCase("IsFishing"))
				{
					type = "remove";
				}
				else
				{
					type = "modify";
				}
			}
			else if (value.equalsIgnoreCase("empty"))
			{
				if (name.equalsIgnoreCase("AbnormalEffects") || name.equalsIgnoreCase("SpecialEffects"))
				{
					type = "remove";
				}
				else
				{
					type = "modify";
				}
			}
			else
			{
				type = "modify";
			}
		}
		else
		{
			type = "add";
		}
		if (pcs.containsKey(npcId) && !pcs.get(npcId).containsKey(name.toLowerCase()) && value.equalsIgnoreCase("Empty"))
		{
			return;
		}
		String pcsValue = type.equalsIgnoreCase("remove") ? value : doCheck(name, value);
		
		String pcsName = name.toLowerCase();
		if (pcs.containsKey(npcId) && (name.equalsIgnoreCase("ClassId") || name.equalsIgnoreCase("Race") || name.equalsIgnoreCase("RaceType") || name.equalsIgnoreCase("Gender")) && (pcs.get(npcId).containsKey("race") && pcs.get(npcId).containsKey("racetype") && pcs.get(npcId).containsKey("gender")))
		{
			if (pcs.get(npcId).containsKey("classid"))
			{
				pcs.get(npcId).remove("classid");
			}
			int classId = 0;
			int raceId = Integer.parseInt(pcs.get(npcId).get("race"));
			boolean isMage = Boolean.parseBoolean(pcs.get(npcId).get("racetype"));
			if (name.equalsIgnoreCase("Race"))
			{
				raceId = Integer.parseInt(pcsValue);
			}
			if (name.equalsIgnoreCase("RaceType"))
			{
				isMage = Boolean.parseBoolean(pcsValue);
			}
			if (raceId > 3)
			{
				isMage = false;
			}
			classId = ClassId.getClassId(raceId, isMage).getId();
			pcs.get(npcId).put("classid", classId + "");
			if (name.equalsIgnoreCase("ClassId"))
			{
				return;
			}
		}
		
		if (!changes.containsKey(type))
		{
			changes.put(type, new HashMap<Integer, HashMap<String, String>>());
		}
		
		if (!changes.get(type).containsKey(npcId))
		{
			changes.get(type).put(npcId, new HashMap<String, String>());
		}
		
		if (changes.get("add").containsKey(npcId) && changes.get("add").get(npcId).containsKey(name))
		{
			changes.get("add").get(npcId).remove(name);
		}
		
		if (!changes.get("modify").containsKey(npcId))
		{
			changes.get("modify").put(npcId, new HashMap<String, String>());
		}
		
		if (changes.get("modify").get(npcId).containsKey(name))
		{
			changes.get("modify").get(npcId).remove(name);
		}
		
		if (changes.get("remove").containsKey(npcId) && changes.get("remove").get(npcId).containsKey(name))
		{
			changes.get("remove").get(npcId).remove(name);
		}
		
		if (type.equals("add"))
		{
			changes.get(type).get(npcId).put(name, value);
			if (!pcs.containsKey(npcId))
			{
				pcs.put(npcId, new HashMap<String, String>());
				pcs.get(npcId).put(pcsName, pcsValue);
				if (pcsName.equalsIgnoreCase("AbnormalEffects"))
				{
					if (pcs.get(npcId).containsKey("real" + pcsName))
					{
						pcs.get(npcId).remove("real" + pcsName);
					}
					pcs.get(npcId).put("real" + pcsName, value);
				}
				if (!pcs.get(npcId).containsKey("mountnpcid") && pcsName.equalsIgnoreCase("mounttype") && (Integer.parseInt(pcsValue) > 0))
				{
					pcs.get(npcId).put("mountnpcid", (Integer.parseInt(pcsValue) == 1 ? "12526" : "12621"));
				}
				L2NpcTemplate template = NpcTable.getInstance().getTemplate(npcId);
				String gender = "male";
				gender = gender.substring(0, 1).toUpperCase() + gender.substring(1, gender.length());
				if (!name.equalsIgnoreCase("Name") && !pcs.get(npcId).containsKey("name"))
				{
					setInfo(npcId, "Name", template.getName());
				}
				if (!name.equalsIgnoreCase("NameColor") && !pcs.get(npcId).containsKey("namecolor"))
				{
					setInfo(npcId, "NameColor", "FFFFFF");
				}
				if (!name.equalsIgnoreCase("Title") && !pcs.get(npcId).containsKey("title"))
				{
					setInfo(npcId, "Title", "");
				}
				if (!name.equalsIgnoreCase("TitleColor") && !pcs.get(npcId).containsKey("titlecolor"))
				{
					setInfo(npcId, "TitleColor", "FFFFFF");
				}
				if (!name.equalsIgnoreCase("Race") && !pcs.get(npcId).containsKey("race"))
				{
					setInfo(npcId, "Race", "Human");
				}
				if (!name.equalsIgnoreCase("RaceType") && !pcs.get(npcId).containsKey("racetype"))
				{
					setInfo(npcId, "RaceType", "Fighter");
				}
				if (!name.equalsIgnoreCase("Gender") && !pcs.get(npcId).containsKey("gender"))
				{
					setInfo(npcId, "Gender", gender);
				}
				if (!name.equalsIgnoreCase("HairStyle") && !pcs.get(npcId).containsKey("hairstyle"))
				{
					setInfo(npcId, "HairStyle", "0");
				}
				if (!name.equalsIgnoreCase("HairColor") && !pcs.get(npcId).containsKey("haircolor"))
				{
					setInfo(npcId, "HairColor", "0");
				}
				if (!name.equalsIgnoreCase("Face") && !pcs.get(npcId).containsKey("face"))
				{
					setInfo(npcId, "Face", "0");
				}
				if (!name.equalsIgnoreCase("IsSitting") && !pcs.get(npcId).containsKey("issitting"))
				{
					setInfo(npcId, "IsSitting", "False");
				}
				if (!name.equalsIgnoreCase("IsEnabled") && !pcs.get(npcId).containsKey("isenabled"))
				{
					setInfo(npcId, "IsEnabled", "True");
				}
				if (!name.equalsIgnoreCase("ClassId") && !pcs.get(npcId).containsKey("classid"))
				{
					setInfo(npcId, "ClassId", "0");
				}
			}
			else
			{
				if (pcsName.equalsIgnoreCase("AbnormalEffects"))
				{
					if (pcs.get(npcId).containsKey("real" + pcsName))
					{
						pcs.get(npcId).remove("real" + pcsName);
					}
					pcs.get(npcId).put("real" + pcsName, value);
				}
				if (pcsName.equalsIgnoreCase("SpecialEffects"))
				{
					if (pcs.get(npcId).containsKey("real" + pcsName))
					{
						pcs.get(npcId).remove("real" + pcsName);
					}
					pcs.get(npcId).put("real" + pcsName, value);
				}
				if (!pcs.get(npcId).containsKey("mountnpcid") && pcsName.equalsIgnoreCase("mounttype") && (Integer.parseInt(pcsValue) > 0))
				{
					pcs.get(npcId).put("mountnpcid", (Integer.parseInt(pcsValue) == 1 ? "12526" : "12621"));
				}
				if (pcs.get(npcId).containsKey(pcsName))
				{
					pcs.get(npcId).remove(pcsName);
				}
				pcs.get(npcId).put(pcsName, pcsValue);
			}
		}
		else if (type.equals("modify"))
		{
			changes.get(type).get(npcId).put(name, value);
			if (!pcs.containsKey(npcId))
			{
				pcs.put(npcId, new HashMap<String, String>());
				pcs.get(npcId).put(pcsName, pcsValue);
				if (pcsName.equalsIgnoreCase("AbnormalEffects"))
				{
					if (pcs.get(npcId).containsKey("real" + pcsName))
					{
						pcs.get(npcId).remove("real" + pcsName);
					}
					pcs.get(npcId).put("real" + pcsName, value);
				}
				if (pcsName.equalsIgnoreCase("SpecialEffects"))
				{
					if (pcs.get(npcId).containsKey("real" + pcsName))
					{
						pcs.get(npcId).remove("real" + pcsName);
					}
					pcs.get(npcId).put("real" + pcsName, value);
				}
				if (pcsName.equalsIgnoreCase("mounttype") && (Integer.parseInt(pcsValue) > 0))
				{
					if (pcs.get(npcId).containsKey("mountnpcid"))
					{
						pcs.get(npcId).remove("mountnpcid");
						setInfo(npcId, "MountNpcId", (Integer.parseInt(pcsValue) == 1 ? "12526" : "12621"));
					}
					else
					{
						pcs.get(npcId).put("mountnpcid", (Integer.parseInt(pcsValue) == 1 ? "12526" : "12621"));
					}
				}
				L2NpcTemplate template = NpcTable.getInstance().getTemplate(npcId);
				String gender = "male";
				gender = gender.substring(0, 1).toUpperCase() + gender.substring(1, gender.length());
				if (!name.equalsIgnoreCase("Name") && !pcs.get(npcId).containsKey("name"))
				{
					setInfo(npcId, "Name", template.getName());
				}
				if (!name.equalsIgnoreCase("NameColor") && !pcs.get(npcId).containsKey("namecolor"))
				{
					setInfo(npcId, "NameColor", "FFFFFF");
				}
				if (!name.equalsIgnoreCase("Title") && !pcs.get(npcId).containsKey("title"))
				{
					setInfo(npcId, "Title", "");
				}
				if (!name.equalsIgnoreCase("TitleColor") && !pcs.get(npcId).containsKey("titlecolor"))
				{
					setInfo(npcId, "TitleColor", "FFFFFF");
				}
				if (!name.equalsIgnoreCase("Race") && !pcs.get(npcId).containsKey("race"))
				{
					setInfo(npcId, "Race", "Human");
				}
				if (!name.equalsIgnoreCase("RaceType") && !pcs.get(npcId).containsKey("racetype"))
				{
					setInfo(npcId, "RaceType", "Fighter");
				}
				if (!name.equalsIgnoreCase("Gender") && !pcs.get(npcId).containsKey("gender"))
				{
					setInfo(npcId, "Gender", gender);
				}
				if (!name.equalsIgnoreCase("HairStyle") && !pcs.get(npcId).containsKey("hairstyle"))
				{
					setInfo(npcId, "HairStyle", "0");
				}
				if (!name.equalsIgnoreCase("HairColor") && !pcs.get(npcId).containsKey("haircolor"))
				{
					setInfo(npcId, "HairColor", "0");
				}
				if (!name.equalsIgnoreCase("Face") && !pcs.get(npcId).containsKey("face"))
				{
					setInfo(npcId, "Face", "0");
				}
				if (!name.equalsIgnoreCase("IsSitting") && !pcs.get(npcId).containsKey("issitting"))
				{
					setInfo(npcId, "IsSitting", "True");
				}
				if (!name.equalsIgnoreCase("IsEnabled") && !pcs.get(npcId).containsKey("isenabled"))
				{
					setInfo(npcId, "IsEnabled", "True");
				}
				if (!name.equalsIgnoreCase("ClassId") && !pcs.get(npcId).containsKey("classid"))
				{
					setInfo(npcId, "ClassId", "0");
				}
			}
			else
			{
				if (pcsName.equalsIgnoreCase("AbnormalEffects"))
				{
					if (pcs.get(npcId).containsKey("real" + pcsName))
					{
						pcs.get(npcId).remove("real" + pcsName);
					}
					pcs.get(npcId).put("real" + pcsName, value);
				}
				if (pcsName.equalsIgnoreCase("SpecialEffects"))
				{
					if (pcs.get(npcId).containsKey("real" + pcsName))
					{
						pcs.get(npcId).remove("real" + pcsName);
					}
					pcs.get(npcId).put("real" + pcsName, value);
				}
				if (pcsName.equalsIgnoreCase("mounttype") && (Integer.parseInt(pcsValue) > 0))
				{
					if (pcs.get(npcId).containsKey("mountnpcid"))
					{
						pcs.get(npcId).remove("mountnpcid");
						setInfo(npcId, "MountNpcId", (Integer.parseInt(pcsValue) == 1 ? "12526" : "12621"));
					}
					else
					{
						pcs.get(npcId).put("mountnpcid", (Integer.parseInt(pcsValue) == 1 ? "12526" : "12621"));
					}
				}
				if (pcs.get(npcId).containsKey(pcsName))
				{
					pcs.get(npcId).remove(pcsName);
				}
				pcs.get(npcId).put(pcsName, pcsValue);
			}
		}
		else if (type.equals("remove"))
		{
			changes.get(type).get(npcId).put(name, value);
			if (pcs.containsKey(npcId) && pcs.get(npcId).containsKey(pcsName))
			{
				if (pcsName.equalsIgnoreCase("AbnormalEffects"))
				{
					if (pcs.get(npcId).containsKey("real" + pcsName))
					{
						pcs.get(npcId).remove("real" + pcsName);
					}
				}
				if (pcsName.equalsIgnoreCase("SpecialEffects"))
				{
					if (pcs.get(npcId).containsKey("real" + pcsName))
					{
						pcs.get(npcId).remove("real" + pcsName);
					}
				}
				pcs.get(npcId).remove(pcsName);
			}
		}
	}
	
	class UpdateNpcToPc implements Runnable
	{
		@Override
		public void run()
		{
			if (!changes.get("add").isEmpty() || !changes.get("modify").isEmpty() || !changes.get("remove").isEmpty())
			{
				rewriteToXml();
			}
		}
	}
	
	public void rewriteToXml()
	{
		try
		{
			if (changes.size() < 1)
			{
				return;
			}
			HashMap<Integer, HashMap<String, String>> values = new HashMap<>();
			File file = new File("data/Npc_to_Pc.xml");
			FileInputStream fistream = new FileInputStream(file);
			DataInputStream in = new DataInputStream(fistream);
			BufferedReader br = new BufferedReader(new InputStreamReader(in));
			String line;
			
			int npcId = 0;
			while ((line = br.readLine()) != null)
			{
				if (!line.contains("<pc npcId=") && !line.contains("<set name="))
				{
					continue;
				}
				if (line.contains("<pc npcId="))
				{
					npcId = Integer.parseInt(line.split("npcId=\"")[1].split("\" is")[0]);
					values.put(npcId, new HashMap<String, String>());
					String isEnabled = line.split("isEnabled=\"")[1].split("\">")[0];
					values.get(npcId).put("IsEnabled", isEnabled);
				}
				else if (line.contains("<set name="))
				{
					if (values.containsKey(npcId))
					{
						String name = line.split("name=\"")[1].split("\" val")[0];
						String value = line.contains("value=\"\"") ? "" : line.split("value=\"")[1].split("\" />")[0];
						values.get(npcId).put(name, value);
					}
				}
			}
			in.close();
			for (Map.Entry<String, HashMap<Integer, HashMap<String, String>>> change : changes.entrySet())
			{
				for (Map.Entry<Integer, HashMap<String, String>> data : change.getValue().entrySet())
				{
					String writeType = change.getKey();
					npcId = data.getKey();
					
					for (Map.Entry<String, String> npcInfo : data.getValue().entrySet())
					{
						if (writeType.equalsIgnoreCase("add") || writeType.equalsIgnoreCase("modify"))
						{
							String name = npcInfo.getKey();
							String value = npcInfo.getValue();
							if (!values.containsKey(npcId))
							{
								values.put(npcId, new HashMap<String, String>());
							}
							if (values.get(npcId).containsKey(name))
							{
								values.get(npcId).remove(name);
							}
							values.get(npcId).put(name, value);
						}
						else if (writeType.equalsIgnoreCase("remove"))
						{
							String name = npcInfo.getKey();
							if (values.containsKey(npcId) && values.get(npcId).containsKey(name))
							{
								values.get(npcId).remove(name);
							}
						}
					}
				}
			}
			
			// Create file
			FileWriter fstream = new FileWriter(file);
			BufferedWriter out = new BufferedWriter(fstream);
			out.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
			out.write("<!-- Saved by Erlandys engine (" + Calendar.getInstance().getTime().toString() + ") -->\n");
			out.write("<pcsList>\n");
			for (Map.Entry<Integer, HashMap<String, String>> value : values.entrySet())
			{
				npcId = value.getKey();
				String isEnabled = Boolean.parseBoolean(value.getValue().get("IsEnabled")) ? "True" : "False";
				String xml = "	<pc npcId=\"" + value.getKey() + "\" isEnabled=\"" + isEnabled + "\">\n";
				String names[] =
				{
					"Name",
					"NameColor",
					"Title",
					"TitleColor",
					"Race",
					"RaceType",
					"Gender",
					"HairStyle",
					"HairColor",
					"Face",
					"RHand",
					"LHand",
					"Enchant",
					"Gloves",
					"Chest",
					"Legs",
					"Feet",
					"Hair1",
					"Hair2",
					"UseAugmentation",
					"PvPFlag",
					"Karma",
					"MountType",
					"MountNpcId",
					"Circle",
					"IsSitting",
					"IsRunning",
					"IsInCombat",
					"PrivateStore",
					"Cubics",
					"IsInPartyChatRoom",
					"Recommendations",
					"IsNoble",
					"IsHero",
					"IsFishing",
					"FishingX",
					"FishingY",
					"FishingZ",
					"ClanId",
					"PledgeClass",
					"PledgeType",
					"AbnormalEffects"
				};
				for (String name2 : names)
				{
					String name = "";
					String val = "";
					name = name2;
					if (!value.getValue().containsKey(name))
					{
						continue;
					}
					val = value.getValue().get(name2);
					xml += "		<set ";
					xml += "name=\"" + name + "\" ";
					xml += "value=\"" + val + "\" ";
					xml += "/>\n";
				}
				xml += "	</pc>\n";
				out.write(xml);
			}
			out.write("</pcsList>");
			out.close();
			changes.get("add").clear();
			changes.get("modify").clear();
			changes.get("remove").clear();
		}
		catch (Exception e)
		{// Catch exception if any
			System.out.println("Error: " + e.getMessage());
			List<L2PcInstance> pls = L2World.getInstance().getAllGMs();
			for (L2PcInstance player : pls)
			{
				player.sendPacket(new CreatureSay(0, Say2.PARTYROOM_COMMANDER, "NpcToPcManager", "Error: " + e + ""));
				
			}
		}
	}
	
	private void initializeXMLFile()
	{
		if (ErGlobalVariables.getInstance().getBoolean("NpcToPcXMLInitialized"))
			return;
		String text = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n";
		text += "<pcsList>\n";
		text += "	<pc npcId=\"35104\" isEnabled=\"True\">\n";
		text += "		<set name=\"Name\" value=\"Tyron\" />\n";
		text += "		<set name=\"NameColor\" value=\"FFFFFF\" />\n";
		text += "		<set name=\"Title\" value=\"Sir\" />\n";
		text += "		<set name=\"TitleColor\" value=\"9B9B9B\" />\n";
		text += "		<set name=\"Race\" value=\"Elf\" />\n";
		text += "		<set name=\"RaceType\" value=\"Fighter\" />\n";
		text += "		<set name=\"Gender\" value=\"Male\" />\n";
		text += "		<set name=\"HairStyle\" value=\"0\" />\n";
		text += "		<set name=\"HairColor\" value=\"0\" />\n";
		text += "		<set name=\"Face\" value=\"0\" />\n";
		text += "		<set name=\"IsSitting\" value=\"False\" />\n";
		text += "	</pc>\n";
		text += "	<pc npcId=\"30001\" isEnabled=\"True\">\n";
		text += "		<set name=\"Name\" value=\"test.Test1 Ha\" />\n";
		text += "		<set name=\"NameColor\" value=\"FFFFFF\" />\n";
		text += "		<set name=\"Title\" value=\"Weapon Merchant\" />\n";
		text += "		<set name=\"TitleColor\" value=\"FFFFFF\" />\n";
		text += "		<set name=\"Race\" value=\"Human\" />\n";
		text += "		<set name=\"RaceType\" value=\"Fighter\" />\n";
		text += "		<set name=\"Gender\" value=\"Male\" />\n";
		text += "		<set name=\"HairStyle\" value=\"0\" />\n";
		text += "		<set name=\"HairColor\" value=\"0\" />\n";
		text += "		<set name=\"Face\" value=\"0\" />\n";
		text += "		<set name=\"PvPFlag\" value=\"1\" />\n";
		text += "		<set name=\"IsSitting\" value=\"False\" />\n";
		text += "		<set name=\"AbnormalEffects\" value=\"stun\" />\n";
		text += "	</pc>\n";
		text += "	<pc npcId=\"30045\" isEnabled=\"True\">\n";
		text += "		<set name=\"Name\" value=\"Kenyos\" />\n";
		text += "		<set name=\"NameColor\" value=\"FFFFFF\" />\n";
		text += "		<set name=\"Title\" value=\"Guard\" />\n";
		text += "		<set name=\"TitleColor\" value=\"FFFFFF\" />\n";
		text += "		<set name=\"Race\" value=\"Elf\" />\n";
		text += "		<set name=\"RaceType\" value=\"Fighter\" />\n";
		text += "		<set name=\"Gender\" value=\"Male\" />\n";
		text += "		<set name=\"HairStyle\" value=\"0\" />\n";
		text += "		<set name=\"HairColor\" value=\"0\" />\n";
		text += "		<set name=\"Face\" value=\"0\" />\n";
		text += "		<set name=\"IsSitting\" value=\"False\" />\n";
		text += "	</pc>\n";
		text += "</pcsList>";
		ErUtils.generateFile("data/", "Npc_to_Pc", ".xml", text);
		ErGlobalVariables.getInstance().setData("NpcToPcXMLInitialized", true);
	}
	
	public static final NpcToPcManager getInstance()
	{
		return SingletonHolder._instance;
	}
	
	private static class SingletonHolder
	{
		protected static final NpcToPcManager _instance = new NpcToPcManager();
	}
}
