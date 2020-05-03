package cfvbaibai.cardfantasy.engine.skill;

import cfvbaibai.cardfantasy.GameUI;
import cfvbaibai.cardfantasy.data.Skill;
import cfvbaibai.cardfantasy.engine.CardInfo;
import cfvbaibai.cardfantasy.engine.HeroDieSignal;
import cfvbaibai.cardfantasy.engine.Player;
import cfvbaibai.cardfantasy.engine.SkillResolver;

import java.util.List;

public final class SelfOutOpponent {
    public static Boolean apply(SkillResolver resolver, Skill cardSkill, CardInfo attacker, String cardName, Player defender) throws HeroDieSignal{
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
        List<CardInfo> fieldList = defender.getField().getAliveCards();

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
