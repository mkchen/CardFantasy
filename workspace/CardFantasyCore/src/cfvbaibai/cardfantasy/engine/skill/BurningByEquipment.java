package cfvbaibai.cardfantasy.engine.skill;

import cfvbaibai.cardfantasy.GameUI;
import cfvbaibai.cardfantasy.data.Skill;
import cfvbaibai.cardfantasy.engine.*;

public final class BurningByEquipment {
    public static void apply(SkillUseInfo skillUseInfo, SkillResolver resolver, CardInfo attacker, EntityInfo defender)
            throws HeroDieSignal {
        if (attacker == null) {
            return;
        }
        for (CardStatusItem item : attacker.getStatus().getStatusOf(CardStatusType.燃烧)) {
            // 同等级燃烧无法叠加
            if (item.getCause().getSkill().getImpact() == skillUseInfo.getSkill().getImpact()) { 
                return;
            }
        }
        Skill skill = skillUseInfo.getSkill();
        int damage = skill.getImpact3();
        GameUI ui = resolver.getStage().getUI();
        ui.useSkill(defender, attacker, skill, true);
        OnAttackBlockingResult result = resolver.resolveAttackBlockingSkills(defender, attacker, skill, damage);
        if (!result.isAttackable()) {
            return;
        }
        CardStatusItem status = CardStatusItem.burning(damage, skillUseInfo);
        ui.addCardStatus(defender, attacker, skill, status);
        attacker.addStatus(status);
    }
}
