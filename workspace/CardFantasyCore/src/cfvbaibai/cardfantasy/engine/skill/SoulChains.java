package cfvbaibai.cardfantasy.engine.skill;

import cfvbaibai.cardfantasy.GameUI;
import cfvbaibai.cardfantasy.Randomizer;
import cfvbaibai.cardfantasy.data.Skill;
import cfvbaibai.cardfantasy.engine.*;
import cfvbaibai.cardfantasy.game.DeckBuilder;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class SoulChains {
    public static void apply(SkillResolver resolver, SkillUseInfo skillUseInfo, EntityInfo attacker, Player defenderHero,int victimCount,int effectNumber) throws HeroDieSignal {

        StageInfo stage = resolver.getStage();
        Randomizer random = stage.getRandomizer();
        List<CardInfo> victims = random.pickRandom(defenderHero.getField().toList(), victimCount, true, null);
        if (victims.size() == 0) {
            return;
        }
        GameUI ui = resolver.getStage().getUI();
        Skill skill = skillUseInfo.getSkill();
        ui.useSkill(attacker, victims, skill, true);
        CardStatusItem statusItem = CardStatusItem.soulWound(skillUseInfo);
        statusItem.setEffectNumber(effectNumber);
        for (CardInfo victim : victims) {
            if (!resolver.resolveAttackBlockingSkills(attacker, victim, skill, 1).isAttackable()) {
                continue;
            }
            if(effectNumber>0) {
                if(!victim.getStatus().getStatusOf(CardStatusType.魂殇).isEmpty()){
                    victim.removeForce(CardStatusType.魂殇);
                }
            }
            ui.addCardStatus(attacker, victim, skill, statusItem);
            victim.addStatus(statusItem);
            List<CardStatusItem> spreadList = victim.getStatus().getStatusOf(CardStatusType.扩散);
            if(spreadList.size()>0){
                SkillUseInfo spreadSkill = spreadList.get(0).getCause();
                List<CardInfo> spreadCardList = new ArrayList<>();
                spreadCardList.add(victim);
                List<CardInfo> randomVictims = random.pickRandom(defenderHero.getField().toList(), 1, true,spreadCardList);
                for (CardInfo randomVictim : randomVictims) {
                    if (!resolver.resolveAttackBlockingSkills(attacker, randomVictim, skill, 1).isAttackable()) {
                        continue;
                    }
                    ui.useSkill(spreadSkill.getOwner(), randomVictim, spreadSkill.getSkill(), true);
                    if (effectNumber > 0) {
                        if (!randomVictim.getStatus().getStatusOf(CardStatusType.魂殇).isEmpty()) {
                            randomVictim.removeForce(CardStatusType.魂殇);
                        }
                    }
                    ui.addCardStatus(attacker, randomVictim, skill, statusItem);
                    randomVictim.addStatus(statusItem);
                }
            }
        }
    }
}
