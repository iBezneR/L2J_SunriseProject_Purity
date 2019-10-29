package l2r.gameserver.communitybbs.SunriseBoards;

import l2r.L2DatabaseFactory;
import l2r.gameserver.data.sql.NpcTable;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

/**
 * @author L2jSunrise Team
 * @Website www.l2jsunrise.com
 */
public class GrandBossList extends AbstractSunriseBoards
{
	private final StringBuilder _list = new StringBuilder();
	
	@Override
	public void load()
	{
		_list.setLength(0);
		int pos = 0;
		
		try (Connection con = L2DatabaseFactory.getInstance().getConnection())
		{
			PreparedStatement statement = con.prepareStatement("SELECT boss_id, status FROM grandboss_data");
			ResultSet result = statement.executeQuery();
			
			while (result.next())
			{
				int npcid = result.getInt("boss_id");
				int status = result.getInt("status");
				if (npcid == 29062)
				{
					continue;
				}
				
				pos++;
				String name = NpcTable.getInstance().getTemplate(npcid).getName();
				boolean rstatus = status == 0;
				addGrandBossToList(pos, name, rstatus);
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
	
	private void addGrandBossToList(int pos, String npcname, boolean rstatus)
	{
		_list.append("<tr>");
		_list.append("<td valign=\"top\" align=\"center\">" + pos + "</td>");
		_list.append("<td valign=\"top\" align=\"center\">" + npcname + "</td>");
		_list.append("<td valign=\"top\" align=\"center\">" + ((rstatus) ? "<font color=99FF00>Alive</font>" : "<font color=CC0000>Dead</font>") + "</td>");
		_list.append("</tr>");
	}
	
	@Override
	public String getList()
	{
		return _list.toString();
	}
	
	public static GrandBossList getInstance()
	{
		return SingletonHolder._instance;
	}
	
	private static class SingletonHolder
	{
		protected static final GrandBossList _instance = new GrandBossList();
	}
}
