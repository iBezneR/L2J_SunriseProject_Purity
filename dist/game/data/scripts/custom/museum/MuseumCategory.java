package custom.museum;

import custom.erengine.ErBonus;
import custom.erengine.ErReward;
import custom.museum.MuseumManager.RefreshTime;
import l2r.gameserver.model.Location;
import l2r.gameserver.model.actor.instance.L2MuseumStatueInstance;

import java.util.ArrayList;
import java.util.HashMap;

public class MuseumCategory
{
	private int _categoryId, _typeId;
	private String _categoryName, _typeName, _type, _additionalText;
	private RefreshTime _refreshTime;
	private boolean _timer;
	private HashMap<Integer, TopPlayer> _players;
	private HashMap<Integer, TopPlayer> _totalTopPlayers;
	private HashMap<Integer, TopPlayer> _statuePlayers;
	private ArrayList<L2MuseumStatueInstance> _spawnedStatues;
	private ArrayList<Location> _statueSpawns;
	private ArrayList<ErReward> _rewards;
	private ArrayList<ErBonus> _bonuses;
	
	MuseumCategory(int categoryId, int typeId, String categoryName, String typeName, String type, String refreshTime, boolean timer, String additionalText, ArrayList<Location> statueSpawns, ArrayList<ErReward> rewards, ArrayList<ErBonus> bonuses)
	{
		_players = new HashMap<>();
		_totalTopPlayers = new HashMap<>();
		_statuePlayers = new HashMap<>();
		_spawnedStatues = new ArrayList<>();
		_categoryId = categoryId;
		_typeId = typeId;
		_categoryName = categoryName;
		_typeName = typeName;
		_type = type;
		_timer = timer;
		_additionalText = additionalText;
		_statueSpawns = statueSpawns;
		_rewards = rewards;
		_bonuses = bonuses;
		for (RefreshTime time : RefreshTime.values())
		{
			if (time.name().toLowerCase().equals(refreshTime))
			{
				_refreshTime = time;
				break;
			}
		}
	}
	
	int getCategoryId()
	{
		return _categoryId;
	}
	
	public int getTypeId()
	{
		return _typeId;
	}
	
	public String getCategoryName()
	{
		return _categoryName;
	}
	
	public String getTypeName()
	{
		return _typeName;
	}
	
	public String getType()
	{
		return _type;
	}
	
	String getAdditionalText()
	{
		return _additionalText;
	}
	
	RefreshTime getRefreshTime()
	{
		return _refreshTime;
	}
	
	public boolean isTimer()
	{
		return _timer;
	}
	
	ArrayList<Location> getStatueSpawns()
	{
		return _statueSpawns;
	}
	
	public ArrayList<ErReward> getRewards()
	{
		return _rewards;
	}
	
	public ArrayList<ErBonus> getBonuses()
	{
		return _bonuses;
	}
	
	HashMap<Integer, TopPlayer> getAllTops()
	{
		return _players;
	}
	
	HashMap<Integer, TopPlayer> getAllTotalTops()
	{
		return _totalTopPlayers;
	}
	
	HashMap<Integer, TopPlayer> getAllStatuePlayers()
	{
		return _statuePlayers;
	}
	
	ArrayList<L2MuseumStatueInstance> getAllSpawnedStatues()
	{
		return _spawnedStatues;
	}
}
