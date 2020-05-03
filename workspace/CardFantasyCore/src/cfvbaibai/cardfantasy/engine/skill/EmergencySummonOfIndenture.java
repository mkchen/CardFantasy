package cfvbaibai.cardfantasy.engine.skill;

import cfvbaibai.cardfantasy.GameUI;
import cfvbaibai.cardfantasy.Randomizer;
import cfvbaibai.cardfantasy.engine.*;

import java.util.ArrayList;
import java.util.List;

public class EmergencySummonOfIndenture {
    public static void apply(SkillResolver resolver, SkillUseInfo skillUseInfo, CardInfo attacker) throws HeroDieSignal {

        Player player = attacker.getOwner();
        List<IndentureInfo> readyIndentureList = new ArrayList<>();

        for (IndentureInfo indenture : player.getIndentureBox().getIndentureInfos()) {
            CardInfo cardInfo = indenture.getCardInfo();
            if(cardInfo.isDead()){
                readyIndentureList.add(indenture);
            }
        }
        StageInfo stage = resolver.getStage();
        Randomizer random = stage.getRandomizer();
        GameUI ui = stage.getUI();

        int victimCount = skillUseInfo.getSkill().getImpact();
        List<IndentureInfo> summonList = random.pickRandom(readyIndentureList, victimCount, true, null);
        if(summonList.size()<=0){
            return;
        }
        ui.useSkill(attacker,summonList,skillUseInfo.getSkill(),true);
        for(IndentureInfo indentureInfo:summonList) {
            CardInfo cardInfo = indentureInfo.getCardInfo();
            SkillUseInfo indentureSkillUseInfo = indentureInfo.getSkillUseInfo();
            cardInfo.reset();
            CardStatusItem summonedStatusItem = CardStatusItem.summoned(indentureSkillUseInfo);
            resolver.getStage().getUI().addCardStatus(indentureInfo, cardInfo, indentureSkillUseInfo.getSkill(), summonedStatusItem);
            cardInfo.addStatus(summonedStatusItem);
            resolver.summonCardIndenture(player, cardInfo, indentureInfo, true, indentureSkillUseInfo.getSkill(), 1);
        }
    }
}
