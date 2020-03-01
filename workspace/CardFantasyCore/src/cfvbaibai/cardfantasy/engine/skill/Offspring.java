package cfvbaibai.cardfantasy.engine.skill;

import cfvbaibai.cardfantasy.GameUI;
import cfvbaibai.cardfantasy.Randomizer;
import cfvbaibai.cardfantasy.data.Skill;
import cfvbaibai.cardfantasy.engine.*;
import cfvbaibai.cardfantasy.game.DeckBuilder;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class Offspring {
    public static void apply(SkillResolver resolver, SkillUseInfo skillUseInfo, CardInfo attackCard, Player defenderHero,int victimCount,int effectNumber) throws HeroDieSignal {

        StageInfo stage = resolver.getStage();
        Randomizer random = stage.getRandomizer();
        List<CardInfo> victims = random.pickRandom(defenderHero.getField().toList(), victimCount, true, null);
        if (victims.size() == 0) {
            return;
        }
        GameUI ui = resolver.getStage().getUI();
        Skill skill = skillUseInfo.getSkill();
        ui.useSkill(attackCard, victims, skill, true);
        CardStatusItem statusItem = CardStatusItem.offspring(skillUseInfo);
        statusItem.setEffectNumber(effectNumber);
        for (CardInfo victim : victims) {
            if (!resolver.resolveAttackBlockingSkills(attackCard, victim, skill, 1).isAttackable()) {
                continue;
            }
            if(effectNumber>0)
            {
                if(!victim.getStatus().getStatusOf(CardStatusType.子嗣).isEmpty()){
                    victim.removeForce(CardStatusType.子嗣);
                }
            }
            ui.addCardStatus(attackCard, victim, skill, statusItem);
            victim.addStatus(statusItem);

            List<CardStatusItem> spreadList = victim.getStatus().getStatusOf(CardStatusType.扩散);
            if (spreadList.size() > 0) {
                SkillUseInfo spreadSkill = spreadList.get(0).getCause();
                List<CardInfo> spreadCardList = new ArrayList<>();
                spreadCardList.add(victim);
                List<CardInfo> randomVictims = random.pickRandom(defenderHero.getField().toList(), 1, true, spreadCardList);
                for (CardInfo randomVictim : randomVictims) {
                    if (randomVictim.getOriginalOwner() != null && randomVictim.getOriginalOwner() != randomVictim.getOwner()) {
                        continue;
                    }
                    if (!resolver.resolveAttackBlockingSkills(attackCard, randomVictim, skill, 1).isAttackable()) {
                        continue;
                    }
                    ui.useSkill(spreadSkill.getOwner(), randomVictim, spreadSkill.getSkill(), true);
                    if (effectNumber > 0) {
                        if (!randomVictim.getStatus().getStatusOf(CardStatusType.子嗣).isEmpty()) {
                            randomVictim.removeForce(CardStatusType.子嗣);
                        }
                    }
                    ui.addCardStatus(attackCard, randomVictim, skill, statusItem);
                    randomVictim.addStatus(statusItem);
                }
            }
        }
    }

    public static void explode(SkillResolver resolver, CardInfo cardInfo, String... summonedCardsDescs) throws HeroDieSignal {
        GameUI ui = resolver.getStage().getUI();
        if(cardInfo ==null){
            return;
        }
        List<CardStatusItem> statusItems = cardInfo.getStatus().getStatusOf(CardStatusType.子嗣);
        if(statusItems.size()<=0){
            return;
        }
        for (CardStatusItem statusItem : statusItems) {
            SkillUseInfo skillUseInfo = statusItem.getCause();
            int effectNumber = statusItem.getEffectNumber();
            if(effectNumber>1) {
                resolver.resolveDebuff(cardInfo, CardStatusType.子嗣);
                continue;
            }else {
                Skill skill = skillUseInfo.getSkill();
                EntityInfo attacker = skillUseInfo.getOwner();
                if (attacker instanceof CardInfo) {
                    CardInfo attackCard = (CardInfo) attacker;
                    ui.useSkill(cardInfo, attacker, skill, true);
                    Player enemy = resolver.getStage().getOpponent(cardInfo.getOwner());
                    List<CardInfo> livingCards = enemy.getField().getAliveCards();
                    List<String> cardDescsToSummon = new LinkedList<String>();
                    for (String summonedCardDesc : summonedCardsDescs) {
                        cardDescsToSummon.add(summonedCardDesc);
                    }
                    List<CardInfo> cardsToSummon = new ArrayList<CardInfo>();
                    List<CardInfo> summonCardCandidates = null;
                    int summonNumber = 0;
                    summonCardCandidates = DeckBuilder.build(summonedCardsDescs).getCardInfos(enemy);
                    for (CardInfo fieldCard : livingCards) {
                        if (fieldCard.getStatus().containsStatusCausedBy(skillUseInfo, CardStatusType.召唤)) {
                            if (fieldCard.getRelationCardInfo() == attackCard) {
                                summonNumber++;
                            }
                        }
                    }
                    if (summonNumber >= 5) {
                        return;
                    }
                    cardsToSummon = Randomizer.getRandomizer().pickRandom(summonCardCandidates, 1, true, null);
                    for (int i = 0; i < cardsToSummon.size(); ++i) {
                        CardInfo summonedCard = cardsToSummon.get(i);
                        CardStatusItem summonedStatusItem = CardStatusItem.summoned(skillUseInfo);
                        resolver.getStage().getUI().addCardStatus(cardInfo, summonedCard, skill, summonedStatusItem);
                        summonedCard.addStatus(summonedStatusItem);
                        CardStatusItem weakStatusItem = CardStatusItem.weak(skillUseInfo);
                        resolver.getStage().getUI().addCardStatus(cardInfo, summonedCard, skill, weakStatusItem);
                        summonedCard.addStatus(weakStatusItem);
                        summonedCard.setRelationCardInfo(attackCard);

                        //杀卡
                        ui.killCard(attacker, cardInfo, skill);
                        cardInfo.removeStatus(CardStatusType.不屈);
                        resolver.killCard(attackCard, cardInfo, skill);
                        //降临
                        resolver.summonCard(summonedCard.getOwner(), summonedCard, cardInfo, true, skill, 1);
                    }
                }
            }
        }
    }
}
