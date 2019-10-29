package l2r.gameserver.network.serverpackets;

import gr.sr.network.handler.ServerTypeConfigs;
import l2r.gameserver.model.actor.instance.L2PcInstance;
import l2r.gameserver.network.NpcStringId;

import java.util.Arrays;
import java.util.List;

public class ExSendUIEvent extends L2GameServerPacket
{
	private final int _objectId;
	private final boolean _type;
	private final boolean _countUp;
	private final int _startTime;
	private final int _endTime;
	private final int _npcstringId;
	private List<String> _params = null;
	
	/**
	 * @param player
	 * @param hide
	 * @param countUp
	 * @param startTime
	 * @param endTime
	 * @param text
	 */
	public ExSendUIEvent(L2PcInstance player, boolean hide, boolean countUp, int startTime, int endTime, String text)
	{
		this(player, hide, countUp, startTime, endTime, -1, text);
	}
	
	/**
	 * @param player
	 * @param hide
	 * @param countUp
	 * @param startTime
	 * @param endTime
	 * @param npcString
	 * @param params
	 */
	public ExSendUIEvent(L2PcInstance player, boolean hide, boolean countUp, int startTime, int endTime, NpcStringId npcString, String... params)
	{
		this(player, hide, countUp, startTime, endTime, npcString.getId(), params);
	}
	
	/**
	 * @param player
	 * @param hide
	 * @param countUp
	 * @param startTime
	 * @param endTime
	 * @param npcstringId
	 * @param params
	 */
	public ExSendUIEvent(L2PcInstance player, boolean hide, boolean countUp, int startTime, int endTime, int npcstringId, String... params)
	{
		_objectId = player.getObjectId();
		_type = hide;
		_countUp = countUp;
		_startTime = startTime;
		_endTime = endTime;
		_npcstringId = npcstringId;
		_params = Arrays.asList(params);
	}
	
	@Override
	protected void writeImpl()
	{
		switch (ServerTypeConfigs.SERVER_TYPE)
		{
			case IL:
				return;
		}
		
		writeC(0xFE);
		
		switch (ServerTypeConfigs.SERVER_TYPE)
		{
			case IL:
			case GF:
			case EPILOGUE:
			case FREYA:
			case H5:
			case GC:
			case SL:
				writeH(0x8E);
				break;
		}
		
		switch (ServerTypeConfigs.SERVER_TYPE)
		{
			case IL:
			case GF:
			case EPILOGUE:
			case FREYA:
				writeD(_objectId);
				writeD(_type ? 1 : 0); // 0: show timer, 1: hide timer
				writeD(0); // unknown
				writeD(0); // unknown
				writeS(_countUp ? "1" : "0"); // "0": count negative, "1": count positive
				writeS(String.valueOf(_startTime / 60)); // timer starting minute(s)
				writeS(String.valueOf(_startTime % 60)); // timer starting second(s)
				if (_params != null)
				{
					String _text = "";
					for (String param : _params)
					{
						_text += param;
					}
					writeS(_text);
				}
				writeS(String.valueOf(_endTime / 60)); // timer length minute(s) (timer will disappear 10 seconds before it ends)
				writeS(String.valueOf(_endTime % 60)); // timer length second(s) (timer will disappear 10 seconds before it ends)
				break;
			case H5:
			case GC:
			case SL:
				writeD(_objectId);
				writeD(_type ? 1 : 0); // 0 = show, 1 = hide (there is 2 = pause and 3 = resume also but they don't work well you can only pause count down and you cannot resume it because resume hides the counter).
				writeD(0);// unknown
				writeD(0);// unknown
				writeS(_countUp ? "1" : "0"); // 0 = count down, 1 = count up
				// timer always disappears 10 seconds before end
				writeS(String.valueOf(_startTime / 60));
				writeS(String.valueOf(_startTime % 60));
				writeS(String.valueOf(_endTime / 60));
				writeS(String.valueOf(_endTime % 60));
				writeD(_npcstringId);
				if (_params != null)
				{
					for (String param : _params)
					{
						writeS(param);
					}
				}
				break;
		}
	}
}