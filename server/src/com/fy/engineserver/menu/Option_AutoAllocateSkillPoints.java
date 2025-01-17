/**
 * 
 */
package com.fy.engineserver.menu;

import com.fy.engineserver.core.Game;
import com.fy.engineserver.datasource.language.Translate;
import com.fy.engineserver.sprite.Player;

/**
 * @author Administrator
 *
 */
public class Option_AutoAllocateSkillPoints extends Option {

	/**
	 * 
	 */
	public Option_AutoAllocateSkillPoints() {
		// TODO Auto-generated constructor stub
	}
	
	@Override
	public void doSelect(Game game, Player player) {
		// TODO Auto-generated method stub
		player.autoAllocateSkillPoints();
	}
	
	public byte getOType() {
		return Option.OPTION_TYPE_SERVER_FUNCTION;
	}

	public void setOType(byte type) {
		//oType = type;
	}

	public int getOId() {
		return OptionConstants.SERVER_FUNCTION_AUTO_ALLOCATE_SKILL_POINTS;
	}

	public void setOId(int oid) {
	}
	
	public String toString(){
		return Translate.text_4911;
	}

}
