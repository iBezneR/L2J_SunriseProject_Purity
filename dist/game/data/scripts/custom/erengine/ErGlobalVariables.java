/*
 * Copyright (C) 2004-2014 L2J Server
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
package custom.erengine;

import l2r.L2DatabaseFactory;
import l2r.gameserver.ThreadPoolManager;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ErGlobalVariables
{
	public static final Logger _log = Logger.getLogger(ErGlobalVariables.class.getName());
	HashMap<String, String> _variables;
	
	public ErGlobalVariables()
	{
		_variables = new HashMap<>();
		checkIfTableExists();
		loadVariables();
		ThreadPoolManager.getInstance().scheduleGeneralAtFixedRate(new UpdateVariables(), 20000, 300000);
	}
	
	public void setData(String name, long data)
	{
		_variables.put(name, String.valueOf(data));
	}
	
	public void setData(String name, int data)
	{
		_variables.put(name, String.valueOf(data));
	}
	
	public void setData(String name, boolean data)
	{
		_variables.put(name, data ? "1" : "0");
	}
	
	public void setData(String name, String data)
	{
		_variables.put(name, data);
	}
	
	public String getString(String name)
	{
		if (!_variables.containsKey(name))
		{
			_log.log(Level.WARNING, "ErGlobalVariables: Variable with name [" + name + "] does not exists!");
			return "";
		}
		return _variables.get(name);
	}
	
	public int getInt(String name)
	{
		if (!_variables.containsKey(name))
		{
			_log.log(Level.WARNING, "ErGlobalVariables: Variable with name [" + name + "] does not exists!");
			return 0;
		}
		return Integer.parseInt(_variables.get(name));
	}
	
	public long getLong(String name)
	{
		if (!_variables.containsKey(name))
		{
			_log.log(Level.WARNING, "ErGlobalVariables: Variable with name [" + name + "] does not exists!");
			return 0;
		}
		return Long.parseLong(_variables.get(name));
	}
	
	public boolean getBoolean(String name)
	{
		if (!_variables.containsKey(name))
		{
			_log.log(Level.WARNING, "ErGlobalVariables: Variable with name [" + name + "] does not exists!");
			return false;
		}
		return _variables.get(name).equals("1");
	}
	
	private void checkIfTableExists()
	{
		try (Connection con = L2DatabaseFactory.getInstance().getConnection();
             PreparedStatement statement = con.prepareStatement("SHOW TABLES LIKE 'er_global_variables'");
             ResultSet rset = statement.executeQuery();)
		{
			if (!rset.next())
			{
				String create = "CREATE TABLE er_global_variables (\n";
				create += "  name varchar(50) NOT NULL DEFAULT '',\n";
				create += "  value varchar(255) DEFAULT NULL,\n";
				create += "  PRIMARY KEY (name)\n";
				create += ") ENGINE=InnoDB DEFAULT CHARSET=utf8;\n";
				try (Statement statement1 = con.createStatement();)
				{
					statement1.executeUpdate(create);
					statement1.close();
				}
			}
			statement.close();
			rset.close();
		}
		catch (Exception e)
		{
			System.out.println(e.toString());
		}
	}
	
	public void loadVariables()
	{
		_variables.clear();
		try (Connection con = L2DatabaseFactory.getInstance().getConnection();
             PreparedStatement statement = con.prepareStatement("SELECT * FROM er_global_variables");
             ResultSet rset = statement.executeQuery();)
		{
			while (rset.next())
			{
				_variables.put(rset.getString("name"), rset.getString("value"));
			}
			statement.close();
			rset.close();
		}
		catch (Exception e)
		{
			_log.log(Level.SEVERE, "Failed loading er global variables data.", e);
		}
	}
	
	public class UpdateVariables implements Runnable
	{
		@Override
		public void run()
		{
			updateVariables();
		}
	}
	
	protected void updateVariables()
	{
		String update = "REPLACE er_global_variables SET name=?, value=?";
		try (Connection con = L2DatabaseFactory.getInstance().getConnection();
             PreparedStatement statement = con.prepareStatement(update);)
		{
			for (Map.Entry<String, String> entry : _variables.entrySet())
			{
				statement.setString(1, entry.getKey());
				statement.setString(2, entry.getValue());
				statement.addBatch();
			}
			statement.executeBatch();
			statement.close();
		}
		catch (Exception e)
		{
			_log.log(Level.WARNING, "Could not store er global variables data: " + e.getMessage(), e);
		}
	}
	
	public static ErGlobalVariables getInstance()
	{
		return SingletonHolder._instance;
	}
	
	private static class SingletonHolder
	{
		protected static final ErGlobalVariables _instance = new ErGlobalVariables();
	}
}