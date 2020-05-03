package cfvbaibai.cardfantasy.engine.skill;

import cfvbaibai.cardfantasy.Randomizer;
import cfvbaibai.cardfantasy.data.Skill;
import cfvbaibai.cardfantasy.engine.*;

import java.util.ArrayList;
import java.util.List;

public final class DisorderMult {
    public static DeadType apply(SkillUseInfo skillUseInfo, SkillResolver resolver, CardInfo card,Player defenderHero,int killCount,int type,int health, EntityInfo attacker, Skill killSkill) throws HeroDieSignal {
        DeadType deadType = resolver.cardDeadBeforeScapegoat(attacker,killSkill,card);
        if(deadType != DeadType.Normal){
            return  deadType;
        }

        int impact2 = skillUseInfo.getSkill().getImpact2();
        int number = skillUseInfo.getSkillNumber();
        if(number==0) {
            card.restoreOwner();
            card.getOwner().getBeforeDeath().addCard(card);
            return deadType;
        }
        if(number<0) {
            number = impact2;
            skillUseInfo.setSkillNumber(impact2);
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
        for(CardInfo cardInfo:defenderHero.getField().getAliveCards()){
            if(cardInfo.isDemon() || cardInfo.isBoss()){
                extraCard.add(cardInfo);
            }else if(cardInfo.getStatus().containsStatus(CardStatusType.不屈)){
                extraCard.add(cardInfo);
            }
        }
        List<CardInfo> cardInfoList = defenderHero.getField().getAliveCards();
        if(type == 0) {
            cardInfoList.addAll(player.getField().getAliveCards());
        }
        List<CardInfo> effectCardList = random.pickRandom(cardInfoList, killCount, true, extraCard);
        if(effectCardList.size()==0){
            card.restoreOwner();
            card.getOwner().getBeforeDeath().addCard(card);
            return deadType;
        }

        int healHP = skill.getImpact();
        if (healHP + card.getHP() > card.getMaxHP()) {
            healHP = card.getMaxHP() - card.getHP();
        }
        if(healHP>0) {
            if (healHP > health) {
                healHP = health;
            }
            resolver.getStage().getUI().useSkill(card, skill, true);
            resolver.getStage().getUI().healCard(card, card, skill, healHP);
            resolver.applyDamage(card, card, skill, -healHP);
        }

        for(CardInfo cardInfo:effectCardList){
            resolver.getStage().getUI().useSkill(card,cardInfo,skill,true);
            resolver.getStage().getUI().killCard(card, cardInfo,skill);
            resolver.killCard(card,cardInfo,skill);//杀死卡牌
        }
        return deadType;

    }

    public static void reset( SkillUseInfo skillUseInfo, CardInfo card) throws HeroDieSignal {
        int impact = skillUseInfo.getSkill().getImpact();
        skillUseInfo.setSkillNumber(impact);
    }
}
