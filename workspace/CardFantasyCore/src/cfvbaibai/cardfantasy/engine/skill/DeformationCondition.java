package cfvbaibai.cardfantasy.engine.skill;

import cfvbaibai.cardfantasy.CardFantasyRuntimeException;
import cfvbaibai.cardfantasy.data.CardSkill;
import cfvbaibai.cardfantasy.data.Skill;
import cfvbaibai.cardfantasy.engine.*;
import cfvbaibai.cardfantasy.game.DeckBuilder;

import java.util.List;

public class DeformationCondition {
    public static void apply(SkillResolver resolver, SkillUseInfo skillUseInfo, CardInfo summoner) throws HeroDieSignal {
        if (summoner == null) {
            throw new CardFantasyRuntimeException("summoner should not be null");
        }
        Player player =  summoner.getOwner();
        List<CardInfo> livingCards = summoner.getOwner().getField().getAliveCards();
        int judgeNumber = 0;

        for(CardInfo liveCard:livingCards){
            if("天".equals(liveCard.getName())){
                judgeNumber = 1;
                if("天".equals(liveCard.getName())){
                    judgeNumber = 0;
                }
                break;
            } else if("地".equals(liveCard.getName())){
                judgeNumber = 10;
                if("地".equals(liveCard.getName())){
                    judgeNumber = 0;
                }
                break;
            } else if("人".equals(liveCard.getName())){
                judgeNumber = 100;
                if("人".equals(liveCard.getName())){
                    judgeNumber = 0;
                }
                break;
            }
        }
        if(judgeNumber == 0){
            return;
        }
        String cardName = "";
        switch (judgeNumber){
            case 1:
                cardName = "天";
                break;
            case 10:
                cardName = "地";
                break;
            case 100:
                cardName = "人";
                break;
        }
        List<CardInfo> summonCardCandidates = DeckBuilder.build(cardName).getCardInfos(player);
        CardInfo addCard = summonCardCandidates.get(0);
        resolver.getStage().getUI().useSkill(summoner, skillUseInfo.getSkill(), true);

        for(CardStatusItem cardStatusItem:summoner.getStatus().getStatusOf(CardStatusType.召唤)){
            addCard.addStatus(cardStatusItem);
        }
        player.getField().expelCard(summoner.getPosition());
        resolver.getStage().getUI().cardDead(summoner);
        resolver.resolveLeaveSkills(summoner);
        resolver.summonCard(summoner.getOwner(), addCard, summoner, false, skillUseInfo.getSkill(), 1);
    }
}
