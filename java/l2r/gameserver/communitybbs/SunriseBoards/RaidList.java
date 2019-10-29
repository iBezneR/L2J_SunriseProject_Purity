package l2r.gameserver.communitybbs.SunriseBoards;

import gr.sr.configsEngine.configs.impl.SmartCommunityConfigs;
import l2r.L2DatabaseFactory;
import l2r.gameserver.data.sql.NpcTable;
import l2r.gameserver.model.actor.templates.L2NpcTemplate;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

/**
 * @author L2jSunrise Team
 * @Website www.l2jsunrise.com
 */
public class RaidList extends AbstractSunriseBoards
{
	private final StringBuilder _list = new StringBuilder();
	
	@Override
	public void load(String rfid)
	{
		_list.setLength(0);
		int type = Integer.parseInt(rfid);
		int stpoint = 0;
		int pos = 0;
		int tempCounter = 0;
		
		for (int count = 1; count != type; count++)
		{
			stpoint += SmartCommunityConfigs.RAID_LIST_RESULTS;
		}
		
		pos = stpoint;
		List<L2NpcTemplate> raids = new ArrayList<>();
		try (Connection con = L2DatabaseFactory.getInstance().getConnection())
		{
			PreparedStatement statement = con.prepareStatement("SELECT boss_id FROM raidboss_spawnlist");
			ResultSet result = statement.executeQuery();
			
			while (result.next())
			{
				raids.add(NpcTable.getInstance().getTemplate(result.getInt("boss_id")));
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		raids.sort((o1, o2) -> NpcTable.getInstance().getTemplate(o1.getId()).getLevel() - NpcTable.getInstance().getTemplate(o2.getId()).getLevel());
		
		for (int i = stpoint; i < raids.size(); i++)
		{
			L2NpcTemplate npc = raids.get(i);
			int npcid = npc.getId();
			String npcname = npc.getName();
			int rlevel = npc.getLevel();
			if (tempCounter >= SmartCommunityConfigs.RAID_LIST_RESULTS)
			{
				break;
			}
			
			try (Connection con = L2DatabaseFactory.getInstance().getConnection())
			{
				PreparedStatement statement = con.prepareStatement("SELECT respawn_time, respawn_delay, respawn_random FROM raidboss_spawnlist WHERE boss_id=" + npcid);
				ResultSet result = statement.executeQuery();
				
				while (result.next())
				{
					pos++;
					tempCounter++;
					long respawn = result.getLong("respawn_time");
					boolean rstatus = respawn == 0;
					int mindelay = result.getInt("respawn_delay");
					int maxdelay = result.getInt("respawn_random");
					mindelay = mindelay / 60 / 60;
					maxdelay = maxdelay / 60 / 60;
					addRaidToList(pos, npcname, rlevel, mindelay, maxdelay, rstatus);
				}
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
		}
	}
	
	private void addRaidToList(int pos, String npcname, int rlevel, int mindelay, int maxdelay, boolean rstatus)
	{
		_list.append("<tr>");
		_list.append("<td FIXWIDTH=25 align=center>" + rlevel + "</td>");
		_list.append("<td FIXWIDTH=100>" + npcname + "</td>");
		_list.append("<td FIXWIDTH=60>" + mindelay + " - " + maxdelay + "</td>");
		_list.append("<td FIXWIDTH=25>" + ((rstatus) ? "<font color=99FF00>Alive</font>" : "<font color=CC0000>Dead</font>") + "</td>");
		_list.append("</tr>");
	}
	
	@Override
	public String getList()
	{
		return _list.toString();
	}
	
	public static RaidList getInstance()
	{
		return SingletonHolder._instance;
	}
	
	private static class SingletonHolder
	{
		protected static final RaidList _instance = new RaidList();
	}
}
