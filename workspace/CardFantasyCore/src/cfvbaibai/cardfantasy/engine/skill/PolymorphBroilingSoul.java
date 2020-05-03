package cfvbaibai.cardfantasy.engine.skill;

import cfvbaibai.cardfantasy.GameUI;
import cfvbaibai.cardfantasy.Randomizer;
import cfvbaibai.cardfantasy.data.Skill;
import cfvbaibai.cardfantasy.engine.*;

import java.util.ArrayList;
import java.util.List;

public class PolymorphBroilingSoul {
    public static void apply(SkillResolver resolver, SkillUseInfo skillUseInfo, EntityInfo attackCard, Player defenderHero,int victimCount,int effectNumber) throws HeroDieSignal {

        StageInfo stage = resolver.getStage();
        Randomizer random = stage.getRandomizer();
        int soulNumber = skillUseInfo.getSkillNumber();
        int impact = skillUseInfo.getSkill().getImpact();
        if(soulNumber == -1){
            soulNumber = 1;
            skillUseInfo.setSkillNumber(soulNumber);
        }else if(soulNumber<impact){
            soulNumber++;
            skillUseInfo.setSkillNumber(soulNumber);
        }

        GameUI ui = resolver.getStage().getUI();
        Skill skill = skillUseInfo.getSkill();
        CardStatusItem statusItem = CardStatusItem.sheep(skillUseInfo);
        CardStatusItem statusItemSlience = CardStatusItem.slience(skillUseInfo);
        statusItem.setEffectNumber(effectNumber);

        for(int soul=0; soul< soulNumber;soul++) {
            List<CardInfo> victims = random.pickRandom(defenderHero.getField().toList(), victimCount, true, null);
            if (victims.size() == 0) {
                return;
            }
            ui.useSkill(attackCard, victims, skill, true);
            for (CardInfo victim : victims) {
                if (!resolver.resolveAttackBlockingSkills(attackCard, victim, skill, 1).isAttackable()) {
                    continue;
                }
                if (effectNumber > 0) {
                    if (!victim.getStatus().getStatusOf(CardStatusType.变羊).isEmpty()) {
                        continue;
                    }
                }
                ui.addCardStatus(attackCard, victim, skill, statusItem);
                victim.addStatus(statusItem);
                ui.addCardStatus(attackCard, victim, skill, statusItemSlience);
                victim.addStatus(statusItemSlience);
                int impactAdd = victim.getInitAT() - skill.getImpact();
                resolver.getStage().getUI().adjustAT(attackCard, victim, -impactAdd, skill);
                victim.addCoefficientEffect(new SkillEffect(SkillEffectType.ATTACK_CHANGE, skillUseInfo, -impactAdd, false));
                int impactHpAdd = victim.getHP() - skill.getImpact2();
                resolver.getStage().getUI().adjustHP(attackCard, victim, -impactHpAdd, skill);
                victim.addCoefficientEffect(new SkillEffect(SkillEffectType.MAXHP_CHANGE, skillUseInfo, -impactHpAdd, false));
            }
        }
    }

    public static void reset( SkillUseInfo skillUseInfo, CardInfo card) throws HeroDieSignal {
        skillUseInfo.setSkillNumber(0);
    }
}
