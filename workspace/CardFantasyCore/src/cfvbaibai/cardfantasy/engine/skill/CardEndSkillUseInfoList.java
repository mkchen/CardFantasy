package cfvbaibai.cardfantasy.engine.skill;

import cfvbaibai.cardfantasy.CardFantasyRuntimeException;
import cfvbaibai.cardfantasy.GameUI;
import cfvbaibai.cardfantasy.data.RuneData;
import cfvbaibai.cardfantasy.data.SkillType;
import cfvbaibai.cardfantasy.engine.*;

import java.util.ArrayList;
import java.util.List;

public final class CardEndSkillUseInfoList {
    public static void apply(SkillResolver resolver, SkillUseInfo skillUseInfo, CardInfo attacker) {
        Player player = attacker.getOwner();
        resolver.getStage().getUI().useSkill(attacker, skillUseInfo.getSkill(), true);
        int impact = skillUseInfo.getSkillNumber();
        skillUseInfo.setSkillNumber(impact);
        List<SkillUseInfo> skillUseInfoList = attacker.getOwner().getCardEndSkillUseInfoList();
        for(SkillUseInfo existSkillUserInfo:skillUseInfoList){
            if(existSkillUserInfo == skillUseInfo){
                return;
            }
        }
        player.addCardEndSkillUseInfoList(skillUseInfo);
    }

    public static void explode(SkillResolver resolver, CardInfo attacker, Player defender) throws HeroDieSignal {

        List<SkillUseInfo> skillUseInfoList = defender.getCardEndSkillUseInfoList();
        StageInfo stage = resolver.getStage();
        GameUI ui = stage.getUI();

        List<SkillUseInfo> deleteSkillUseInfoList = new ArrayList<>();
        for(SkillUseInfo skillUseInfo:skillUseInfoList){
            CardInfo cardInfo = (CardInfo) skillUseInfo.getOwner();
            if(cardInfo.isDead()){
                deleteSkillUseInfoList.add(skillUseInfo);
                continue;
            }
            if (!FailureSkillUseInfoList.exploded(resolver, cardInfo, attacker.getOwner())) {
                continue;
            }
            int impact = skillUseInfo.getSkill().getImpact();
            if (resolver.getStage().getRandomizer().roll100(impact)) {
                Ancient.apply(resolver, skillUseInfo, attacker, defender, 2, 1);
            }
        }
        for(SkillUseInfo deleteSkillUseInfo:deleteSkillUseInfoList){
            defender.removeCardEndSkillUseInfoList(deleteSkillUseInfo);
        }
    }

    public static void remove(EntityInfo attacker, SkillResolver resolver) {

        if (attacker == null) {
            throw new CardFantasyRuntimeException("card cannot be null");
        }
        List<SkillUseInfo> skillUseInfoList = attacker.getOwner().getCardEndSkillUseInfoList();

        if(skillUseInfoList.size() == 0) {
            return;
        }

        List<SkillUseInfo> removeSkillUserInfoList = new ArrayList<>();

        for(SkillUseInfo skillUseInfo:skillUseInfoList) {
            if(skillUseInfo.getOwner() == attacker) {
                removeSkillUserInfoList.add(skillUseInfo);
            }
        }
        for(SkillUseInfo skillUseInfo:removeSkillUserInfoList) {
            attacker.getOwner().removeCardEndSkillUseInfoList(skillUseInfo);
        }
    }
}
