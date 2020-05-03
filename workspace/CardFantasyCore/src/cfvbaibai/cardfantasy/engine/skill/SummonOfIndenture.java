package cfvbaibai.cardfantasy.engine.skill;

import cfvbaibai.cardfantasy.engine.*;

import java.util.List;

public class SummonOfIndenture {
    public static void apply(SkillResolver resolver, IndentureInfo indentureInfo) throws HeroDieSignal {
        Player owner = indentureInfo.getOwner();
        CardInfo cardInfo = indentureInfo.getCardInfo();
        if(cardInfo.isAlive()){
            return;
        }
        SkillUseInfo skillUseInfo = indentureInfo.getSkillUseInfo();
        cardInfo.reset();
        CardStatusItem summonedStatusItem = CardStatusItem.summoned(skillUseInfo);
        resolver.getStage().getUI().addCardStatus(indentureInfo, cardInfo, skillUseInfo.getSkill(), summonedStatusItem);
        cardInfo.addStatus(summonedStatusItem);
        resolver.summonCardIndenture(owner, cardInfo, indentureInfo, true, skillUseInfo.getSkill(),1);
    }
}
