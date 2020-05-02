package cfvbaibai.cardfantasy.engine.skill;

import cfvbaibai.cardfantasy.GameUI;
import cfvbaibai.cardfantasy.Randomizer;
import cfvbaibai.cardfantasy.data.Skill;
import cfvbaibai.cardfantasy.engine.*;

import java.util.List;

public final class TaoistNature {
    public static void apply(SkillResolver resolver, SkillUseInfo skillUseInfo, CardInfo attacker, Player defender) throws HeroDieSignal {
        StageInfo stage = resolver.getStage();
        GameUI ui = stage.getUI();

        List<CardInfo> livingCards = attacker.getOwner().getField().getAliveCards();

        /*天:1,地:10,人:100 1.死亡链接,2.古神的低语3.圣炎,10.全体送还,20.地裂,30:全体阻碍2,100:祈愿2,200:集结,300:全体加速2*/
        int judgeNumber = 0;
        for(CardInfo liveCard:livingCards){
            if("天".equals(liveCard.getName())){
                judgeNumber += 1;
            } else if("地".equals(liveCard.getName())){
                judgeNumber += 10;
            } else if("人".equals(liveCard.getName())){
                judgeNumber += 100;
            }
        }
        if(judgeNumber >=300){
            AllSpeedUp.apply(skillUseInfo, resolver, attacker);
        } else if(judgeNumber >=200){
            HandCardAddSkillNormalType.apply(resolver,skillUseInfo.getAttachedUseInfo1(),attacker,skillUseInfo.getAttachedUseInfo1().getAttachedUseInfo1().getSkill(),1,1);
        } else if(judgeNumber >=100){
            Supplication.apply(resolver, skillUseInfo, attacker, defender);
        } else if(judgeNumber >=30){
            AllDelay.apply(skillUseInfo, resolver, attacker, defender);
        } else if(judgeNumber >=20){
            GiantEarthquakesLandslides.apply(resolver, skillUseInfo.getSkill(), attacker, defender, 1);
        } else if(judgeNumber >=10){
            ReturnNumber.apply(resolver, skillUseInfo.getSkill(), attacker, defender, -1);
        } else if(judgeNumber >=3){
            HolyFire.apply(skillUseInfo.getSkill(), resolver, attacker, defender);
        } else if(judgeNumber >=2){
            Ancient.apply(resolver, skillUseInfo, attacker, defender, 3, 1);
        } else if(judgeNumber >=1){
            SoulLink.apply(resolver, skillUseInfo, attacker, defender, 5, 3);
        }
    }
}
