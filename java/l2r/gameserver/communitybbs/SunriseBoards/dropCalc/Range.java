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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Range
{
	private int[] nums;
	
	/**
	 * num between start to end <font color=FF0000>dont contains end</font>
	 * @param start
	 * @param end
	 **/
	public Range(int start, int end)
	{
		if (start >= end)
		{
			nums = new int[]
			{
				0
			};
			throw new RuntimeException("Wrong Range! start must less than end");
		}
		
		nums = new int[end - start];
		for (int i = 0; i < nums.length; i++)
		{
			nums[i] = start++;
		}
	}
	
	public List<Integer> values()
	{
		List<Integer> list = new ArrayList<>();
		for (int i : getNums())
		{
			list.add(i);
		}
		return list;
	}
	
	public int[] getNums()
	{
		return nums;
	}
	
	public int[] add(int... values)
	{
		int oldLength = nums.length;
		nums = Arrays.copyOf(nums, nums.length + values.length);
		for (int i = 0; i < values.length; i++)
		{
			nums[oldLength + i] = values[i];
		}
		return nums;
	}
	
	@Override
	public String toString()
	{
		return Arrays.toString(nums);
	}
	
}