package cfvbaibai.cardfantasy.engine.skill;

import cfvbaibai.cardfantasy.CardFantasyRuntimeException;
import cfvbaibai.cardfantasy.GameUI;
import cfvbaibai.cardfantasy.data.RuneData;
import cfvbaibai.cardfantasy.data.SkillType;
import cfvbaibai.cardfantasy.engine.*;

import java.util.ArrayList;
import java.util.List;

public final class SummonReturnSkillUseInfoList {
    public static void apply(SkillResolver resolver, SkillUseInfo skillUseInfo, CardInfo attacker) {
        Player player = attacker.getOwner();
        resolver.getStage().getUI().useSkill(attacker, skillUseInfo.getSkill(), true);
        int impact = skillUseInfo.getSkillNumber();
        skillUseInfo.setSkillNumber(impact);
        List<SkillUseInfo> skillUseInfoList = attacker.getOwner().getSummonReturnSkillUseInfoList();
        for(SkillUseInfo existSkillUserInfo:skillUseInfoList){
            if(existSkillUserInfo == skillUseInfo){
                return;
            }
        }
        player.addSummonReturnSkillUseInfoList(skillUseInfo);
    }

    public static void explode(SkillResolver resolver, CardInfo attacker, Player defender) throws HeroDieSignal {

        if(attacker.isBoss() || attacker.isDead()){
            return;
        }
        if (!FailureSkillUseInfoList.exploded(resolver, attacker, defender)) {
            for (RuneInfo rune : attacker.getOwner().getRuneBox().getRunes()) {
                if (!rune.isActivated()) {
                    continue;
                }
                if (rune.is(RuneData.鬼步) && !attacker.isSilent()) {
                    return;
                }
            }
            for (SkillUseInfo blockSkillUseInfo : attacker.getUsableNormalSkills()) {
                if (blockSkillUseInfo.getType() == SkillType.脱困 ||
                        blockSkillUseInfo.getType() == SkillType.神威 ||
                        blockSkillUseInfo.getType() == SkillType.临 ||
                        blockSkillUseInfo.getType() == SkillType.村正 ||
                        blockSkillUseInfo.getType() == SkillType.敏捷 ||
                        blockSkillUseInfo.getType() == SkillType.灵力魔阵 ||
                        blockSkillUseInfo.getType() == SkillType.月之守望 ||
                        blockSkillUseInfo.getType() == SkillType.冰神附体 ||
                        blockSkillUseInfo.getType() == SkillType.以逸待劳 ||
                        blockSkillUseInfo.getType() == SkillType.不灭原核 ||
                        blockSkillUseInfo.getType() == SkillType.黄天当立 ||
                        blockSkillUseInfo.getType() == SkillType.破阵弧光 ||
                        blockSkillUseInfo.getType() == SkillType.时光迁跃 ||
                        blockSkillUseInfo.getType() == SkillType.骑士信仰 ||
                        blockSkillUseInfo.getType() == SkillType.隐蔽 ||
                        blockSkillUseInfo.getType() == SkillType.女武神之辉 ||
                        blockSkillUseInfo.getType() == SkillType.无冕之王 ||
                        blockSkillUseInfo.getType() == SkillType.再生金蝉 ||
                        blockSkillUseInfo.getType() == SkillType.神之守护) {
                    return;
                }
            }
        }
        List<SkillUseInfo> skillUseInfoList = defender.getSummonReturnSkillUseInfoList();
        StageInfo stage = resolver.getStage();
        GameUI ui = stage.getUI();

        List<SkillUseInfo> deleteSkillUseInfoList = new ArrayList<>();
        for(SkillUseInfo skillUseInfo:skillUseInfoList){
            CardInfo cardInfo = (CardInfo) skillUseInfo.getOwner();
            if(cardInfo.isDead()){
                deleteSkillUseInfoList.add(skillUseInfo);
                continue;
            }
            if (!FailureSkillUseInfoList.exploded(resolver, cardInfo, attacker.getOwner())) {
                continue;
            }
            int number = skillUseInfo.getSkill().getImpact2();
            int oldNumber = skillUseInfo.getSkillNumber();
            int rate = oldNumber;
            if(oldNumber<100){
                rate = oldNumber + number;
                skillUseInfo.setSkillNumber(rate);
            }
            if (resolver.getStage().getRandomizer().roll100(rate)) {
                ui.useSkill(cardInfo, attacker, skillUseInfo.getSkill(), true);
                ui.returnCard(cardInfo, attacker, skillUseInfo.getSkill());
                attacker.getOwner().getField().expelCard(attacker.getPosition());
                attacker.setSummonNumber(0);
                attacker.setAddDelay(0);
                attacker.setRuneActive(false);
                resolver.resolveLeaveSkills(attacker);
                if (attacker.isSummonedMinion()) {
                    return;
                }
                attacker.restoreOwner();
                ui.cardToDeck(attacker.getOwner(), attacker);
                attacker.getOwner().getDeck().addCard(attacker);
                attacker.reset();
                return;
            }else{
                ui.useSkill(skillUseInfo.getOwner(), attacker, skillUseInfo.getSkill(), false);
            }

        }
        for(SkillUseInfo deleteSkillUseInfo:deleteSkillUseInfoList){
            defender.removeSummonReturnSkillUseInfoList(deleteSkillUseInfo);
        }
    }

    public static void remove(EntityInfo attacker, SkillResolver resolver) {

        if (attacker == null) {
            throw new CardFantasyRuntimeException("card cannot be null");
        }
        List<SkillUseInfo> skillUseInfoList = attacker.getOwner().getSummonReturnSkillUseInfoList();

        if(skillUseInfoList.size() == 0) {
            return;
        }

        List<SkillUseInfo> removeSkillUserInfoList = new ArrayList<>();

        for(SkillUseInfo skillUseInfo:skillUseInfoList) {
            if(skillUseInfo.getOwner() == attacker) {
                removeSkillUserInfoList.add(skillUseInfo);
            }
        }
        for(SkillUseInfo skillUseInfo:removeSkillUserInfoList) {
            attacker.getOwner().removeSummonReturnSkillUseInfoList(skillUseInfo);
        }
    }

    public static void reset(Player attacker, Player defender) {

        List<SkillUseInfo> attackerSkillUseInfoList = attacker.getSummonReturnSkillUseInfoList();

        List<SkillUseInfo> defenderSkillUseInfoList = defender.getSummonReturnSkillUseInfoList();

        for(SkillUseInfo skillUseInfo:attackerSkillUseInfoList){
            int impact = skillUseInfo.getSkill().getImpact();
            skillUseInfo.setSkillNumber(impact);
        }

        for(SkillUseInfo skillUseInfo:defenderSkillUseInfoList){
            int impact = skillUseInfo.getSkill().getImpact();
            skillUseInfo.setSkillNumber(impact);
        }
    }
}
