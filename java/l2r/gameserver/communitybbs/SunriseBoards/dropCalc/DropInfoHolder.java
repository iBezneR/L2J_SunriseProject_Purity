/*
 * Copyright (C) 2004-2015 L2J Server
 * 
 * This file is part of L2J Server.
 * 
 * L2J Server is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * L2J Server is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package l2r.gameserver.communitybbs.SunriseBoards.dropCalc;

/**
 * @author vGodFather
 */
public class DropInfoHolder
{
	public static final int MAX_CHANCE = 1000000;
	
	private int _npcId;
	private String _npcName;
	private byte _npcLevel;
	private long _minDrop;
	private long _maxDrop;
	private double _chance;
	private boolean _isSweep;
	
	public DropInfoHolder(int npcId, String npcName, byte level, long min, long max, double chance, boolean sweep)
	{
		_npcId = npcId;
		_npcName = npcName;
		_npcLevel = level;
		_minDrop = min;
		_maxDrop = max;
		_chance = chance;
		_isSweep = sweep;
	}
	
	public int getNpcId()
	{
		return _npcId;
	}
	
	public void setNpcId(int npcId)
	{
		_npcId = npcId;
	}
	
	public String getName()
	{
		return _npcName;
	}
	
	public void setName(String name)
	{
		_npcName = name;
	}
	
	public byte getLevel()
	{
		return _npcLevel;
	}
	
	public void setLevel(byte level)
	{
		_npcLevel = level;
	}
	
	public boolean isSweep()
	{
		return _isSweep;
	}
	
	public void setIsSweep(boolean isSweep)
	{
		_isSweep = isSweep;
	}
	
	/**
	 * Returns the minimum quantity of items dropped
	 * @return int
	 */
	public long getMin()
	{
		return _minDrop;
	}
	
	/**
	 * Returns the maximum quantity of items dropped
	 * @return int
	 */
	public long getMax()
	{
		return _maxDrop;
	}
	
	/**
	 * Returns the chance of having a drop
	 * @return int
	 */
	public double getChance()
	{
		return _chance;
	}
	
	/**
	 * Sets the value for minimal quantity of dropped items
	 * @param mindrop : int designating the quantity
	 */
	public void setMinDrop(int mindrop)
	{
		_minDrop = mindrop;
	}
	
	/**
	 * Sets the value for maximal quantity of dopped items
	 * @param maxdrop : int designating the quantity of dropped items
	 */
	public void setMaxDrop(int maxdrop)
	{
		_maxDrop = maxdrop;
	}
	
	/**
	 * Sets the chance of having the item for a drop
	 * @param chance : int designating the chance
	 */
	public void setChance(double chance)
	{
		_chance = chance;
	}
}
