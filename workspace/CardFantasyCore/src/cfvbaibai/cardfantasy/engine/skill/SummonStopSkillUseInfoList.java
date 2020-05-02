package cfvbaibai.cardfantasy.engine.skill;

import cfvbaibai.cardfantasy.CardFantasyRuntimeException;
import cfvbaibai.cardfantasy.GameUI;
import cfvbaibai.cardfantasy.engine.*;

import java.util.ArrayList;
import java.util.List;

public final class SummonStopSkillUseInfoList {
    public static void apply(SkillResolver resolver, SkillUseInfo skillUseInfo, EntityInfo attacker) {

        Player player = attacker.getOwner();
        resolver.getStage().getUI().useSkill(attacker, skillUseInfo.getSkill(), true);
        int impact = skillUseInfo.getSkillNumber();
        skillUseInfo.setSkillNumber(impact);
        List<SkillUseInfo> skillUseInfoList = attacker.getOwner().getSummonStopSkillUseInfoList();
        for(SkillUseInfo existSkillUserInfo:skillUseInfoList){
            if(existSkillUserInfo == skillUseInfo){
                return;
            }
        }
        player.addSummonStopSkillUseInfoList(skillUseInfo);
    }

    public static boolean explode(SkillResolver resolver, EntityInfo attacker, Player defender) throws HeroDieSignal {

        boolean flag = false;

        if (!(attacker instanceof CardInfo)) {
            return flag;
        }

        List<SkillUseInfo> skillUseInfoList = defender.getSummonStopSkillUseInfoList();
        StageInfo stage = resolver.getStage();
        GameUI ui = stage.getUI();
        int number =0;
        SkillUseInfo useSkillUseInfo = null;
        List<SkillUseInfo> deleteSkillUseInfoList = new ArrayList<>();
        for(SkillUseInfo skillUseInfo:skillUseInfoList){
            CardInfo cardInfo = (CardInfo) skillUseInfo.getOwner();
            if(cardInfo.isDead()){
                deleteSkillUseInfoList.add(skillUseInfo);
                continue;
            }
            ui.useSkill(skillUseInfo.getOwner(), attacker, skillUseInfo.getSkill(), true);
            number = skillUseInfo.getSkillNumber();
            skillUseInfo.setSkillNumber(number-1);
            useSkillUseInfo = skillUseInfo;
            flag = true;
            break;
        }
        if(number<=1){
            defender.removeSummonStopSkillUseInfoList(useSkillUseInfo);
        }
        for(SkillUseInfo skillUseInfo:deleteSkillUseInfoList){
            defender.removeSummonStopSkillUseInfoList(skillUseInfo);
        }
        return flag;
    }

    public static boolean explodeEquipment(SkillResolver resolver, EntityInfo attacker, Player defender) throws HeroDieSignal {

        boolean flag = false;

        List<SkillUseInfo> skillUseInfoList = defender.getSummonStopSkillUseInfoList();
        StageInfo stage = resolver.getStage();
        GameUI ui = stage.getUI();
        int number =0;
        SkillUseInfo useSkillUseInfo = null;
        for(SkillUseInfo skillUseInfo:skillUseInfoList){
            CardInfo cardInfo = (CardInfo) skillUseInfo.getOwner();
            if(cardInfo.isDead()){
                continue;
            }
            ui.useSkill(skillUseInfo.getOwner(), attacker, skillUseInfo.getSkill(), true);
            number = skillUseInfo.getSkillNumber();
            skillUseInfo.setSkillNumber(number-1);
            useSkillUseInfo = skillUseInfo;
            flag = true;
            break;
        }
        if(number<=1){
            defender.removeSummonStopSkillUseInfoList(useSkillUseInfo);
        }
        return flag;
    }

    public static boolean exploded(SkillResolver resolver, EntityInfo attacker, Player defender){

        boolean flag = false;

        if (!(attacker instanceof CardInfo)) {
            return flag;
        }

        List<SkillUseInfo> skillUseInfoList = defender.getSummonStopSkillUseInfoList();
        StageInfo stage = resolver.getStage();
        GameUI ui = stage.getUI();
        int number =0;
        SkillUseInfo useSkillUseInfo = null;
        for(SkillUseInfo skillUseInfo:skillUseInfoList){
            ui.useSkill(skillUseInfo.getOwner(), attacker, skillUseInfo.getSkill(), true);
            number = skillUseInfo.getSkillNumber();
            skillUseInfo.setSkillNumber(number-1);
            useSkillUseInfo = skillUseInfo;
            flag = true;
            break;
        }
        if(number<=1){
            defender.removeSummonStopSkillUseInfoList(useSkillUseInfo);
        }
        return flag;
    }
}
