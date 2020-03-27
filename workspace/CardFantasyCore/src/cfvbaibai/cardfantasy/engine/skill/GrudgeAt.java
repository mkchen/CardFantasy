package cfvbaibai.cardfantasy.engine.skill;

import cfvbaibai.cardfantasy.GameUI;
import cfvbaibai.cardfantasy.Randomizer;
import cfvbaibai.cardfantasy.data.Skill;
import cfvbaibai.cardfantasy.engine.*;

import java.util.ArrayList;
import java.util.List;

public class GrudgeAt {
    public static void apply(SkillResolver resolver, SkillUseInfo skillUseInfo, CardInfo attackCard, Player defenderHero,int victimCount) throws HeroDieSignal {

        StageInfo stage = resolver.getStage();
        Randomizer random = stage.getRandomizer();
        List<CardInfo> excluCards=new ArrayList<>();
        for(CardInfo fieldCard:defenderHero.getField().getAliveCards())
        {
            if(fieldCard.getStatus().containsStatus(CardStatusType.咒恨))
            {
                excluCards.add(fieldCard);
            }
        }
        List<CardInfo> victims = random.pickRandom(defenderHero.getField().toList(), victimCount, true, excluCards);

        if (victims.size() == 0) {
            return;
        }
        GameUI ui = resolver.getStage().getUI();
        Skill skill = skillUseInfo.getSkill();
        int effectNumber = skill.getImpact();
        int damage = skill.getImpact2();
        ui.useSkill(attackCard, victims, skill, true);
        CardStatusItem statusItem2 = CardStatusItem.grudgeAt(damage,skillUseInfo);
        CardStatusItem statusItem1 = CardStatusItem.slience(skillUseInfo);
        statusItem1.setEffectNumber(effectNumber);
        statusItem2.setEffectNumber(effectNumber);
        for (CardInfo victim : victims) {
            if (!resolver.resolveAttackBlockingSkills(attackCard, victim, skill, 1).isAttackable()) {
                continue;
            }
            if(effectNumber>0)
            {
                if(!victim.getStatus().getStatusOf(CardStatusType.咒恨).isEmpty()){
                    victim.removeForce(CardStatusType.咒恨);
                    victim.removeForce(CardStatusType.沉默);
                }
            }
            ui.addCardStatus(attackCard, victim, skill, statusItem2);
            if(!victim.isDeman()) {
                victim.addStatus(statusItem1);
            }
            victim.addStatus(statusItem2);

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
                        if (!randomVictim.getStatus().getStatusOf(CardStatusType.咒恨).isEmpty()) {
                            randomVictim.removeForce(CardStatusType.咒恨);
                            randomVictim.removeForce(CardStatusType.沉默);
                        }
                    }
                    ui.addCardStatus(attackCard, randomVictim, skill, statusItem2);
                    if(!randomVictim.isDeman()) {
                        randomVictim.addStatus(statusItem1);
                    }
                    randomVictim.addStatus(statusItem2);
                }
            }
        }
    }

    public static void Infected(SkillResolver resolver, CardInfo defendCard) throws HeroDieSignal {
        if (defendCard == null) {
            return;
        }
        Player defenderHero = defendCard.getOwner();
        List<CardStatusItem> items = defendCard.getStatus().getStatusOf(CardStatusType.咒恨);
        CardStatusItem item = items.get(0);
        SkillUseInfo skillUseInfo=item.getCause();
        //CardInfo attackCard = skillUseInfo.getOwner();
        StageInfo stage = resolver.getStage();
        Randomizer random = stage.getRandomizer();
        List<CardInfo> excluCards=new ArrayList<>();
        for(CardInfo fieldCard:defenderHero.getField().getAliveCards())
        {
            if(fieldCard.getStatus().containsStatus(CardStatusType.咒恨))
            {
                excluCards.add(fieldCard);
            }
        }
        List<CardInfo> victims = random.pickRandom(defenderHero.getField().getAliveCards(), 1, true, excluCards);

        if (victims.size() == 0) {
            return;
        }
        GameUI ui = resolver.getStage().getUI();
        Skill skill = skillUseInfo.getSkill();
        int effectNumber = skill.getImpact();
        int damage = skill.getImpact2();
        ui.useSkill(skillUseInfo.getOwner(), victims, skill, true);
        CardStatusItem statusItem2 = CardStatusItem.grudgeAt(damage,skillUseInfo);
        CardStatusItem statusItem1 = CardStatusItem.slience(skillUseInfo);
        statusItem1.setEffectNumber(effectNumber);
        statusItem2.setEffectNumber(effectNumber);
        for (CardInfo victim : victims) {
            if (!resolver.resolveAttackBlockingSkills(skillUseInfo.getOwner(), victim, skill, 1).isAttackable()) {
                continue;
            }
            if(effectNumber>0)
            {
                if(!victim.getStatus().getStatusOf(CardStatusType.咒恨).isEmpty()){
                    victim.removeForce(CardStatusType.咒恨);
                }
            }
            ui.addCardStatus(skillUseInfo.getOwner(), victim, skill, statusItem2);
            if(!victim.isDeman()) {
                victim.addStatus(statusItem1);
            }
            victim.addStatus(statusItem2);
        }
    }
}
