package l2r.gameserver.network.serverpackets;

import gr.sr.network.handler.ServerTypeConfigs;
import l2r.gameserver.enums.HtmlActionScope;

public final class NpcQuestHtmlMessage extends AbstractHtmlPacket
{
	private final int _questId;
	
	public NpcQuestHtmlMessage(int npcObjId, int questId)
	{
		super(npcObjId);
		_questId = questId;
	}
	
	@Override
	protected final void writeImpl()
	{
		switch (ServerTypeConfigs.SERVER_TYPE)
		{
			case IL:
				return;
		}
		
		writeC(0xFE);
		
		switch (ServerTypeConfigs.SERVER_TYPE)
		{
			case GF:
			case EPILOGUE:
			case FREYA:
			case H5:
				writeH(0x8D);
				break;
			case GC:
			case SL:
				writeH(0x8E);
				break;
		}
		
		writeD(getNpcObjId());
		writeS(getHtml());
		writeD(_questId);
	}
	
	@Override
	public HtmlActionScope getScope()
	{
		return HtmlActionScope.NPC_QUEST_HTML;
	}
}
