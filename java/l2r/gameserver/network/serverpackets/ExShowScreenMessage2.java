package l2r.gameserver.network.serverpackets;

import gr.sr.network.handler.ServerTypeConfigs;
import gr.sr.network.handler.types.ServerType;

public class ExShowScreenMessage2 extends L2GameServerPacket
{
	public static enum ScreenMessageAlign
	{
		TOP_LEFT,
		TOP_CENTER,
		TOP_RIGHT,
		MIDDLE_LEFT,
		MIDDLE_CENTER,
		MIDDLE_RIGHT,
		BOTTOM_CENTER,
		BOTTOM_RIGHT
	}
	
	private final int defaultNpcStringId = ServerTypeConfigs.SERVER_TYPE == ServerType.FREYA ? 0 : -1;
	
	private final int _type, _sysMessageId;
	private final boolean _big_font, _effect;
	private final ScreenMessageAlign _text_align;
	private final int _time;
	private final String _text;
	private final int _npcString;
	private final boolean _fade;
	
	public ExShowScreenMessage2(String text, int time, ScreenMessageAlign text_align, boolean big_font)
	{
		_type = 1;
		_sysMessageId = -1;
		_fade = false;
		_text = text;
		_time = time;
		_text_align = text_align;
		_big_font = big_font;
		_effect = false;
		_npcString = defaultNpcStringId;
	}
	
	public ExShowScreenMessage2(String text, int time, ScreenMessageAlign text_align)
	{
		this(text, time, text_align, true);
	}
	
	public ExShowScreenMessage2(String text, int time)
	{
		this(text, time, ScreenMessageAlign.MIDDLE_CENTER);
	}
	
	public ExShowScreenMessage2(String text, int time, ScreenMessageAlign text_align, boolean big_font, int type, int messageId, boolean showEffect)
	{
		_type = type;
		_sysMessageId = messageId;
		_fade = false;
		_text = text;
		_time = time;
		_text_align = text_align;
		_big_font = big_font;
		_effect = showEffect;
		_npcString = defaultNpcStringId;
	}
	
	@Override
	protected final void writeImpl()
	{
		writeC(0xFE);
		
		switch (ServerTypeConfigs.SERVER_TYPE)
		{
			case IL:
				writeH(0x38);
				break;
			case GF:
			case EPILOGUE:
			case FREYA:
			case H5:
				writeH(0x39);
				break;
		}
		
		writeD(_type); // 0 - system messages, 1 - your defined text
		writeD(_sysMessageId); // system message id (_type must be 0 otherwise no effect)
		writeD(_text_align.ordinal() + 1); // placement of the text
		writeD(0x00); // ?
		writeD(_big_font ? 0 : 1); // text size
		writeD(0x00); // ?
		writeD(0x00); // ?
		writeD(_effect == true ? 1 : 0); // upper effect (0 - disabled, 1 enabled) - _position must be 2 (center) otherwise no effect
		writeD(_time); // the message is displayed in milliseconds
		writeD(_fade ? 0x01 : 0x00);
		
		switch (ServerTypeConfigs.SERVER_TYPE)
		{
			case IL:
			case GF:
			case EPILOGUE:
				writeS(_text);
				break;
			case FREYA:
			case H5:
			case GC:
			case SL:
				writeD(_npcString);
				writeS(_text); // text messages
				break;
		}
	}
}