package cfvbaibai.cardfantasy.engine.skill;

import cfvbaibai.cardfantasy.data.Skill;
import cfvbaibai.cardfantasy.engine.*;

import java.util.List;

public final class Snipe {
    public static void apply(SkillUseInfo skillUseInfo,Skill cardSkill, SkillResolver resolver, EntityInfo attacker, Player defenderPlayer,
            int targetCount) throws HeroDieSignal {
        int rate = cardSkill.getImpact();
        int damage = cardSkill.getImpact3();
        if (damage == 0) {
            
        }

        List<CardInfo> victims = defenderPlayer.getField().getCardsWithLowestHP(targetCount);
        resolver.getStage().getUI().useSkill(attacker, victims, cardSkill, true);
        for (CardInfo victim : victims) {
            int magicEchoSkillResult = resolver.resolveMagicEchoSkill(attacker, victim, cardSkill);
            if (magicEchoSkillResult==1||magicEchoSkillResult==2) {
                if (attacker instanceof CardInfo) {
                    CardInfo attackCard =  (CardInfo)attacker;
                    if(attackCard.isDead())
                    {
                        if (magicEchoSkillResult == 1) {
                            continue;
                        }
                    }
                    else{
                        resolver.getStage().getUI().attackCard(victim, attackCard, cardSkill, damage);
                        OnDamagedResult damageResult2 = resolver.applyDamage(victim, attackCard, cardSkill, damage);
                        if (resolver.getStage().getRandomizer().roll100(rate)) {
                            CardStatusItem status = CardStatusItem.frozen(skillUseInfo);
                            if (!resolver.resolveBlockStatusSkills(victim, attackCard, skillUseInfo, status).isBlocked()) {
                                resolver.getStage().getUI().addCardStatus(victim, attackCard, cardSkill, status);
                                attackCard.addStatus(status);
                            }
                        }
                        resolver.resolveDeathSkills(victim, attackCard, cardSkill, damageResult2);
                    }
                }
                if (magicEchoSkillResult == 1) {
                    continue;
                }
            }
            resolver.getStage().getUI().attackCard(attacker, victim, cardSkill, damage);
            OnDamagedResult damageResult = resolver.applyDamage(attacker, victim, cardSkill, damage);
            if (attacker instanceof CardInfo) {
                if(rate>0)
                {
                    if (resolver.getStage().getRandomizer().roll100(rate)) {
                        CardStatusItem status = CardStatusItem.frozen(skillUseInfo);
                        if (!resolver.resolveBlockStatusSkills(attacker, victim, skillUseInfo, status).isBlocked()) {
                            resolver.getStage().getUI().addCardStatus(attacker, victim, cardSkill, status);
                            victim.addStatus(status);
                        }
                    }
                }
            }
            resolver.resolveDeathSkills(attacker, victim, cardSkill, damageResult);
        }
    }
}
