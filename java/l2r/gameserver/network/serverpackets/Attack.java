package l2r.gameserver.network.serverpackets;

import gr.sr.network.handler.ServerTypeConfigs;
import l2r.gameserver.model.Hit;
import l2r.gameserver.model.Location;
import l2r.gameserver.model.actor.L2Character;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class Attack extends L2GameServerPacket
{
	private final int _attackerObjId;
	private final boolean _soulshot;
	private final int _ssGrade;
	private final Location _attackerLoc;
	private final Location _targetLoc;
	private final List<Hit> _hits = new ArrayList<>();
	
	/**
	 * @param attacker
	 * @param target
	 * @param useShots
	 * @param ssGrade
	 */
	public Attack(L2Character attacker, L2Character target, boolean useShots, int ssGrade)
	{
		_attackerObjId = attacker.getObjectId();
		_soulshot = useShots;
		_ssGrade = ssGrade;
		_attackerLoc = new Location(attacker);
		_targetLoc = new Location(target);
	}
	
	/**
	 * Adds hit to the attack (Attacks such as dual dagger/sword/fist has two hits)
	 * @param target
	 * @param damage
	 * @param miss
	 * @param crit
	 * @param shld
	 */
	public void addHit(L2Character target, int damage, boolean miss, boolean crit, byte shld)
	{
		_hits.add(new Hit(target, damage, miss, crit, shld, _soulshot, _ssGrade));
	}
	
	/**
	 * @return {@code true} if current attack contains at least 1 hit.
	 */
	public boolean hasHits()
	{
		return !_hits.isEmpty();
	}
	
	/**
	 * @return {@code true} if attack has soul shot charged.
	 */
	public boolean hasSoulshot()
	{
		return _soulshot;
	}
	
	/**
	 * Writes current hit
	 * @param hit
	 */
	private void writeHit(Hit hit)
	{
		switch (ServerTypeConfigs.SERVER_TYPE)
		{
			case IL:
			case GF:
			case EPILOGUE:
			case FREYA:
			case H5:
				writeD(hit.getTargetId());
				writeD(hit.getDamage());
				writeC(hit.getFlags());
				break;
			case GC:
			case SL:
				writeD(hit.getTargetId());
				writeD(hit.getDamage());
				writeD(hit.getFlags());
				writeD(hit.getGrade()); // GOD
				break;
		}
	}
	
	@Override
	protected final void writeImpl()
	{
		switch (ServerTypeConfigs.SERVER_TYPE)
		{
			case IL:
				writeC(0x33);
				break;
			case GF:
			case EPILOGUE:
			case FREYA:
			case H5:
			case GC:
			case SL:
				writeC(0x33);
				break;
		}
		
		final Iterator<Hit> it = _hits.iterator();
		
		switch (ServerTypeConfigs.SERVER_TYPE)
		{
			case IL:
			case GF:
			case EPILOGUE:
			case FREYA:
			case H5:
				writeD(_attackerObjId);
				writeHit(it.next());
				writeLoc(_attackerLoc);
				
				writeH(_hits.size() - 1);
				while (it.hasNext())
				{
					writeHit(it.next());
				}
				
				writeLoc(_targetLoc);
				break;
			case GC:
			case SL:
				final Hit firstHit = it.next();
				
				writeD(_attackerObjId);
				writeD(firstHit.getTargetId());
				writeD(0x00); // Ertheia Unknown
				writeD(firstHit.getDamage());
				writeD(firstHit.getFlags());
				writeD(firstHit.getGrade()); // GOD getGrade
				writeLoc(_attackerLoc);
				
				writeH(_hits.size() - 1);
				while (it.hasNext())
				{
					writeHit(it.next());
				}
				
				writeLoc(_targetLoc);
				break;
		}
	}
}
