package cfvbaibai.cardfantasy.engine.skill;

import cfvbaibai.cardfantasy.GameUI;
import cfvbaibai.cardfantasy.Randomizer;
import cfvbaibai.cardfantasy.data.CardSkill;
import cfvbaibai.cardfantasy.data.Skill;
import cfvbaibai.cardfantasy.engine.*;

import java.util.ArrayList;
import java.util.List;

/**
 * Trap 1*level enemy card at 65% probability.
 * 
 * Can be blocked by Immue
 */
public final class Petrifaction {
    public static void apply(SkillUseInfo skillUseInfo, SkillResolver resolver, CardInfo attacker, Player defender)
            throws HeroDieSignal {
        int position = attacker.getPosition();
        if(defender.getField().getCard(position) == null){
            return;
        }
        Skill skill = skillUseInfo.getSkill();
        int effectNumber = skill.getImpact2();
        List<CardInfo> victims = resolver.getAdjacentCards(defender.getField(),position);
        GameUI ui = resolver.getStage().getUI();
        ui.useSkill(attacker, victims, skill, true);
        CardStatusItem statusItem = CardStatusItem.petrifaction(skillUseInfo);
        statusItem.setEffectNumber(effectNumber);
        Skill addSkill = skillUseInfo.getAttachedUseInfo1().getSkill();
        CardSkill cardSkill = new CardSkill(addSkill.getType(), addSkill.getLevel(), 0, false, false, false, false);
        for (CardInfo victim : victims) {
            if (!resolver.resolveAttackBlockingSkills(attacker, victim, skill, 1).isAttackable()) {
                continue;
            }
            if(effectNumber>0)
            {
                if(!victim.getStatus().getStatusOf(CardStatusType.石化).isEmpty()){
                    victim.removeForce(CardStatusType.石化);
                }
            }
            ui.addCardStatus(attacker, victim, skill, statusItem);
            victim.addStatus(statusItem);
            SkillUseInfo thisSkillUserInfo=null;
            thisSkillUserInfo = new SkillUseInfo(victim,cardSkill);
            thisSkillUserInfo.setGiveSkill(2);
            victim.addSkill(thisSkillUserInfo);

            List<CardStatusItem> spreadList = victim.getStatus().getStatusOf(CardStatusType.扩散);
            if (spreadList.size() > 0) {
                SkillUseInfo spreadSkill = spreadList.get(0).getCause();
                List<CardInfo> spreadCardList = new ArrayList<>();
                spreadCardList.add(victim);
                StageInfo stage = resolver.getStage();
                Randomizer random = stage.getRandomizer();
                List<CardInfo> randomVictims = random.pickRandom(defender.getField().toList(), 1, true, spreadCardList);
                for (CardInfo randomVictim : randomVictims) {
                    if (!resolver.resolveAttackBlockingSkills(attacker, randomVictim, skill, 1).isAttackable()) {
                        continue;
                    }
                    if(effectNumber>0)
                    {
                        if(!randomVictim.getStatus().getStatusOf(CardStatusType.石化).isEmpty()){
                            randomVictim.removeForce(CardStatusType.石化);
                        }
                    }
                    ui.useSkill(spreadSkill.getOwner(), randomVictim, spreadSkill.getSkill(), true);
                    ui.addCardStatus(attacker, randomVictim, skill, statusItem);
                    randomVictim.addStatus(statusItem);
                    SkillUseInfo spreadSkillUserInfo=null;
                    spreadSkillUserInfo = new SkillUseInfo(randomVictim,cardSkill);
                    spreadSkillUserInfo.setGiveSkill(2);
                    randomVictim.addSkill(spreadSkillUserInfo);
                }
            }
        }
    }

    public static void reset(CardInfo cardInfo,int type) throws HeroDieSignal {
        if(cardInfo == null){
            return;
        }
        if(type == 0){
            CardStatus status = cardInfo.getStatus();
            List<CardStatusItem>  petrifactionStatus= status.getStatusOf(CardStatusType.石化);
            for(CardStatusItem cardStatusItem:petrifactionStatus){
                SkillUseInfo skillUseInfo = cardStatusItem.getCause();
                if(skillUseInfo.getOwner() == cardInfo){
                    cardInfo.removeAssignGiveSkill(skillUseInfo);
                }
            }
        }else if(type == 1){
            CardStatus status = cardInfo.getStatus();
            List<CardStatusItem>  petrifactionStatus= status.getStatusOf(CardStatusType.石化);
            for(CardStatusItem cardStatusItem:petrifactionStatus){
                if(cardStatusItem.getEffectNumber() >1){
                    return;
                }
                SkillUseInfo skillUseInfo = cardStatusItem.getCause();
                if(skillUseInfo.getOwner() == cardInfo){
                    cardInfo.removeAssignGiveSkill(skillUseInfo);
                }
            }
        }
    }
}
