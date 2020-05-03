package cfvbaibai.cardfantasy.engine.skill;

import cfvbaibai.cardfantasy.GameUI;
import cfvbaibai.cardfantasy.data.Skill;
import cfvbaibai.cardfantasy.data.SkillType;
import cfvbaibai.cardfantasy.engine.*;

import java.util.ArrayList;
import java.util.List;

public final class UnderworldTrio {
    
    public static void apply(SkillResolver resolver, CardInfo attacker,CardInfo defender) throws HeroDieSignal {
        if(defender.getStatus().containsStatus(CardStatusType.不屈)){
            return;
        }
        if(defender.isBoss()){
            return;
        }
        int position = defender.getPosition();
        Boolean flag = false;
        if(position>0){
            CardInfo preCardInfo = defender.getOwner().getField().getCard(position-1);
            CardInfo nextCardInfo = defender.getOwner().getField().getCard(position+1);
            if(preCardInfo==null&&nextCardInfo==null){
                flag = true;
            }
        }
        else{
            CardInfo nextCardInfo = defender.getOwner().getField().getCard(position+1);
            if(nextCardInfo==null){
                flag = true;
            }
        }
        if(!flag){
            return;
        }
        int type = 0;//0不发动1，2侧击3夹击
        boolean self = false;// 自身是冥界
        SkillUseInfo attackSkillUseInfo = null;
        CardInfo preCardInfo = null;
        if(position>0) {
            preCardInfo = defender.getOwner().getField().getCard(position - 1);
        }
        CardInfo nextCardInfo = defender.getOwner().getField().getCard(position+1);
        for(SkillUseInfo skillUseInfo:attacker.getUsableNormalSkills()){
            if(skillUseInfo.getType() == SkillType.冥界三重奏 || skillUseInfo.getType() == SkillType.云雾缭绕 || skillUseInfo.getType() == SkillType.龙虎棍法){
                attackSkillUseInfo = skillUseInfo;
                type++;
                self = true;
                break;
            }
        }
        if(preCardInfo!=null) {
            for (SkillUseInfo skillUseInfo : preCardInfo.getUsableNormalSkills()) {
                if (skillUseInfo.getType() == SkillType.冥界三重奏 || skillUseInfo.getType() == SkillType.云雾缭绕 || skillUseInfo.getType() == SkillType.龙虎棍法) {
                    attackSkillUseInfo = skillUseInfo;
                    type++;
                    break;
                }
            }
        }
        if(nextCardInfo!=null) {
            for (SkillUseInfo skillUseInfo : nextCardInfo.getUsableNormalSkills()) {
                if (skillUseInfo.getType() == SkillType.冥界三重奏 || skillUseInfo.getType() == SkillType.云雾缭绕 || skillUseInfo.getType() == SkillType.龙虎棍法) {
                    attackSkillUseInfo = skillUseInfo;
                    type++;
                    break;
                }
            }
        }
        if(type>0){
            GameUI ui = resolver.getStage().getUI();
            //大于等于二
            if(self&&type>=2){
                ui.killCard(attacker, defender, attackSkillUseInfo.getAttachedUseInfo2().getSkill());
                int reallyRate = resolver.getStage().getRandomizer().next(0,100);
                int rate = 80;
                if(attackSkillUseInfo.getType() == SkillType.龙虎棍法){
                    rate = 85;
                }
                ui.roll100(reallyRate, rate);
                if(reallyRate<=rate) {
                    resolver.killCard(attacker, defender, attackSkillUseInfo.getAttachedUseInfo2().getSkill());
                }else{
                    resolver.killCard(attacker, defender, attackSkillUseInfo.getAttachedUseInfo1().getSkill());
                }
            }else{
                ui.killCard(attacker, defender, attackSkillUseInfo.getSkill());
                resolver.killCard(attacker, defender, attackSkillUseInfo.getAttachedUseInfo1().getSkill());
            }
        }

    }
}
