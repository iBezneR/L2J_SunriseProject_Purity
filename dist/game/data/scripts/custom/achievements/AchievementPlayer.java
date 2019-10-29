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
package custom.achievements;

import l2r.L2DatabaseFactory;
import l2r.gameserver.model.actor.instance.L2PcInstance;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Calendar;
import java.util.HashMap;

/**
 * @author Erlandys
 *
 */
public class AchievementPlayer
{
	private L2PcInstance _player;
	private HashMap<String, AchievementPObject> _achievements = new HashMap<>();
	private long _achievementTitleChanged = 0;
	private Achievement _titleAchievement = null;
	
	public AchievementPlayer(L2PcInstance player)
	{
		_player = player;
		readAchievements();
	}
	
	public long getAchievementTitleChanged()
	{
		return _achievementTitleChanged;
	}
	
	public void setAchievementTitleChanged(long time)
	{
		_achievementTitleChanged = time;
	}
	
	public void addAchievementPoints(String type, int count)
	{
		if (!_achievements.containsKey(type))
		{
			Achievement a = AchievementsParser.getInstance().getAchievement(type);
			if (a == null)
			{
				return;
			}
			if (!a.isVisible(_player))
				return;
			int requiredValue = a.getRequiredValue(1);
			boolean finished = requiredValue == -1;
			boolean isDaily = a.isDaily();
			long nextRefresh = 0;
			if (isDaily)
			{
				Calendar c = Calendar.getInstance();
				c.set(Calendar.MILLISECOND, 0);
				c.set(Calendar.SECOND, 0);
				c.set(Calendar.MINUTE, 0);
				c.set(Calendar.HOUR_OF_DAY, 0);
				c.add(Calendar.DAY_OF_YEAR, 1);
				nextRefresh = c.getTimeInMillis();
			}
			AchievementPObject pa = new AchievementPObject(_player, type, 0, 0, finished, a, requiredValue, isDaily, nextRefresh, null);
			pa.increaseCount(count);
			_achievements.put(type, pa);
			return;
		}
		AchievementPObject pa = _achievements.get(type);
		pa.increaseCount(count);
	}
	
	public void setAchievementPoints(String type, long count)
	{
		if (!_achievements.containsKey(type))
		{
			Achievement a = AchievementsParser.getInstance().getAchievement(type);
			if (a == null)
			{
				return;
			}
			if (!a.isVisible(_player))
				return;
			int requiredValue = a.getRequiredValue(1);
			boolean finished = requiredValue == -1;
			boolean isDaily = a.isDaily();
			long nextRefresh = 0;
			if (isDaily)
			{
				Calendar c = Calendar.getInstance();
				c.set(Calendar.MILLISECOND, 0);
				c.set(Calendar.SECOND, 0);
				c.set(Calendar.MINUTE, 0);
				c.set(Calendar.HOUR_OF_DAY, 0);
				c.add(Calendar.DAY_OF_YEAR, 1);
				nextRefresh = c.getTimeInMillis();
			}
			AchievementPObject pa = new AchievementPObject(_player, type, 0, 0, finished, a, requiredValue, isDaily, nextRefresh, null);
			pa.setCount(count);
			_achievements.put(type, pa);
			return;
		}
		AchievementPObject pa = _achievements.get(type);
		pa.setCount(count);
	}
	
	public void startCountingAchievement(String type)
	{
		if (!_achievements.containsKey(type))
		{
			Achievement a = AchievementsParser.getInstance().getAchievement(type);
			if (a == null)
			{
				return;
			}
			if (!a.isVisible(_player))
				return;
			int level = a.getLevelByPoints(0);
			int requiredValue = a.getRequiredValue(level + 1);
			boolean finished = requiredValue == -1;
			boolean isDaily = a.isDaily();
			long nextRefresh = 0;
			if (isDaily)
			{
				Calendar c = Calendar.getInstance();
				c.set(Calendar.MILLISECOND, 0);
				c.set(Calendar.SECOND, 0);
				c.set(Calendar.MINUTE, 0);
				c.set(Calendar.HOUR_OF_DAY, 0);
				c.add(Calendar.DAY_OF_YEAR, 1);
				nextRefresh = c.getTimeInMillis();
			}
			_achievements.put(type, new AchievementPObject(_player, type, 0, level, finished, a, requiredValue, isDaily, nextRefresh, null));
			return;
		}
		AchievementPObject pa = _achievements.get(type);
		pa.startCounting();
	}

	public void stopCountingAchievement(String type)
	{
		if (!_achievements.containsKey(type))
			return;
		
		AchievementPObject pa = _achievements.get(type);
		pa.stopCounting();
	}
	
	public int getAchievementLevel(String type)
	{
		int level = 0;
		if (_achievements.containsKey(type))
		{
			level = _achievements.get(type).getLevel();
		}
		return level;
	}
	
