package cfvbaibai.cardfantasy.engine.skill;

import cfvbaibai.cardfantasy.GameUI;
import cfvbaibai.cardfantasy.Randomizer;
import cfvbaibai.cardfantasy.data.Skill;
import cfvbaibai.cardfantasy.engine.*;

import java.util.ArrayList;
import java.util.List;

public final class TurbulenceDeck {
    public static void apply(Skill cardSkill, SkillResolver resolver, CardInfo attacker, Player defender,int rate1) throws HeroDieSignal {
        StageInfo stage = resolver.getStage();
        Randomizer random = stage.getRandomizer();
        GameUI ui = stage.getUI();
        List<CardInfo> attackerFieldList = attacker.getOwner().getField().getAliveCards();
        List<CardInfo> defendeFieldList = defender.getField().getAliveCards();
        int impact = cardSkill.getImpact();

        List<CardInfo> extraList = new ArrayList<>();
        extraList.add(attacker);
        List<CardInfo> attackerVictims = random.pickRandom(attackerFieldList, impact, true, extraList);
        List<CardInfo> defenderVictims = random.pickRandom(defendeFieldList, impact, true, null);
        ui.useSkill(attacker,attackerVictims,cardSkill,true);
        ui.useSkill(attacker,defenderVictims,cardSkill,true);
        for(CardInfo victim:defenderVictims){
            OnAttackBlockingResult result = resolver.resolveAttackBlockingSkills(attacker, victim, cardSkill, 1);
            if (!result.isAttackable()) {
                continue;
            }
            int reallyRate = random.next(0,100);
            if(reallyRate<rate1){
                defender.getField().expelCard(victim.getPosition());
                victim.setSummonNumber(0);
                victim.setAddDelay(0);
                victim.setRuneActive(false);
                resolver.resolveLeaveSkills(victim);
                ui.returnCard(attacker, victim, cardSkill);
                // 如果是被召唤的卡牌，发动逃跑技能后应该直接消失
                if (victim.isSummonedMinion()) {
                    continue;
                }
                ui.cardToDeck(defender, victim);
                defender.getDeck().addCard(victim);
                victim.reset();
            }
        }

        for(CardInfo victim:attackerVictims){
            OnAttackBlockingResult result = resolver.resolveAttackBlockingSkills(attacker, victim, cardSkill, 1);
            if (!result.isAttackable()) {
                continue;
            }
            int reallyRate = random.next(0,100);
            if(reallyRate<rate1){
                attacker.getOwner().getField().expelCard(victim.getPosition());
                victim.setSummonNumber(0);
                victim.setAddDelay(0);
                victim.setRuneActive(false);
                resolver.resolveLeaveSkills(victim);
                ui.returnCard(attacker, victim, cardSkill);
                // 如果是被召唤的卡牌，发动逃跑技能后应该直接消失
                if (victim.isSummonedMinion()) {
                    continue;
                }

                ui.cardToDeck(attacker.getOwner(), victim);
                attacker.getOwner().getDeck().addCard(victim);
                victim.reset();
            }
        }
    }
}
