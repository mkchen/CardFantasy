package cfvbaibai.cardfantasy.engine.skill;

import cfvbaibai.cardfantasy.Randomizer;
import cfvbaibai.cardfantasy.data.CardSkill;
import cfvbaibai.cardfantasy.data.Skill;
import cfvbaibai.cardfantasy.data.SkillType;
import cfvbaibai.cardfantasy.engine.*;

import java.util.ArrayList;
import java.util.List;

public final class HellPrison {
    public static void apply(SkillResolver resolver, Player player, Player defender) throws HeroDieSignal
    {
        List<CardInfo> allHandCards = defender.getHand().toList();
        StageInfo stage = resolver.getStage();
        Randomizer random = stage.getRandomizer();
        List<CardInfo> fileCard = random.pickRandom(player.getField().toList(), -1, true, null);
        if(resolver.resolveStopDelay(defender))
        {
            return;
        }
        for(CardInfo attackerCard :fileCard) {
            if(attackerCard==null)
            {
                continue;
            }
            for(SkillUseInfo skillUseInfo :attackerCard.getAllUsableSkills()) {
                if(skillUseInfo.getSkill().getType()==SkillType.冥狱牢囚 || skillUseInfo.getSkill().getType()==SkillType.雷狱牢囚 || skillUseInfo.getSkill().getType()==SkillType.食梦) {
                    resolver.getStage().getUI().useSkill(attackerCard, allHandCards, skillUseInfo.getSkill(), true);
                    for (CardInfo card : allHandCards) {
                        if(resolver.resolveStopCardDelay(card))
                        {
                            continue;
                        }
                        resolver.getStage().getUI().useSkill(attackerCard, allHandCards, skillUseInfo.getSkill(), true);
                        int summonDelay = card.getSummonDelay();
                        resolver.getStage().getUI().increaseSummonDelay(card, skillUseInfo.getSkill().getImpact());
                        card.setSummonDelay(summonDelay + skillUseInfo.getSkill().getImpact());
                    }
                } else if(skillUseInfo.getSkill().getType()==SkillType.坚不可摧) {
                    resolver.getStage().getUI().useSkill(attackerCard, allHandCards, skillUseInfo.getSkill(), true);
                    for (CardInfo card : allHandCards) {
                        if(resolver.resolveStopCardDelay(card)) {
                            continue;
                        }
                        resolver.getStage().getUI().useSkill(attackerCard, allHandCards, skillUseInfo.getSkill(), true);
                        int summonDelay = card.getSummonDelay();
                        resolver.getStage().getUI().increaseSummonDelay(card, skillUseInfo.getSkill().getImpact2());
                        card.setSummonDelay(summonDelay + skillUseInfo.getSkill().getImpact2());
                    }
                } else if(skillUseInfo.getSkill().getType()==SkillType.血祭禁术){
                    Skill addSkill = skillUseInfo.getAttachedUseInfo1().getSkill();
                    CardSkill cardSkill = new CardSkill(addSkill.getType(), addSkill.getLevel(), 0, false, false, false, false);
                    List<CardInfo> addCard= new ArrayList<CardInfo>();
                    List<CardInfo> revivableCards = new ArrayList<CardInfo>();
                    SkillUseInfo thisSkillUserInfo=null;
                    for (CardInfo handCard : allHandCards) {
                        if (handCard != null && !handCard.containsAllSkill(addSkill.getType())) {
                            revivableCards.add(handCard);
                        }
                    }
                    if (revivableCards.isEmpty()) {
                        return;
                    }
                    addCard = Randomizer.getRandomizer().pickRandom(
                            revivableCards, 1, true, null);

                    for (CardInfo once : addCard) {
                        OnAttackBlockingResult result = resolver.resolveAttackBlockingSkills(attackerCard, once, skillUseInfo.getSkill(), 1);
                        if(!result.isAttackable()) {
                            continue;
                        }
                        if(once.containsAllSkill(addSkill.getType()))
                        {
                            continue;
                        }
                        thisSkillUserInfo = new SkillUseInfo(once,cardSkill);
                        thisSkillUserInfo.setGiveSkill(2);
                        once.addSkill(thisSkillUserInfo);
                    }
                }
            }
        }
    }

    public static void applyCoordination(SkillResolver resolver, Player player)
    {
        List<CardInfo> allHandCards = player.getHand().toList();
        StageInfo stage = resolver.getStage();
        Randomizer random = stage.getRandomizer();
        List<CardInfo> fileCard = random.pickRandom(player.getField().toList(), -1, true, null);
        for(CardInfo attackerCard :fileCard) {
            if(attackerCard==null)
            {
                continue;
            }
//            if(!attackerCard.containsUsableSkill(SkillType.蝶息)&&!attackerCard.containsUsableSkill(SkillType.樱蝶加速)&&!attackerCard.containsUsableSkill(SkillType.晦月之咒))
//            {
//                continue;
//            }
            for(SkillUseInfo skillUseInfo :attackerCard.getAllUsableSkills()) {
                if(skillUseInfo.getSkill().getType()==SkillType.蝶息||skillUseInfo.getSkill().getType()==SkillType.樱蝶加速||skillUseInfo.getSkill().getType()==SkillType.晦月之咒
                        ||skillUseInfo.getSkill().getType()==SkillType.邪龙领域) {
                    int summonDelayOffset = skillUseInfo.getAttachedUseInfo2().getSkill().getImpact();
                    resolver.getStage().getUI().useSkill(attackerCard, allHandCards, skillUseInfo.getSkill(), true);
                    for (CardInfo card : allHandCards) {
                        int summonDelay = card.getSummonDelay();
                        int summonDelayOffsetReally = summonDelayOffset;
                        if (summonDelay < summonDelayOffset) {
                            summonDelayOffsetReally = summonDelay;
                        }
                        if (summonDelayOffsetReally == 0) {
                            continue;
                        }
                        resolver.getStage().getUI().increaseSummonDelay(card, -summonDelayOffsetReally);
                        card.setSummonDelay(summonDelay - summonDelayOffsetReally);
                    }
                }
            }
        }
    }
}
