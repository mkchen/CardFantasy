package cfvbaibai.cardfantasy.engine.skill;


import cfvbaibai.cardfantasy.CardFantasyRuntimeException;
import cfvbaibai.cardfantasy.data.CardSkill;
import cfvbaibai.cardfantasy.data.Skill;
import cfvbaibai.cardfantasy.data.SkillTag;
import cfvbaibai.cardfantasy.engine.CardInfo;
import cfvbaibai.cardfantasy.engine.SkillResolver;
import cfvbaibai.cardfantasy.engine.SkillUseInfo;

import java.util.ArrayList;
import java.util.List;

//给单个手牌添加技能 等待时间长的优先
public class HandCardAddSkillDelay {
    public static void apply(SkillResolver resolver, SkillUseInfo skillUseInfo, CardInfo card, Skill addSkill) {
        if (card == null) {
            throw new CardFantasyRuntimeException("card should not be null or dead!HandCardAddSkillDelay");
        }
        Skill skill = skillUseInfo.getSkill();
        CardSkill cardSkill = new CardSkill(addSkill.getType(), addSkill.getLevel(), 0, false, false, false, false);
        resolver.getStage().getUI().useSkill(card, skill, true);
        List<CardInfo> allHandCards = card.getOwner().getHand().toList();
        CardInfo oneCard = null;
        List<CardInfo> addCard = new ArrayList<CardInfo>();
        boolean flag = true;
        for (CardInfo ally : allHandCards) {
            if (oneCard != null) {
                if (ally.getSummonDelay() > oneCard.getSummonDelay()) {
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
            if (once.containsUsableSkill(cardSkill.getType())){
                continue;
            }
            SkillUseInfo thisSkillUserInfo= null;
            thisSkillUserInfo = new SkillUseInfo(once,cardSkill);
            thisSkillUserInfo.setGiveSkill(2);
            once.addSkill(thisSkillUserInfo);
        }
    }
}
