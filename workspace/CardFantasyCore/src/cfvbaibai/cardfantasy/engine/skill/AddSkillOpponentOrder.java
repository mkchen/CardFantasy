package cfvbaibai.cardfantasy.engine.skill;

import cfvbaibai.cardfantasy.data.CardSkill;
import cfvbaibai.cardfantasy.data.Skill;
import cfvbaibai.cardfantasy.data.SkillType;
import cfvbaibai.cardfantasy.engine.*;

import java.util.ArrayList;
import java.util.List;

public class AddSkillOpponentOrder {
    public static void apply(SkillResolver resolver, SkillUseInfo skillUseInfo, CardInfo card, Skill addSkill1,Skill addSkill2,Skill addSkill3,Player defenderHero)throws HeroDieSignal {
        Skill skill = skillUseInfo.getSkill();
        int impact = skill.getImpact();
        CardSkill cardSkill1 = null;
        CardSkill cardSkill2 = null;
        CardSkill cardSkill3 = null;
        if(skill.getType() == SkillType.我要打10个) {
            cardSkill1 = new CardSkill(addSkill1.getType(), addSkill1.getLevel(), 0, false, true, false, false);
            cardSkill2 = new CardSkill(addSkill2.getType(), addSkill2.getLevel(), 0, false, true, false, false);
        }
        List<CardInfo> handCardInfoList = defenderHero.getHand().toList();
        List<CardInfo> orderList = new ArrayList<>();
        orderList.addAll(handCardInfoList);
        int length = orderList.size();
        for(int i=0;i<length;i++){
            for(int j=0;j<length-1-i;j++){
                if (orderList.get(j).getSummonDelay() > orderList.get(j+1).getSummonDelay()) {
                    CardInfo cardInfo = null;
                    cardInfo = orderList.get(j);
                    orderList.set(j,orderList.get(j+1));
                    orderList.set(j+1,cardInfo);
                }
            }
        }
        for(int i=0;i<impact&&i<length;i++){
            CardInfo once = orderList.get(i);
            resolver.getStage().getUI().useSkill(card,once,skill,true);
            SkillUseInfo thisSkillUserInfo1=null;
            SkillUseInfo thisSkillUserInfo2=null;
            SkillUseInfo thisSkillUserInfo3=null;
            if(cardSkill1!=null&&!once.containsUsableSkill(cardSkill1.getType())){
                thisSkillUserInfo1 = new SkillUseInfo(once,cardSkill1);
                thisSkillUserInfo1.setGiveSkill(1);
                once.addSkill(thisSkillUserInfo1);
            }
            if(cardSkill2!=null&&!once.containsUsableSkill(cardSkill2.getType())){
                thisSkillUserInfo2 = new SkillUseInfo(once,cardSkill2);
                thisSkillUserInfo2.setGiveSkill(1);
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
