package cfvbaibai.cardfantasy.engine.skill;

import cfvbaibai.cardfantasy.GameUI;
import cfvbaibai.cardfantasy.data.Skill;
import cfvbaibai.cardfantasy.engine.*;

import java.util.List;

public class UnbendingHero {
    public static boolean apply(SkillUseInfo skillUseInfo, SkillResolver resolver, CardInfo card,Player defender) {
        int impact = skillUseInfo.getSkill().getImpact();
        if(skillUseInfo.getSkillNumber()==0)
        {
            return false;
        }
        if(skillUseInfo.getSkillNumber()==-1)
        {
            skillUseInfo.setSkillNumber(impact);
        }
        skillUseInfo.setSkillNumber(skillUseInfo.getSkillNumber()-1);
        GameUI ui = resolver.getStage().getUI();
        Skill skill = skillUseInfo.getSkill();
        ui.useSkill(card, skill, true);
        Field field = defender.getField();
        for (int i = 0; i < field.size(); ++i) {
            CardInfo liveCard = field.getCard(i);
            if (card == liveCard) {
                field.expelCard(i);
                ParadiseLost.removeCard(resolver,card,defender);
                card.getOwner().getGrave().addCard(card);
                break;
            }
        }
        return true;
    }
}
