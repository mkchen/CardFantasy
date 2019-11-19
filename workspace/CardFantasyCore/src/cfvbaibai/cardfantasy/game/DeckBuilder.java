package cfvbaibai.cardfantasy.game;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import cfvbaibai.cardfantasy.data.*;
import org.apache.commons.lang3.StringUtils;

import cfvbaibai.cardfantasy.DeckBuildRuntimeException;

public final class DeckBuilder {

    private static CardDataStore store;

    /**
     * C卡片名-等级+技能名技能等级*数量
     */
    private final static String CARD_REGEX = 
        "^" +
        "(?<CardName>[^\\-+SD*?]+)" +
        "(\\+SK1(?<SummonFlag1>(S|降临)?)(?<DeathFlag1>(D|死契)?)(?<PrecastFlag1>(PRE|先机)?)(?<PostcastFlag1>(POST|遗志)?)" +
        "(?<SkillName1>[^\\d\\-*?]+)(?<SkillLevel1>\\d+)?)?" +
        "(\\+SK2(?<SummonFlag2>(S|降临)?)(?<DeathFlag2>(D|死契)?)(?<PrecastFlag2>(PRE|先机)?)(?<PostcastFlag2>(POST|遗志)?)" +
        "(?<SkillName2>[^\\d\\-*?]+)(?<SkillLevel2>\\d+)?)?" +
        "(\\+SK3(?<SummonFlag3>(S|降临)?)(?<DeathFlag3>(D|死契)?)(?<PrecastFlag3>(PRE|先机)?)(?<PostcastFlag3>(POST|遗志)?)" +
        "(?<SkillName3>[^\\d\\-*?]+)(?<SkillLevel3>\\d+)?)?" +
        "(\\+(?<SummonFlag>(S|降临)?)(?<DeathFlag>(D|死契)?)(?<PrecastFlag>(PRE|先机)?)(?<PostcastFlag>(POST|遗志)?)" +
        "(?<ExtraSkillName>[^\\d\\-*?]+)(?<ExtraSkillLevel>\\d+)?)?" +
        "(\\-(?<CardLevel>\\d+))?" +
        "(\\*(?<Count>\\d+))?" +
        "(\\?HP(?<HP>\\d+))?" +
        "(\\?AT(?<AT>\\d+))?" +
        "(\\?DE(?<Delay>\\d+))?" +
        "$";

    /**
     * Q卡片名-等级+技能名技能等级*数量
     */
    private final static String INDEBTURE_REGEX =
//            "^" +
//                    "(?<CardName>[^\\-+SD*?]+)" +
//                    "(\\+(?<SummonFlag>(S|降临)?)(?<DeathFlag>(D|死契)?)(?<PrecastFlag>(PRE|先机)?)(?<PostcastFlag>(POST|遗志)?)" +
//                    "(?<ExtraSkillName>[^\\d\\-*?]+)(?<ExtraSkillLevel>\\d+)?)?" +
//                    "(\\-(?<CardLevel>\\d+))?" +
//                    "(\\*(?<Count>\\d+))?" +
////                    "(\\+IN(?<IndentureName>[^\\d\\-*?]+)(?<IndentureLevel>\\d+)?)?" +
//                    "$";
            "^" +
                    "(?<CardName>[^\\-+SD*]+)" +
                    "(\\+(?<SummonFlag>(S|降临)?)(?<DeathFlag>(D|死契)?)(?<PrecastFlag>(PRE|先机)?)(?<PostcastFlag>(POST|遗志)?)" +
                    "(?<ExtraSkillName>[^\\d\\-*?]+)(?<ExtraSkillLevel>\\d+)?)?" +
                    "(\\-(?<CardLevel>\\d+))?" +
                    "(\\*(?<Count>\\d+))?" +
                    "(\\?IN(?<IndentureName>[^\\d\\-*?]+)(?<IndentureLevel>\\d+)?)?" +
                    "$";

