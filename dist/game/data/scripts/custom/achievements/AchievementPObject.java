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

import l2r.gameserver.ThreadPoolManager;
import l2r.gameserver.model.actor.instance.L2PcInstance;

import java.util.Calendar;
import java.util.concurrent.ScheduledFuture;

/**
 * @author erlan
 *
 */
public class AchievementPObject
{
	private L2PcInstance _player;
	private String _type;
	long _count;
	int _level;
	boolean _finished;
	Achievement _achievement;
	int _nextRequiredCount;
	boolean _isDaily;
	long _nextRefresh;
	boolean _isWithTime;
	ScheduledFuture<?> _timer = null;
	
	public AchievementPObject(L2PcInstance player, String type, long count, int level, boolean finished, Achievement a, int requiredValue, boolean isDaily, long nextRefresh, String unclaimedRewards[])
	{
		_player = player;
		_type = type;
		_count = count;
		_level = level;
		_finished = finished;
		_achievement = a;
		_nextRequiredCount = requiredValue;
		if (_nextRequiredCount == -1)
		{
			_finished = true;
		}
		_isDaily = isDaily;
		_nextRefresh = nextRefresh;
		_isWithTime = _achievement.isWithTime();
		if (_isDaily && _nextRefresh > System.currentTimeMillis())
		{
			ThreadPoolManager.getInstance().scheduleGeneral(() -> refresh(), _nextRefresh - System.currentTimeMillis());
		}
		else if (_isDaily && _nextRefresh < System.currentTimeMillis())
		{
			refresh();
		}
	}
	
	void refresh()
	{
		_level = 0;
		_count = 0;
		_finished = false;
		_isDaily = true;
		Calendar c = Calendar.getInstance();
		c.set(Calendar.MILLISECOND, 0);
		c.set(Calendar.SECOND, 0);
		c.set(Calendar.MINUTE, 0);
		c.set(Calendar.HOUR_OF_DAY, 0);
		c.add(Calendar.DAY_OF_YEAR, 1);
		_nextRefresh = c.getTimeInMillis();
		ThreadPoolManager.getInstance().scheduleGeneral(() -> refresh(), _nextRefresh - System.currentTimeMillis());
	}
	
	public String getType()
	{
		return _type;
	}
	
	public long getCount()
	{
		return _count;
	}
	
	public int getLevel()
	{
		return _level;
	}

	public boolean isFinished()
	{
		return _finished;
	}

	public boolean isDaily()
	{
		return _isDaily;
	}
	
	public Achievement getAchievement()
	{
		return _achievement;
	}

	public int getNextRequiredCount()
	{
		return _nextRequiredCount;
	}

	public long getNextRefresh()
	{
		return _nextRefresh;
	}
	
	public void setCount(long count)
	{
		if (_achievement.getLevel(_level + 1) == null || (_finished && !_achievement.getLevel(_level + 1).isVisible(_player)))
			return;
		_count = count;
		if (_count >= _nextRequiredCount)
		{
			_level = _achievement.getLevelByPoints(_count);
			_count = 0;
			_nextRequiredCount = _achievement.getRequiredValue(_level + 1);
			_achievement.sendGift(_player, _level);
			if (_nextRequiredCount > 0 && !_achievement.getLevel(_level + 1).isVisible(_player)) {
				_nextRequiredCount = -1;
			}
			if (_nextRequiredCount < 0)
			{
				_finished = true;
				if (_isWithTime)
					stopCounting();
			}
		}
	}
	
	public void increaseCount(int count)
	{
		setCount(count + _count);
	}
	
	public void setLevel(int level)
	{
		_level = level;
	}
	
	public void isFinished(boolean f)
	{
		_finished = f;
	}
	
	public void startCounting()
	{
		if (_timer != null)
		{
			_timer.cancel(true);
			_timer = null;
		}
		_timer = ThreadPoolManager.getInstance().scheduleGeneralAtFixedRate(new IncreaseSecond(), 1000, 1000);
	}
	
	public void stopCounting()
	{
		_timer.cancel(true);
		_timer = null;
	}
	
	private class IncreaseSecond implements Runnable
	{
		public IncreaseSecond()
		{
		}

		@Override
		public void run()
		{
			increaseCount(1);
		}
	}
}
