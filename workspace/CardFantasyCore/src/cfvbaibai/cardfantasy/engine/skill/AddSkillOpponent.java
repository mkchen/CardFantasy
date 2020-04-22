package cfvbaibai.cardfantasy.engine.skill;

import cfvbaibai.cardfantasy.CardFantasyRuntimeException;
import cfvbaibai.cardfantasy.Randomizer;
import cfvbaibai.cardfantasy.data.CardSkill;
import cfvbaibai.cardfantasy.data.Skill;
import cfvbaibai.cardfantasy.data.SkillType;
import cfvbaibai.cardfantasy.engine.*;

import java.util.ArrayList;
import java.util.List;

public class AddSkillOpponent {
    public static void apply(SkillResolver resolver, SkillUseInfo skillUseInfo, CardInfo card, Skill addSkill,int number,Player defenderHero,int skillType)throws HeroDieSignal {
        if (card == null || card.isDead()) {
            throw new CardFantasyRuntimeException("card should not be null or dead!AddSkillOpponent");
        }
        Skill skill = skillUseInfo.getSkill();
        boolean precastSkill =false;
        boolean summonSkill =false;
        boolean deathSkill =false;
        boolean postcastSkill = false;
        if(skillType ==1)
        {
            precastSkill = true;
        }
        else if(skillType == 2)
        {
            summonSkill = true;
        }
        else if(skillType == 3)
        {
            deathSkill = true;
        }
        else if(skillType ==4)
        {
            postcastSkill = true;
        }
        CardSkill cardSkill = new CardSkill(addSkill.getType(), addSkill.getLevel(), 0, summonSkill, deathSkill, precastSkill, postcastSkill);
        resolver.getStage().getUI().useSkill(card, skill, true);
        List<CardInfo> allHandCards = defenderHero.getHand().toList();
        List<CardInfo> addCard= new ArrayList<CardInfo>();
        List<CardInfo> revivableCards = new ArrayList<CardInfo>();
        boolean flag = true;
        for (CardInfo handCard : allHandCards) {
//            for(SkillUseInfo skillInfo:handCard.getSkillUserInfos())
//            {
//                if(skillInfo.getGiveSkill()==2)
//                {
//                    flag=false;
//                    break;
//                }
//            }
//            if(!flag)
//            {
//                flag =true;
//                continue;
//            }
            if (handCard != null && !handCard.containsAllSkill(addSkill.getType())) {
                revivableCards.add(handCard);
            }
        }
        if (revivableCards.isEmpty()) {
            return;
        }
        addCard = Randomizer.getRandomizer().pickRandom(
                revivableCards, number, true, null);

        for (CardInfo once : addCard) {
            OnAttackBlockingResult result = resolver.resolveAttackBlockingSkills(card, once, skill, 1);
            if(!result.isAttackable()) {
                continue;
            }
            if(once.containsAllSkill(addSkill.getType()))
            {
                continue;
            }
            SkillUseInfo thisSkillUserInfo=null;
            thisSkillUserInfo = new SkillUseInfo(once,cardSkill);
            thisSkillUserInfo.setGiveSkill(2);
            once.addSkill(thisSkillUserInfo);
        }
    }

}
