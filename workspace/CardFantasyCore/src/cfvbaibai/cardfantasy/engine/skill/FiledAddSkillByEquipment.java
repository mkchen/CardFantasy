package cfvbaibai.cardfantasy.engine.skill;


import cfvbaibai.cardfantasy.CardFantasyRuntimeException;
import cfvbaibai.cardfantasy.GameUI;
import cfvbaibai.cardfantasy.Randomizer;
import cfvbaibai.cardfantasy.data.CardSkill;
import cfvbaibai.cardfantasy.data.Skill;
import cfvbaibai.cardfantasy.engine.*;

import java.util.List;

public final class FiledAddSkillByEquipment {
    public static void apply(SkillResolver resolver, SkillUseInfo skillUseInfo, EntityInfo attacker,Player attacerPlayer, Skill addSkill, int victimCount) {
        StageInfo stage = resolver.getStage();
        Randomizer random = stage.getRandomizer();
        GameUI ui = stage.getUI();

        List<CardInfo> addCard = random.pickRandom(attacerPlayer.getField().toList(), victimCount, true, null);
        Skill skill = skillUseInfo.getSkill();
        CardSkill cardSkill = new CardSkill(addSkill.getType(), addSkill.getLevel(), 0, false, false, false, false);
        resolver.getStage().getUI().useSkill(attacker, skill, true);
        for (CardInfo thisCard : addCard) {
            if(thisCard.containsUsableSkill(cardSkill.getType())){
                continue;
            }
            SkillUseInfo thisSkillUserInfo=null;
            thisSkillUserInfo = new SkillUseInfo(thisCard,cardSkill);
            thisSkillUserInfo.setGiveSkill(1);
            thisCard.addSkill(thisSkillUserInfo);
        }
    }
}
