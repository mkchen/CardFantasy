package cfvbaibai.cardfantasy.engine.skill;

import cfvbaibai.cardfantasy.GameUI;
import cfvbaibai.cardfantasy.Randomizer;
import cfvbaibai.cardfantasy.data.Skill;
import cfvbaibai.cardfantasy.data.SkillType;
import cfvbaibai.cardfantasy.engine.*;

import java.util.List;

public class ParadiseLost {
    public static void apply(SkillResolver resolver, SkillUseInfo skillUseInfo, CardInfo attackCard, CardInfo defenderCard) throws HeroDieSignal {

        GameUI ui = resolver.getStage().getUI();
        Skill skill = skillUseInfo.getSkill();
        ui.useSkill(defenderCard, attackCard, skill, true);
        CardStatusItem statusItem = CardStatusItem.paradiseLost(skillUseInfo);
        if(attackCard.isBoss()){
           return;
        }
        ui.addCardStatus(defenderCard, attackCard, skill, statusItem);
        attackCard.addStatus(statusItem);
        attackCard.getOwner().getField().removeCard(attackCard);
        resolver.resolveLeaveSkills(attackCard);
        ui.returnCard(defenderCard, attackCard, skill);
        if(attackCard.isSummonedMinion()) {
            return;
        }
        attackCard.getOriginalOwner().getOutField().addCard(attackCard);
    }

    public static void remove(SkillResolver resolver,CardInfo attackCard, Player defenderHero) throws HeroDieSignal {
        Boolean flag = false;
        SkillUseInfo skillUseInfo = null;
        for (SkillUseInfo deadCardSkillUseInfo : attackCard.getAllNormalSkills()) {
            if(deadCardSkillUseInfo.getType() == SkillType.失乐园){
                skillUseInfo = deadCardSkillUseInfo;
                flag = true;
            }
        }
        if(!flag){
            return;
        }
        StageInfo stage = resolver.getStage();
        Randomizer random = stage.getRandomizer();
        GameUI ui = resolver.getStage().getUI();
        List<CardInfo> victims = random.pickRandom(defenderHero.getOutField().getAllCards(), -1, true, null);
        for(CardInfo outCard:victims) {
            List<CardStatusItem> raptureStatusItems = outCard.getStatus().getStatusOf(CardStatusType.失乐);
            if(raptureStatusItems.size()>0) {
                for(CardStatusItem cardStatusItem:raptureStatusItems) {
                    if(cardStatusItem.getCause() == skillUseInfo) {
                        outCard.restoreOwner();
                        outCard.reset();
                        defenderHero.getOutField().removeCard(outCard);
                        outCard.getOwner().getDeck().addCard(outCard);
                        ui.cardToDeck(outCard.getOwner(), outCard);
                        break;
                    }
                }
            }
        }
    }

    public static void removeCard(SkillResolver resolver,CardInfo attackCard, Player defenderHero) {
        Boolean flag = false;
        SkillUseInfo skillUseInfo = null;
        for (SkillUseInfo deadCardSkillUseInfo : attackCard.getAllNormalSkills()) {
            if(deadCardSkillUseInfo.getType() == SkillType.失乐园){
                skillUseInfo = deadCardSkillUseInfo;
                flag = true;
            }
        }
        if(!flag){
            return;
        }
        StageInfo stage = resolver.getStage();
        Randomizer random = stage.getRandomizer();
        GameUI ui = resolver.getStage().getUI();
        List<CardInfo> victims = random.pickRandom(defenderHero.getOutField().getAllCards(), -1, true, null);
        for(CardInfo outCard:victims) {
            List<CardStatusItem> raptureStatusItems = outCard.getStatus().getStatusOf(CardStatusType.失乐);
            if(raptureStatusItems.size()>0) {
                for(CardStatusItem cardStatusItem:raptureStatusItems) {
                    if(cardStatusItem.getCause() == skillUseInfo) {
                        outCard.restoreOwner();
                        outCard.reset();
                        defenderHero.getOutField().removeCard(outCard);
                        outCard.getOwner().getDeck().addCard(outCard);
                        ui.cardToDeck(outCard.getOwner(), outCard);
                        break;
                    }
                }
            }
        }
    }
}
