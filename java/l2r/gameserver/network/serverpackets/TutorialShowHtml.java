package l2r.gameserver.network.serverpackets;

import gr.sr.network.handler.ServerTypeConfigs;
import l2r.gameserver.enums.HtmlActionScope;

public final class TutorialShowHtml extends AbstractHtmlPacket
{
	public static final int NORMAL_WINDOW = 1;
	public static final int LARGE_WINDOW = 2;
	
	private final int _type;
	
	public TutorialShowHtml(String html)
	{
		super(html);
		_type = NORMAL_WINDOW;
	}
	
	public TutorialShowHtml(int npcObjId, String html)
	{
		super(npcObjId, html);
		
		_type = NORMAL_WINDOW;
	}
	
	@Override
	protected void writeImpl()
	{
		switch (ServerTypeConfigs.SERVER_TYPE)
		{
			case IL:
				writeC(0xA0);
				break;
			case GF:
			case EPILOGUE:
			case FREYA:
			case H5:
			case GC:
			case SL:
				writeC(0xA6);
				break;
		}
		
		switch (ServerTypeConfigs.SERVER_TYPE)
		{
			case GC:
			case SL:
				writeD(_type);
				break;
		}
		
		writeS(getHtml());
	}
	
	@Override
	public HtmlActionScope getScope()
	{
		return HtmlActionScope.TUTORIAL_HTML;
	}
}