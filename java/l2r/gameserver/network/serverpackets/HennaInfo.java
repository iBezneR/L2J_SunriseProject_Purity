package l2r.gameserver.network.serverpackets;

import java.util.ArrayList;
import java.util.List;

import l2r.gameserver.model.actor.instance.L2PcInstance;
import l2r.gameserver.model.items.L2Henna;

import gr.sr.network.handler.ServerTypeConfigs;

public final class HennaInfo extends L2GameServerPacket
{
	private final L2PcInstance _activeChar;
	private final List<L2Henna> _hennas = new ArrayList<>();
	
	public HennaInfo(L2PcInstance player)
	{
		_activeChar = player;
		for (L2Henna henna : _activeChar.getHennaEx().getHennaList())
		{
			if (henna != null)
			{
				_hennas.add(henna);
			}
		}
	}
	
	@Override
	protected void writeImpl()
	{
		switch (ServerTypeConfigs.SERVER_TYPE)
		{
			case IL:
				writeC(0xE4);
				break;
			case GF:
			case EPILOGUE:
			case FREYA:
			case H5:
			case GC:
			case SL:
				writeC(0xE5);
				break;
		}
		
		writeC(_activeChar.getHennaEx().getHennaStatINT()); // equip INT
		writeC(_activeChar.getHennaEx().getHennaStatSTR()); // equip STR
		writeC(_activeChar.getHennaEx().getHennaStatCON()); // equip CON
		writeC(_activeChar.getHennaEx().getHennaStatMEN()); // equip MEN
		writeC(_activeChar.getHennaEx().getHennaStatDEX()); // equip DEX
		writeC(_activeChar.getHennaEx().getHennaStatWIT()); // equip WIT
		writeD(3); // Slots
		writeD(_hennas.size()); // Size
		for (L2Henna henna : _hennas)
		{
			writeD(henna.getDyeId());
			writeD(0x01);
		}
		
		switch (ServerTypeConfigs.SERVER_TYPE)
		{
			case GC:
			case SL:
				if (_activeChar.getHennaEx().getHenna(4) != null)
				{
					writeD(_activeChar.getHennaEx().getHenna(4).getDyeId());
					writeD(0x00); // Premium Slot Dye Time Left
					writeD(_activeChar.getHennaEx().getHenna(4).isAllowedClass(_activeChar.getClassId()) ? 0x01 : 0x00);
				}
				else
				{
					writeD(0x00); // Premium Slot Dye ID
					writeD(0x00); // Premium Slot Dye Time Left
					writeD(0x00); // Premium Slot Dye ID isValid
				}
				break;
		}
	}
}
