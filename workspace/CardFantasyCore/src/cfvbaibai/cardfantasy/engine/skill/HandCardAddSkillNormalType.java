package cfvbaibai.cardfantasy.engine.skill;

import cfvbaibai.cardfantasy.Randomizer;
import cfvbaibai.cardfantasy.data.CardSkill;
import cfvbaibai.cardfantasy.data.Skill;
import cfvbaibai.cardfantasy.engine.CardInfo;
import cfvbaibai.cardfantasy.engine.SkillResolver;
import cfvbaibai.cardfantasy.engine.SkillUseInfo;

import java.util.ArrayList;
import java.util.List;

public class HandCardAddSkillNormalType {
    public static void apply(SkillResolver resolver, SkillUseInfo skillUseInfo, CardInfo card, Skill addSkill,int number,int type) {
        if (card == null ) {
            return;
        }
        Skill skill = skillUseInfo.getSkill();
        CardSkill cardSkill = null;
        switch (type) {
            case 0:
                cardSkill = new CardSkill(addSkill.getType(), addSkill.getLevel(), 0, false, false, false, false);
                break;
            case 1:
                cardSkill = new CardSkill(addSkill.getType(), addSkill.getLevel(), 0, true, false, false, false);
                break;
            case 2:
                cardSkill = new CardSkill(addSkill.getType(), addSkill.getLevel(), 0, false, true, false, false);
                break;
            case 3:
                cardSkill = new CardSkill(addSkill.getType(), addSkill.getLevel(), 0, false, false, true, false);
                break;
            case 4:
                cardSkill = new CardSkill(addSkill.getType(), addSkill.getLevel(), 0, false, false, false, true);
                break;
        }
        resolver.getStage().getUI().useSkill(card, skill, true);
        List<CardInfo> allHandCards = card.getOwner().getHand().toList();
        List<CardInfo> addCard=new ArrayList<CardInfo>();
        List<CardInfo> revivableCards = new ArrayList<CardInfo>();
        boolean flag = true;
        for (CardInfo handCard : allHandCards) {
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
            if(once.containsAllSkill(addSkill.getType())) {
                continue;
            }
            SkillUseInfo thisSkillUserInfo= null;
            thisSkillUserInfo = new SkillUseInfo(once,cardSkill);
            thisSkillUserInfo.setGiveSkill(2);
            once.addSkill(thisSkillUserInfo);
        }
    }
}
