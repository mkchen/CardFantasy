package cfvbaibai.cardfantasy.engine.skill;

import cfvbaibai.cardfantasy.GameUI;
import cfvbaibai.cardfantasy.Randomizer;
import cfvbaibai.cardfantasy.data.Skill;
import cfvbaibai.cardfantasy.engine.*;

import java.util.List;

public final class ReturnCardAndDelay {
    public static void apply(SkillResolver resolver, Skill cardSkill, CardInfo attacker, Player defenderHero,int delay,int victimCount) throws HeroDieSignal {
        if (attacker == null) {
            return;
        }
        StageInfo stage = resolver.getStage();
        Randomizer random = stage.getRandomizer();
        List<CardInfo> defenderList = random.pickRandom(defenderHero.getField().getAliveCards(), victimCount, true, null);
        GameUI ui = resolver.getStage().getUI();
        for(CardInfo defender :defenderList) {
            if (defender == null || defender.isBoss()) {
                continue;
            }
            OnAttackBlockingResult result = resolver.resolveAttackBlockingSkills(attacker, defender, cardSkill, 1);
            if (!result.isAttackable()) {
                continue;
            }
            if (defenderHero.getHand().isFull()) {
                ui.useSkill(attacker, defender, cardSkill, true);
                Return.returnCard2(resolver, cardSkill, attacker, defender, true);
                if (!defender.getStatus().containsStatus(CardStatusType.召唤)) {
                    resolver.getStage().getUI().increaseSummonDelay(defender, delay);
                    defender.setAddDelay(2);
                }
                continue;
            }
            ui.useSkill(attacker, defender, cardSkill, true);
            Return.returnHand(resolver, cardSkill, attacker, defender);
            int summonDelay = defender.getSummonDelay();
            if (!defender.getStatus().containsStatus(CardStatusType.召唤)) {
                resolver.getStage().getUI().increaseSummonDelay(defender, delay);
                defender.setSummonDelay(summonDelay + delay);
            }
        }
    }
}
