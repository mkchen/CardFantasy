package cfvbaibai.cardfantasy.engine.skill;


import cfvbaibai.cardfantasy.CardFantasyRuntimeException;
import cfvbaibai.cardfantasy.GameUI;
import cfvbaibai.cardfantasy.Randomizer;
import cfvbaibai.cardfantasy.data.CardSkill;
import cfvbaibai.cardfantasy.data.Skill;
import cfvbaibai.cardfantasy.data.SkillType;
import cfvbaibai.cardfantasy.engine.CardInfo;
import cfvbaibai.cardfantasy.engine.SkillResolver;
import cfvbaibai.cardfantasy.engine.SkillUseInfo;
import cfvbaibai.cardfantasy.engine.StageInfo;

import java.util.ArrayList;
import java.util.List;

public final class AddHandOneCardMultSkill {
    public static void apply(SkillResolver resolver, SkillUseInfo skillUseInfo, CardInfo card, Skill addSkill1,Skill addSkill2,Skill addSkill3) {
        if (card == null) {
            throw new CardFantasyRuntimeException("card should not be null or dead!AddHandOneCardMultSkill");
        }
        Skill skill = skillUseInfo.getSkill();
        CardSkill cardSkill1 = null;
        CardSkill cardSkill2 = null;
        CardSkill cardSkill3 = null;
        if(skill.getType() == SkillType.余音袅袅) {
            cardSkill1 = new CardSkill(addSkill1.getType(), addSkill1.getLevel(), 0, true, false, false, false);
            cardSkill2 = new CardSkill(addSkill2.getType(), addSkill2.getLevel(), 0, true, false, false, false);
        }
        resolver.getStage().getUI().useSkill(card, skill, true);
        List<CardInfo> allHandCards = card.getOwner().getHand().toList();
        CardInfo oneCard = null;
        List<CardInfo> addCard = new ArrayList<CardInfo>();
        boolean flag = true;
        for (CardInfo ally : allHandCards) {
            if (oneCard != null) {
                if (ally.getSummonDelay() < oneCard.getSummonDelay()) {
                    oneCard = ally;
                }
            } else {
                oneCard = ally;
            }
        }
        if (oneCard != null) {
            addCard.add(oneCard);
        }
        for (CardInfo once : addCard) {
            SkillUseInfo thisSkillUserInfo1=null;
            SkillUseInfo thisSkillUserInfo2=null;
            SkillUseInfo thisSkillUserInfo3=null;
            if(cardSkill1!=null&&!once.containsUsableSkill(cardSkill1.getType())){
                thisSkillUserInfo1 = new SkillUseInfo(once,cardSkill1);
                thisSkillUserInfo1.setGiveSkill(2);
                once.addSkill(thisSkillUserInfo1);
            }
            if(cardSkill2!=null&&!once.containsUsableSkill(cardSkill2.getType())){
                thisSkillUserInfo2 = new SkillUseInfo(once,cardSkill2);
                thisSkillUserInfo2.setGiveSkill(2);
                once.addSkill(thisSkillUserInfo2);
            }
            if(cardSkill3!=null&&!once.containsUsableSkill(cardSkill3.getType())){
                thisSkillUserInfo3 = new SkillUseInfo(once,cardSkill3);
                thisSkillUserInfo3.setGiveSkill(2);
                once.addSkill(thisSkillUserInfo3);
            }
        }
    }
}
