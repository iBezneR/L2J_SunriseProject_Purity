package custom.museum;

import custom.museum.MuseumManager.RefreshTime;

import java.util.HashMap;
import java.util.Map;

public class MuseumPlayer
{
	int _objectId;
	String _name;
	HashMap<String, long[]> _data;
	
	public MuseumPlayer(int objectId, String name, HashMap<String, long[]> data)
	{
		_objectId = objectId;
		_name = name;
		_data = data;
	}
	
	public long getData(RefreshTime time, String type)
	{
		if (!_data.containsKey(type))
		{
			return 0;
		}
		return _data.get(type)[time.ordinal()];
	}
	
	public long[] getData(String type)
	{
		if (!_data.containsKey(type))
		{
			return null;
		}
		return _data.get(type);
	}
	
	public void resetData(RefreshTime time)
	{
		if (time.equals(RefreshTime.Total))
		{
			return;
		}
		HashMap<String, long[]> data = new HashMap<>();
		long d[];
		for (Map.Entry<String, long[]> entry : _data.entrySet())
		{
			d = entry.getValue();
			d[time.ordinal()] = 0;
			data.put(entry.getKey(), d);
		}
		_data = data;
	}
	
	public HashMap<String, long[]> getData()
	{
		return _data;
	}
	
	public void addData(String type, long data)
	{
		long d[] =
		{
			0,
			0,
			0,
			0
		};
		if (getData(type) != null)
		{
			d = getData(type);
		}
		d[0] += data;
		d[1] += data;
		d[2] += data;
		d[3] += data;
		_data.put(type, d);
	}
}
