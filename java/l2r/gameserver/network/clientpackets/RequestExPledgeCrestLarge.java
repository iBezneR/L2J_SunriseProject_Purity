package l2r.gameserver.network.clientpackets;

import l2r.gameserver.data.sql.CrestTable;
import l2r.gameserver.model.L2Crest;
import l2r.gameserver.network.serverpackets.ExPledgeCrestLarge;

public final class RequestExPledgeCrestLarge extends L2GameClientPacket
{
	private static final String _C__D0_10_REQUESTEXPLEDGECRESTLARGE = "[C] D0:10 RequestExPledgeCrestLarge";
	
	private int _crestId;
	
	@Override
	protected void readImpl()
	{
		_crestId = readD();
	}
	
	@Override
	protected void runImpl()
	{
		final L2Crest crest = CrestTable.getInstance().getCrest(_crestId);
		final byte[] data = crest != null ? crest.getData() : null;
		if (data != null)
		{
			sendPacket(new ExPledgeCrestLarge(_crestId, data, 0, 0));
		}
	}
	
	@Override
	public String getType()
	{
		return _C__D0_10_REQUESTEXPLEDGECRESTLARGE;
	}
	
	@Override
	protected boolean triggersOnActionRequest()
	{
		return false;
	}
}