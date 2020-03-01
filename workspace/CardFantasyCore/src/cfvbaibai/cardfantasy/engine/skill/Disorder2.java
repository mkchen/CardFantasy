package cfvbaibai.cardfantasy.engine.skill;

import cfvbaibai.cardfantasy.Randomizer;
import cfvbaibai.cardfantasy.data.Skill;
import cfvbaibai.cardfantasy.engine.*;

import java.util.ArrayList;
import java.util.List;

public final class Disorder2 {
    public static void apply(SkillUseInfo skillUseInfo, SkillResolver resolver, CardInfo card,Player defenderHero) throws HeroDieSignal {
        if (card.hasUsed(skillUseInfo)) {
            return;
        }
        card.setUsed(skillUseInfo);
        Skill skill = skillUseInfo.getSkill();
        //Player player = card.getOwner();
        StageInfo stage = resolver.getStage();
        Randomizer random = stage.getRandomizer();
        List<CardInfo> extraCard =new ArrayList<>();
        extraCard.add(card);
        for(CardInfo cardInfo:defenderHero.getField().getAliveCards()){
            if(cardInfo.isDeman() || cardInfo.isBoss()){
                extraCard.add(cardInfo);
            }else if(cardInfo.getStatus().containsStatus(CardStatusType.不屈)){
                extraCard.add(cardInfo);
            }
        }
        List<CardInfo> cardInfoList = defenderHero.getField().getAliveCards();
        //cardInfoList.addAll(defenderHero.getField().getAliveCards());
        List<CardInfo> effectCardList = random.pickRandom(cardInfoList, 1, true, extraCard);
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
}
