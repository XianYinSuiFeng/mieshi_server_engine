package com.fy.engineserver.menu.jiazu;

import com.fy.engineserver.core.Game;
import com.fy.engineserver.datasource.language.Translate;
import com.fy.engineserver.menu.Option;
import com.fy.engineserver.message.GameMessageFactory;
import com.fy.engineserver.message.JIAZU_SHOW_JIAZU_FUNCTION_RES;
import com.fy.engineserver.sprite.Player;

/**
 * 家族工资
 * 
 * 
 */
public class Option_Jiazu_Salary extends Option {

	public Option_Jiazu_Salary() {
		// TODO Auto-generated constructor stub
	}

	@Override
	public void doSelect(Game game, Player player) {
		if (player.getJiazuId() <= 0) {
			player.sendError(Translate.你没有家族);
			return;
		}
		JIAZU_SHOW_JIAZU_FUNCTION_RES res = new JIAZU_SHOW_JIAZU_FUNCTION_RES(GameMessageFactory.nextSequnceNum(), (byte) 3);
		player.addMessageToRightBag(res);
	}
}
