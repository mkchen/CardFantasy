package cfvbaibai.cardfantasy.engine.skill;


import cfvbaibai.cardfantasy.CardFantasyRuntimeException;
import cfvbaibai.cardfantasy.GameUI;
import cfvbaibai.cardfantasy.Randomizer;
import cfvbaibai.cardfantasy.data.CardSkill;
import cfvbaibai.cardfantasy.data.Skill;
import cfvbaibai.cardfantasy.data.SkillType;
//import cfvbaibai.cardfantasy.engine.CardInfo;
//import cfvbaibai.cardfantasy.engine.SkillResolver;
//import cfvbaibai.cardfantasy.engine.SkillUseInfo;
//import cfvbaibai.cardfantasy.engine.StageInfo;
import cfvbaibai.cardfantasy.engine.*;

import java.util.List;

public final class AddFiledCardMultSkillByEquipmentHWold {
    public static void apply(SkillResolver resolver, SkillUseInfo skillUseInfo, EntityInfo card, Skill addSkill1,Skill addSkill2,Skill addSkill3) {
        if (card == null) {
            throw new CardFantasyRuntimeException("card should not be null!");
        }
        StageInfo stage = resolver.getStage();
        Randomizer random = stage.getRandomizer();
        GameUI ui = stage.getUI();
        Skill skill = skillUseInfo.getSkill();
        int skilllevel = skill.getLevel();
        int victimCount = 1;
        CardSkill cardSkill1 = null;
        CardSkill cardSkill2 = null;
        CardSkill cardSkill3 = null;
        //int victimCount = skill.getImpact();
        //int victimCount = effectCount;
        if(skill.getType() == SkillType.知人善任) {
            cardSkill1 = new CardSkill(addSkill1.getType(), addSkill1.getLevel(), 0, false, false, false, false);
            cardSkill2 = new CardSkill(addSkill2.getType(), addSkill2.getLevel(), 0, false, true, false, false);
            cardSkill3 = new CardSkill(addSkill3.getType(), addSkill3.getLevel(), 0, false, true, false, false);
        } else if(skill.getType() == SkillType.不朽 || skill.getType() == SkillType.装备恶魔重生) {
            cardSkill1 = new CardSkill(addSkill1.getType(), addSkill1.getLevel(), 0, false, false, false, false);
        //} else if(skill.getType() == SkillType.进退自如) {
            //victimCount = skill.getImpact2();
        //    cardSkill1 = new CardSkill(addSkill1.getType(), addSkill1.getLevel(), 0, false, false, false, false);
        }
        resolver.getStage().getUI().useSkill(card, skill, true);

        for(int i=0;i<skilllevel;i++) {
            List<CardInfo> addCard = random.pickRandom(card.getOwner().getField().toList(), victimCount, true, null);
            for (CardInfo thisCard : addCard) {
                SkillUseInfo thisSkillUserInfo1=null;
                SkillUseInfo thisSkillUserInfo2=null;
                SkillUseInfo thisSkillUserInfo3=null;
                if(cardSkill1!=null&&!thisCard.containsUsableSkill(cardSkill1.getType())){
                    thisSkillUserInfo1 = new SkillUseInfo(thisCard,cardSkill1);
                    thisSkillUserInfo1.setGiveSkill(2);
                    thisCard.addSkill(thisSkillUserInfo1);
                }
                if(cardSkill2!=null&&!thisCard.containsUsableSkill(cardSkill2.getType())){
                    thisSkillUserInfo2 = new SkillUseInfo(thisCard,cardSkill2);
                    thisSkillUserInfo2.setGiveSkill(2);
                    thisCard.addSkill(thisSkillUserInfo2);
                }
                if(cardSkill3!=null&&!thisCard.containsUsableSkill(cardSkill3.getType())){
                    thisSkillUserInfo3 = new SkillUseInfo(thisCard,cardSkill3);
                    thisSkillUserInfo3.setGiveSkill(2);
                    thisCard.addSkill(thisSkillUserInfo3);
                }
            }
        }
    }
}
