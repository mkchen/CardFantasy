package cfvbaibai.cardfantasy.engine.skill;

import cfvbaibai.cardfantasy.GameUI;
import cfvbaibai.cardfantasy.data.Skill;
import cfvbaibai.cardfantasy.engine.*;

import java.util.List;

public final class NebulaChainByEquipment {
    public static void apply(SkillUseInfo skillUseInfo, SkillResolver resolver, EntityInfo attacker,Player defender) throws HeroDieSignal {
        GameUI ui = resolver.getStage().getUI();
        Skill skill = skillUseInfo.getSkill();
        int level = skill.getImpact();
        for(int i=0;i<level;i++) {
            if (SummonStopSkillUseInfoList.explodeEquipment(resolver, attacker, defender)) {
                continue;
            }
            CardInfo target = null;
            List<CardInfo> allDeckCards = attacker.getOwner().getDeck().toList();
            for (CardInfo card : allDeckCards) {
                if (target == null || card.getSummonDelay() > target.getSummonDelay()) {
                    target = card;
                }
            }
            if (target == null) {
                // No card in deck.
                return;
            }
            resolver.getStage().getUI().useSkill(attacker, target, skillUseInfo.getSkill(), true);
            resolver.summonCard(attacker.getOwner(), target, null, false, skillUseInfo.getSkill(), 0);
            CardStatusItem item = CardStatusItem.weak(skillUseInfo);
            target.addStatus(item);
        }
    }
}
