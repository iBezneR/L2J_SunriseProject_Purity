package l2r.gameserver.network.serverpackets;

import gr.sr.network.handler.ServerTypeConfigs;
import l2r.gameserver.model.actor.instance.L2PcInstance;
import l2r.gameserver.model.quest.QuestState;

import java.util.LinkedList;
import java.util.List;

public class QuestList extends L2GameServerPacket
{
	private final List<QuestState> _activeQuests;
	private final byte[] _oneTimeQuestMask;
	
	public QuestList(L2PcInstance player)
	{
		_activeQuests = new LinkedList<>();
		_oneTimeQuestMask = new byte[128];
		
		for (QuestState qs : player.getAllQuestStates())
		{
			final int questId = qs.getQuest().getId();
			if (questId > 0)
			{
				if (qs.isStarted())
				{
					_activeQuests.add(qs);
				}
				else if (qs.isCompleted() && !(((questId > 255) && (questId < 10256)) || (questId > 11023)))
				{
					_oneTimeQuestMask[(questId % 10000) / 8] |= 1 << (questId % 8);
				}
			}
		}
	}
	
	@Override
	protected final void writeImpl()
	{
		switch (ServerTypeConfigs.SERVER_TYPE)
		{
			case IL:
				writeC(0x80);
				break;
			case GF:
			case EPILOGUE:
			case FREYA:
			case H5:
			case GC:
			case SL:
				writeC(0x86);
				break;
		}
		
		writeH(_activeQuests.size());
		for (QuestState qs : _activeQuests)
		{
			writeD(qs.getQuest().getId());
			
			int states = qs.getInt("__compltdStateFlags");
			writeD(states != 0 ? states : qs.getCond());
		}
		
		switch (ServerTypeConfigs.SERVER_TYPE)
		{
			case GF:
			case EPILOGUE:
			case FREYA:
			case H5:
				writeB(_oneTimeQuestMask);
				break;
		}
	}
}