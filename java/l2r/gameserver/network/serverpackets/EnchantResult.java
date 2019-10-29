package l2r.gameserver.network.serverpackets;

import gr.sr.network.handler.ServerTypeConfigs;
import l2r.gameserver.model.items.instance.L2ItemInstance;

public class EnchantResult extends L2GameServerPacket
{
	public static int SUCCESS = 0;
	public static int FAIL = 1;
	public static int ERROR = 2;
	public static int BLESSED_FAIL = 3;
	public static int NO_CRYSTAL = 4;
	public static int SAFE_FAIL = 5;
	
	private final int _result;
	private final int _crystal;
	private final int _count;
	private final int _enchantLevel;
	private final int[] _enchantOptions;
	
	public EnchantResult(int result, int crystal, int count, int enchantLevel, int[] options)
	{
		_result = result;
		_crystal = crystal;
		_count = count;
		_enchantLevel = enchantLevel;
		_enchantOptions = options;
	}
	
	public EnchantResult(int result, int crystal, int count)
	{
		this(result, crystal, count, 0, L2ItemInstance.DEFAULT_ENCHANT_OPTIONS);
	}
	
	public EnchantResult(int result, L2ItemInstance item)
	{
		this(result, 0, 0, item.getEnchantLevel(), item.getEnchantOptions());
	}
	
	@Override
	protected final void writeImpl()
	{
		switch (ServerTypeConfigs.SERVER_TYPE)
		{
			case IL:
				writeC(0x81);
				break;
			case GF:
			case EPILOGUE:
			case FREYA:
			case H5:
			case GC:
			case SL:
				writeC(0x87);
				break;
		}
		
		writeD(_result);
		writeD(_crystal);
		writeQ(_count);
		
		switch (ServerTypeConfigs.SERVER_TYPE)
		{
			case GC:
			case SL:
				writeD(_enchantLevel);
				for (int option : _enchantOptions)
				{
					writeH(option);
				}
				break;
		}
	}
}
