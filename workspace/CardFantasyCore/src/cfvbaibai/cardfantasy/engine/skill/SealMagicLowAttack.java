package cfvbaibai.cardfantasy.engine.skill;

import cfvbaibai.cardfantasy.GameUI;
import cfvbaibai.cardfantasy.Randomizer;
import cfvbaibai.cardfantasy.data.Skill;
import cfvbaibai.cardfantasy.engine.*;

import java.util.ArrayList;
import java.util.List;

public class SealMagicLowAttack {
    public static void apply(SkillUseInfo skillUseInfo, SkillResolver resolver, CardInfo attacker, Player defender, int number) throws HeroDieSignal {
        StageInfo stage = resolver.getStage();
        GameUI ui = stage.getUI();
        Skill skill = skillUseInfo.getSkill();
        List<CardInfo> victims = new ArrayList<>();
        List<CardInfo> defendFieldList =  defender.getField().getAliveCards();
        CardInfo cardInfo = null;
        for(CardInfo fieldCardInfo: defendFieldList){
            if(cardInfo == null)
            {
                cardInfo = fieldCardInfo;
            }else if(fieldCardInfo.getCurrentAT() > cardInfo.getCurrentAT()){
                cardInfo = fieldCardInfo;
            }
        }
        if(cardInfo !=null){
            victims.add(cardInfo);
        }
        for (CardInfo card : victims) {
            ui.useSkill(attacker, card, skill, true);
            if (card.getCurrentAT() > attacker.getCurrentAT()) {
                int adjAT = skill.getImpact();
                if(adjAT>0) {
                    resolver.getStage().getUI().adjustAT(attacker, card, adjAT, skill);
                    card.addEffect(new SkillEffect(SkillEffectType.ATTACK_CHANGE, skillUseInfo, adjAT, true));
                }
            } else if (card.getCurrentAT() < attacker.getCurrentAT()) {
                int adjAT = skill.getImpact();
                if(adjAT>0) {
                    resolver.getStage().getUI().adjustAT(attacker, attacker, adjAT, skill);
                    attacker.addEffect(new SkillEffect(SkillEffectType.ATTACK_CHANGE, skillUseInfo, adjAT, true));
                }
                ui.killCard(attacker, card, skill);
                card.removeStatus(CardStatusType.不屈);
                resolver.killCard(attacker, card, skill);
            }
        }
    }
}
