package cfvbaibai.cardfantasy.engine.skill;

import cfvbaibai.cardfantasy.Randomizer;
import cfvbaibai.cardfantasy.data.Skill;
import cfvbaibai.cardfantasy.engine.*;

import java.util.ArrayList;
import java.util.List;

public final class Scapegoat {
    public static DeadType apply(SkillUseInfo skillUseInfo, SkillResolver resolver, CardInfo card, EntityInfo attacker, Skill killSkill) throws HeroDieSignal {

        DeadType deadType = resolver.cardDeadBeforeScapegoat(attacker,killSkill,card);
        if(deadType != DeadType.Normal){
            return  deadType;
        }

        int number = skillUseInfo.getSkillNumber();
        if(number==0) {
            card.restoreOwner();
            card.getOwner().getBeforeDeath().addCard(card);
            return deadType;
        }
        if(number<0) {
            number = 1;
            skillUseInfo.setSkillNumber(1);
        }
        skillUseInfo.setSkillNumber(number-1);
        Skill skill = skillUseInfo.getSkill();
        Player player = card.getOwner();
        StageInfo stage = resolver.getStage();
        Randomizer random = stage.getRandomizer();
        List<CardInfo> extraCard =new ArrayList<>();
        extraCard.add(card);
        for(CardInfo cardInfo:player.getField().getAliveCards()){
            if(cardInfo == card){

            }else if(cardInfo.isDemon() || cardInfo.isBoss()){
                extraCard.add(cardInfo);
            }else if(cardInfo.getStatus().containsStatus(CardStatusType.不屈)){
                extraCard.add(cardInfo);
            }
        }
        List<CardInfo> defenderList = random.pickRandom(player.getField().getAliveCards(), 1, true, extraCard);
        if(defenderList.size()==0){
            card.restoreOwner();
            card.getOwner().getBeforeDeath().addCard(card);
            return deadType;
        }
        int healHP = skill.getImpact();
        if (healHP + card.getHP() > card.getMaxHP()) {
            healHP = card.getMaxHP() - card.getHP();
        }
        if(healHP>0) {
            resolver.getStage().getUI().useSkill(card, skill, true);
            resolver.getStage().getUI().healCard(card, card, skill, healHP);
            resolver.applyDamage(card, card, skill, -healHP);
        }
        for(CardInfo cardInfo:defenderList){
            resolver.getStage().getUI().useSkill(card, cardInfo,skill, true);
            resolver.getStage().getUI().killCard(card, cardInfo,skill);
            resolver.killCard(card,cardInfo,skill);//杀死己方卡牌
        }
        return deadType;

    }
}
