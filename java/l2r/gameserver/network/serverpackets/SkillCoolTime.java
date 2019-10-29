package l2r.gameserver.network.serverpackets;

import gr.sr.network.handler.ServerTypeConfigs;
import l2r.gameserver.model.TimeStamp;
import l2r.gameserver.model.actor.instance.L2PcInstance;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class SkillCoolTime extends L2GameServerPacket
{
	private final List<TimeStamp> _skillReuseTimeStamps = new ArrayList<>();
	
	public SkillCoolTime(L2PcInstance player)
	{
		final Map<Integer, TimeStamp> skillReuseTimeStamps = player.getSkillReuseTimeStamps();
		if (skillReuseTimeStamps != null)
		{
			for (TimeStamp ts : skillReuseTimeStamps.values())
			{
				if (ts.hasNotPassed())
				{
					_skillReuseTimeStamps.add(ts);
				}
			}
		}
	}
	
	@Override
	protected void writeImpl()
	{
		switch (ServerTypeConfigs.SERVER_TYPE)
		{
			case IL:
				writeC(0xC1);
				break;
			case GF:
			case EPILOGUE:
			case FREYA:
			case H5:
			case GC:
			case SL:
				writeC(0xC7);
				break;
		}
		
		writeD(_skillReuseTimeStamps.size());
		for (TimeStamp ts : _skillReuseTimeStamps)
		{
			writeD(ts.getSkillId());
			
			switch (ServerTypeConfigs.SERVER_TYPE)
			{
				case GF:
				case EPILOGUE:
				case FREYA:
				case H5:
				case GC:
				case SL:
					writeD(0x00); // Skill Level,but not used
					break;
			}
			
			writeD((int) ts.getReuse() / 1000);
			writeD((int) ts.getRemaining() / 1000);
		}
	}
}
