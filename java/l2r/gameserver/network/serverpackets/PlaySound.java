package l2r.gameserver.network.serverpackets;

import gr.sr.network.handler.ServerTypeConfigs;
import l2r.gameserver.model.L2Object;

public class PlaySound extends L2GameServerPacket
{
	private final int _type;
	// 0 Sound, 1 Music, 2 Voice
	private final String _soundFile;
	private final int _bindToObject;
	private final int _objectId; // used for ships
	private final int _locX;
	private final int _locY;
	private final int _locZ;
	// Only for Music and Voice
	private final int _delay;
	
	public static PlaySound createSound(String soundName)
	{
		return new PlaySound(soundName);
	}
	
	public static PlaySound createSound(String soundName, L2Object obj)
	{
		return new PlaySound(soundName, obj);
	}
	
	public static PlaySound createMusic(String soundName)
	{
		return createMusic(soundName, 0);
	}
	
	public static PlaySound createMusic(String soundName, int delay)
	{
		return new PlaySound(1, soundName, delay);
	}
	
	public static PlaySound createVoice(String soundName)
	{
		return createVoice(soundName, 0);
	}
	
	public static PlaySound createVoice(String soundName, int delay)
	{
		return new PlaySound(2, soundName, delay);
	}
	
	private PlaySound(String soundFile)
	{
		_type = 0;
		_soundFile = soundFile;
		_bindToObject = 0;
		_objectId = 0;
		_locX = 0;
		_locY = 0;
		_locZ = 0;
		_delay = 0;
	}
	
	private PlaySound(String soundFile, L2Object obj)
	{
		_type = 0;
		_soundFile = soundFile;
		if (obj != null)
		{
			_bindToObject = 1;
			_objectId = obj.getObjectId();
			_locX = obj.getX();
			_locY = obj.getY();
			_locZ = obj.getZ();
		}
		else
		{
			_bindToObject = 0;
			_objectId = 0;
			_locX = 0;
			_locY = 0;
			_locZ = 0;
		}
		_delay = 0;
	}
	
	private PlaySound(int type, String soundFile, int radius)
	{
		_type = type;
		_soundFile = soundFile;
		_bindToObject = 0;
		_objectId = 0;
		_locX = 0;
		_locY = 0;
		_locZ = 0;
		_delay = radius;
	}
	
	public String getSoundName()
	{
		return _soundFile;
	}
	
	@Override
	protected final void writeImpl()
	{
		switch (ServerTypeConfigs.SERVER_TYPE)
		{
			case IL:
				writeC(0x98);
				break;
			case GF:
			case EPILOGUE:
			case FREYA:
			case H5:
			case GC:
			case SL:
				writeC(0x9E);
				break;
		}
		
		writeD(_type);
		writeS(_soundFile);
		writeD(_bindToObject);
		writeD(_objectId);
		writeD(_locX);
		writeD(_locY);
		writeD(_locZ);
		writeD(_delay);
	}
}