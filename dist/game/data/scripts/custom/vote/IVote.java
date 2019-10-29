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

import custom.erengine.ErBonus;
import custom.erengine.ErReward;
import custom.erengine.ErSMPos;
import l2r.gameserver.ThreadPoolManager;
import l2r.gameserver.model.actor.instance.L2PcInstance;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.concurrent.ScheduledFuture;

/**
 * @author Erlandys
 */
public abstract class IVote
{
	int _id;
	String _type;
	String _link, _linkToCheck;
	int _refreshesAfter;
	HashMap<Integer, ArrayList<ErReward>> _allRewards;
	HashMap<Integer, ArrayList<ErBonus>> _allBonuses;
	ArrayList<Integer> _rewardIds;
	boolean _isVoting = false;
	ScheduledFuture<?> _scheduler = null;
	ScheduledFuture<?> _voteCountsScheduler = null;
	int _startVoteCount = 0;
	int _currentVoteCount = 0;
	int _rewardId = -1;
	boolean _withIP = false;
	
	public IVote(int id, String type, String link, String linkToCheck, int refreshesAfter, HashMap<Integer, ArrayList<ErReward>> allRewards, HashMap<Integer, ArrayList<ErBonus>> allBonuses, ArrayList<Integer> rewardIds, boolean withIP)
	{
		_id = id;
		_type = type;
		_link = link;
		_linkToCheck = linkToCheck;
		_refreshesAfter = refreshesAfter;
		_allRewards = allRewards;
		_allBonuses = allBonuses;
		_withIP = withIP;
		_rewardIds = rewardIds;
	}
	
	public int getId()
	{
		return _id;
	}
	
	public String getLink()
	{
		return _link;
	}
	
	public boolean isWithIP()
	{
		return _withIP;
	}
	
	public String getLinkToCheck()
	{
		return _linkToCheck;
	}
	
	public int refreshesAfter()
	{
		return _refreshesAfter;
	}
	
	public boolean isVoting()
	{
		return _isVoting;
	}
	
	public String getVoteName()
	{
		return _type;
	}
	
	public ArrayList<Integer> getRewardIds()
	{
		return _rewardIds;
	}
	
	public Collection<ArrayList<ErReward>> getRewards()
	{
		return _allRewards.values();
	}
	
	public ArrayList<ErReward> getReward(int id)
	{
		if (!_allRewards.containsKey(id))
		{
			return null;
		}
		return _allRewards.get(id);
	}
	
	public Collection<ArrayList<ErBonus>> getBonuses()
	{
		return _allBonuses.values();
	}
	
	public ArrayList<ErBonus> getBonus(int id)
	{
		if (!_allBonuses.containsKey(id))
		{
			return null;
		}
		return _allBonuses.get(id);
	}
	
	public void startVoting(L2PcInstance player, int rewardId)
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
		if (vp != null)
		{
			vp.isVoting(true);
		}
		_rewardId = rewardId;
		_isVoting = true;
		int voteCount = getVoteCount();
		_startVoteCount = voteCount;
		_currentVoteCount = voteCount;
		_scheduler = ThreadPoolManager.getInstance().scheduleGeneral(new Voting(player, vp, VoteManager.VOTE_TIME), 1000);
		_voteCountsScheduler = ThreadPoolManager.getInstance().scheduleGeneral(new VotesCount(player), 1000);
	}
	
	public class Voting implements Runnable
	{
		L2PcInstance _player;
		int _leftTime;
		VotingPlayer _vp;
		
		public Voting(L2PcInstance player, VotingPlayer vp, int leftTime)
		{
			_player = player;
			_vp = vp;
			_leftTime = leftTime;
		}
		
		@Override
		public void run()
		{
			if (_currentVoteCount != _startVoteCount)
			{
				successfulVote(_player);
				_player.sendMessage("You have successfully voted for " + _type + "!");
				VoteManager.SendVotingMessage(_player, "You have successfully voted for " + _type + "!");
				_voteCountsScheduler.cancel(true);
				_isVoting = false;
				if (!_player.isGM())
				{
					VoteManager.getInstance().addBan(_player.getAccountName());
				}
				VoteBBSManager.getInstance().cbByPass("_bbsvote;type;" + getId(), _player);
				return;
			}
			if (_leftTime == 0)
			{
				if (VoteManager.ENABLE_IDLE_TIME)
				{
					_player.sendMessage("You haven't voted for " + _type + " try after " + VoteManager.IDLE_TIME + " mins!");
					VoteManager.SendVotingMessage(_player, "You haven't voted for " + _type + " try after " + VoteManager.IDLE_TIME + " mins!");
				}
				_isVoting = false;
				if (_vp != null)
				{
					_vp.isVoting(false);
				}
				if (!_player.isGM())
				{
					VoteManager.getInstance().addBan(_player.getAccountName());
				}
				VoteBBSManager.getInstance().cbByPass("_bbsvote;type;" + getId(), _player);
				return;
			}
			VoteManager.SendVotingMessage(_player, "You have to vote for " + _type + " in " + _leftTime + "!", ErSMPos.TOP_CENTER, 600);
			_leftTime--;
			_scheduler = ThreadPoolManager.getInstance().scheduleGeneral(new Voting(_player, _vp, _leftTime), 1000);
		}
	}
	
	public class VotesCount implements Runnable
	{
		L2PcInstance _player;
		
		public VotesCount(L2PcInstance player)
		{
			_player = player;
		}
		
		@Override
		public void run()
		{
			if (!_isVoting)
			{
				return;
			}
			if (_withIP)
			{
				_currentVoteCount = getVoteCount(_player);
			}
			else
			{
				_currentVoteCount = getVoteCount();
			}
			_voteCountsScheduler = ThreadPoolManager.getInstance().scheduleGeneral(new VotesCount(_player), 1000);
		}
	}
	
	public void successfulVote(L2PcInstance player)
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
		if (vp == null)
		{
			return;
		}
		if (_allRewards.containsKey(_rewardId) || _allBonuses.containsKey(_rewardId))
		{
			if (_allRewards.containsKey(_rewardId))
			{
				ArrayList<ErReward> rewards = _allRewards.get(_rewardId);
				for (ErReward r : rewards)
				{
					r.giveReward(player);
				}
			}
			if (_allBonuses.containsKey(_rewardId))
			{
				ArrayList<ErBonus> rewards = _allBonuses.get(_rewardId);
				for (ErBonus b : rewards)
				{
					player.getPlayerBonuses().addBonus(b);
				}
			}
			vp.successfulVote(_id, _rewardId, (_refreshesAfter * 1000) + System.currentTimeMillis());
		}
		else
		{
			System.out.println("At top " + getVoteName() + " reward with id " + _id + " does not exists!");
			System.out.print("At top " + getVoteName() + " exists only: ");
			for (int vr : _allRewards.keySet())
			{
				System.out.print(vr + " ");
			}
			System.out.println("");
		}
		vp.isVoting(false);
	}
	
	public abstract int getVoteCount();
	
	public abstract int getVoteCount(L2PcInstance player);
}
