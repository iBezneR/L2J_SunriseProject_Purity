package l2r.gameserver.network.serverpackets;

import gr.sr.network.handler.ServerTypeConfigs;
import l2r.gameserver.model.actor.instance.L2PcInstance;
import l2r.gameserver.model.items.L2Henna;

public final class HennaItemRemoveInfo extends L2GameServerPacket
{
	private final L2PcInstance _activeChar;
	private final L2Henna _henna;
	
	public HennaItemRemoveInfo(L2Henna henna, L2PcInstance player)
	{
		_henna = henna;
		_activeChar = player;
	}
	
	@Override
	protected void writeImpl()
	{
		switch (ServerTypeConfigs.SERVER_TYPE)
		{
			case IL:
				writeC(0xE6);
				break;
			case GF:
			case EPILOGUE:
			case FREYA:
			case H5:
			case GC:
			case SL:
				writeC(0xE7);
				break;
		}
		
		writeD(_henna.getDyeId()); // symbol Id
		writeD(_henna.getDyeItemId()); // item id of dye
		
		switch (ServerTypeConfigs.SERVER_TYPE)
		{
			case IL:
				writeD(_henna.getCancelCount()); // total amount of dye require
				writeD(_henna.getCancelFee()); // total amount of Adena require to remove symbol
				break;
			case GF:
			case EPILOGUE:
			case FREYA:
			case H5:
			case GC:
			case SL:
				writeQ(_henna.getCancelCount()); // total amount of dye require
				writeQ(_henna.getCancelFee()); // total amount of Adena require to remove symbol
				break;
		}
		
		writeD(_henna.isAllowedClass(_activeChar.getClassId()) ? 0x01 : 0x00); // able to remove or not
		
		switch (ServerTypeConfigs.SERVER_TYPE)
		{
			case IL:
				writeD((int) _activeChar.getAdena());
				break;
			case GF:
			case EPILOGUE:
			case FREYA:
			case H5:
			case GC:
			case SL:
				writeQ(_activeChar.getAdena());
				break;
		}
		
		switch (ServerTypeConfigs.SERVER_TYPE)
		{
			case IL:
			case GF:
			case EPILOGUE:
			case FREYA:
			case H5:
				writeD(_activeChar.getINT()); // current INT
				writeC(_activeChar.getINT() - _henna.getStatINT()); // equip INT
				writeD(_activeChar.getSTR()); // current STR
				writeC(_activeChar.getSTR() - _henna.getStatSTR()); // equip STR
				writeD(_activeChar.getCON()); // current CON
				writeC(_activeChar.getCON() - _henna.getStatCON()); // equip CON
				writeD(_activeChar.getMEN()); // current MEN
				writeC(_activeChar.getMEN() - _henna.getStatMEN()); // equip MEN
				writeD(_activeChar.getDEX()); // current DEX
				writeC(_activeChar.getDEX() - _henna.getStatDEX()); // equip DEX
				writeD(_activeChar.getWIT()); // current WIT
				writeC(_activeChar.getWIT() - _henna.getStatWIT()); // equip WIT
				break;
			case GC:
			case SL:
				writeD(_activeChar.getINT()); // current INT
				writeH(_activeChar.getINT() - _henna.getStatINT()); // equip INT
				writeD(_activeChar.getSTR()); // current STR
				writeH(_activeChar.getSTR() - _henna.getStatSTR()); // equip STR
				writeD(_activeChar.getCON()); // current CON
				writeH(_activeChar.getCON() - _henna.getStatCON()); // equip CON
				writeD(_activeChar.getMEN()); // current MEN
				writeH(_activeChar.getMEN() - _henna.getStatMEN()); // equip MEN
				writeD(_activeChar.getDEX()); // current DEX
				writeH(_activeChar.getDEX() - _henna.getStatDEX()); // equip DEX
				writeD(_activeChar.getWIT()); // current WIT
				writeH(_activeChar.getWIT() - _henna.getStatWIT()); // equip WIT
				writeD(0x00); // current LUC
				writeH(0x00); // equip LUC
				writeD(0x00); // current CHA
				writeH(0x00); // equip CHA
				writeD(0x00);
				break;
		}
	}
}
