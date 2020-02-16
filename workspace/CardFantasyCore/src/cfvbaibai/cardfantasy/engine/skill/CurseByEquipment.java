package cfvbaibai.cardfantasy.engine.skill;

import cfvbaibai.cardfantasy.CardFantasyRuntimeException;
import cfvbaibai.cardfantasy.data.Skill;
import cfvbaibai.cardfantasy.engine.EntityInfo;
import cfvbaibai.cardfantasy.engine.HeroDieSignal;
import cfvbaibai.cardfantasy.engine.Player;
import cfvbaibai.cardfantasy.engine.SkillResolver;

public final class CurseByEquipment {
    public static void apply(SkillResolver resolver, Skill cardSkill, EntityInfo attacker, Player defenderHero) throws HeroDieSignal {
        if (attacker == null) {
            throw new CardFantasyRuntimeException("attacker is null");
        }
        int impact2 = cardSkill.getImpact2();
        for(int i=0;i<impact2;i++) {
            int damage = cardSkill.getImpact();
            resolver.attackHero(attacker, defenderHero, cardSkill, damage);
        }
    }
}
