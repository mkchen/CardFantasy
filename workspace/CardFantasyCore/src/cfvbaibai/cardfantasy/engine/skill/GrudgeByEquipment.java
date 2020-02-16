package cfvbaibai.cardfantasy.engine.skill;

import cfvbaibai.cardfantasy.GameUI;
import cfvbaibai.cardfantasy.Randomizer;
import cfvbaibai.cardfantasy.data.Skill;
import cfvbaibai.cardfantasy.engine.*;

import java.util.ArrayList;
import java.util.List;

public class GrudgeByEquipment {
    public static void apply(SkillResolver resolver, SkillUseInfo skillUseInfo, EntityInfo attacker, Player defenderHero,int victimCount,int effectCount) throws HeroDieSignal {

        StageInfo stage = resolver.getStage();
        Randomizer random = stage.getRandomizer();
        Skill skill = skillUseInfo.getSkill();
        int impact = skill.getImpact();
        GameUI ui = resolver.getStage().getUI();
        int effectNumber = effectCount;
        int damage = skill.getImpact2();
        for(int i=0;i<impact;i++) {
            List<CardInfo> excluCards = new ArrayList<>();
            for (CardInfo fieldCard : defenderHero.getField().getAliveCards()) {
                if (fieldCard.getStatus().containsStatus(CardStatusType.咒怨)) {
                    excluCards.add(fieldCard);
                }
            }
            List<CardInfo> victims = random.pickRandom(defenderHero.getField().toList(), victimCount, true, excluCards);

            if (victims.size() == 0) {
                return;
            }
            ui.useSkill(attacker, victims, skill, true);
            CardStatusItem statusItem2 = CardStatusItem.grudge(damage, skillUseInfo);
            CardStatusItem statusItem1 = CardStatusItem.slience(skillUseInfo);
            statusItem1.setEffectNumber(effectNumber);
            statusItem2.setEffectNumber(effectNumber);
            for (CardInfo victim : victims) {
                if (!resolver.resolveAttackBlockingSkills(attacker, victim, skill, 1).isAttackable()) {
                    continue;
                }
                if (effectNumber > 0) {
                    if (!victim.getStatus().getStatusOf(CardStatusType.咒怨).isEmpty()) {
                        victim.removeForce(CardStatusType.咒怨);
                        victim.removeForce(CardStatusType.沉默);
                    }
                }
                ui.addCardStatus(attacker, victim, skill, statusItem2);
                if (!victim.isDeman()) {
                    victim.addStatus(statusItem1);
                }
                victim.addStatus(statusItem2);

                List<CardStatusItem> spreadList = victim.getStatus().getStatusOf(CardStatusType.扩散);
                if (spreadList.size() > 0) {
                    SkillUseInfo spreadSkill = spreadList.get(0).getCause();
                    List<CardInfo> spreadCardList = new ArrayList<>();
                    spreadCardList.add(victim);
                    List<CardInfo> randomVictims = random.pickRandom(defenderHero.getField().toList(), 1, true, spreadCardList);
                    for (CardInfo randomVictim : randomVictims) {
                        if (!resolver.resolveAttackBlockingSkills(attacker, randomVictim, skill, 1).isAttackable()) {
                            continue;
                        }
                        ui.useSkill(spreadSkill.getOwner(), randomVictim, spreadSkill.getSkill(), true);
                        if (effectNumber > 0) {
                            if (!randomVictim.getStatus().getStatusOf(CardStatusType.咒怨).isEmpty()) {
                                randomVictim.removeForce(CardStatusType.咒怨);
                                randomVictim.removeForce(CardStatusType.沉默);
                            }
                        }
                        ui.addCardStatus(attacker, randomVictim, skill, statusItem2);
                        if (!randomVictim.isDeman()) {
                            randomVictim.addStatus(statusItem1);
                        }
                        randomVictim.addStatus(statusItem2);
                    }
                }
            }
        }
    }
}
