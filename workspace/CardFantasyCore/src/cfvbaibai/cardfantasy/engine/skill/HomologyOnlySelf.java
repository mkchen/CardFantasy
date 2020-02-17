package cfvbaibai.cardfantasy.engine.skill;

import cfvbaibai.cardfantasy.CardFantasyRuntimeException;
import cfvbaibai.cardfantasy.engine.*;

import java.util.ArrayList;
import java.util.List;

public final class HomologyOnlySelf {
    public static void apply(SkillResolver resolver, SkillUseInfo skillUseInfo, CardInfo reviver,String cardName) throws HeroDieSignal {
        if (reviver == null) {
            throw new CardFantasyRuntimeException("reviver should not be null");
        }
        int impact = 1;
        if(skillUseInfo.getSkillNumber()==0) {
            return;
        }
        if(skillUseInfo.getSkillNumber()==-1) {
            skillUseInfo.setSkillNumber(impact);
        }
        skillUseInfo.setSkillNumber(skillUseInfo.getSkillNumber()-1);
        Grave grave = reviver.getOwner().getGrave();
        Hand hand = reviver.getOwner().getHand();
        Deck deck = reviver.getOwner().getDeck();
        List<CardInfo> revivableCards = new ArrayList<CardInfo>();
        for (CardInfo deckCard : deck.toList()) {
            if(deckCard.getName().equals(cardName)) {
                revivableCards.add(deckCard);
            }
        }
        for (CardInfo handCard : hand.toList()) {
            if(handCard.getName().equals(cardName)) {
                revivableCards.add(handCard);
            }
        }
        for (CardInfo deadCard : grave.toList()) {
            if(deadCard.getName().equals(cardName)) {
                revivableCards.add(deadCard);
            }
        }
        if (revivableCards.isEmpty()) {
            return;
        }
        resolver.getStage().getUI().useSkill(reviver, revivableCards, skillUseInfo.getSkill(), true);
        for(CardInfo cardInfo:revivableCards)
        {
            if(cardInfo.isAlive())
            {
                continue;
            }
            //添加卡牌如果被移除，就不在触发牵丝诡术
            if(reviver.getOwner().getOutField().contains(cardInfo)){
                continue;
            }
            if( reviver.getOwner().getGrave().contains(cardInfo)) {
                reviver.getOwner().getGrave().removeCard(cardInfo);
            }
            reviver.getOwner().getHand().removeCard(cardInfo);
            reviver.getOwner().getDeck().removeCard(cardInfo);
            resolver.summonCard(reviver.getOwner(), cardInfo, reviver, false, skillUseInfo.getSkill(),0);
            CardStatusItem item = CardStatusItem.weak(skillUseInfo);
            resolver.getStage().getUI().addCardStatus(reviver, cardInfo, skillUseInfo.getSkill(), item);
            cardInfo.addStatus(item);
        }
    }
}
