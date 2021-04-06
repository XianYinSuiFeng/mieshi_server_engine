package com.fy.engineserver.datasource.buff;

import com.fy.engineserver.articleEnchant.ControlBuff;
import com.fy.engineserver.articleEnchant.EnchantEntityManager;
import com.fy.engineserver.core.Game;
import com.fy.engineserver.datasource.skill.master.SkEnhanceManager;
import com.fy.engineserver.datasource.skill.passivetrigger.PassiveTriggerImmune;
import com.fy.engineserver.sprite.Fighter;
import com.fy.engineserver.sprite.Player;
import com.fy.engineserver.sprite.Sprite;
import com.fy.engineserver.sprite.pet.Pet;
import com.xuanzhi.tools.simplejpa.annotation.SimpleEmbeddable;

/**
 * 定身
 *
 */
@SimpleEmbeddable
public class Buff_DingSheng extends Buff implements ControlBuff{

	/**
	 * 通知此buff，生命开始，此方法调用后，才会调用心跳函数
	 */
	public void start(Fighter owner){
		if(owner instanceof Player){
			Player p = (Player)owner;
			if(p.isImmunity()){
				this.setInvalidTime(0);
			} else if (p.getImmuType() == PassiveTriggerImmune.免疫定身 || p.getImmuType() == PassiveTriggerImmune.免疫控制) {
				this.setInvalidTime(0);
			} else{
					long decreaseTime = (this.getInvalidTime() - this.getStartTime()) * p.decreaseConTimeRate / 1000;
					this.setInvalidTime(this.getInvalidTime() - decreaseTime);
					EnchantEntityManager.instance.notifyCheckEnchant(p);
//				if(p.getHorse() > 0){
//					p.getDownFromHorse();
//				}
				/*if (p.checkPassiveEnchant(EnchantEntityManager.受到控制类技能) == 100) {
					this.setInvalidTime(0);
				} else {
					p.setHold(true);
				}*/
				p.setHold(true);
				SkEnhanceManager.getInst().addSkillBuff(p);
			}
		}else if(owner instanceof Sprite){
			Sprite s = (Sprite)owner;
			if(s.isImmunity()){
				this.setInvalidTime(0);
			}else{
				boolean b = true;
				if (owner instanceof Pet) {
					Pet p = (Pet) owner;
					if (p.getImmuType() == PassiveTriggerImmune.免疫定身) {
						b = false;
						this.setInvalidTime(0);
					}
				}
				if (b) {
					s.setHold(true);
				}
			}
		}
	}
	
	
	/**
	 * 通知此buff，生命结束，此方法调用后，就不会再调用心跳函数了
	 */
	public void end(Fighter owner){
		if(owner instanceof Player){
			Player p = (Player)owner;
			p.setHold(false);
		}else if(owner instanceof Sprite){
			Sprite s = (Sprite)owner;
			s.setHold(false);
		}
	}
	
	/**
	 * 心跳函数
	 */
	public void heartbeat(Fighter owner,long heartBeatStartTime, long interval, Game game){
		super.heartbeat(owner, heartBeatStartTime, interval, game);
		
		if(owner instanceof Player){
			Player p = (Player)owner;
			if(p.isImmunity()){
				this.setInvalidTime(0);//立即失效
			} else if (p.getImmuType() == PassiveTriggerImmune.免疫定身 || p.getImmuType() == PassiveTriggerImmune.免疫控制) {
				this.setInvalidTime(0);
			} else{
//				if(p.getHorse() > 0){
//					p.getDownFromHorse();
//				}
			}
		}else if(owner instanceof Sprite){
			Sprite s = (Sprite)owner;
			if(s.isImmunity()){
				this.setInvalidTime(0);//立即失效
			} else if (owner instanceof Pet) {
				Pet p = (Pet) owner;
				if (p.getImmuType() == PassiveTriggerImmune.免疫定身) {
					this.setInvalidTime(0);
				}
			}
		}
	}

}