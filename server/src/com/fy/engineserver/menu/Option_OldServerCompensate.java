/**
 * 
 */
package com.fy.engineserver.menu;

import com.fy.engineserver.core.Game;
import com.fy.engineserver.datasource.language.Translate;
import com.fy.engineserver.operating.OldServerCompensateManager;
import com.fy.engineserver.sprite.Player;

/**
 * @author Administrator
 *
 */
public class Option_OldServerCompensate extends Option {

	/**
	 * 
	 */
	public Option_OldServerCompensate() {
		// TODO Auto-generated constructor stub
	}
	
	@Override
	public void doSelect(Game game, Player player) {
		// TODO Auto-generated method stub
		OldServerCompensateManager.getInstance().takeOldServerCompensate(player);
	}
	
	public byte getOType() {
		return Option.OPTION_TYPE_SERVER_FUNCTION;
	}

	public void setOType(byte type) {
		//oType = type;
	}

	public int getOId() {
		return OptionConstants.SERVER_FUNCTION_OLD_SERVER_COMPENSATE;
	}

	public void setOId(int oid) {
	}
	
	public String toString(){
		return Translate.text_5349 ;
	}

}