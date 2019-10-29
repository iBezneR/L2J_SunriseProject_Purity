package l2r.gameserver.network.serverpackets;

import gr.sr.network.handler.ServerTypeConfigs;
import l2r.gameserver.GameTimeController;
import l2r.gameserver.model.actor.instance.L2PcInstance;

public class CharSelected extends L2GameServerPacket
{
	private final L2PcInstance _activeChar;
	private final int _sessionId;
	
	public CharSelected(L2PcInstance cha, int sessionId)
	{
		_activeChar = cha;
		_sessionId = sessionId;
	}
	
	@Override
	protected final void writeImpl()
	{
		switch (ServerTypeConfigs.SERVER_TYPE)
		{
			case IL:
				writeC(0x15);
				break;
			case GF:
			case EPILOGUE:
			case FREYA:
			case H5:
			case GC:
			case SL:
				writeC(0x0B);
				break;
		}
		
		switch (ServerTypeConfigs.SERVER_TYPE)
		{
			case GC:
			case SL:
				writeS(_activeChar.getName());
				writeD(_activeChar.getObjectId());
				writeS(_activeChar.getTitle());
				writeD(_sessionId);
				writeD(_activeChar.getClanId());
				writeD(0x00); // ??
				writeD(_activeChar.getAppearance().getSex() ? 1 : 0);
				writeD(_activeChar.getRace().ordinal());
				writeD(_activeChar.getClassId().getId());
				writeD(0x01); // active ??
				writeD(_activeChar.getX());
				writeD(_activeChar.getY());
				writeD(_activeChar.getZ());
				writeF(_activeChar.getCurrentHp());
				writeF(_activeChar.getCurrentMp());
				writeQ(_activeChar.getSp());
				writeQ(_activeChar.getExp());
				writeD(_activeChar.getLevel());
				writeD(_activeChar.getKarma());
				writeD(_activeChar.getPkKills());
				writeD(GameTimeController.getInstance().getGameTime() % (24 * 60)); // "reset" on 24th hour
				writeD(0x00);
				writeD(_activeChar.getClassId().getId());
				
				writeB(new byte[16]);
				
				writeD(0x00);
				writeD(0x00);
				writeD(0x00);
				writeD(0x00);
				
				writeD(0x00);
				
				writeD(0x00);
				writeD(0x00);
				writeD(0x00);
				writeD(0x00);
				
				writeB(new byte[28]);
				writeD(0x00);
				return;
		}
		
		writeS(_activeChar.getName());
		writeD(_activeChar.getObjectId());
		writeS(_activeChar.getTitle());
		writeD(_sessionId);
		writeD(_activeChar.getClanId());
		writeD(0x00); // ??
		writeD(_activeChar.getAppearance().getSex() ? 1 : 0);
		writeD(_activeChar.getRace().ordinal());
		writeD(_activeChar.getClassId().getId());
		writeD(0x01); // active ??
		writeD(_activeChar.getX());
		writeD(_activeChar.getY());
		writeD(_activeChar.getZ());
		
		writeF(_activeChar.getCurrentHp());
		writeF(_activeChar.getCurrentMp());
		writeD(_activeChar.getSp());
		writeQ(_activeChar.getExp());
		writeD(_activeChar.getLevel());
		writeD(_activeChar.getKarma()); // thx evill33t
		writeD(_activeChar.getPkKills());
		writeD(_activeChar.getINT());
		writeD(_activeChar.getSTR());
		writeD(_activeChar.getCON());
		writeD(_activeChar.getMEN());
		writeD(_activeChar.getDEX());
		writeD(_activeChar.getWIT());
		
		switch (ServerTypeConfigs.SERVER_TYPE)
		{
			case IL:
				for (int i = 0; i < 30; i++)
				{
					writeD(0x00);
				}
				
				writeD(0x00); // c3 work
				writeD(0x00); // c3 work
				break;
		}
		
		writeD(GameTimeController.getInstance().getGameTime() % (24 * 60)); // "reset" on 24th hour
		writeD(0x00);
		
		writeD(_activeChar.getClassId().getId());
		
		writeD(0x00);
		writeD(0x00);
		writeD(0x00);
		writeD(0x00);
		
		switch (ServerTypeConfigs.SERVER_TYPE)
		{
			case GF:
			case EPILOGUE:
			case FREYA:
			case H5:
				writeB(new byte[64]);
				writeD(0x00);
				break;
		}
	}
}
