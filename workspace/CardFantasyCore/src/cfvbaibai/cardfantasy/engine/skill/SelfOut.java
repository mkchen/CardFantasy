package cfvbaibai.cardfantasy.engine.skill;

import cfvbaibai.cardfantasy.GameUI;
import cfvbaibai.cardfantasy.data.Race;
import cfvbaibai.cardfantasy.data.Skill;
import cfvbaibai.cardfantasy.data.SkillType;
import cfvbaibai.cardfantasy.engine.*;

import java.util.ArrayList;
import java.util.List;

public final class SelfOut {
    public static Boolean apply(SkillResolver resolver, Skill cardSkill, CardInfo attacker, String cardName) throws HeroDieSignal{
        GameUI ui = resolver.getStage().getUI();
        if(attacker.isBoss()|| attacker.isDemon()){
            return false;
        }
        if(attacker.isSummonedMinion()){
            return  false;
        }
        if(attacker.isAlive()){
            return false;
        }
        if(!attacker.getOwner().getBeforeDeath().contains(attacker)){
            return false;
        }

        List<CardInfo> fieldList = attacker.getOwner().getField().getAliveCards();

        boolean isExist = false;

        for(CardInfo cardInfo:fieldList){
            if(cardName.equals(cardInfo.getName())){
                isExist = true;
                break;
            }
        }
        if(isExist){
            return false;
        }
        attacker.getOwner().getBeforeDeath().removeCard(attacker);
        attacker.restoreOwner();
        ui.useSkill(attacker,attacker,cardSkill,true);
        ui.cardToOutField(attacker.getOwner(), attacker);
        attacker.getOwner().getOutField().addCard(attacker);
        return true;
    }
}
