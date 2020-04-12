package cfvbaibai.cardfantasy.engine.skill;


import cfvbaibai.cardfantasy.CardFantasyRuntimeException;
import cfvbaibai.cardfantasy.GameUI;
import cfvbaibai.cardfantasy.Randomizer;
import cfvbaibai.cardfantasy.data.CardSkill;
import cfvbaibai.cardfantasy.data.Skill;
import cfvbaibai.cardfantasy.data.SkillType;
import cfvbaibai.cardfantasy.engine.*;

import java.util.List;

public final class AddFiledCardMultSkillByEquipment {
    public static void apply(SkillResolver resolver, SkillUseInfo skillUseInfo, EntityInfo attacker, Skill addSkill1) {
        StageInfo stage = resolver.getStage();
        Randomizer random = stage.getRandomizer();
        GameUI ui = stage.getUI();
        Skill skill = skillUseInfo.getSkill();
        CardSkill cardSkill1 = null;
        int victimCount = skill.getImpact();
        cardSkill1 = new CardSkill(addSkill1.getType(), addSkill1.getLevel(), 0, false, false, false, false);
        for (int i=0;i<victimCount;i++) {
            resolver.getStage().getUI().useSkill(attacker, skill, true);
            List<CardInfo> addCard = random.pickRandom(attacker.getOwner().getField().toList(), 1, true, null);
            if(addCard.size()==0){
                break;
            }
            for(CardInfo thisCard:addCard) {
                SkillUseInfo thisSkillUserInfo1 = null;
                if (cardSkill1 != null && !thisCard.containsUsableSkill(cardSkill1.getType())) {
                    thisSkillUserInfo1 = new SkillUseInfo(thisCard, cardSkill1);
                    thisSkillUserInfo1.setGiveSkill(2);
                    thisCard.addSkill(thisSkillUserInfo1);
                }
            }
        }
    }
}
