package cfvbaibai.cardfantasy.engine.skill;

import cfvbaibai.cardfantasy.CardFantasyRuntimeException;
import cfvbaibai.cardfantasy.Randomizer;
import cfvbaibai.cardfantasy.data.CardSkill;
import cfvbaibai.cardfantasy.data.Skill;
import cfvbaibai.cardfantasy.engine.*;
import cfvbaibai.cardfantasy.game.DeckBuilder;

import java.util.ArrayList;
import java.util.List;

public class SummonMultipleExtraSkill {
    public static void apply(SkillResolver resolver, SkillUseInfo skillUseInfo, CardInfo summoner, int summonPicks,Skill addSkill, String... summonedCardsDescs) throws HeroDieSignal {
        if (summoner == null) {
            throw new CardFantasyRuntimeException("summoner should not be null");
        }
        List<CardInfo> cardsToSummon = new ArrayList<CardInfo>();
        List<CardInfo> summonCardCandidates = null;
        int summonNumber=0;
        Player attacker = summoner.getOwner();
        Skill skill = skillUseInfo.getSkill();
        List<CardInfo> livingCards = attacker.getField().getAliveCards();
        summonCardCandidates = DeckBuilder.build(summonedCardsDescs).getCardInfos(attacker);
        for (CardInfo fieldCard : livingCards) {
            if (fieldCard.getStatus().containsStatusCausedBy(skillUseInfo, CardStatusType.召唤)) {
                if(fieldCard.getRelationCardInfo()==summoner)
                {
                    summonNumber++;
                }
            }
        }
        if(summonNumber>=summonPicks) {
            return;
        }
        CardSkill cardSkill = null;
        if(addSkill!=null) {
            Boolean summonSkill = false;
            Boolean preSkill = false;
            Boolean deathSkill = false;
            Boolean postSkill = false;
            if (addSkill.isPostcastSkill()) {
                postSkill = true;
            } else if (addSkill.isDeathSkill()) {
                deathSkill = true;
            } else if (addSkill.isPrecastSkill()) {
                preSkill = true;
            } else if (addSkill.isSummonSkill()) {
                summonSkill = true;
            }
            cardSkill = new CardSkill(addSkill.getType(), addSkill.getLevel(), 0, summonSkill, deathSkill, preSkill, postSkill);
        }

        cardsToSummon = Randomizer.getRandomizer().pickRandom(summonCardCandidates, 1, true, null);
        for (int i = 0; i < cardsToSummon.size(); ++i) {
            CardInfo summonedCard = cardsToSummon.get(i);
            if(cardSkill !=null) {
                summonedCard.setExtraSkill(cardSkill);
                SkillUseInfo thisSkillUserInfo = new SkillUseInfo(summonedCard, cardSkill);
                summonedCard.addSkill(thisSkillUserInfo);
            }
            CardStatusItem summonedStatusItem = CardStatusItem.summoned(skillUseInfo);
            resolver.getStage().getUI().addCardStatus(summoner, summonedCard, skill, summonedStatusItem);
            summonedCard.addStatus(summonedStatusItem);
            CardStatusItem weakStatusItem = CardStatusItem.weak(skillUseInfo);
            resolver.getStage().getUI().addCardStatus(summoner, summonedCard, skill, weakStatusItem);
            summonedCard.addStatus(weakStatusItem);
            summonedCard.setRelationCardInfo(summoner);
            resolver.summonCard(summonedCard.getOwner(), summonedCard, summoner, true, skill,1);
        }
    }
}
