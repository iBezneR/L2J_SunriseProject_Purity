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
package custom.captcha;

import custom.erengine.ErConfig;
import custom.erengine.ErSMPos;
import l2r.gameserver.ThreadPoolManager;
import l2r.gameserver.model.actor.instance.L2PcInstance;
import l2r.util.Rnd;

import java.util.concurrent.ScheduledFuture;

/**
 * @author Erlandas
 */
public class CaptchaPlayer
{
	private int _killsCount = 0;
	private long _lastKill = 0;
	private int _needKills = 0;
	private int _failedCaptchas = 0;
	private L2PcInstance _player;
	private String _captcha = "";
	private String _inputedCaptcha = "";
	private int _inputedNumbers = 0;
	private int[] _numbers = new int[10];
	private ScheduledFuture<?> _scheduler = null;
	private int _imageId = 0;
	
	public CaptchaPlayer(L2PcInstance player)
	{
		_player = player;
	}
	
	public void increaseKill()
	{
		if (!ErConfig.CAPTCHA_ENABLED)
		{
			return;
		}
		if (!_inputedCaptcha.equals(""))
		{
			return;
		}
		
		if ((_lastKill + ErConfig.CAPTCHA_IDLE_TIME) < System.currentTimeMillis())
		{
			_killsCount = 1;
			_lastKill = System.currentTimeMillis();
			_needKills = Rnd.get(ErConfig.CAPTCHA_MIN_MONSTERS_COUNT, ErConfig.CAPTCHA_MAX_MONSTERS_COUNT);
			return;
		}
		_killsCount++;
		_lastKill = System.currentTimeMillis();
		if (_needKills <= _killsCount)
		{
			Captcha.getInstance().startCaptcha(_player);
		}
	}
	
	void setFailedCaptchas(int failedCaptchas)
	{
		_failedCaptchas = failedCaptchas;
	}
	
	int getFailedCaptchas()
	{
		return _failedCaptchas;
	}
	
	public void setCaptcha(String captcha)
	{
		_captcha = captcha;
	}
	
	public String getCaptcha()
	{
		return _captcha;
	}
	
	void setInputedCaptcha(String captcha)
	{
		_inputedCaptcha = captcha;
	}
	
	void increaseInputedCaptcha(String captcha)
	{
		_inputedCaptcha = _inputedCaptcha.substring(0, _inputedNumbers * 2) + captcha + " " + _inputedCaptcha.substring((_inputedNumbers * 2) + 2);
		_inputedNumbers++;
	}
	
	void decreaseInputedCaptcha(String captcha)
	{
		if (_inputedNumbers < 1)
		{
			return;
		}
		_inputedCaptcha = _inputedCaptcha.substring(0, (_inputedNumbers - 1) * 2);
		for (int i = _inputedNumbers; i <= ErConfig.CAPTCHAS_LENGTH; i++)
		{
			_inputedCaptcha += "* ";
		}
		_inputedNumbers--;
	}
	
	String getInputedCaptcha()
	{
		return _inputedCaptcha;
	}
	
	int getInputedNumbers()
	{
		return _inputedNumbers;
	}
	
	void setInputedNumbers()
	{
		_inputedNumbers = 0;
	}
	
	void setHtmlNumbers(int[] numbers)
	{
		_numbers = numbers;
	}
	
	int[] getHtmlNumbers()
	{
		return _numbers;
	}
	
	void clearKilledValues()
	{
		_killsCount = 0;
		_lastKill = 0;
		_needKills = Rnd.get(ErConfig.CAPTCHA_MIN_MONSTERS_COUNT, ErConfig.CAPTCHA_MAX_MONSTERS_COUNT);
	}
	
	void startTimer()
	{
		_scheduler = ThreadPoolManager.getInstance().scheduleGeneral(new CaptchaTimer(ErConfig.CAPTCHA_DELAY / 1000), 1000);
	}
	
	void stopTimer()
	{
		_scheduler.cancel(true);
	}
	
	int getImageId()
	{
		return _imageId;
	}
	
	void setImageId(int img)
	{
		_imageId = img;
	}
	
	public class CaptchaTimer implements Runnable
	{
		int _leftTime;
		
		CaptchaTimer(int leftTime)
		{
			_leftTime = leftTime;
		}
		
		@Override
		public void run()
		{
			if (_leftTime < 1)
			{
				Captcha.getInstance().checkCaptcha(_player);
				return;
			}
			Captcha.getInstance().showScreenMessage(_player, _leftTime + "s remaining!", 600, ErSMPos.TOP_RIGHT, true);
			_leftTime--;
			_scheduler = ThreadPoolManager.getInstance().scheduleGeneral(new CaptchaTimer(_leftTime), 1000);
		}
	}
}
