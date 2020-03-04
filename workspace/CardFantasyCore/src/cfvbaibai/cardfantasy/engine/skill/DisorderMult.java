package cfvbaibai.cardfantasy.engine.skill;

import cfvbaibai.cardfantasy.Randomizer;
import cfvbaibai.cardfantasy.data.Skill;
import cfvbaibai.cardfantasy.engine.*;

import java.util.ArrayList;
import java.util.List;

public final class DisorderMult {
    public static void apply(SkillUseInfo skillUseInfo, SkillResolver resolver, CardInfo card,Player defenderHero,int killCount,int type) throws HeroDieSignal {
        int impact2 = skillUseInfo.getSkill().getImpact2();
        int number = skillUseInfo.getSkillNumber();
        if(number==0)
        {
            return;
        }
        if(number<0)
        {
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

            }else if(cardInfo.isDeman() || cardInfo.isBoss()){
                extraCard.add(cardInfo);
            }else if(cardInfo.getStatus().containsStatus(CardStatusType.不屈)){
                extraCard.add(cardInfo);
            }
        }
        for(CardInfo cardInfo:defenderHero.getField().getAliveCards()){
            if(cardInfo.isDeman() || cardInfo.isBoss()){
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
            return;
        }
        for(CardInfo cardInfo:effectCardList){
            resolver.killCard(card,cardInfo,skill);//杀死卡牌
        }
        int healHP = skill.getImpact();
        if (healHP + card.getHP() > card.getMaxHP()) {
            healHP = card.getMaxHP() - card.getHP();
        }
        if (healHP == 0) {
            return;
        }
        OnAttackBlockingResult result = resolver.resolveHealBlockingSkills(card, card, skill);
        if (!result.isAttackable()) {
            return;
        }
        resolver.getStage().getUI().useSkill(card, skill, true);
        resolver.getStage().getUI().healCard(card, card, skill, healHP);
        resolver.applyDamage(card, card, skill, -healHP);

    }

    public static void reset( SkillUseInfo skillUseInfo, CardInfo card) throws HeroDieSignal {
        int impact = skillUseInfo.getSkill().getImpact();
        skillUseInfo.setSkillNumber(impact);
    }
}
