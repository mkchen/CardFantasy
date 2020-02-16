package cfvbaibai.cardfantasy.engine.skill;

import cfvbaibai.cardfantasy.GameUI;
import cfvbaibai.cardfantasy.Randomizer;
import cfvbaibai.cardfantasy.engine.*;

import java.util.List;

public final class ScatterHereAndThere {
    public static void apply(SkillUseInfo skillUseInfo, SkillResolver resolver, CardInfo attacker) throws HeroDieSignal {
        int impact = 1;
        if(skillUseInfo.getSkillNumber()==0)
        {
            return;
        }
        if(skillUseInfo.getSkillNumber()==-1)
        {
            skillUseInfo.setSkillNumber(impact);
        }
        List<CardInfo> allDeckCards = attacker.getOwner().getDeck().toList();
        if(allDeckCards.size()==0){
            return;
        }
        skillUseInfo.setSkillNumber(skillUseInfo.getSkillNumber()-1);
        StageInfo stage = resolver.getStage();
        Randomizer random = stage.getRandomizer();
        GameUI ui = stage.getUI();

        List<CardInfo> victims = random.pickRandom(allDeckCards, 1, true, null);
        attacker.getOwner().getField().expelCard(attacker.getPosition());
        attacker.restoreOwner();
        attacker.reset();
        ui.cardToDeck(attacker.getOriginalOwner(), attacker);
        attacker.getOriginalOwner().getDeck().addCard(attacker);
        for(CardInfo cardInfo:victims){
            ui.useSkill(attacker, cardInfo, skillUseInfo.getSkill(), true);
            resolver.summonCardScatter(cardInfo.getOwner(), cardInfo, attacker, false, skillUseInfo,0);
        }
    }

    public static void reset( SkillUseInfo skillUseInfo, CardInfo card) throws HeroDieSignal {
        skillUseInfo.setSkillNumber(1);
    }
}
