package l2r.gameserver.network.serverpackets;

import gr.sr.network.handler.ServerTypeConfigs;
import l2r.gameserver.network.SystemMessageId;

public class ConfirmDlg extends AbstractMessagePacket<ConfirmDlg>
{
	private int _time;
	private int _requesterId;
	
	public ConfirmDlg(SystemMessageId smId)
	{
		super(smId);
	}
	
	public ConfirmDlg(int id)
	{
		this(SystemMessageId.getSystemMessageId(id));
	}
	
	public ConfirmDlg(String text)
	{
		this(SystemMessageId.S1);
		addString(text);
	}
	
	public ConfirmDlg addTime(int time)
	{
		_time = time;
		return this;
	}
	
	public ConfirmDlg addRequesterId(int id)
	{
		_requesterId = id;
		return this;
	}
	
	@Override
	protected void writeParamsSize(int size)
	{
		writeD(size);
	}
	
	@Override
	protected void writeParamType(int type)
	{
		writeD(type);
	}
	
	@Override
	protected final void writeImpl()
	{
		switch (ServerTypeConfigs.SERVER_TYPE)
		{
			case IL:
				writeC(0xED);
				break;
			case GF:
			case EPILOGUE:
			case FREYA:
			case H5:
			case GC:
			case SL:
				writeC(0xF3);
				break;
		}
		
		switch (ServerTypeConfigs.SERVER_TYPE)
		{
			case IL:
			case GF:
			case EPILOGUE:
			case FREYA:
			case H5:
				writeD(getId());
				break;
			case GC:
			case SL:
				writeD(getId());
				break;
		}
		
		writeMe();
		writeD(_time);
		writeD(_requesterId);
	}
}
