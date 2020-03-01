package cfvbaibai.cardfantasy.engine.skill;

import cfvbaibai.cardfantasy.CardFantasyRuntimeException;
import cfvbaibai.cardfantasy.GameUI;
import cfvbaibai.cardfantasy.Randomizer;
import cfvbaibai.cardfantasy.data.Card;
import cfvbaibai.cardfantasy.data.Skill;
import cfvbaibai.cardfantasy.engine.*;

import java.util.ArrayList;
import java.util.List;

public final class Substitution {
    public static void apply(SkillResolver resolver, SkillUseInfo skillUseInfo, CardInfo cardInfo, Player opponent) throws HeroDieSignal{
        Skill skill = skillUseInfo.getSkill();
        // Grave is a stack, find the last-in card and revive it.
        int impact = skill.getImpact();

        List<CardInfo> deadCards = opponent.getGrave().toList();
        List<CardInfo> cardsToDeck = Randomizer.getRandomizer().pickRandom(
                deadCards, impact, true, null);
        if(cardsToDeck.size()==0) {
            return;
        }
        List<CardInfo> deckList = opponent.getDeck().toList();
        List<CardInfo> selectCardList = new ArrayList<>();
        for(CardInfo selectCard:deckList){
            if(!selectCard.isBoss()){
                selectCardList.add(selectCard);
            }
        }
        List<CardInfo> cardsToGrave = Randomizer.getRandomizer().pickRandom(
                selectCardList, impact, true, null);
        if(cardsToGrave.size() == 0){
            return;
        }
        GameUI ui = resolver.getStage().getUI();
        ui.useSkill(cardInfo, cardsToDeck, skill, true);
        int number = cardsToDeck.size()> cardsToGrave.size()?cardsToGrave.size():cardsToDeck.size();
        for (int i=0;i<number;i++) {
            CardInfo deadCard = cardsToDeck.get(i);
            CardInfo deckCard = cardsToGrave.get(i);

            deadCard.restoreOwner();
            ui.cardToDeck(deadCard.getOwner(), deadCard);
            deadCard.getOwner().getGrave().removeCard(deadCard);
            deadCard.getOwner().getDeck().addCard(deadCard);
            deadCard.reset();

            deckCard.restoreOwner();
            ui.cardToGrave(deckCard.getOwner(), deckCard);
            ParadiseLost.remove(resolver,deckCard,deckCard.getOwner());
            deckCard.getOwner().getDeck().removeCard(deckCard);
            deckCard.getOwner().getGrave().addCard(deckCard);
        }
    }
}
