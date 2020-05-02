package cfvbaibai.cardfantasy.engine.skill;

import cfvbaibai.cardfantasy.CardFantasyRuntimeException;
import cfvbaibai.cardfantasy.Randomizer;
import cfvbaibai.cardfantasy.data.Skill;
import cfvbaibai.cardfantasy.engine.*;
import cfvbaibai.cardfantasy.game.DeckBuilder;

import java.util.ArrayList;
import java.util.List;

public class SummonMultipleBroilingSoul {
    public static void apply(SkillResolver resolver, SkillUseInfo skillUseInfo, CardInfo summoner, int summonPicks, String... summonedCardsDescs) throws HeroDieSignal {

        List<CardInfo> cardsToSummon = new ArrayList<CardInfo>();
        int soulNumber = skillUseInfo.getSkillNumber();
        int impact2 = skillUseInfo.getSkill().getImpact2();
        if(soulNumber == -1){
            soulNumber = 1;
            skillUseInfo.setSkillNumber(soulNumber);
        }else if(soulNumber<impact2){
            soulNumber++;
            skillUseInfo.setSkillNumber(soulNumber);
        }
//
        int summonNumber=0;
        Player attacker = summoner.getOwner();
        Skill skill = skillUseInfo.getSkill();
        List<CardInfo> livingCards = attacker.getField().getAliveCards();
        for (CardInfo fieldCard : livingCards) {
            if(summoner.isSummonedMinion()) {
                for (CardStatusItem item : fieldCard.getStatus().getAllItems()) {
                    if (item.getType() == CardStatusType.召唤) {
                        if(item.getCause().getType()==skillUseInfo.getType()) {
                            summonNumber++;
                            break;
                        }
                    }
                }
            }
        }
        if(summonNumber>=summonPicks) {
            return;
        }else{
            soulNumber = summonPicks - summonNumber;
        }
        for(int soul=0; soul< soulNumber;soul++) {
            List<CardInfo> summonCardCandidates = null;
            summonCardCandidates = DeckBuilder.build(summonedCardsDescs).getCardInfos(attacker);
            cardsToSummon = Randomizer.getRandomizer().pickRandom(summonCardCandidates, 1, true, null);
            for (int i = 0; i < cardsToSummon.size(); ++i) {
                CardInfo summonedCard = cardsToSummon.get(i);
                CardStatusItem summonedStatusItem = CardStatusItem.summoned(skillUseInfo);
                resolver.getStage().getUI().addCardStatus(summoner, summonedCard, skill, summonedStatusItem);
                summonedCard.addStatus(summonedStatusItem);
                CardStatusItem weakStatusItem = CardStatusItem.weak(skillUseInfo);
                resolver.getStage().getUI().addCardStatus(summoner, summonedCard, skill, weakStatusItem);
                summonedCard.addStatus(weakStatusItem);
//                summonedCard.setRelationCardInfo(summoner);
                resolver.summonCard(summonedCard.getOwner(), summonedCard, summoner, true, skill, 1);

            }
        }
    }

    public static void reset( SkillUseInfo skillUseInfo, CardInfo card) throws HeroDieSignal {
        skillUseInfo.setSkillNumber(0);
    }
}

