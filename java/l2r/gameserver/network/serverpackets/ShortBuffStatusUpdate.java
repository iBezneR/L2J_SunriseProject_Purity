package l2r.gameserver.network.serverpackets;

import gr.sr.network.handler.ServerTypeConfigs;

public class ShortBuffStatusUpdate extends L2GameServerPacket
{
	public static final ShortBuffStatusUpdate RESET_SHORT_BUFF = new ShortBuffStatusUpdate(0, 0, 0);
	
	private final int _skillId;
	private final int _skillLvl;
	private final int _duration;
	
	public ShortBuffStatusUpdate(int skillId, int skillLvl, int duration)
	{
		_skillId = skillId;
		_skillLvl = skillLvl;
		_duration = duration;
	}
	
	@Override
	protected final void writeImpl()
	{
		switch (ServerTypeConfigs.SERVER_TYPE)
		{
			case IL:
				writeC(0xF4);
				break;
			case GF:
			case EPILOGUE:
			case FREYA:
			case H5:
			case GC:
			case SL:
				writeC(0xFA);
				break;
		}
		
		writeD(_skillId);
		
		switch (ServerTypeConfigs.SERVER_TYPE)
		{
			case IL:
			case GF:
			case EPILOGUE:
			case FREYA:
			case H5:
				writeD(_skillLvl);
				break;
			case GC:
			case SL:
				writeH(_skillLvl);
				writeH(0x00); // sublevel
				break;
		}
		
		writeD(_duration);
	}
}
