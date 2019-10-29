package l2r.gameserver.network.serverpackets;

import gr.sr.network.handler.ServerTypeConfigs;

/**
 * @author vGodFather
 */
public class ExConfirmAddingContact extends L2GameServerPacket
{
	private final String _charName;
	private final boolean _added;
	
	public ExConfirmAddingContact(String charName, boolean added)
	{
		_charName = charName;
		_added = added;
	}
	
	@Override
	protected void writeImpl()
	{
		switch (ServerTypeConfigs.SERVER_TYPE)
		{
			case IL:
			case GF:
			case EPILOGUE:
			case FREYA:
				return;
		}
		
		writeC(0xFE);
		
		switch (ServerTypeConfigs.SERVER_TYPE)
		{
			case H5:
				writeH(0xD2);
				break;
			case GC:
			case SL:
				writeH(0xD3);
				break;
		}
		
		writeS(_charName);
		writeD(_added ? 0x01 : 0x00);
	}
}
