package cfvbaibai.cardfantasy.game;

import cfvbaibai.cardfantasy.CardFantasyRuntimeException;
import cfvbaibai.cardfantasy.GameUI;
import cfvbaibai.cardfantasy.data.PlayerCardBuffSkill;
import cfvbaibai.cardfantasy.data.PlayerInfo;
import cfvbaibai.cardfantasy.data.Skill;
import cfvbaibai.cardfantasy.data.SkillType;
import cfvbaibai.cardfantasy.engine.BattleEngine;
import cfvbaibai.cardfantasy.engine.GameEndCause;
import cfvbaibai.cardfantasy.engine.GameResult;
import cfvbaibai.cardfantasy.engine.Rule;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import java.util.Random;

public class PvdEngine extends GameEngine {
    protected DungeonsStages dungeons;
    public PvdEngine(GameUI ui, DungeonsStages dungeons) {
        super(ui, Rule.getMapBattle());
        this.dungeons = dungeons;
    }

    public PveGameResult play(PlayerInfo player, String mapId,Rule rule,int p1HeroHpBuff,int p1CardAtBuff,int p1CardHpBuff) {
        GameUI ui = this.getUI();
        ui.showMessage("加载地图" + mapId + "...");
        //设定是否为全灭随机模拟，是的话，回头胜利条件改为全灭
        boolean mFlag = false;
        if(mapId.indexOf("m") != -1){
            mFlag = true;
            mapId = mapId.replace("m", "");
        }
        //假如关卡为地下城120综合，则随机选120层的某一组
        if(mapId.indexOf("d-120-") == 0){
            //String x = "x";
            //if(mapId == "d-120-1"){
            //    x = mapId;
            //}
            //throw new CardFantasyRuntimeException("待确认的地图:" + mapId +":"+ x);
            Random rand = new Random();
            int i = rand.nextInt(10)+17;
            mapId = "d-"+ i +"-1";
        }else if(mapId.indexOf("d-110-") == 0){
            Random rand = new Random();
            int i = rand.nextInt(10)+7;
            mapId = "d-"+ i +"-1";
        }else if(mapId.indexOf("d-11020-") == 0){
            Random rand = new Random();
            int i = rand.nextInt(20)+7;
            mapId = "d-"+ i +"-1";
        }
        MapInfo map = dungeons.getDungeons(mapId);
        if (map == null) {
            throw new CardFantasyRuntimeException("无法找到地图: " + mapId);
        }
        
        rule.setCondition(map.getCondition());
        if(mFlag){
            rule.setCondition(VictoryCondition.parse("EnemyAllCardsDie"));
        }
        


        ui.showMessage("启动战斗引擎...");
        List<Skill> p1CardBuffs = new ArrayList<Skill>();
        if (p1CardAtBuff != 100) {
            p1CardBuffs.add(new PlayerCardBuffSkill(SkillType.原始攻击调整, p1CardAtBuff - 100));
        }
        if (p1CardHpBuff != 100) {
            p1CardBuffs.add(new PlayerCardBuffSkill(SkillType.原始体力调整, p1CardHpBuff - 100));
        }
        
        //随机从卡牌池里选择指定数量的不重复卡牌进卡组
        String mapdeckinfo = map.getDeckInfo();
        if (mapId.indexOf("Y-") == 0) {
            String[] yzcards={"巅峰军曹合金+神恩2","真理之光+神性爆发6","风暴熊猫+神性爆发7","神鬼之医华佗+拉莱耶领域","冰霜之魂+不屈","碧波龙姬+不动","五虎上将关羽+请帮帮我","圣殿守护者+破军8","天公将军张角+荼蘼盛放","天公将军张角+太平清领书",
            "库卡隆战士+永生审判","奥西里斯+永生审判",
            "雪女+免疫","龙城义士+免疫","潜水学徒+免疫","圣殿守护者+免疫","死亡麋鹿+免疫",
            "东吴四杰吕蒙+影青龙","碧波龙姬+影青龙","电脑怪杰+影青龙","圣域裁决者+影青龙","五子良将张辽+影青龙","暗影猎手+影青龙","盗宝松鼠+影青龙","爆弹强袭+影青龙","五虎上将赵云+影青龙",
            "血色玫瑰+白驹过隙","华舞霓裳+白驹过隙","的卢+白驹过隙","五子良将乐进+白驹过隙",
            "无限之神+三位一体","萌虎御风使+三位一体","盗宝松鼠+三位一体",
            "混元斗者+六道轮回","鬼半藏+六道轮回","帝国守望者+六道轮回","紫电+六道轮回","血色玫瑰+轮回渡厄","丽日清风+轮回渡厄",
            "妲己+嗜魔之体","妲己+嗜魔之体","全知之神+嗜魔之体","全知之神+嗜魔之体","火熊猫+嗜魔之体","时空女神+嗜魔之体","灰烬天使+嗜魔之体","巨噬藤+嗜魔之体","五虎上将赵云+嗜魔之体",
            "圣殿守护者+结界立场","巅峰武师+结界立场","爆弹强袭+结界立场",
            "钻石王牌+顽强7","钻石王牌+顽强7","龙城义士+顽强7","五虎上将马超+骁袭","五虎上将黄忠+重整","利维坦+木牛流马","律政佳人逆转+降临死亡链接","斩羽斑鸠+穿刺10","电波教师+下自成蹊","庚子灵鼠+热情似火",
            "月樱公主+不灭6","碧海绯樱+不灭6","月樱公主+战术性撤退","风暴主宰+雷狱牢囚","灵龙守护者+水流壁10","科学家进化+肉食者","大天狗+肉食者","科学家进化+万里追魂","大天狗+万里追魂","东吴四杰鲁肃+幻化","土熊猫+紊乱"};
            //"黄金镖客+天官帝君","碧海绯樱+天官帝君","忘忧清乐+双飞燕","庚子灵鼠+辞旧迎新","朱雀焚天+嗜魔之体","璀璨之星+恒星之力","云梦仙子+红尘缥缈仙",
            int cardscnt = yzcards.length;
            int yzmapid = Integer.valueOf(mapId.substring(mapId.indexOf("-")+1, mapId.lastIndexOf("-")));
            int thiscardcnt = 0;
            if(yzmapid == 1){
                thiscardcnt = 1;
            }else if(yzmapid == 2){
                thiscardcnt = 2;
            }else if(yzmapid == 3){
                thiscardcnt = 3;
            }else if(yzmapid == 4){
                thiscardcnt = 4;
            }else if(yzmapid == 5 || yzmapid == 6){
                thiscardcnt = 5;
            }else if(yzmapid == 7){
                thiscardcnt = 6;
            }else if(yzmapid == 8){
                thiscardcnt = 7;
            }else if(yzmapid == 9){
                thiscardcnt = 8;
            }else if(yzmapid == 10){
                thiscardcnt = 9;
            }

            if(thiscardcnt > cardscnt){
                throw new CardFantasyRuntimeException("错误，需要选出的卡牌数（"+thiscardcnt+"）大于卡牌池的数量（"+cardscnt+"），请核查");
            }

            //设置一个数组下编的列表，回头再用这个列表里的下编去取卡牌池数组里的卡牌
            ArrayList<Integer> list = new ArrayList<Integer>();
            Random rand = new Random();
            boolean[] bool = new boolean[cardscnt];
            int num = 0;
            for (int i = 0; i < thiscardcnt; i++) {
                do {
                    // 如果产生的数相同继续循环
                    num = rand.nextInt(cardscnt);
                } while (bool[num]);
                bool[num] = true;
                list.add(num);
            }

            //取卡牌
            String newdeckinfo = "";
            for (int i = 0; i < thiscardcnt; i++) {
                int x = list.get(i);
                newdeckinfo = newdeckinfo + yzcards[x] +",";
            }

            mapdeckinfo = mapdeckinfo + newdeckinfo;
            //String test = map.getEnemyHero().toString();
            //throw new CardFantasyRuntimeException("待确认的地图: " + mapId +":"+ p1HeroHpBuff +" deckInfo：" + mapdeckinfo);
        }
        PlayerInfo player2 = PlayerBuilder.build(true, "地下城", mapdeckinfo, 130,p1CardBuffs,p1HeroHpBuff);
        BattleEngine engine = new BattleEngine(ui, rule);
        engine.registerPlayers(player2, player);
        ui.showMessage("战斗开始...");
        GameResult result = engine.playGame();
        PveGameResult gameResult = null;
        try {
            if (result.getCause() == GameEndCause.战斗超时) {
                gameResult = PveGameResult.TIMEOUT;
            } else if (result.getWinner().getId().equals(player.getId())) {
                if (rule.getCondition().meetCriteria(result)) {
                    gameResult = PveGameResult.ADVANCED_WIN;
                } else {
                    gameResult = PveGameResult.BASIC_WIN;
                }
            } else {
                gameResult = PveGameResult.LOSE;
            }
            return gameResult;
        } finally {
            ui.mapStageResult(gameResult);
        }
    }

