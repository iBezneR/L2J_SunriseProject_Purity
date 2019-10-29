package l2r.gameserver.network.serverpackets;

import gr.sr.network.handler.ServerTypeConfigs;
import l2r.gameserver.model.actor.L2Character;
import l2r.gameserver.model.interfaces.ILocational;

public final class FlyToLocation extends L2GameServerPacket
{
	private final int _destX, _destY, _destZ;
	private final int _chaObjId, _chaX, _chaY, _chaZ;
	private final FlyType _type;
	private int _flySpeed;
	private int _flyDelay;
	private int _animationSpeed;
	
	public enum FlyType
	{
		THROW_UP,
		THROW_HORIZONTAL,
		DUMMY, // no effect
		CHARGE,
		PUSH_HORIZONTAL,
		JUMP_EFFECTED,
		NOT_USED,
		PUSH_DOWN_HORIZONTAL,
		WARP_BACK,
		WARP_FORWARD
	}
	
	public FlyToLocation(L2Character cha, int destX, int destY, int destZ, FlyType type)
	{
		_chaObjId = cha.getObjectId();
		_chaX = cha.getX();
		_chaY = cha.getY();
		_chaZ = cha.getZ();
		_destX = destX;
		_destY = destY;
		_destZ = destZ;
		_type = type;
	}
	
	public FlyToLocation(L2Character cha, ILocational dest, FlyType type)
	{
		this(cha, dest.getX(), dest.getY(), dest.getZ(), type);
	}
	
	@Override
	protected void writeImpl()
	{
		switch (ServerTypeConfigs.SERVER_TYPE)
		{
			case IL:
				writeC(0xC5);
				break;
			case GF:
			case EPILOGUE:
			case FREYA:
			case H5:
			case GC:
			case SL:
				writeC(0xD4);
				break;
		}
		
		writeD(_chaObjId);
		writeD(_destX);
		writeD(_destY);
		writeD(_destZ);
		writeD(_chaX);
		writeD(_chaY);
		writeD(_chaZ);
		writeD(_type.ordinal());
		
		switch (ServerTypeConfigs.SERVER_TYPE)
		{
			case GC:
			case SL:
				writeD(_flySpeed);
				writeD(_flyDelay);
				writeD(_animationSpeed);
				break;
		}
	}
}
