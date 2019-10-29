package l2r.gameserver.network.serverpackets;

import java.util.List;

import l2r.gameserver.model.ActionKey;
import l2r.gameserver.model.UIKeysSettings;
import l2r.gameserver.model.actor.instance.L2PcInstance;

import gr.sr.network.handler.ServerTypeConfigs;

public class ExUISetting extends L2GameServerPacket
{
	private final UIKeysSettings _uiSettings;
	private int buffsize, categories;
	
	public ExUISetting(L2PcInstance player)
	{
		_uiSettings = player.getUISettings();
		calcSize();
	}
	
	private void calcSize()
	{
		int size = 16; // initial header and footer
		int category = 0;
		int numKeyCt = _uiSettings.getKeys().size();
		for (int i = 0; i < numKeyCt; i++)
		{
			size++;
			if (_uiSettings.getCategories().containsKey(category))
			{
				List<Integer> catElList1 = _uiSettings.getCategories().get(category);
				size = size + catElList1.size();
			}
			category++;
			size++;
			if (_uiSettings.getCategories().containsKey(category))
			{
				List<Integer> catElList2 = _uiSettings.getCategories().get(category);
				size = size + catElList2.size();
			}
			category++;
			size = size + 4;
			if (_uiSettings.getKeys().containsKey(i))
			{
				List<ActionKey> keyElList = _uiSettings.getKeys().get(i);
				size = size + (keyElList.size() * 20);
			}
		}
		buffsize = size;
		categories = category;
	}
	
	@Override
	protected void writeImpl()
	{
		switch (ServerTypeConfigs.SERVER_TYPE)
		{
			case IL:
				return;
		}
		
		writeC(0xFE);
		
		switch (ServerTypeConfigs.SERVER_TYPE)
		{
			case GF:
			case EPILOGUE:
			case FREYA:
			case H5:
				writeH(0x70);
				break;
			case GC:
			case SL:
				writeH(0x71);
				break;
		}
		
		writeD(buffsize);
		writeD(categories);
		
		int category = 0;
		
		int numKeyCt = _uiSettings.getKeys().size();
		writeD(numKeyCt);
		for (int i = 0; i < numKeyCt; i++)
		{
			if (_uiSettings.getCategories().containsKey(category))
			{
				List<Integer> catElList1 = _uiSettings.getCategories().get(category);
				writeC(catElList1.size());
				for (int cmd : catElList1)
				{
					writeC(cmd);
				}
			}
			else
			{
				writeC(0x00);
			}
			category++;
			
			if (_uiSettings.getCategories().containsKey(category))
			{
				List<Integer> catElList2 = _uiSettings.getCategories().get(category);
				writeC(catElList2.size());
				for (int cmd : catElList2)
				{
					writeC(cmd);
				}
			}
			else
			{
				writeC(0x00);
			}
			category++;
			
			if (_uiSettings.getKeys().containsKey(i))
			{
				List<ActionKey> keyElList = _uiSettings.getKeys().get(i);
				writeD(keyElList.size());
				for (ActionKey akey : keyElList)
				{
					writeD(akey.getCommandId());
					writeD(akey.getKeyId());
					writeD(akey.getToogleKey1());
					writeD(akey.getToogleKey2());
					writeD(akey.getShowStatus());
				}
			}
			else
			{
				writeD(0x00);
			}
		}
		writeD(0x11);
		writeD(0x10);
	}
}