    //玩家阵容,地图号,运行次数...
    public PveGameResultStat massivePlay(PlayerInfo player, String mapId, int count,Rule rule,int p1HeroHpBuff,int p1CardAtBuff,int p1CardHpBuff) {
        this.getUI().showMessage("Play " + count + " on " + mapId);
        PveGameResultStat stat = new PveGameResultStat();
        for (int i = 0; i < count; ++i) {
            stat.addResult(play(player, mapId,rule,p1HeroHpBuff,p1CardAtBuff,p1CardHpBuff));
        }
        return stat;
    }

    public List<DeckEvaluation> optimizeDeck(int p1HeroHpBuff,int p1CardAtBuff,int p1CardHpBuff,int runeCount, int cardCount, String mapId, int heroLevel,Rule rule,
            int resultCount, String... descs) {
        this.getUI().showMessage("Optimize deck for map: " + mapId);
        DeckStartupInfo deck = DeckBuilder.build(descs);
        List<DeckStartupInfo> decks = deck.generateCombinations(runeCount, cardCount);
        System.out.println(decks.size() + " combinations found!");
        List<DeckEvaluation> evals = new ArrayList<DeckEvaluation>();
        for (int i = 0; i < decks.size(); ++i) {
            System.out.println(String.format("Processing deck: %d / %d", i, decks.size()));
            DeckStartupInfo currentDeck = decks.get(i);
            PlayerInfo player = new PlayerInfo(true, "ME", heroLevel, null, 100, currentDeck.getRunes(), currentDeck.getIndentures(), currentDeck.getEquipments(),  currentDeck.getCards());
            PveGameResultStat stat = massivePlay(player, mapId, 100,rule,p1HeroHpBuff,p1CardAtBuff,p1CardHpBuff);
            evals.add(new DeckEvaluation(stat, currentDeck));
        }
        System.out.println();
        Collections.sort(evals);

        System.out.println("Size of evals: " + evals.size());
        if (resultCount >= evals.size() || resultCount < 0) {
            return new ArrayList<DeckEvaluation>(evals);
        }

        List<DeckEvaluation> result = new ArrayList<DeckEvaluation>(resultCount);
        for (DeckEvaluation eval : evals) {
            if (result.size() == resultCount) {
                break;
            }
            result.add(eval);
        }
        return result;
    }

}
