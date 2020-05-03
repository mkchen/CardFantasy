package cfvbaibai.cardfantasy.engine.skill;

import java.util.ArrayList;
import java.util.List;

import cfvbaibai.cardfantasy.GameUI;
import cfvbaibai.cardfantasy.data.Race;
import cfvbaibai.cardfantasy.data.Skill;
import cfvbaibai.cardfantasy.data.SkillType;
import cfvbaibai.cardfantasy.engine.*;

public final class HolyFire {
    public static void apply(Skill cardSkill, SkillResolver resolver, EntityInfo attacker, Player defender) throws HeroDieSignal{
        if (defender.getGrave().size() == 0) {
            return;
        }
        if(!resolver.resolveStopHolyFire(defender))
        {
            return;
        }
        GameUI ui = resolver.getStage().getUI();
        List<CardInfo> candidates = new ArrayList<CardInfo>();
        for (CardInfo deadCard : defender.getGrave().toList()) {
            if (deadCard.getRace() != Race.DEMON) {
                candidates.add(deadCard);
            }
        }
        if(candidates.size() !=0) {
            CardInfo victim = resolver.getStage().getRandomizer().pickRandom(
                    candidates, 1, true, null).get(0);
            ui.useSkill(attacker, victim, cardSkill, true);
            if (SoulSeal.soulSealed(resolver, attacker)) {
                return;
            }
            for(SkillUseInfo skillUseInfo:victim.getUsableNormalSkills()){
                if(skillUseInfo.getType() == SkillType.浴火重生 || skillUseInfo.getType() == SkillType.大圣归来 || skillUseInfo.getType() == SkillType.拉莱耶领域
                        || skillUseInfo.getType() == SkillType.入木三分){
                    ui.useSkill(victim, victim, skillUseInfo.getSkill(), true);
                    defender.getGrave().removeCard(victim);
                    resolver.summonCard(defender, victim, victim, false, skillUseInfo.getSkill(),0);
                    CardStatusItem item = CardStatusItem.weak(skillUseInfo);
                    resolver.getStage().getUI().addCardStatus(victim, victim, skillUseInfo.getSkill(), item);
                    victim.addStatus(item);
                    return;
                }
            }
            ui.cardToOutField(defender, victim);
            defender.getGrave().removeCard(victim);
            defender.getOutField().addCard(victim);
        }
    }
}
