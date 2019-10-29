package l2r.gameserver.network.serverpackets;

import gr.sr.network.handler.ServerTypeConfigs;

public class ExEnchantSkillResult extends L2GameServerPacket
{
	private static final ExEnchantSkillResult STATIC_PACKET_TRUE = new ExEnchantSkillResult(true);
	private static final ExEnchantSkillResult STATIC_PACKET_FALSE = new ExEnchantSkillResult(false);
	
	public static final ExEnchantSkillResult valueOf(boolean result)
	{
		return result ? STATIC_PACKET_TRUE : STATIC_PACKET_FALSE;
	}
	
	private final boolean _enchanted;
	
	public ExEnchantSkillResult(boolean enchanted)
	{
		_enchanted = enchanted;
	}
	
	@Override
	protected void writeImpl()
	{
		switch (ServerTypeConfigs.SERVER_TYPE)
		{
			case IL:
			case GF:
				return;
		}
		
		writeC(0xFE);
		
		switch (ServerTypeConfigs.SERVER_TYPE)
		{
			case EPILOGUE:
			case FREYA:
			case H5:
				writeH(0xA7);
				break;
			case GC:
			case SL:
				writeH(0xA8);
				break;
		}
		
		writeD(_enchanted ? 1 : 0);
	}
}
