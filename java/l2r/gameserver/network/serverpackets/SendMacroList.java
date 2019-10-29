package l2r.gameserver.network.serverpackets;

import gr.sr.network.handler.ServerTypeConfigs;
import l2r.gameserver.model.Macro;
import l2r.gameserver.model.MacroCmd;
import l2r.gameserver.network.handlers.types.MacroUpdateType;

public class SendMacroList extends L2GameServerPacket
{
	private final int _rev;
	private final int _count;
	private final Macro _macro;
	private final MacroUpdateType _updateType;
	
	public SendMacroList(int rev, int count, Macro macro, MacroUpdateType updateType)
	{
		_rev = rev;
		_count = count;
		_macro = macro;
		_updateType = updateType;
	}
	
	@Override
	protected final void writeImpl()
	{
		switch (ServerTypeConfigs.SERVER_TYPE)
		{
			case IL:
				writeC(0xE7);
				break;
			case GF:
			case EPILOGUE:
			case FREYA:
			case H5:
			case GC:
			case SL:
				writeC(0xE8);
				break;
		}
		
		switch (ServerTypeConfigs.SERVER_TYPE)
		{
			case IL:
			case GF:
			case EPILOGUE:
			case FREYA:
			case H5:
				writeD(_rev); // macro change revision (changes after each macro edition)
				writeC(0x00); // unknown
				writeC(_count); // count of Macros
				writeC(_macro != null ? 1 : 0); // unknown
				break;
			case GC:
			case SL:
				writeC(_updateType.getId());
				writeD(_updateType != MacroUpdateType.LIST ? _macro.getId() : 0x00); // modified, created or deleted macro's id
				writeC(_count); // count of Macros
				writeC(_macro != null ? 1 : 0); // unknown
				break;
		}
		
		if (_macro != null)
		{
			writeD(_macro.getId()); // Macro ID
			writeS(_macro.getName()); // Macro Name
			writeS(_macro.getDescr()); // Desc
			writeS(_macro.getAcronym()); // acronym
			
			switch (ServerTypeConfigs.SERVER_TYPE)
			{
				case IL:
				case GF:
				case EPILOGUE:
				case FREYA:
				case H5:
					writeC(_macro.getIcon()); // icon
					break;
				case GC:
				case SL:
					writeD(_macro.getIcon()); // icon
					break;
			}
			
			writeC(_macro.getCommands().size()); // count
			
			int i = 1;
			for (MacroCmd cmd : _macro.getCommands())
			{
				writeC(i++); // command count
				writeC(cmd.getType().ordinal()); // type 1 = skill, 3 = action, 4 = shortcut
				writeD(cmd.getD1()); // skill id
				writeC(cmd.getD2()); // shortcut id
				writeS(cmd.getCmd()); // command name
			}
		}
	}
}
