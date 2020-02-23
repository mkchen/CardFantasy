package cfvbaibai.cardfantasy.engine.skill;

import cfvbaibai.cardfantasy.GameUI;
import cfvbaibai.cardfantasy.engine.*;

import java.util.List;

public final class TrumpetHorn {
    public static void apply(SkillUseInfo skillUseInfo, SkillResolver resolver, CardInfo attacker) throws HeroDieSignal {
        int impact = skillUseInfo.getSkill().getImpact();
        int number = skillUseInfo.getSkillNumber();
        if(number==0)
        {
            return;
        }
        if(number<0)
        {
            number = impact;
            skillUseInfo.setSkillNumber(impact);
        }
        skillUseInfo.setSkillNumber(number-1);
        resolver.getStage().getUI().useSkill(attacker, attacker, skillUseInfo.getSkill(), true);
        resolver.summonCard(attacker.getOwner(), attacker, null, false, skillUseInfo.getSkill(),0);
        CardStatusItem item = CardStatusItem.Trumpet(skillUseInfo);
        resolver.getStage().getUI().addCardStatus(attacker, attacker, skillUseInfo.getSkill(), item);
        attacker.addStatus(item);
    }

    public static void explode(SkillResolver resolver, CardInfo cardInfo) throws HeroDieSignal {
        if(cardInfo == null){
            return;
        }
        List<CardStatusItem> statusItems = cardInfo.getStatus().getStatusOf(CardStatusType.深渊);
        if(statusItems.size()<=0){
            return;
        }
        GameUI ui = resolver.getStage().getUI();
        for (CardStatusItem statusItem : statusItems) {
            SkillUseInfo skillUseInfo = statusItem.getCause();
            cardInfo.getOwner().getField().expelCard(cardInfo.getPosition());
            cardInfo.setSummonNumber(0);
            cardInfo.setAddDelay(0);
            cardInfo.setRuneActive(false);
            resolver.resolveLeaveSkills(cardInfo);
            cardInfo.restoreOwner();
            Hand hand = cardInfo.getOwner().getHand();
            if (hand.isFull()) {
                ui.cardToDeck(cardInfo.getOwner(), cardInfo);
                cardInfo.getOwner().getDeck().addCard(cardInfo);
                cardInfo.reset();
            } else {
                ui.cardToHand(cardInfo.getOwner(), cardInfo);
                hand.addCard(cardInfo);
                cardInfo.reset();
            }
        }
    }
}
