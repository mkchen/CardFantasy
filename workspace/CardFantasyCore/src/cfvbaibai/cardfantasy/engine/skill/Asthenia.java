package cfvbaibai.cardfantasy.engine.skill;

import cfvbaibai.cardfantasy.GameUI;
import cfvbaibai.cardfantasy.Randomizer;
import cfvbaibai.cardfantasy.data.Skill;
import cfvbaibai.cardfantasy.data.SkillType;
import cfvbaibai.cardfantasy.engine.*;

import java.util.ArrayList;
import java.util.List;

public class Asthenia {
    public static void apply(SkillResolver resolver, SkillUseInfo skillUseInfo, EntityInfo attackCard, Player defenderHero,int victimCount,int effectNumber) throws HeroDieSignal {

        StageInfo stage = resolver.getStage();
        Randomizer random = stage.getRandomizer();
        List<CardInfo> victims = random.pickRandom(defenderHero.getField().toList(), victimCount, true, null);
        if (victims.size() == 0) {
            return;
        }
        GameUI ui = resolver.getStage().getUI();
        Skill skill = skillUseInfo.getSkill();
        int impact = skill.getImpact2();
        if(skillUseInfo.getType() == SkillType.虚实){
            impact = skill.getImpact3();
        }
        ui.useSkill(attackCard, victims, skill, true);
        CardStatusItem status = CardStatusItem.paralyzed(skillUseInfo);
        status.setEffect(impact);
        status.setEffectNumber(effectNumber);
        CardStatusItem statusItem = CardStatusItem.asthenia(skillUseInfo);
        statusItem.setEffectNumber(effectNumber);
        for (CardInfo victim : victims) {
            if (!resolver.resolveAttackBlockingSkills(attackCard, victim, skill, 1).isAttackable()) {
                continue;
            }
            if(effectNumber>0)
            {
                if(!victim.getStatus().getStatusOf(CardStatusType.虚化).isEmpty()){
                    victim.removeForce(CardStatusType.虚化);
                }
                if(!victim.getStatus().getStatusOf(CardStatusType.麻痹).isEmpty())
                {
                    victim.removeForce(CardStatusType.麻痹);
                }
            }
            ui.addCardStatus(attackCard, victim, skill, status);
            ui.addCardStatus(attackCard, victim, skill, statusItem);
            victim.addStatus(status);
            victim.addStatus(statusItem);

            List<CardStatusItem> spreadList = victim.getStatus().getStatusOf(CardStatusType.扩散);
            if (spreadList.size() > 0) {
                SkillUseInfo spreadSkill = spreadList.get(0).getCause();
                List<CardInfo> spreadCardList = new ArrayList<>();
                spreadCardList.add(victim);
                List<CardInfo> randomVictims = random.pickRandom(defenderHero.getField().toList(), 1, true, spreadCardList);
                for (CardInfo randomVictim : randomVictims) {
                    if (!resolver.resolveAttackBlockingSkills(attackCard, randomVictim, skill, 1).isAttackable()) {
                        continue;
                    }
                    ui.useSkill(spreadSkill.getOwner(), randomVictim, spreadSkill.getSkill(), true);
                    if (effectNumber > 0) {
                        if(!randomVictim.getStatus().getStatusOf(CardStatusType.虚化).isEmpty()){
                            randomVictim.removeForce(CardStatusType.虚化);
                        }
                        if(!randomVictim.getStatus().getStatusOf(CardStatusType.麻痹).isEmpty())
                        {
                            randomVictim.removeForce(CardStatusType.麻痹);
                        }
                    }
                    ui.addCardStatus(attackCard, randomVictim, skill, status);
                    ui.addCardStatus(attackCard, randomVictim, skill, statusItem);
                    randomVictim.addStatus(status);
                    randomVictim.addStatus(statusItem);
                }
            }
        }
    }

    public static boolean explode(SkillResolver resolver, CardInfo defender,int dodgeRate) {
        if(defender.getStatus().getStatusOf(CardStatusType.虚化).isEmpty()){
            return false;
        }
        boolean bingo = true;
        GameUI ui = resolver.getStage().getUI();
        bingo = resolver.getStage().getRandomizer().roll100(dodgeRate);
        if (bingo) {
            List<CardStatusItem> statusItems = defender.getStatus().getStatusOf(CardStatusType.虚化);
            for (CardStatusItem statusItem : statusItems) {
                SkillUseInfo skillUseInfo = statusItem.getCause();
                EntityInfo attacker = skillUseInfo.getOwner();
                if(attacker instanceof CardInfo) {
                    ui.useSkill(attacker, defender, skillUseInfo.getSkill(), true);
                }
            }
            return true;
        } else {
            return false;
        }
    }
}
