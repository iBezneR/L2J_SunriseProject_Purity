package l2r.gameserver.network.serverpackets;

import java.util.ArrayList;
import java.util.List;

import l2r.gameserver.model.actor.L2Npc;
import l2r.gameserver.network.NpcStringId;

import gr.sr.network.handler.ServerTypeConfigs;

public final class NpcSay extends L2GameServerPacket
{
	private int defaultNpcStringId;
	
	private final int _objectId;
	private final int _textType;
	private final int _npcId;
	private String _text;
	private final int _npcString;
	private List<String> _parameters;
	
	public NpcSay(int objectId, int messageType, int npcId, String text)
	{
		_objectId = objectId;
		_textType = messageType;
		_npcId = 1000000 + npcId;
		
		switch (ServerTypeConfigs.SERVER_TYPE)
		{
			case FREYA:
				_npcString = 0;
				defaultNpcStringId = 0;
				break;
			case H5:
				_npcString = -1;
				defaultNpcStringId = -1;
				break;
			default:
				_npcString = -1;
				defaultNpcStringId = -1;
				break;
		}
		
		_text = text;
	}
	
	public NpcSay(L2Npc npc, int messageType, String text)
	{
		_objectId = npc.getObjectId();
		_textType = messageType;
		_npcId = 1000000 + npc.getId();
		
		switch (ServerTypeConfigs.SERVER_TYPE)
		{
			case FREYA:
				_npcString = 0;
				defaultNpcStringId = 0;
				break;
			case H5:
				_npcString = -1;
				defaultNpcStringId = -1;
				break;
			default:
				_npcString = -1;
				defaultNpcStringId = -1;
				break;
		}
		
		_text = text;
	}
	
	public NpcSay(int objectId, int messageType, int npcId, NpcStringId npcString)
	{
		_objectId = objectId;
		_textType = messageType;
		_npcId = 1000000 + npcId;
		_npcString = npcString.getId();
		
		switch (ServerTypeConfigs.SERVER_TYPE)
		{
			case FREYA:
				defaultNpcStringId = 0;
				break;
			case H5:
				defaultNpcStringId = -1;
				break;
			default:
				defaultNpcStringId = -1;
				break;
		}
	}
	
	public NpcSay(L2Npc npc, int messageType, NpcStringId npcString)
	{
		_objectId = npc.getObjectId();
		_textType = messageType;
		_npcId = 1000000 + npc.getId();
		_npcString = npcString.getId();
		
		switch (ServerTypeConfigs.SERVER_TYPE)
		{
			case FREYA:
				defaultNpcStringId = 0;
				break;
			case H5:
				defaultNpcStringId = -1;
				break;
			default:
				defaultNpcStringId = -1;
				break;
		}
	}
	
	/**
	 * @param text the text to add as a parameter for this packet's message (replaces S1, S2 etc.)
	 * @return this NpcSay packet object
	 */
	public NpcSay addStringParameter(String text)
	{
		if (_parameters == null)
		{
			_parameters = new ArrayList<>();
		}
		_parameters.add(text);
		return this;
	}
	
	/**
	 * @param params a list of strings to add as parameters for this packet's message (replaces S1, S2 etc.)
	 * @return this NpcSay packet object
	 */
	public NpcSay addStringParameters(String... params)
	{
		if ((params != null) && (params.length > 0))
		{
			if (_parameters == null)
			{
				_parameters = new ArrayList<>();
			}
			
			for (String item : params)
			{
				if ((item != null) && (item.length() > 0))
				{
					_parameters.add(item);
				}
			}
		}
		return this;
	}
	
	@Override
	protected final void writeImpl()
	{
		switch (ServerTypeConfigs.SERVER_TYPE)
		{
			case IL:
				writeC(0x02);
				break;
			case GF:
			case EPILOGUE:
			case FREYA:
			case H5:
			case GC:
			case SL:
				writeC(0x30);
				break;
		}
		
		writeD(_objectId);
		writeD(_textType);
		writeD(_npcId);
		
		switch (ServerTypeConfigs.SERVER_TYPE)
		{
			case IL:
				writeS(_text);
				break;
			case GF:
			case EPILOGUE:
			case FREYA:
			case H5:
			case GC:
			case SL:
				writeD(_npcString);
				
				if (_npcString == defaultNpcStringId)
				{
					writeS(_text);
				}
				else if (_parameters != null)
				{
					for (String s : _parameters)
					{
						writeS(s);
					}
				}
				break;
		}
	}
}