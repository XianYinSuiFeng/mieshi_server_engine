package com.fy.engineserver.datasource.buff;

import com.fy.engineserver.datasource.language.Translate;

public class BuffTemplate_ZengShu extends BuffTemplate{

	protected int speed[];
	
	public int[] getSpeed() {
		return speed;
	}

	public void setSpeed(int[] speed) {
		this.speed = speed;
	}

	public BuffTemplate_ZengShu(){
		setName(Translate.text_3317);
		setDescription(Translate.text_3387);
		speed = new int[]{1,3,5,7,9,11,13,15,17,19};
	}
	
	public Buff createBuff(int level) {
		Buff_ZhengShu buff = new Buff_ZhengShu();
		buff.setTemplate(this);
		buff.setGroupId(this.getGroupId()); 
		buff.setTemplateName(this.getName());
		buff.setLevel(level);
		buff.setAdvantageous(isAdvantageous());
		buff.setFightStop(this.isFightStop());
		buff.setCanUseType(this.getCanUseType());
		buff.setSyncWithClient(this.isSyncWithClient());
		buff.setIconId(iconId);
		if(speed != null && speed.length > level){
			if(speed[level] > 0)
				buff.setDescription(Translate.text_3210+speed[level]+"%");
			else if(speed[level] < 0)
				buff.setDescription(Translate.text_3211+(-speed[level])+"%");
		}else{
			buff.setDescription(Translate.text_3212);
		}
		return buff;
	}

}
