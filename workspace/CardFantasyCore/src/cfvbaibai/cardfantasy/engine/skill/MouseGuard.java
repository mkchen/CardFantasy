package cfvbaibai.cardfantasy.engine.skill;

import cfvbaibai.cardfantasy.GameUI;
import cfvbaibai.cardfantasy.Randomizer;
import cfvbaibai.cardfantasy.data.Skill;
import cfvbaibai.cardfantasy.engine.*;
import cfvbaibai.cardfantasy.game.DeckBuilder;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class MouseGuard {
    public static void apply(SkillResolver resolver, SkillUseInfo skillUseInfo, CardInfo attackCard, Player defenderHero, int victimCount, int effectNumber, int type) throws HeroDieSignal {

        StageInfo stage = resolver.getStage();
        Randomizer random = stage.getRandomizer();
        int position = attackCard.getPosition();
        CardInfo positionCard = defenderHero.getField().getCard(position);
        List<CardInfo> victims = new ArrayList<>();
        List<CardInfo> extraList = new ArrayList<>();
        extraList.add(positionCard);
        if (type == 0) {
            victims = random.pickRandom(defenderHero.getField().toList(), victimCount, true, extraList);
        }
        if (positionCard != null) {
            victims.add(positionCard);
        }
        if (victims.size() == 0) {
            return;
        }
        GameUI ui = resolver.getStage().getUI();
        Skill skill = skillUseInfo.getSkill();
        ui.useSkill(attackCard, victims, skill, true);
        CardStatusItem statusItem = CardStatusItem.mouseGuard(skillUseInfo);
        statusItem.setEffectNumber(effectNumber);
        for (CardInfo victim : victims) {
            if (!resolver.resolveAttackBlockingSkills(attackCard, victim, skill, 1).isAttackable()) {
                continue;
            }
            if (effectNumber > 0) {
                if (!victim.getStatus().getStatusOf(CardStatusType.庚子).isEmpty()) {
                    victim.removeForce(CardStatusType.庚子);
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
                        if (!randomVictim.getStatus().getStatusOf(CardStatusType.庚子).isEmpty()) {
                            randomVictim.removeForce(CardStatusType.庚子);
                        }
                    }
                    ui.addCardStatus(attackCard, randomVictim, skill, statusItem);
                    randomVictim.addStatus(statusItem);
                }
            }
        }
    }

    public static void explode(SkillResolver resolver, CardInfo cardInfo, Player defender) throws HeroDieSignal {
        if(cardInfo ==null){
            return;
        }
        GameUI ui = resolver.getStage().getUI();
        List<CardStatusItem> statusItems = cardInfo.getStatus().getStatusOf(CardStatusType.庚子);
        if (statusItems.size() <= 0) {
            return;
        }
        int hp = defender.getHP();
        for (CardStatusItem statusItem : statusItems) {
            int recordHp = statusItem.getEffect();
            if (recordHp <= hp) {
                statusItem.setEffect(0);
                resolver.resolveDebuff(cardInfo, CardStatusType.庚子);
                return;
            } else {
                SkillUseInfo skillUseInfo = statusItem.getCause();
                if(skillUseInfo.getOwner() instanceof  CardInfo) {
                    CardInfo attackCard = (CardInfo) skillUseInfo.getOwner();
                    Skill skill = skillUseInfo.getSkill();
                    int healHero = recordHp - hp;
                    resolver.attackHero(defender, defender.getOwner(), skill, -healHero);
                    ui.killCard(attackCard, cardInfo, skill);
                    cardInfo.removeStatus(CardStatusType.不屈);
                    resolver.killCard(attackCard, cardInfo, skill);
                }
            }
        }
    }

    public static void explodePre(SkillResolver resolver, CardInfo cardInfo, Player defender) throws HeroDieSignal {
        GameUI ui = resolver.getStage().getUI();
        List<CardStatusItem> statusItems = cardInfo.getStatus().getStatusOf(CardStatusType.庚子);
        if (statusItems.size() <= 0) {
            return;
        }
        int hp = defender.getHP();
        for (CardStatusItem statusItem : statusItems) {
            statusItem.setEffect(hp);
        }
    }
}