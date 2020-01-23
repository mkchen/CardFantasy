package cfvbaibai.cardfantasy.engine.skill;

import cfvbaibai.cardfantasy.data.Skill;
import cfvbaibai.cardfantasy.data.SkillType;
import cfvbaibai.cardfantasy.engine.*;

import java.util.HashMap;
import java.util.List;

public final class PsionicDecay {
    public static void apply(SkillUseInfo skillUseInfo,SkillResolver resolver, EntityInfo attacker, Player defenderPlayer,int damage) throws HeroDieSignal {
        Skill cardSkill = skillUseInfo.getSkill();
        int impact2 = cardSkill.getImpact2();
        damage = damage*impact2/100;

        List<CardInfo> victims = defenderPlayer.getField().getAliveCards();
        resolver.getStage().getUI().useSkill(attacker, victims, cardSkill, true);
        for (CardInfo victim : victims) {
            int magicEchoSkillResult = resolver.resolveMagicEchoSkill(attacker, victim, cardSkill);
            if (magicEchoSkillResult==1) {
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
                        resolver.resolveDeathSkills(victim, attackCard, cardSkill, damageResult2);
                    }
                }
                if (magicEchoSkillResult == 1) {
                    continue;
                }
            }
            resolver.getStage().getUI().attackCard(attacker, victim, cardSkill, damage);
            OnDamagedResult damageResult = resolver.applyDamage(attacker, victim, cardSkill, damage);
            resolver.resolveDeathSkills(attacker, victim, cardSkill, damageResult);
        }
    }
}
