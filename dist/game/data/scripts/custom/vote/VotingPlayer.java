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

import l2r.L2DatabaseFactory;
import l2r.gameserver.model.actor.instance.L2PcInstance;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.HashMap;
import java.util.Map.Entry;

/**
 * @author Erlandys
 */
public class VotingPlayer
{
	L2PcInstance _player;
	
	HashMap<Integer, Long> _votesExpire;
	HashMap<Integer, Integer> _voteRewards;
	
	boolean _isVoting;
	
	public VotingPlayer(L2PcInstance player)
	{
		_player = player;

		_votesExpire = new HashMap<>();
		_voteRewards = new HashMap<>();
		
		_isVoting = false;
		
		restoreVotes();
	}
	
	private void restoreVotes()
	{
		try (Connection con = L2DatabaseFactory.getInstance().getConnection();)
		{
			PreparedStatement statement = con.prepareStatement("SELECT * FROM er_character_votes WHERE account = ?");
			statement.setString(1, _player.getAccountName());
			ResultSet rset = statement.executeQuery();
			if (rset.next())
			{
				String votes[] = rset.getString("votes").split(";");
				for (String voteParts : votes)
				{
					if (voteParts.length() < 2)
						continue;
					String vParts[] = voteParts.split(",");
					int voteId = Integer.parseInt(vParts[0]);
					int rewardId = Integer.parseInt(vParts[1]);
					long voteExpires = Long.parseLong(vParts[2]);
					if (voteExpires <= System.currentTimeMillis())
					{
						continue;
					}
					_votesExpire.put(voteId, voteExpires);
					_voteRewards.put(voteId, rewardId);
				}
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	public void updateVotes()
	{
		try (Connection con = L2DatabaseFactory.getInstance().getConnection();)
		{
			try (PreparedStatement statement = con.prepareStatement("REPLACE INTO er_character_votes (account, votes) values (?, ?)"))
			{
				String votes = "";
				for (Entry<Integer, Long> entry : _votesExpire.entrySet())
				{
					votes += entry.getKey() + ",";
					votes += _voteRewards.get(entry.getKey()) + ",";
					votes += entry.getValue() + ";";
				}
				statement.setString(1, _player.getAccountName());
				statement.setString(2, votes);
				statement.execute();
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		
	}
	
	public boolean hasVotedToday(int voteId)
	{
		if (_votesExpire.containsKey(voteId) && _votesExpire.get(voteId) <= System.currentTimeMillis())
		{
			_votesExpire.remove(voteId);
			_voteRewards.remove(voteId);
			return true;
		}
		return _votesExpire.containsKey(voteId);
	}
	
	public boolean isVoting()
	{
		return _isVoting;
	}
	
	public boolean hasReward(int typeId, int rewardId)
	{
		return _voteRewards.containsKey(typeId) && _voteRewards.get(typeId) == rewardId;
	}
	
	public void isVoting(boolean isVoting)
	{
		_isVoting = isVoting;
	}
	
	public void successfulVote(int voteId, int rewardId, long expireAt)
	{
		_votesExpire.put(voteId, expireAt);
		_voteRewards.put(voteId, rewardId);
	}
	
	public long getNextVote(int voteId)
	{
		if (_votesExpire.containsKey(voteId))
			return _votesExpire.get(voteId);
		return -1;
	}
}