	public long getAchievementCount(String type)
	{
		long count = 0;
		if (_achievements.containsKey(type))
		{
			count = _achievements.get(type).getCount();
		}
		return count;
	}
	
	public int getAchievementRequiredCount(String type)
	{
		int count = 0;
		if (_achievements.containsKey(type))
		{
			count = _achievements.get(type).getNextRequiredCount();
		}
		return count;
	}
	
	public boolean isAchievementFinished(String type)
	{
		boolean finished = false;
		if (_achievements.containsKey(type))
		{
			finished = _achievements.get(type).isFinished();
		}
		return finished;
	}
	
	public void readAchievements()
	{
		_achievements.clear();
		try (Connection con = L2DatabaseFactory.getInstance().getConnection();
             PreparedStatement ps = con.prepareStatement("SELECT * FROM er_character_achievements WHERE objectId=?"))
		{
			// Retrieve the L2PcInstance from the characters table of the database
			ps.setInt(1, _player.getObjectId());
			try (ResultSet rs = ps.executeQuery())
			{
				while (rs.next())
				{
					String type = rs.getString("type");
					Achievement a = AchievementsParser.getInstance().getAchievement(type);
					if (a == null)
					{
						continue;
					}
					long count = rs.getLong("count");
					int level = rs.getInt("level");
					boolean isFinished = rs.getInt("finished") == 1;
					boolean isDaily = rs.getInt("is_daily") == 1;
					long nextRefresh = rs.getLong("next_refresh");
					if (isDaily && nextRefresh < System.currentTimeMillis())
						continue;
					String uRewards = rs.getString("unclaimed_rewards");
					String unclaimedRewards[] = uRewards != null ? uRewards.split(";") : null;
					
					long titleChanged = rs.getLong("title_changed");
					
					if (rs.getInt("has_title") == 1)
						setTitle(a, titleChanged);
					else if (titleChanged > System.currentTimeMillis())
						_achievementTitleChanged = titleChanged;
					
					int requiredValue = a.getRequiredValue(level + 1);
					AchievementPObject pa = new AchievementPObject(_player, type, count, level, isFinished, a, requiredValue, isDaily, nextRefresh, unclaimedRewards);
					_achievements.put(type, pa);
				}
			}
		}
		catch (Exception e)
		{
			System.out.println("Failed loading character achievements." + e);
		}
	}
	
	public void updateAchievements()
	{
		try (Connection con = L2DatabaseFactory.getInstance().getConnection();
             PreparedStatement ps = con.prepareStatement("REPLACE INTO er_character_achievements SET objectId=?, type=?, count=?, level=?, finished=?, is_daily=?, next_refresh=?, has_title=?, title_changed=?"))
		{
			boolean hasWrittenTime = false;
			for (AchievementPObject pa : _achievements.values())
			{
				ps.setInt(1, _player.getObjectId());
				ps.setString(2, pa.getType());
				ps.setLong(3, pa.getCount());
				ps.setInt(4, pa.getLevel());
				ps.setInt(5, pa.isFinished() ? 1 : 0);
				ps.setInt(6, pa.isDaily() ? 1 : 0);
				ps.setLong(7, pa.getNextRefresh());
				ps.setInt(8, _titleAchievement != null && pa.getType().equalsIgnoreCase(_titleAchievement.getType()) ? 1 : 0);
				if (_titleAchievement != null)
					ps.setLong(9, pa.getType().equalsIgnoreCase(_titleAchievement.getType()) && System.currentTimeMillis() < _achievementTitleChanged ? _achievementTitleChanged : 0);
				else
				{
					ps.setLong(9, !hasWrittenTime && System.currentTimeMillis() < _achievementTitleChanged ? _achievementTitleChanged : 0);
					hasWrittenTime = true;
				}
				ps.addBatch();
			}
			ps.addBatch();
			ps.executeBatch();
		}
		catch (Exception e)
		{
			System.out.println("Could not store character achievements: " + this + " - " + e.getMessage() + e);
		}
	}
	
	public boolean hasTitle()
	{
		return _titleAchievement != null;
	}
	
	public Achievement getTitleAchievement()
	{
		return _titleAchievement;
	}
	
	public void setTitle(Achievement object)
	{
		setTitle(object, System.currentTimeMillis() + (AchievementsParser.TITLE_BAN * 3600 * 1000));
	}
	
	public void setTitle(Achievement object, long time)
	{
		if (object == null)
		{
			_titleAchievement = null;
			return;
		}
		_titleAchievement = object;
		_achievementTitleChanged = time;
		_player.setTitle(_titleAchievement.getTitle());
		_player.getAppearance().setTitleColor(_titleAchievement.getTitleColor());
	}
}