    /**
     * Z-装备血量+技能名技能等级*数量
     */
    private final static String EQUIPMENT_REGEX =
            "^" +
                    "(\\-HP(?<HP>\\d+))?" +
                    "(\\+(?<ExtraSkillName>[^\\d\\-*?]+)(?<ExtraSkillLevel>\\d+)?)?" +
                    "(\\+(?<ExtraSkillName2>[^\\d\\-*?]+)(?<ExtraSkillLevel2>\\d+)?)?" +
                    "(\\+(?<ExtraSkillName3>[^\\d\\-*?]+)(?<ExtraSkillLevel3>\\d+)?)?" +
                    "(\\*(?<Count>\\d+))?" +
                    "$";

    private static Pattern CARD_PATTERN;

    static {
        try {
            CARD_PATTERN = Pattern.compile(CARD_REGEX);
            store = CardDataStore.loadDefault();
        } catch (Exception e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }

    private static Pattern INDEBTURE_PATTERN;

    static {
        try {
            INDEBTURE_PATTERN = Pattern.compile(INDEBTURE_REGEX);
            store = CardDataStore.loadDefault();
        } catch (Exception e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }

    private static Pattern EQUIPMENT_PATTERN;

    static {
        try {
            EQUIPMENT_PATTERN = Pattern.compile(EQUIPMENT_REGEX);
            store = CardDataStore.loadDefault();
        } catch (Exception e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }

    public static String[] splitDescsText(String descsText) {
        descsText = descsText.replace(' ', ',').replace('　', ',').replace('，', ',').replace('、', ',');
        descsText = descsText.replace("\r\n", ",").replace("\n", ",");
        descsText = descsText.replace('＋', '+').replace('＊', '*').replace('－', '-')
                .replace('？', '?').replace('h', 'H').replace('p', 'P').replace('a', 'A').replace('d', 'D')
                .replace('t', 'T').replace('s', 'S').replace('k', 'K').replace('i', 'I').replace('n', 'N')
                .replace('z', 'Z');
        descsText = descsText.replace(":", "").replace("：", "").replace("·", "");
        descsText = Zht2Zhs.getInstance().convert(descsText);
        return descsText.split(",");
    }
    public static DeckStartupInfo multiBuild(String descsText) {
        return build(splitDescsText(descsText));
    }

    public static DeckStartupInfo build(String ... descs) {
        DeckStartupInfo deck = new DeckStartupInfo();
        for (String desc : descs) {
            if (desc == null || desc.length() == 0) {
                continue;
            }
            desc = desc.trim();
            if (desc.length() > 1 && desc.charAt(0) == 'C') {
                parseAndAddCard(deck, desc.substring(1));
            } else if (desc.length() > 1 && desc.charAt(0) == 'R') {
                parseAndAddRune(deck, desc.substring(1));
            } else if (desc.length() > 1 && desc.charAt(0) == 'Q') {
                parseAndAddIndenture(deck, desc.substring(1));
            } else if (desc.length() > 1 && desc.charAt(0) == 'Z') {
                parseAndAddEquipment(deck, desc.substring(1));
            } else {
                if (!parseAndAddCard(deck, desc)) {
                    if (!parseAndAddRune(deck, desc)) {
                        throw new DeckBuildRuntimeException("无效的卡牌或符文: " + desc);
                    }
                }
            }
        }
        return deck;
    }
    
    private static boolean parseAndAddRune(DeckStartupInfo deck, String desc) {
        if (desc.contains("*0")) {
            return true;
        }
        Rune rune = parseRuneDesc(desc);

        if (rune == null) {
            return false;
        }

        deck.addRune(rune);
        return true;
    }

    public static Rune parseRuneDesc(String desc) {
        String runeDesc = desc;
        int iDash = runeDesc.indexOf('-');
        int runeLevel = 4;
        String runeName = runeDesc;
        if (iDash >= 0) {
            try {
                runeLevel = Integer.parseInt(runeDesc.substring(iDash + 1));
            } catch (NumberFormatException e) {
                throw new DeckBuildRuntimeException("无效的符文: " + runeDesc, e);
            }
            runeName = runeDesc.substring(0, iDash);
        }
        
        RuneData runeData = null;
        try {
            runeData = RuneData.valueOf(runeName);
        } catch (IllegalArgumentException e) {
            return null;
        }

        Rune rune = new Rune(runeData, 0);
        rune.growToLevel(runeLevel);
        return rune;
    }
    
    private static int cardNameSuffix = 0;
    private static int getCardNameSuffix() {
        ++cardNameSuffix;
        cardNameSuffix %= 100;
        return cardNameSuffix;
    }
    
    private static boolean parseAndAddCard(DeckStartupInfo deck, String desc) {
        List<Card> cards = parseCardDesc(desc);

        if (cards == null) {
            return false;
        }

        for (Card c : cards) {
            deck.addCard(c);
        }

        return true;
    }

    /**
     * Card description text pattern:
     * C卡片名-等级+技能名技能等级*数量
     * Example: C金属巨龙-10+暴风雪1*5
     * @param desc
     * @param desc
     */
    public static List<Card> parseCardDesc(String desc) {
        List<Card> ret = new ArrayList<Card>();

        String cardDesc = desc;
        Matcher matcher = CARD_PATTERN.matcher(cardDesc);
        if (!matcher.matches()) {
            throw new DeckBuildRuntimeException("无效的卡牌: " + desc);
        }
        String cardName = matcher.group("CardName");
        String cardLevelText = matcher.group("CardLevel");
        int cardLevel = 10;
        if (cardLevelText != null) {
            try {
                cardLevel = Integer.parseInt(cardLevelText);
            } catch (NumberFormatException e) {
                throw new DeckBuildRuntimeException("无效的卡牌: " + desc, e);
            }
        }
        String extraSkillName = matcher.group("ExtraSkillName");
        SkillType extraSkillType = null;
        if (extraSkillName != null) {
            try {
                extraSkillType = SkillType.valueOf(extraSkillName);
            } catch (IllegalArgumentException e) {
                throw new DeckBuildRuntimeException("无效的卡牌: " + desc, e);
            }
        }
        if (extraSkillType != null && cardLevelText == null) {
            cardLevel = 15;
        }
        String extraSkillLevelText = matcher.group("ExtraSkillLevel");
        int extraSkillLevel = 0;
        if (extraSkillLevelText != null) {
            try {
                extraSkillLevel = Integer.parseInt(extraSkillLevelText);
            } catch (NumberFormatException e) {
                throw new DeckBuildRuntimeException("无效的卡牌: " + desc, e);
            }
        }
        if (extraSkillLevel < 0 || extraSkillLevel > 10) {
            throw new DeckBuildRuntimeException("无效的卡牌：" + desc + "，洗炼技能等级不得大于10");
        }
        String hpText = matcher.group("HP");
        int hp = -1;
        if (hpText != null) {
            try {
                hp = Integer.parseInt(hpText);
            } catch (NumberFormatException e) {
                throw new DeckBuildRuntimeException("无效的卡牌: " + desc, e);
            }
        }
        if (hp <= 0&&hp!=-1 ) {
            throw new DeckBuildRuntimeException("无效的卡牌：" + desc + "，卡牌生命值不得小于0");
        }
        String atText = matcher.group("AT");
        int at = -1;
        if (atText != null) {
            try {
                at = Integer.parseInt(atText);
            } catch (NumberFormatException e) {
                throw new DeckBuildRuntimeException("无效的卡牌: " + desc, e);
            }
        }
        if (at <= 0&&at!=-1 ) {
            throw new DeckBuildRuntimeException("无效的卡牌：" + desc + "，攻击力不得小于0");
        }

        //new add start
       
        boolean summonSkill = !StringUtils.isBlank(matcher.group("SummonFlag"));
        boolean deathSkill = !StringUtils.isBlank(matcher.group("DeathFlag"));
        boolean precastSkill = !StringUtils.isBlank(matcher.group("PrecastFlag"));
        boolean postcastSkill = !StringUtils.isBlank(matcher.group("PostcastFlag"));

        boolean summonSkill1 = !StringUtils.isBlank(matcher.group("SummonFlag1"));
        boolean deathSkill1 = !StringUtils.isBlank(matcher.group("DeathFlag1"));
        boolean precastSkill1 = !StringUtils.isBlank(matcher.group("PrecastFlag1"));
        boolean postcastSkill1 = !StringUtils.isBlank(matcher.group("PostcastFlag1"));

        boolean summonSkill2 = !StringUtils.isBlank(matcher.group("SummonFlag2"));
        boolean deathSkill2 = !StringUtils.isBlank(matcher.group("DeathFlag2"));
        boolean precastSkill2 = !StringUtils.isBlank(matcher.group("PrecastFlag2"));
        boolean postcastSkill2 = !StringUtils.isBlank(matcher.group("PostcastFlag2"));

        boolean summonSkill3 = !StringUtils.isBlank(matcher.group("SummonFlag3"));
        boolean deathSkill3 = !StringUtils.isBlank(matcher.group("DeathFlag3"));
        boolean precastSkill3 = !StringUtils.isBlank(matcher.group("PrecastFlag3"));
        boolean postcastSkill3 = !StringUtils.isBlank(matcher.group("PostcastFlag3"));

        //end

        String countText = matcher.group("Count");
        int count = 1;
        if (countText != null) {
            try {
                count = Integer.parseInt(countText);
            } catch (NumberFormatException e) {
                throw new DeckBuildRuntimeException("无效的卡牌: " + desc, e);
            }
        }

        CardData data = store.getCard(cardName);
        if (data == null) {
            return null;
        }
        
        String prefix = "";
        CardSkill extraSkill = null;
        if (extraSkillType != null) {
            extraSkill = new CardSkill(extraSkillType, extraSkillLevel, 15, summonSkill, deathSkill, precastSkill, postcastSkill);
            prefix = extraSkillName;
            if (extraSkillLevel != 0) {
                prefix += extraSkillLevel;
            }
        }

        //new add start

        String skillName1 = matcher.group("SkillName1");
        SkillType skillType1 = null;
        if (skillName1 != null) {
            try {
                skillType1 = SkillType.valueOf(skillName1);
            } catch (IllegalArgumentException e) {
                throw new DeckBuildRuntimeException("无效的卡牌: " + desc, e);
            }
        }

        String skillName2 = matcher.group("SkillName2");
        SkillType skillType2 = null;
        if (skillName2 != null) {
            try {
                skillType2 = SkillType.valueOf(skillName2);
            } catch (IllegalArgumentException e) {
                throw new DeckBuildRuntimeException("无效的卡牌: " + desc, e);
            }
        }

        String skillName3 = matcher.group("SkillName3");
        SkillType skillType3 = null;
        if (skillName3 != null) {
            try {
                skillType3 = SkillType.valueOf(skillName3);
            } catch (IllegalArgumentException e) {
                throw new DeckBuildRuntimeException("无效的卡牌: " + desc, e);
            }
        }

        String delayText = matcher.group("Delay");
        int delay = 0;
        if (delayText != null) {
            try {
                delay = Integer.parseInt(delayText);
            } catch (NumberFormatException e) {
                throw new DeckBuildRuntimeException("无效的卡牌: " + desc, e);
            }
        }

        String skillLevelText1 = matcher.group("SkillLevel1");
        int skillLevel1 = 0;
        if (skillLevelText1 != null) {
            try {
                skillLevel1 = Integer.parseInt(skillLevelText1);
            } catch (NumberFormatException e) {
                throw new DeckBuildRuntimeException("无效的卡牌: " + desc, e);
            }
        }

        String skillLevelText2 = matcher.group("SkillLevel2");
        int skillLevel2 = 0;
        if (skillLevelText2 != null) {
            try {
                skillLevel2 = Integer.parseInt(skillLevelText2);
            } catch (NumberFormatException e) {
                throw new DeckBuildRuntimeException("无效的卡牌: " + desc, e);
            }
        }

        String skillLevelText3 = matcher.group("SkillLevel3");
        int skillLevel3 = 0;
        if (skillLevelText3 != null) {
            try {
                skillLevel3 = Integer.parseInt(skillLevelText3);
            } catch (NumberFormatException e) {
                throw new DeckBuildRuntimeException("无效的卡牌: " + desc, e);
            }
        }

        CardSkill skill1 = null;
        if (skillType1 != null) {
            skill1 = new CardSkill(skillType1, skillLevel1, 0, summonSkill1, deathSkill1, precastSkill1, postcastSkill1);
        }

        CardSkill skill2 = null;
        if (skillType2 != null) {
            skill2 = new CardSkill(skillType2, skillLevel2, 5, summonSkill2, deathSkill2, precastSkill2, postcastSkill2);
        }

        CardSkill skill3 = null;
        if (skillType3 != null) {
            skill3 = new CardSkill(skillType3, skillLevel3, 10, summonSkill3, deathSkill3, precastSkill3, postcastSkill3);
        }


        //end
        
        for (int j = 0; j < count; ++j) {
            Card card = new Card(data, cardLevel, extraSkill, prefix, String.valueOf(getCardNameSuffix()),hp,at);
            //new add start
            if(skill1!=null)
            {
                card.addNormalSkill(skill1);
            }
            if(skill2!=null)
            {
                card.addNormalSkill(skill2);
            }
            if(skill3!=null)
            {
                card.addNormalSkill(skill3);
            }
            if(delayText!=null)
            {
                card.setSetDelay(delay);
            }

            // new add end
            ret.add(card);
        }
        
        return ret;
    }

    private static boolean parseAndAddIndenture(DeckStartupInfo deck, String desc) {
        List<Indenture> indentures = parseIndentureDesc(desc);

        if (indentures == null) {
            return false;
        }

        for (Indenture indenture : indentures) {
            deck.addIndentures(indenture);
        }

        return true;
    }

    /**
     * Indenture description text pattern:
     * Q卡片名-等级+技能名技能等级*数量?IN契约名称
     * Example: Q金属巨龙-10+暴风雪1*5?IN契约名称
     * @param desc
     * @param desc
     */
    public static List<Indenture> parseIndentureDesc(String desc) {
        List<Indenture> ret = new ArrayList<Indenture>();
        String indentureDesc = desc;
        Matcher matcher = INDEBTURE_PATTERN.matcher(indentureDesc);
        if (!matcher.matches()) {
            throw new DeckBuildRuntimeException("无效的契约: " + desc);
        }
        String countText = matcher.group("Count");
        int count = 1;
        if (countText != null) {
            try {
                count = Integer.parseInt(countText);
            } catch (NumberFormatException e) {
                throw new DeckBuildRuntimeException("无效的卡牌: " + desc, e);
            }
        }
        if(count == 0){
            return  ret;
        }
        String cardName = matcher.group("CardName");
        String cardLevelText = matcher.group("CardLevel");
        int cardLevel = 10;
        if (cardLevelText != null) {
            try {
                cardLevel = Integer.parseInt(cardLevelText);
            } catch (NumberFormatException e) {
                throw new DeckBuildRuntimeException("无效的卡牌: " + desc, e);
            }
        }
        String extraSkillName = matcher.group("ExtraSkillName");
        SkillType extraSkillType = null;
        if (extraSkillName != null) {
            try {
                extraSkillType = SkillType.valueOf(extraSkillName);
            } catch (IllegalArgumentException e) {
                throw new DeckBuildRuntimeException("无效的卡牌: " + desc, e);
            }
        }
        if (extraSkillType != null && cardLevelText == null) {
            cardLevel = 15;
        }
        String extraSkillLevelText = matcher.group("ExtraSkillLevel");
        int extraSkillLevel = 0;
        if (extraSkillLevelText != null) {
            try {
                extraSkillLevel = Integer.parseInt(extraSkillLevelText);
            } catch (NumberFormatException e) {
                throw new DeckBuildRuntimeException("无效的卡牌: " + desc, e);
            }
        }
        if (extraSkillLevel < 0 || extraSkillLevel > 10) {
            throw new DeckBuildRuntimeException("无效的卡牌：" + desc + "，洗炼技能等级不得大于10");
        }

        boolean summonSkill = !StringUtils.isBlank(matcher.group("SummonFlag"));
        boolean deathSkill = !StringUtils.isBlank(matcher.group("DeathFlag"));
        boolean precastSkill = !StringUtils.isBlank(matcher.group("PrecastFlag"));
        boolean postcastSkill = !StringUtils.isBlank(matcher.group("PostcastFlag"));

        String indentureText = matcher.group("IndentureName");
        IndentureData indentureData = null;
        try {
            indentureData = IndentureData.valueOf(indentureText);
        } catch (IllegalArgumentException e) {
            throw new DeckBuildRuntimeException("无效的契约: " + desc, e);
        }

        String indentureLevelText = matcher.group("IndentureLevel");
        int indentureLevel = 1;
        if (indentureLevelText != null) {
            try {
                indentureLevel = Integer.parseInt(indentureLevelText);
            } catch (NumberFormatException e) {
                throw new DeckBuildRuntimeException("无效的契约: " + desc, e);
            }
        }

        CardData data = store.getCard(cardName);
        if (data == null) {
            return null;
        }

        String prefix = "";
        CardSkill extraSkill = null;
        if (extraSkillType != null) {
            extraSkill = new CardSkill(extraSkillType, extraSkillLevel, 15, summonSkill, deathSkill, precastSkill, postcastSkill);
            prefix = extraSkillName;
            if (extraSkillLevel != 0) {
                prefix += extraSkillLevel;
            }
        }

        for (int j = 0; j < count; ++j) {
            Card card = new Card(data, cardLevel, extraSkill, prefix, String.valueOf(getCardNameSuffix()),-1,-1);
            Indenture indenture = new Indenture(indentureData,card,indentureLevel);
            ret.add(indenture);
        }

        return ret;
    }

    private static boolean parseAndAddEquipment(DeckStartupInfo deck, String desc) {
        List<Equipment> equipments = parseEquipmentDesc(desc);

        if (equipments == null) {
            return false;
        }

        for (Equipment equipment : equipments) {
            deck.addEquipments(equipment);
        }

        return true;
    }

    /**
     * Indenture description text pattern:
     * Z-装备血量+技能经常技能等级
     * Example: Z-Hp500+暴风雪1
     * @param desc
     * @param desc
     */
    public static List<Equipment> parseEquipmentDesc(String desc) {
        List<Equipment> ret = new ArrayList<Equipment>();
        String equipmentDesc = desc;
        Matcher matcher = EQUIPMENT_PATTERN.matcher(equipmentDesc);
        if (!matcher.matches()) {
            throw new DeckBuildRuntimeException("无效的装备: " + desc);
        }
        String countText = matcher.group("Count");
        int count = 1;
        if (countText != null) {
            try {
                count = Integer.parseInt(countText);
            } catch (NumberFormatException e) {
                throw new DeckBuildRuntimeException("无效的装备: " + desc, e);
            }
        }
        if(count == 0){
            return  ret;
        }
        String hpText = matcher.group("HP");
        int hp = 0;
        if (hpText != null) {
            try {
                hp = Integer.parseInt(hpText);
            } catch (NumberFormatException e) {
                throw new DeckBuildRuntimeException("无效的装备: " + desc, e);
            }
        }

        String extraSkillName = matcher.group("ExtraSkillName");
        SkillType extraSkillType = null;
        if (extraSkillName != null) {
            try {
                extraSkillType = SkillType.valueOf(extraSkillName);
            } catch (IllegalArgumentException e) {
                throw new DeckBuildRuntimeException("无效的装备: " + desc, e);
            }
        }

        List<CardSkill> cardSkillList = new ArrayList<>();
        String extraSkillLevelText = matcher.group("ExtraSkillLevel");
        int extraSkillLevel = 0;
        if (extraSkillLevelText != null) {
            try {
                extraSkillLevel = Integer.parseInt(extraSkillLevelText);
            } catch (NumberFormatException e) {
                throw new DeckBuildRuntimeException("无效的装备: " + desc, e);
            }
        }
        if (extraSkillLevel < 0 || extraSkillLevel > 10) {
            throw new DeckBuildRuntimeException("无效的装备：" + desc + "，技能等级不得大于10");
        }

        CardSkill extraSkill = null;
        if (extraSkillType != null) {
            extraSkill = new CardSkill(extraSkillType, extraSkillLevel, 15, false, false, false, false);
        }

        String extraSkillName2 = matcher.group("ExtraSkillName2");
        SkillType extraSkillType2 = null;
        if (extraSkillName2 != null) {
            try {
                extraSkillType2 = SkillType.valueOf(extraSkillName2);
            } catch (IllegalArgumentException e) {
                throw new DeckBuildRuntimeException("无效的装备: " + desc, e);
            }
        }
        String extraSkillLevel2Text = matcher.group("ExtraSkillLevel2");
        int extraSkillLevel2 = 0;
        if (extraSkillLevel2Text != null) {
            try {
                extraSkillLevel2 = Integer.parseInt(extraSkillLevel2Text);
            } catch (NumberFormatException e) {
                throw new DeckBuildRuntimeException("无效的装备: " + desc, e);
            }
        }
        if (extraSkillLevel2 < 0 || extraSkillLevel2 > 10) {
            throw new DeckBuildRuntimeException("无效的装备：" + desc + "，技能等级不得大于10");
        }

        CardSkill extraSkill2 = null;
        if (extraSkillType2 != null) {
            extraSkill2 = new CardSkill(extraSkillType2, extraSkillLevel2, 15, false, false, false, false);
        }

        String extraSkillName3 = matcher.group("ExtraSkillName3");
        SkillType extraSkillType3 = null;
        if (extraSkillName3 != null) {
            try {
                extraSkillType3 = SkillType.valueOf(extraSkillName3);
            } catch (IllegalArgumentException e) {
                throw new DeckBuildRuntimeException("无效的装备: " + desc, e);
            }
        }
        String extraSkillLevel3Text = matcher.group("ExtraSkillLevel3");
        int extraSkillLevel3 = 0;
        if (extraSkillLevel3Text != null) {
            try {
                extraSkillLevel3 = Integer.parseInt(extraSkillLevel3Text);
            } catch (NumberFormatException e) {
                throw new DeckBuildRuntimeException("无效的装备: " + desc, e);
            }
        }
        if (extraSkillLevel3 < 0 || extraSkillLevel3 > 10) {
            throw new DeckBuildRuntimeException("无效的装备：" + desc + "，技能等级不得大于10");
        }

        CardSkill extraSkill3 = null;
        if (extraSkillType3 != null) {
            extraSkill3 = new CardSkill(extraSkillType3, extraSkillLevel3, 15, false, false, false, false);
        }

        for (int j = 0; j < count; ++j) {
            Equipment equipment = new Equipment(hp);
            if(extraSkill!=null){
                equipment.insertSkill(extraSkill);
            }
            if(extraSkill2!=null){
                equipment.insertSkill(extraSkill2);
            }
            if(extraSkill3!=null){
                equipment.insertSkill(extraSkill3);
            }
            ret.add(equipment);
        }

        return ret;
    }

    public static String getDeckDesc(Card card) {
        StringBuffer sb = new StringBuffer();
        sb.append(card.getName());
        Skill extraSkill = card.getExtraSkill();
        if (extraSkill != null) {
            sb.append('+');
            sb.append(getDeckDesc(extraSkill));
        }
        sb.append('-');
        sb.append(card.getLevel());
        return sb.toString();
    }
    
    public static String getDeckDesc(Rune rune) {
        return rune.getName() + rune.getLevel();
    }
    
    public static String getDeckDesc(Skill skill) {
        String desc = skill.getType().name();
        if (skill.getLevel() != 0) {
            desc += skill.getLevel();
        }
        if (skill.isDeathSkill()) {
            return "死契" + desc;
        }
        if (skill.isSummonSkill()) {
            return "降临" + desc;
        }
        if (skill.isPrecastSkill()) {
            return "先机" + desc;
        }
        if (skill.isPostcastSkill()) {
            return "遗志" + desc;
        }
        return desc;
    }

    public static String getSortedDeckDesc(PlayerInfo player) {
        return getSortedDeckDesc(player.getCards(), player.getRunes());
    }

    public static String getSortedDeckDesc(String descsText) {
        DeckStartupInfo dsi = DeckBuilder.multiBuild(descsText);
        return getSortedDeckDesc(dsi.getCards(), dsi.getRunes());
    }
    
    private static String getSortedDeckDesc(Collection <Card> cards, Collection <Rune> runes) {
        List<Card> cardList = new ArrayList<Card>(cards);
        Collections.sort(cardList);
        List<Rune> runeList = new ArrayList<Rune>(runes);
        Collections.sort(runeList);
        StringBuffer sb = new StringBuffer();
        for (Card card : cardList) {
            sb.append(DeckBuilder.getDeckDesc(card));
            sb.append(',');
        }
        for (Rune rune : runeList) {
            sb.append(DeckBuilder.getDeckDesc(rune));
            sb.append(',');
        }
        return sb.toString();
    }
}