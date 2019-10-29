package custom.museum;

public class TopPlayer {
	int _objectId;
	long _count;
	String _name;
	
	public TopPlayer(int objectId, String name, long count)
	{
		_objectId = objectId;
		_name = name;
		_count = count;
	}
	
	public int getObjectId()
	{
		return _objectId;
	}
	
	public String getName()
	{
		return _name;
	}
	
	public long getCount()
	{
		return _count;
	}
}
