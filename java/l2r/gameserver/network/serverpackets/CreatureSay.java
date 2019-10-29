package l2r.gameserver.network.serverpackets;

import gr.sr.network.handler.ServerTypeConfigs;
import gr.sr.network.handler.types.ServerType;
import l2r.gameserver.model.actor.instance.L2PcInstance;
import l2r.gameserver.network.NpcStringId;
import l2r.gameserver.network.SystemMessageId;

import java.util.ArrayList;
import java.util.List;

public final class CreatureSay extends L2GameServerPacket
{
	private final int _objectId;
	private final int _textType;
	private String _charName = null;
	private int _charId = 0;
	private String _text = null;
	private int _npcString = ServerTypeConfigs.SERVER_TYPE == ServerType.FREYA ? 0 : -1;
	private List<String> _parameters;
	// private int _mask;
	// private final int _charLevel = -1;
	
	/**
	 * @param objectId
	 * @param messageType
	 * @param charName
	 * @param text
	 */
	public CreatureSay(int objectId, int messageType, String charName, String text)
	{
		_objectId = objectId;
		_textType = messageType;
		_charName = charName;
		_text = text;
	}
	
	public CreatureSay(int objectId, int messageType, int charId, NpcStringId npcString)
	{
		_objectId = objectId;
		_textType = messageType;
		_charId = charId;
		_npcString = npcString.getId();
	}
	
	public CreatureSay(int objectId, int messageType, String charName, NpcStringId npcString)
	{
		_objectId = objectId;
		_textType = messageType;
		_charName = charName;
		_npcString = npcString.getId();
	}
	
	public CreatureSay(int objectId, int messageType, int charId, SystemMessageId sysString)
	{
		_objectId = objectId;
		_textType = messageType;
		_charId = charId;
		_npcString = sysString.getId();
	}
	
	/**
	 * String parameter for argument S1,S2,.. in npcstring-e.dat
	 * @param text
	 */
	public void addStringParameter(String text)
	{
		if (_parameters == null)
		{
			_parameters = new ArrayList<>();
		}
		_parameters.add(text);
	}
	
	@Override
	protected final void writeImpl()
	{
		switch (ServerTypeConfigs.SERVER_TYPE)
		{
			case IL:
			case GF:
			case EPILOGUE:
			case FREYA:
			case H5:
			case GC:
			case SL:
				writeC(0x4A);
				break;
		}
		
		writeD(_objectId);
		writeD(_textType);
		if (_charName != null)
		{
			writeS(_charName);
		}
		else
		{
			writeD(_charId);
		}
		
		switch (ServerTypeConfigs.SERVER_TYPE)
		{
			case IL:
			case GF:
			case EPILOGUE:
			case FREYA:
				if (_text != null)
				{
					writeS(_text);
				}
				else
				{
					writeD(_npcString);
				}
				break;
			case H5:
			case GLORY:
				writeD(_npcString); // High Five NPCString ID
				
				if (_text != null)
				{
					writeS(_text);
				}
				else
				{
					if (_parameters != null)
					{
						for (String s : _parameters)
						{
							writeS(s);
						}
					}
				}
				break;
			case GC:
			case SL:
				writeD(_npcString); // High Five NPCString ID
				
				if (_text != null)
				{
					writeS(_text);
					
					// TODO
					// if ((_charLevel > 0) && (_textType == Say2.TELL))
					// {
					// writeC(_mask);
					// if ((_mask & 0x10) == 0)
					// {
					// writeC(_charLevel);
					// }
					// }
				}
				else
				{
					if (_parameters != null)
					{
						for (String s : _parameters)
						{
							writeS(s);
						}
					}
				}
				break;
		}
	}
	
	@Override
	public final void runImpl()
	{
		L2PcInstance _pci = getClient().getActiveChar();
		if (_pci != null)
		{
			_pci.broadcastSnoop(_textType, _charName, _text);
		}
	}
}
