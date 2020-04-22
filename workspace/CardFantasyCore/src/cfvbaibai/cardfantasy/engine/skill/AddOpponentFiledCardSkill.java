package cfvbaibai.cardfantasy.engine.skill;


import cfvbaibai.cardfantasy.CardFantasyRuntimeException;
import cfvbaibai.cardfantasy.GameUI;
import cfvbaibai.cardfantasy.Randomizer;
import cfvbaibai.cardfantasy.data.CardSkill;
import cfvbaibai.cardfantasy.data.Skill;
import cfvbaibai.cardfantasy.data.SkillType;
import cfvbaibai.cardfantasy.engine.*;

import java.util.List;

public final class AddOpponentFiledCardSkill {
    public static void apply(SkillResolver resolver, SkillUseInfo skillUseInfo, CardInfo card, Skill addSkill, Player defender) {
        if (card == null || card.isDead()) {
            throw new CardFantasyRuntimeException("card should not be null or dead!AddOpponentFiledCardSkill");
        }
        StageInfo stage = resolver.getStage();
        Randomizer random = stage.getRandomizer();
        GameUI ui = stage.getUI();
        Skill skill = skillUseInfo.getSkill();
        CardSkill cardSkill1 = null;
        cardSkill1 = new CardSkill(addSkill.getType(), addSkill.getLevel(), 0, false, false, false, false);
        resolver.getStage().getUI().useSkill(card, skill, true);
        int victimCount = skill.getImpact();
        List<CardInfo> addCard = random.pickRandom(defender.getField().toList(), victimCount, true, null);
        for (CardInfo thisCard : addCard) {
            SkillUseInfo thisSkillUserInfo1=null;
            if(cardSkill1!=null&&!thisCard.containsUsableSkill(cardSkill1.getType())){
                thisSkillUserInfo1 = new SkillUseInfo(thisCard,cardSkill1);
                thisSkillUserInfo1.setGiveSkill(2);
                thisCard.addSkill(thisSkillUserInfo1);
            }
        }
    }
}
