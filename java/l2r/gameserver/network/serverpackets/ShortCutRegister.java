package l2r.gameserver.network.serverpackets;

import gr.sr.network.handler.ServerTypeConfigs;
import l2r.gameserver.model.Shortcut;

public final class ShortCutRegister extends L2GameServerPacket
{
	private final Shortcut _shortcut;
	
	public ShortCutRegister(Shortcut shortcut)
	{
		_shortcut = shortcut;
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
				writeC(0x44);
				break;
		}
		
		writeD(_shortcut.getType().ordinal());
		writeD(_shortcut.getSlot() + (_shortcut.getPage() * 12)); // C4 Client
		switch (_shortcut.getType())
		{
			case ITEM:
			{
				writeD(_shortcut.getId());
				writeD(_shortcut.getCharacterType());
				writeD(_shortcut.getSharedReuseGroup());
				writeD(0x00); // unknown
				writeD(0x00); // unknown
				writeD(0x00); // item augment id
				
				switch (ServerTypeConfigs.SERVER_TYPE)
				{
					case GC:
					case SL:
						writeD(0x00); // TODO: Find me
						break;
				}
				break;
			}
			case SKILL:
			{
				writeD(_shortcut.getId());
				
				switch (ServerTypeConfigs.SERVER_TYPE)
				{
					case IL:
					case GF:
					case EPILOGUE:
					case FREYA:
					case H5:
						writeD(_shortcut.getLevel());
						break;
					case GC:
					case SL:
						writeH(_shortcut.getLevel());
						writeH(_shortcut.getSubLevel());
						break;
				}
				
				writeD(_shortcut.getSharedReuseGroup());
				writeC(0x00); // C5
				writeD(_shortcut.getCharacterType());
				
				switch (ServerTypeConfigs.SERVER_TYPE)
				{
					case GC:
					case SL:
						writeD(0x00); // TODO: Find me
						writeD(0x00); // TODO: Find me
						break;
				}
				break;
			}
			case ACTION:
			case MACRO:
			case RECIPE:
			case BOOKMARK:
			{
				writeD(_shortcut.getId());
				writeD(_shortcut.getCharacterType());
				break;
			}
		}
	}
}
