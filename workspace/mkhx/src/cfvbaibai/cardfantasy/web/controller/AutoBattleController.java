package cfvbaibai.cardfantasy.web.controller;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.Collator;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.DoubleToIntFunction;
import java.io.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import cfvbaibai.cardfantasy.data.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import cfvbaibai.cardfantasy.Base64Encoder;
import cfvbaibai.cardfantasy.CardFantasyRuntimeException;
import cfvbaibai.cardfantasy.Compressor;
import cfvbaibai.cardfantasy.GameUI;
import cfvbaibai.cardfantasy.Global;
import cfvbaibai.cardfantasy.engine.BattleEngine;
import cfvbaibai.cardfantasy.engine.CardInfo;
import cfvbaibai.cardfantasy.engine.GameResult;
import cfvbaibai.cardfantasy.engine.Player;
import cfvbaibai.cardfantasy.engine.Rule;
import cfvbaibai.cardfantasy.game.DeckBuilder;
import cfvbaibai.cardfantasy.game.DummyGameUI;
import cfvbaibai.cardfantasy.game.DummyVictoryCondition;
import cfvbaibai.cardfantasy.game.GameResultStat;
import cfvbaibai.cardfantasy.game.LilithDataStore;
import cfvbaibai.cardfantasy.game.MapInfo;
import cfvbaibai.cardfantasy.game.MapStages;
import cfvbaibai.cardfantasy.game.DungeonsStages;
import cfvbaibai.cardfantasy.game.PlayerBuilder;
import cfvbaibai.cardfantasy.game.PvdEngine;
import cfvbaibai.cardfantasy.game.PveEngine;
import cfvbaibai.cardfantasy.game.PveGameResult;
import cfvbaibai.cardfantasy.game.PvlEngine;
import cfvbaibai.cardfantasy.game.PvlGameTimeoutException;
import cfvbaibai.cardfantasy.game.VictoryCondition;
import cfvbaibai.cardfantasy.game.launcher.ArenaGameResult;
import cfvbaibai.cardfantasy.game.launcher.BossGameResult;
import cfvbaibai.cardfantasy.game.launcher.GameLauncher;
import cfvbaibai.cardfantasy.game.launcher.GameSetup;
import cfvbaibai.cardfantasy.game.launcher.LilithGameResult;
import cfvbaibai.cardfantasy.game.launcher.MapGameResult;
import cfvbaibai.cardfantasy.game.launcher.TrivialBossGameResult;
import cfvbaibai.cardfantasy.web.ErrorHelper;
import cfvbaibai.cardfantasy.web.Utils;
import cfvbaibai.cardfantasy.web.animation.BattleRecord;
import cfvbaibai.cardfantasy.web.animation.EntityDataRuntimeInfo;
import cfvbaibai.cardfantasy.web.animation.SkillTypeRuntimeInfo;
import cfvbaibai.cardfantasy.web.animation.StructuredRecordGameUI;
import cfvbaibai.cardfantasy.web.animation.WebPlainTextGameUI;
import cfvbaibai.cardfantasy.web.beans.JsonHandler;
import cfvbaibai.cardfantasy.web.beans.Logger;
import cfvbaibai.cardfantasy.web.beans.UserAction;
import cfvbaibai.cardfantasy.web.beans.UserActionRecorder;

@Controller
public class AutoBattleController {
    @Autowired
    private JsonHandler jsonHandler;

    @Autowired
    private UserActionRecorder userActionRecorder;

    @Autowired
    private MapStages maps;

    @Autowired
    private DungeonsStages  dungeons;

    @Autowired
    private LilithDataStore lilithDataStore;

    @Autowired
    private Logger logger;

    @Autowired
    private ErrorHelper errorHelper;

    private static List<Skill> buildBuffsForLilithEvents(String eventCardNames) {
        List<Skill> player2Buffs = new ArrayList<Skill>();
        if (eventCardNames != null) {
            String[] eventCardNameArray = DeckBuilder.splitDescsText(eventCardNames);
            for (String eventCardName : eventCardNameArray) {
                CardData cardData = CardDataStore.loadDefault().getCard(eventCardName);
                if (cardData == null) {
                    throw new CardFantasyRuntimeException("无效的活动卡牌：" + eventCardName);
                }
                int level = 0;
                switch (cardData.getStar()) {
                    case 3: level = 50; break;
                    case 4: level = 100; break;
                    case 5: level = 200; break;
                    default: throw new CardFantasyRuntimeException("无效的活动卡牌：" + eventCardName);
                }
                player2Buffs.add(new LilithCardBuffSkill(SkillType.原始体力调整, level, eventCardName));
                player2Buffs.add(new LilithCardBuffSkill(SkillType.原始攻击调整, level, eventCardName));
            }
        }
        return player2Buffs;
    }

    private void outputBattleOptions(PrintWriter writer, int firstAttack, int deckOrder, int p1hhpb, int p1catb, int p1chpb, int p2hhpb, int p2catb, int p2chpb, VictoryCondition vc1) {
        if (firstAttack == -1) {
            writer.write("按规则决定先攻");
        } else if (firstAttack == 0) {
            writer.write("玩家1先攻");
        } else {
            writer.write("玩家2先攻");
        }
        writer.write("; ");
        if (deckOrder == 0) {
            writer.write("随机出牌");
        } else {
            writer.write("按指定顺序出牌");
        }
        writer.write("<br />");
        if (p1hhpb != 100) {
            writer.write("玩家1英雄体力调整: " + p1hhpb + "%<br />");
        }
        if (p1catb != 100) {
            writer.write("玩家1卡牌攻击调整: " + p1catb + "%<br />");
        }
        if (p1chpb != 100) {
            writer.write("玩家1卡牌体力调整: " + p1chpb + "%<br />");
        }
        if (p2hhpb != 100) {
            writer.write("玩家2英雄体力调整: " + p2hhpb + "%<br />");
        }
        if (p2catb != 100) {
            writer.write("玩家2卡牌攻击调整: " + p2catb + "%<br />");
        }
        if (p2chpb != 100) {
            writer.write("玩家2卡牌体力调整: " + p2chpb + "%<br />");
        }
        if (vc1 != null && !(vc1 instanceof DummyVictoryCondition)) {
            writer.write("玩家1胜利条件: " + vc1.getDescription() + "<br />");
        }
    }

    @RequestMapping(value = "/PlayAuto1MatchGame")
    public void playAuto1MatchGame(HttpServletRequest request, HttpServletResponse response,
            @RequestParam("deck1") String deck1, @RequestParam("count") int count,
            @RequestParam("deck2") String deck2, @RequestParam("hlv1") int heroLv1, @RequestParam("hlv2") int heroLv2,
            @RequestParam("fa") int firstAttack, @RequestParam("do") int deckOrder,
            @RequestParam("p1hhpb") int p1HeroHpBuff, @RequestParam("p1catb") int p1CardAtBuff, @RequestParam("p1chpb") int p1CardHpBuff,
            @RequestParam("p2hhpb") int p2HeroHpBuff, @RequestParam("p2catb") int p2CardAtBuff, @RequestParam("p2chpb") int p2CardHpBuff,
            @RequestParam("vc1") String victoryConditionText1
            ) throws IOException {
        PrintWriter writer = response.getWriter();
        try {
            logger.info("PlayAuto1MatchGame from " + request.getRemoteAddr() + ":");
            String logMessage = String.format(
                "Deck1=%s<br />Deck2=%s<br />Lv1=%d, Lv2=%d, FirstAttack=%d, DeckOrder=%d, VictoryCondition1=%s",
                deck1, deck2, heroLv1, heroLv2, firstAttack, deckOrder, victoryConditionText1);
            logger.info(logMessage);
            VictoryCondition vc1 = VictoryCondition.parse(victoryConditionText1);
            outputBattleOptions(writer, firstAttack, deckOrder, 
                    p1HeroHpBuff, p1CardAtBuff, p1CardHpBuff, p2HeroHpBuff, p2CardAtBuff, p2CardHpBuff, vc1);
            WebPlainTextGameUI ui = new WebPlainTextGameUI();
            GameSetup setup = GameSetup.setupArenaGame(
                    deck1, deck2, heroLv1, heroLv2,
                    p1CardAtBuff, p1CardHpBuff, p1HeroHpBuff,
                    p2CardAtBuff, p2CardHpBuff, p2HeroHpBuff,
                    firstAttack, deckOrder, vc1,
                    1, ui);
            ArenaGameResult result = GameLauncher.playArenaGame(setup);
            this.userActionRecorder.addAction(new UserAction(new Date(), request.getRemoteAddr(), "Play Auto 1Match Game", logMessage));
            writer.print(Utils.getCurrentDateTime() + "<br />" + result.getDeckValidationResult() + ui.getAllText());
            logger.info("Winner: " + result.getStat().getLastResult().getWinner().getId());
        } catch (Exception e) {
            writer.print(errorHelper.handleError(e, false));
        }
    }

    @RequestMapping(value = "/SimAuto1MatchGame", headers = "Accept=application/json")
    public void simulateAuto1MatchGame(HttpServletRequest request, HttpServletResponse response,
            @RequestParam("deck1") String deck1, @RequestParam("count") int count,
            @RequestParam("deck2") String deck2, @RequestParam("hlv1") int heroLv1, @RequestParam("hlv2") int heroLv2,
            @RequestParam("fa") int firstAttack, @RequestParam("do") int deckOrder,
            @RequestParam("p1hhpb") int p1HeroHpBuff, @RequestParam("p1catb") int p1CardAtBuff, @RequestParam("p1chpb") int p1CardHpBuff,
            @RequestParam("p2hhpb") int p2HeroHpBuff, @RequestParam("p2catb") int p2CardAtBuff, @RequestParam("p2chpb") int p2CardHpBuff,
            @RequestParam("vc1") String victoryConditionText1
    ) throws IOException {
        PrintWriter writer = response.getWriter();
        response.setContentType("application/json");
        try {
            logger.info("SimulateAuto1MatchGame from " + request.getRemoteAddr() + ":");
            String logMessage = String.format(
                "Deck1=%s<br />Deck2=%s<br />Lv1=%d, Lv2=%d, FirstAttack=%d, DeckOrder=%d, VictoryCondition1=%s",
                deck1, deck2, heroLv1, heroLv2, firstAttack, deckOrder, victoryConditionText1);
            logger.info(logMessage);
            this.userActionRecorder.addAction(new UserAction(new Date(), request.getRemoteAddr(), "Simulate Auto 1Match Game", logMessage));
            VictoryCondition vc1 = VictoryCondition.parse(victoryConditionText1);
            StructuredRecordGameUI ui = new StructuredRecordGameUI();
            GameSetup setup = GameSetup.setupArenaGame(
                    deck1, deck2, heroLv1, heroLv2,
                    p1CardAtBuff, p1CardHpBuff, p1HeroHpBuff,
                    p2CardAtBuff, p2CardHpBuff, p2HeroHpBuff,
                    firstAttack, deckOrder, vc1,
                    1, ui);
            ArenaGameResult result = GameLauncher.playArenaGame(setup);
            BattleRecord record = ui.getRecord();
            writer.print(jsonHandler.toJson(record));
            logger.info("Winner: " + result.getStat().getLastResult().getWinner().getId());
        } catch (Exception e) {
            writer.print(errorHelper.handleError(e, true));
        }
    }
    
    @RequestMapping(value = "/PlayAutoMassiveGame")
    public void playAutoMassiveGame(HttpServletRequest request, HttpServletResponse response,
            @RequestParam("deck1") String deck1, @RequestParam("deck2") String deck2,
            @RequestParam("hlv1") int heroLv1, @RequestParam("hlv2") int heroLv2, @RequestParam("count") int count,
            @RequestParam("fa") int firstAttack, @RequestParam("do") int deckOrder,
            @RequestParam("p1hhpb") int p1HeroHpBuff, @RequestParam("p1catb") int p1CardAtBuff, @RequestParam("p1chpb") int p1CardHpBuff,
            @RequestParam("p2hhpb") int p2HeroHpBuff, @RequestParam("p2catb") int p2CardAtBuff, @RequestParam("p2chpb") int p2CardHpBuff,
            @RequestParam("vc1") String victoryConditionText1
    ) throws IOException {
        PrintWriter writer = response.getWriter();
        try {
            logger.info("PlayAutoMassiveGame from " + request.getRemoteAddr() + ":");
            String logMessage = String.format("Deck1=%s<br />Deck2=%s<br />Lv1=%d, Lv2=%d, FirstAttack=%d, DeckOrder=%d, Count=%d, VictoryCondition1=%s",
                deck1, deck2, heroLv1, heroLv2, firstAttack, deckOrder, count, victoryConditionText1);
            logger.info(logMessage);
            this.userActionRecorder.addAction(new UserAction(new Date(), request.getRemoteAddr(), "Play Auto Massive Game", logMessage));
            VictoryCondition vc1 = VictoryCondition.parse(victoryConditionText1);
            outputBattleOptions(writer, firstAttack, deckOrder, p1HeroHpBuff, p1CardAtBuff, p1CardHpBuff, p2HeroHpBuff, p2CardAtBuff, p2CardHpBuff, vc1);
            
            GameSetup setup = null;
            ArenaGameResult result = null;
            GameResultStat stat = null;

            int selectlevel = 0;
            if(count < -10){        //取出跟着count带进来的排序类型和排序次数, 由于不想去改原作者的代码, 所以只好用麻烦的方法来传过来
                selectlevel = count/10;
                count = count - selectlevel*10;
                if(count == -1){
                    count = 10;
                }else if(count == -2){
                    count = 100;
                }else if(count == -3){
                    count = 1000;
                }else if(count == -4){
                    count = 10000;
                }
            }
            //虽然下面这些在官方千场里用不到, 但不想写在里面, 嫌麻烦
            String cardswithcnt = "";
            String cardsnocnt = "";
            List<sortCard> sortcards = new ArrayList<>();
            deck2 = deck2.replace("，", ",");
            deck2 = deck2 + ",";    //在结尾加逗号是为了方便循环的时候不用写额外的判断语句，保证每个卡牌后都至少有一个逗号
            deck2 = deck2.replace(" ", "");
            deck2 = deck2.replace(",,", ",");
            deck2 = deck2.replace(",,", ",");
            String subdeck = deck2;
            int firstcnt = subdeck.indexOf(',');
            String errornote = "";


            if (selectlevel == -100 || selectlevel == -101){        //-100:本卡组卡牌胜率排序;  -101:本卡组卡牌升15胜率排序
                while (firstcnt != -1){
                    //取卡牌
                    String thisdeck = subdeck.substring(0, firstcnt+1).replace(" ","");  //第一个取出来的卡牌
                    subdeck = subdeck.substring(firstcnt+1);            //剩下的卡牌组，用来接着循环取剩下的卡牌

                    
                    if(thisdeck.replace(",", "") != ""){
                        String newdeck = "";
                        String thisdeck15 = "";
                        if(selectlevel == -100){        //当为卡组排序时,每次进行去掉本张卡的模拟
                            newdeck = deck2.replaceFirst(thisdeck.replace("+", "\\+"), "");  //去掉取出来的卡牌的卡牌组
                        }else if(selectlevel == -101){
                            
                            newdeck = deck2.replaceFirst(thisdeck.replace("+", "\\+"), "");  //去掉取出来的卡牌的卡牌组
                            if(thisdeck.indexOf('-') == -1){            //当本卡没有加等级的时候,加上等级15
                                thisdeck15 = thisdeck +"-15";
                            }else{                                      //当本卡有加等级的时候,把10级或14级换成15级, 应该不会有别的等级出现
                                thisdeck15 = thisdeck.replace("-10", "-15").replace("-14", "-15");
                            } 
                            newdeck = thisdeck15 +","+ newdeck;
                        }
                        

                        //将卡牌胜率写进列表
                        //result = null;
                        //ui = new DummyGameUI();
                        //result = GameLauncher.playMapGame(newdeck, map, heroLv, count, ui);
                        //sortcards.add(new sortCard(thisdeck,Double.valueOf(result.getAdvWinCount())));
                        setup = GameSetup.setupArenaGame(
                            deck1, newdeck, heroLv1, heroLv2,
                            p1CardAtBuff, p1CardHpBuff, p1HeroHpBuff, p2CardAtBuff, p2CardHpBuff, p2HeroHpBuff,
                            firstAttack, deckOrder, vc1, count, new DummyGameUI());
                        result = GameLauncher.playArenaGame(setup);
                        stat = result.getStat();
                        sortcards.add(new sortCard(thisdeck,Double.valueOf(stat.getP2Win())));
                    }

                    firstcnt = subdeck.indexOf(',');                    //下一个卡牌在哪里结束

                }
                //最后运行一次正常的, 好显示数据及对比
                setup = GameSetup.setupArenaGame(
                    deck1, deck2, heroLv1, heroLv2,
                    p1CardAtBuff, p1CardHpBuff, p1HeroHpBuff, p2CardAtBuff, p2CardHpBuff, p2HeroHpBuff,
                    firstAttack, deckOrder, vc1, count, new DummyGameUI());
                result = GameLauncher.playArenaGame(setup);
                stat = result.getStat();
                //开始排序列表
                Collections.sort(sortcards, new Comparator<sortCard>() {
                    @Override
                    public int compare(sortCard o1, sortCard o2) {
                        return o2.getCnt().compareTo(o1.getCnt());
                    }
                });
                //从列表中取出来
                int i = 1;
                for(sortCard sortcard:sortcards){
                    cardswithcnt = cardswithcnt + sortcard.cardname.replace(",","");
                    cardswithcnt = cardswithcnt +"("+ sortcard.cnt.toString() +"),";

                    if(selectlevel == -100){            //当为卡组排序的时候, 最后按强度卡牌排序显示卡组
                        cardsnocnt = cardsnocnt + sortcard.cardname +" ";
                    }else if(selectlevel == -101){      //当为15排序的时候, 最后显示最强卡牌15级加其它卡牌来显示卡组
                        if(i ==1){
                            String thisdeck = sortcard.cardname;
                            String newdeck = deck2.replaceFirst(thisdeck.replace("+", "\\+"), "");  //去掉取出来的卡牌的卡牌组
                            thisdeck = thisdeck.replace(",","");
                            if(thisdeck.indexOf('-') == -1){            //当本卡没有加等级的时候,加上等级15
                                thisdeck = thisdeck +"-15";
                            }else{                                      //当本卡有加等级的时候,把10级或14级换成15级, 应该不会有别的等级出现
                                thisdeck = thisdeck.replace("-10", "-15").replace("-14", "-15");
                            } 
                            cardsnocnt = thisdeck +", "+ newdeck;
                        }
                        i++;
                    }
                }
            } else if(selectlevel <= -110 && selectlevel >= -139){              //在已获得的卡牌中选择出对本战胜率最高的
                
                String bestdeck = "";
                Double bestcnt = 0.0;

                InputStream cardFWStream;
                //String txturl = "";
                //List txturls;
                List<String> txturls=new ArrayList<>();

                //从文件中读取出卡牌列表
                if(selectlevel == -110){            //精选345
                    txturls.add("cfvbaibai/cardfantasy/data/MyCard5S.txt");
                    txturls.add("cfvbaibai/cardfantasy/data/MyCard5F.txt");
                    txturls.add("cfvbaibai/cardfantasy/data/MyCard5H.txt");
                    txturls.add("cfvbaibai/cardfantasy/data/MyCard5T.txt");
                    txturls.add("cfvbaibai/cardfantasy/data/MyCard4S.txt");
                    txturls.add("cfvbaibai/cardfantasy/data/MyCard4F.txt");
                    txturls.add("cfvbaibai/cardfantasy/data/MyCard4H.txt");
                    txturls.add("cfvbaibai/cardfantasy/data/MyCard4T.txt");
                    txturls.add("cfvbaibai/cardfantasy/data/MyCard3.txt");
                    txturls.add("cfvbaibai/cardfantasy/data/MyCard4M.txt");
                    txturls.add("cfvbaibai/cardfantasy/data/MyCard5M.txt");
               
                }else if(selectlevel == -120 || selectlevel == -125){      //符文精选或全符文时

                    txturls.add("cfvbaibai/cardfantasy/data/MyCardFS.txt");
                    txturls.add("cfvbaibai/cardfantasy/data/MyCardFF.txt");
                    txturls.add("cfvbaibai/cardfantasy/data/MyCardFH.txt");
                    txturls.add("cfvbaibai/cardfantasy/data/MyCardFT.txt");
    
                }else if(selectlevel == -111 ){             
                    txturls.add("cfvbaibai/cardfantasy/data/MyCard1.txt");
                }else if(selectlevel == -112){
                    txturls.add("cfvbaibai/cardfantasy/data/MyCard2.txt");
                }else if(selectlevel == -113){
                    txturls.add("cfvbaibai/cardfantasy/data/MyCard3.txt");
                }else if(selectlevel == -114){
                    txturls.add("cfvbaibai/cardfantasy/data/MyCard4S.txt");
                    txturls.add("cfvbaibai/cardfantasy/data/MyCard4F.txt");
                    txturls.add("cfvbaibai/cardfantasy/data/MyCard4H.txt");
                    txturls.add("cfvbaibai/cardfantasy/data/MyCard4T.txt");
                    txturls.add("cfvbaibai/cardfantasy/data/MyCard4M.txt");
                }else if(selectlevel == -115){
                    txturls.add("cfvbaibai/cardfantasy/data/MyCard5S.txt");
                    txturls.add("cfvbaibai/cardfantasy/data/MyCard5F.txt");
                    txturls.add("cfvbaibai/cardfantasy/data/MyCard5H.txt");
                    txturls.add("cfvbaibai/cardfantasy/data/MyCard5T.txt");
                    txturls.add("cfvbaibai/cardfantasy/data/MyCard5M.txt");
                }else if(selectlevel == -116){      //45星
                    txturls.add("cfvbaibai/cardfantasy/data/MyCard5S.txt");
                    txturls.add("cfvbaibai/cardfantasy/data/MyCard5F.txt");
                    txturls.add("cfvbaibai/cardfantasy/data/MyCard5H.txt");
                    txturls.add("cfvbaibai/cardfantasy/data/MyCard5T.txt");
                    txturls.add("cfvbaibai/cardfantasy/data/MyCard4S.txt");
                    txturls.add("cfvbaibai/cardfantasy/data/MyCard4F.txt");
                    txturls.add("cfvbaibai/cardfantasy/data/MyCard4H.txt");
                    txturls.add("cfvbaibai/cardfantasy/data/MyCard4T.txt");
                    txturls.add("cfvbaibai/cardfantasy/data/MyCard4M.txt");
                    txturls.add("cfvbaibai/cardfantasy/data/MyCard5M.txt");
                }else if(selectlevel == -121){       //水
                    txturls.add("cfvbaibai/cardfantasy/data/MyCardFS.txt");
                }else if(selectlevel == -122){       //风
                    txturls.add("cfvbaibai/cardfantasy/data/MyCardFF.txt");
                }else if(selectlevel == -123){       //火
                    txturls.add("cfvbaibai/cardfantasy/data/MyCardFH.txt");
                }else if(selectlevel == -124){       //土
                    txturls.add("cfvbaibai/cardfantasy/data/MyCardFT.txt");
                }else if(selectlevel == -131){       //王国
                    txturls.add("cfvbaibai/cardfantasy/data/MyCard5S.txt");
                    txturls.add("cfvbaibai/cardfantasy/data/MyCard4S.txt");
                }else if(selectlevel == -132){       //森林
                    txturls.add("cfvbaibai/cardfantasy/data/MyCard5F.txt");
                    txturls.add("cfvbaibai/cardfantasy/data/MyCard4F.txt");
                }else if(selectlevel == -133){       //蛮荒
                    txturls.add("cfvbaibai/cardfantasy/data/MyCard5T.txt");
                    txturls.add("cfvbaibai/cardfantasy/data/MyCard4T.txt");
                }else if(selectlevel == -134){       //地狱
                    txturls.add("cfvbaibai/cardfantasy/data/MyCard5H.txt");
                    txturls.add("cfvbaibai/cardfantasy/data/MyCard4H.txt");
                }

                //从mycard文件列表中取出卡牌
                for (int i = 0; i < txturls.size(); i++) {
                    
                    String txturl = txturls.get(i); 
                    cardFWStream = CardDataStore.class.getClassLoader().getResourceAsStream(txturl);
                    //将文件中的符文读进list里
                    BufferedReader br = new BufferedReader(new InputStreamReader(cardFWStream, "UTF-8"));//构造一个BufferedReader类来读取文件
                    String line = null;
                    while((line = br.readLine())!=null){//使用readLine方法，一次读一行
                        if(line.indexOf("//") == -1){      //当卡牌不为尚未收录的卡牌时
                            line = line.replace('，', ',');
                            if(selectlevel == -110 || selectlevel == -120){      //当选择类型为精选时
                                if(line.indexOf(',') != -1){      //当为标记卡牌时
                                    sortcards.add(new sortCard(line.replace("-",","), 0.0));
                                }
                            }else{                                              //当选择类型为非精选时
                                sortcards.add(new sortCard(line.replace("-",","), 0.0));
                            }
                        }
                    }
                    br.close();    
                }


                //用list里的符文分别模拟，将获得的分数写进list里
                for(sortCard sortcard:sortcards){
                    String olddeck = deck2;
                    olddeck = olddeck.replace('，', ',');
                    olddeck = olddeck.substring(olddeck.indexOf(',')+1);
                    String thiscardname = sortcard.cardname.replace("，",",").replace(",", "");
                    /*if(thiscardname.indexOf("-") == -1){
                        if(selectlevel <= -120){     //为符文时
                            thiscardname = thiscardname +"-4, ";
                        }else{                      //为卡牌时
                            thiscardname = thiscardname +"-10, ";
                        }
                    }
                    String newdeck = thiscardname + olddeck;*/
                    String newdeck = thiscardname +", "+ olddeck;
                    //进行模拟并取值
                    setup = GameSetup.setupArenaGame(
                        deck1, newdeck, heroLv1, heroLv2,
                        p1CardAtBuff, p1CardHpBuff, p1HeroHpBuff, p2CardAtBuff, p2CardHpBuff, p2HeroHpBuff,
                        firstAttack, deckOrder, vc1, count, new DummyGameUI());
                    result = GameLauncher.playArenaGame(setup);
                    stat = result.getStat();
                    

                    //result = null;
                    //ui = new DummyGameUI();
                    //result = GameLauncher.playMapGame(newdeck, map, heroLv, count, ui);
                    Double thiscnt = Double.valueOf(stat.getP2Win());
                    sortcard.cnt = thiscnt;
                    if(thiscnt > bestcnt){
                        bestcnt = thiscnt;
                        bestdeck = newdeck;
                    }
                    if(result.getDeckValidationResult() != ""){
                        errornote = errornote + sortcard.cardname +":"+ result.getDeckValidationResult() +",";
                    }
                }
                cardsnocnt = bestdeck;

                //list排序
                Collections.sort(sortcards, new Comparator<sortCard>() {
                    @Override
                    public int compare(sortCard o1, sortCard o2) {
                        return o2.getCnt().compareTo(o1.getCnt());
                    }
                });

                //从list中将前20的卡牌和分数取出来
                int i = 0;
                for(sortCard sortcard:sortcards){
                    cardswithcnt = cardswithcnt + sortcard.cardname.replace(",","");
                    cardswithcnt = cardswithcnt +"("+ sortcard.cnt.toString() +"),";

                    i++;
                    if(i>=20){
                        break;
                    }
                    
                }

                //最后显示一遍原始卡组的分数
                setup = GameSetup.setupArenaGame(
                        deck1, deck2, heroLv1, heroLv2,
                        p1CardAtBuff, p1CardHpBuff, p1HeroHpBuff, p2CardAtBuff, p2CardHpBuff, p2HeroHpBuff,
                        firstAttack, deckOrder, vc1, count, new DummyGameUI());
                result = GameLauncher.playArenaGame(setup);
                stat = result.getStat();


            }else{
                setup = GameSetup.setupArenaGame(
                        deck1, deck2, heroLv1, heroLv2,
                        p1CardAtBuff, p1CardHpBuff, p1HeroHpBuff, p2CardAtBuff, p2CardHpBuff, p2HeroHpBuff,
                        firstAttack, deckOrder, vc1, count, new DummyGameUI());
                result = GameLauncher.playArenaGame(setup);
                stat = result.getStat();
            }


            writer.append(Utils.getCurrentDateTime() + "<br />");
            writer.append(result.getDeckValidationResult());
            writer.append(errornote);
            writer.append("<table>");
            writer.append("<tr><td>超时: </td><td>" + stat.getTimeoutCount() + "</td></tr>");
            writer.append("<tr><td>玩家1获胜: </td><td>" + stat.getP1Win() + "</td></tr>");
            writer.append("<tr><td>玩家2获胜: </td><td>" + stat.getP2Win() + "</td></tr>");
            if (!(vc1 instanceof DummyVictoryCondition)) {
                writer.append("<tr><td>条件符合: </td><td>" + stat.getConditionMet() + "</td></tr>");
            }
            writer.append("<tr><td>强度卡组排序: </td><td>"+ cardswithcnt +"</td></tr>");
            writer.append("<tr><td>卡组排序卡组: </td><td>"+ cardsnocnt +"</td></tr>");
            writer.append("</table>");
            writer.append("<input type='hidden' value='myrate" + stat.getP1Win() + "' />");
            logger.info("TO:P1:P2 = " + stat.getTimeoutCount() + ":" + stat.getP1Win() + ":" + stat.getP2Win());
        } catch (Exception e) {
            writer.print(errorHelper.handleError(e, false));
        }
    }

    @RequestMapping(value = "/PlayBoss1MatchGame")
    public void playBoss1MatchGame(HttpServletRequest request, HttpServletResponse response,
            @RequestParam("deck") String deck, @RequestParam("count") int count, @RequestParam("gt") int guardType,
            @RequestParam("hlv") int heroLv, @RequestParam("bn") String bossName, @RequestParam("bk") int buffKingdom,
            @RequestParam("bf") int buffForest, @RequestParam("bs") int buffSavage, @RequestParam("bh") int buffHell) throws IOException {
        PrintWriter writer = response.getWriter();
        try {
            String logMessage = String.format("Deck=%s<br />HeroLV=%d, Boss=%s, GuardType=%d", deck, heroLv, bossName, guardType);
            logger.info("PlayBoss1MatchGame: " + logMessage);
            this.userActionRecorder.addAction(new UserAction(new Date(), request.getRemoteAddr(), "Play Boss 1Match Game", logMessage));
            WebPlainTextGameUI ui = new WebPlainTextGameUI();
            GameSetup setup = GameSetup.setupBossGame(
                    deck, bossName, heroLv, buffKingdom, buffForest, buffSavage, buffHell, guardType, 1, ui);
            BossGameResult result = GameLauncher.playBossGame(setup);
            writer.print(Utils.getCurrentDateTime() + "<br />");
            writer.print("<div style='color: red'>" + result.getValidationResult() + "</div>");
            GameResult detail = result.getLastDetail();
            writer.print("造成伤害：" + detail.getDamageToBoss() + "<br />");
            writer.print("------------------ 战斗过程 ------------------<br />");
            writer.print(ui.getAllText());
            logger.info("Winner: " + detail.getWinner().getId() + ", Damage to boss: " + detail.getDamageToBoss());
        } catch (Exception e) {
            writer.print(errorHelper.handleError(e, false));
        }
    }

    @RequestMapping(value = "/PlayLilith1MatchGame")
    public void playLilith1MatchGame(HttpServletRequest request, HttpServletResponse response,
            @RequestParam("deck") String deck, @RequestParam("ecg") boolean enableCustomGuards, @RequestParam("cg") String customGuards,
            @RequestParam("cgab") int customGuardsAtBuff, @RequestParam("cghb") int customGuardsHpBuff,
            @RequestParam("count") int count, @RequestParam("gt") int gameType,
            @RequestParam("hlv") int heroLv, @RequestParam("ln") String lilithName,
            @RequestParam("tc") int targetRemainingGuardCount, @RequestParam("rhp") int remainingHP,
            @RequestParam("ec") String eventCardNames
            ) throws IOException {
        PrintWriter writer = response.getWriter();
        WebPlainTextGameUI ui = new WebPlainTextGameUI();
        try {
            String logMessage = String.format("Deck=%s<br />HeroLV=%d, Lilith=%s, GameType=%d, EventCards=%s", deck, heroLv, lilithName, gameType, eventCardNames);
            logger.info("PlayLilith1MatchGame: " + logMessage);
            this.userActionRecorder.addAction(new UserAction(new Date(), request.getRemoteAddr(), "Play Lilith 1Match Game", logMessage));
            LilithGameResult result = null;
            if (enableCustomGuards && gameType == 0) {
                result = GameLauncher.playCustomLilithGame(
                        deck, lilithName + "," + customGuards, heroLv, customGuardsAtBuff, customGuardsHpBuff,
                        gameType, targetRemainingGuardCount, remainingHP, eventCardNames, 1, ui);
            } else {
                result = GameLauncher.playLilithGame(
                        deck, lilithName, heroLv, gameType, 
                        targetRemainingGuardCount, remainingHP, eventCardNames, 1, ui);
            }

            writer.print(Utils.getCurrentDateTime() + "<br />");
            String resultMessage = String.format("共进行 %f 轮进攻，平均每轮对莉莉丝造成 %f 点伤害<br />",
                result.getAvgBattleCount(), result.getAvgDamageToLilith());
            writer.print(resultMessage);
            writer.print("<div style='color: red'>" + result.getValidationResult() + "</div>");
            writer.print("------------------ 战斗过程 ------------------<br />");
            writer.print(ui.getAllText());
            logger.info(resultMessage);
        } catch (PvlGameTimeoutException e) {
            writer.print("进攻超过最大次数，你的卡组太弱了<br />");
            writer.print("------------------ 战斗过程 ------------------<br />");
            writer.print(ui.getAllText());
        } catch (Exception e) {
            writer.print(errorHelper.handleError(e, false));
        }
    }

    @RequestMapping(value = "/SimulateBoss1MatchGame", headers = "Accept=application/json")
    public void simulateBoss1MatchGame(HttpServletRequest request, HttpServletResponse response,
            @RequestParam("deck") String deck, @RequestParam("count") int count, @RequestParam("gt") int guardType,
            @RequestParam("hlv") int heroLv, @RequestParam("bn") String bossName, @RequestParam("bk") int buffKingdom,
            @RequestParam("bf") int buffForest, @RequestParam("bs") int buffSavage, @RequestParam("bh") int buffHell) throws IOException {
        PrintWriter writer = response.getWriter();
        response.setContentType("application/json");
        try {
            logger.info("SimulateBoss1MatchGame from " + request.getRemoteAddr() + ":");
            logger.info("Deck = " + deck);
            logger.info("Hero LV = " + heroLv + ", Boss = " + bossName);
            this.userActionRecorder.addAction(new UserAction(new Date(), request.getRemoteAddr(), "Simulate Boss 1Match Game",
                    String.format("Deck=%s<br />HeroLV=%d, Boss=%s, GuardType=%d", deck, heroLv, bossName, guardType)));
            StructuredRecordGameUI ui = new StructuredRecordGameUI();
            GameSetup setup = GameSetup.setupBossGame(
                    deck, bossName, heroLv, buffKingdom, buffForest, buffSavage, buffHell, guardType, 1, ui);
            BossGameResult result = GameLauncher.playBossGame(setup);

            BattleRecord record = ui.getRecord();
            writer.print(jsonHandler.toJson(record));
            GameResult detail = result.getLastDetail();
            logger.info("Winner: " + detail.getWinner().getId() + ", Damage to boss: " + detail.getDamageToBoss());
        } catch (Exception e) {
            writer.print(errorHelper.handleError(e, true));
        }
    }

    @RequestMapping(value = "/SimulateLilith1MatchGame", headers = "Accept=application/json")
    public void simulateLilith1MatchGame(HttpServletRequest request, HttpServletResponse response,
        @RequestParam("deck") String deck, @RequestParam("ecg") boolean enableCustomGuards,
        @RequestParam("cg") String customGuards, @RequestParam("cgab") int customGuardsAtBuff, @RequestParam("cghb") int customGuardsHpBuff,
        @RequestParam("count") int count, @RequestParam("gt") int gameType,
        @RequestParam("hlv") int heroLv, @RequestParam("ln") String lilithName,
        @RequestParam("tc") int targetRemainingGuardCount, @RequestParam("rhp") int remainingHP,
        @RequestParam("ec") String eventCardNames
        ) throws IOException {
        PrintWriter writer = response.getWriter();
        response.setContentType("application/json");
        try {
            String logMessage = String.format("Deck=%s<br />HeroLV=%d, Lilith=%s, GameType=%d, EventCards=%s", deck, heroLv, lilithName, gameType, eventCardNames);
            logger.info("SimulateLilith1MatchGame: " + logMessage);
            this.userActionRecorder.addAction(new UserAction(new Date(), request.getRemoteAddr(), "Simulate Lilith 1Match Game", logMessage));

            PlayerInfo player1 = null;
            if (enableCustomGuards && gameType == 0) {
                List<Skill> player1Buffs = PvlEngine.getCardBuffs(customGuardsAtBuff, customGuardsHpBuff);
                player1 = PlayerBuilder.build(false, "莉莉丝", lilithName + "," + customGuards, 99999, player1Buffs, 100);
            } else {
                player1 = PlayerBuilder.buildLilith(lilithDataStore, lilithName, gameType == 0);
            }
            List<Skill> player2Buffs = buildBuffsForLilithEvents(eventCardNames);
            PlayerInfo player2 = PlayerBuilder.build(true, "玩家", deck, heroLv, player2Buffs, 100);
            StructuredRecordGameUI ui = new StructuredRecordGameUI();
            BattleEngine engine = new BattleEngine(ui, Rule.getDefault());
            engine.registerPlayers(player1, player2);
            if (gameType == 1) {
                Player lilithPlayer = engine.getStage().getPlayers().get(0);
                CardInfo lilithCard = lilithPlayer.getDeck().toList().get(0);
                lilithCard.setRemainingHP(remainingHP);
            }
            engine.playGame();
            BattleRecord record = ui.getRecord();
            writer.print(jsonHandler.toJson(record));
        } catch (Exception e) {
            writer.print(errorHelper.handleError(e, true));
        }
    }

    /**
     * 
     * @param request
     * @param response
     * @param deck
     * @param count
     * @param guardType
     * @param heroLv
     * @param bossName
     * @param buffKingdom
     * @param buffForest
     * @param buffSavage
     * @param buffHell
     * @throws IOException
     * Response JSON: BossMassiveGameResult
     */
    @RequestMapping(value = "/PlayBossMassiveGame")
    public void playBossMassiveGame(HttpServletRequest request, HttpServletResponse response,
            @RequestParam("deck") String deck, @RequestParam("count") int count, @RequestParam("gt") int guardType,
            @RequestParam("hlv") int heroLv, @RequestParam("bn") String bossName, @RequestParam("bk") int buffKingdom,
            @RequestParam("bf") int buffForest, @RequestParam("bs") int buffSavage, @RequestParam("bh") int buffHell
            ) throws IOException {
        PrintWriter writer = response.getWriter();
        try {
            logger.info("PlayBossMassiveGame from " + request.getRemoteAddr() + ":");
            logger.info("Deck = " + deck);
            logger.info("Count = " + count + ", Hero LV = " + heroLv + ", Boss = " + bossName);
            response.setContentType("application/json");
            this.userActionRecorder.addAction(new UserAction(new Date(), request.getRemoteAddr(), "Play Boss Massive Game",
                    String.format("Deck=%s<br />HeroLV=%d, Boss=%s, Count=%d, GuardType=%d", deck, heroLv, bossName, count, guardType)));
            GameUI ui = new DummyGameUI();
            GameSetup setup = GameSetup.setupBossGame(deck, bossName, heroLv, buffKingdom, buffForest, buffSavage, buffHell, guardType, count, ui);
            BossGameResult result = GameLauncher.playBossGame(setup);
            TrivialBossGameResult resultBean = new TrivialBossGameResult(result);
            resultBean.setValidationResult("<div style='color: red'>" + resultBean.getValidationResult() + "</div>");
            long averageDamageToBoss = Math.round(result.getAvgDamage());
            logger.info("Average damage to boss: " + averageDamageToBoss);
            writer.print(jsonHandler.toJson(resultBean));
        } catch (Exception e) {
            response.setStatus(500);
            writer.print("{'error':'" + e.getMessage() + "'}");
            this.logger.error(e);
        }
    }

    //排序的卡牌类，包含卡牌模拟名和胜率
    public class sortCard{

        private String cardname;
    
        private Double cnt;
    
        public sortCard() {}
    
        public sortCard(String cardname, Double cnt) {
            this.cardname = cardname;
            this.cnt = cnt;
        }
    
        public String getName() {
            return cardname;
        }
    
        public void setName(String cardname) {
            this.cardname = cardname;
        }
    
        public Double getCnt() {
            return cnt;
        }
    
        public void setCnt(Double cnt) {
            this.cnt = cnt;
        }
        
        /*
        @Override
        public String toString() {
            return "Student{" +
                    "name='" + cardname + '\'' +
                    ", age=" + cnt +
                    '}';
        }
        */
    }


 
        
      
    @RequestMapping(value = "/PlayLilithMassiveGame")
    public void playLilithMassiveGame(HttpServletRequest request, HttpServletResponse response,
        @RequestParam("deck") String deck, @RequestParam("ecg") boolean enableCustomGuards, @RequestParam("cg") String customGuards,
        @RequestParam("cgab") int customGuardsAtBuff, @RequestParam("cghb") int customGuardsHpBuff,
        @RequestParam("count") int count, @RequestParam("gt") int gameType,
        @RequestParam("hlv") int heroLv, @RequestParam("ln") String lilithName,
        @RequestParam("tc") int targetRemainingGuardCount, @RequestParam("rhp") int remainingHP,
        @RequestParam("ec") String eventCardNames
        ) throws IOException {
        PrintWriter writer = response.getWriter();
        try {
            String logMessage = String.format("Deck=%s<br />HeroLV=%d, Lilith=%s, GameType=%d, EventCards=%s", deck, heroLv, lilithName, gameType, eventCardNames);
            logger.info("PlayLilithMassiveGame: " + logMessage);
            this.userActionRecorder.addAction(new UserAction(new Date(), request.getRemoteAddr(), "Play Lilith Massive Game", logMessage));

            writer.append(Utils.getCurrentDateTime() + "<br />");
            writer.append("模拟场次: " + count + "<br />");

            //-100:卡组排序, -101:15级排序, -110到-129:卡牌选择和符文选择
            //String ttest = "";
            int selectlevel = 0;
            if(count < -10){        //取出跟着count带进来的排序类型和排序次数, 由于不想去改原作者的代码, 所以只好用麻烦的方法来传过来
                selectlevel = count/10;
                count = count - selectlevel*10;
                if(count == -1){
                    count = 10;
                }else if(count == -2){
                    count = 100;
                }else if(count == -3){
                    count = 1000;
                }else if(count == -4){
                    count = 10000;
                }
            }
            //ttest = ttest + selectlevel +","+ count;
            //count = 10;
            List<sortCard> sortcards = new ArrayList<>();
            LilithGameResult result = null;

            String cardswithcnt = "";
            String cardsnocnt = "";
            String errornote = "";


            if (selectlevel == -100 || selectlevel == -101){        //-100:本卡组卡牌胜率排序;  -101:本卡组卡牌升15胜率排序
                //selectlevel = count;
                //count = 1000;

                //把中文逗号换成英文，打多了的减掉
                deck = deck.replace("，", ",");
                deck = deck + ",";    //在结尾加逗号是为了方便循环的时候不用写额外的判断语句，保证每个卡牌后都至少有一个逗号
                deck = deck.replace(" ", "");   //先去掉了空格才能接下来去掉双逗号
                deck = deck.replace(",,", ",");
                deck = deck.replace(",,", ",");
                String subdeck = deck;

                
                //String cardswithcnt = "";
                //String cardsnocnt = "";

                //List<sortCard> sortcards = new ArrayList<>();
                //sortcards.add(new sortCard("a", 18.1));

                int firstcnt = subdeck.indexOf(',');
                //String firstdeck = subdeck;
                //String newdesk;
                //LilithGameResult result = null;
                while (firstcnt != -1){

                    String thisdeck = subdeck.substring(0, firstcnt+1).replace(" ","");  //第一个取出来的卡牌
                    subdeck = subdeck.substring(firstcnt+1);            //剩下的卡牌组，用来接着循环取剩下的卡牌

                    if(thisdeck.replace(",", "") != ""){
                        String newdeck = "";
                        String thisdeck15 = "";
                        if(selectlevel == -100){        //当为卡组排序时,每次进行去掉本张卡的模拟
                            newdeck = deck.replaceFirst(thisdeck.replace("+", "\\+"), "");  //去掉取出来的卡牌的卡牌组
                        }else if(selectlevel == -101){
                            
                            newdeck = deck.replaceFirst(thisdeck.replace("+", "\\+"), "");  //去掉取出来的卡牌的卡牌组
                            if(thisdeck.indexOf('-') == -1){            //当本卡没有加等级的时候,加上等级15
                                thisdeck15 = thisdeck +"-15";
                            }else{                                      //当本卡有加等级的时候,把10级或14级换成15级, 应该不会有别的等级出现
                                thisdeck15 = thisdeck.replace("-10", "-15").replace("-14", "-15");
                            } 
                            newdeck = thisdeck15 +","+ newdeck;
                        }

                        try {
                            result = null;
                            GameUI ui = new DummyGameUI();
                            result = GameLauncher.playLilithGame(
                                newdeck, lilithName, heroLv, gameType, 
                                        targetRemainingGuardCount, remainingHP, eventCardNames, count, ui);

                            sortcards.add(new sortCard(thisdeck,result.getAvgBattleCount()));

                        } catch (PvlGameTimeoutException e) {
                            writer.append("进攻次数超过最大次数，你的卡组太弱了");
                        }
                        
                    }

                    firstcnt = subdeck.indexOf(',');                    //下一个卡牌在哪里结束
                    
                }
                    
                //最后运行一次正常的
                try {
                    result = null;
                    GameUI ui = new DummyGameUI();
                    result = GameLauncher.playLilithGame(
                                deck, lilithName, heroLv, gameType, 
                                targetRemainingGuardCount, remainingHP, eventCardNames, count, ui);

                } catch (PvlGameTimeoutException e) {
                    writer.append("进攻次数超过最大次数，你的卡组太弱了");
                }
                    

                Collections.sort(sortcards, new Comparator<sortCard>() {
                    @Override
                    public int compare(sortCard o1, sortCard o2) {
                        //return o1.getCnt()>o2.getCnt()? -1:(o1.getCnt()==o2.getCnt()? 0:1);
                        return o1.getCnt().compareTo(o2.getCnt());
                    }
                });
                int i = 1;
                for(sortCard sortcard:sortcards){
                    //System.out.println(student.cardname);
                    //System.out.println(student.cnt.toString());
                    cardswithcnt = cardswithcnt + sortcard.cardname.replace(",","");
                    cardswithcnt = cardswithcnt +"("+ sortcard.cnt.toString() +"),";

                    if(selectlevel == -100){            //当为卡组排序的时候, 最后按强度卡牌排序显示卡组
                        cardsnocnt = cardsnocnt + sortcard.cardname +" ";
                    }else if(selectlevel == -101){      //当为15排序的时候, 最后显示最强卡牌15级加其它卡牌来显示卡组
                        if(i ==1){
                            String thisdeck = sortcard.cardname;
                            String newdeck = deck.replaceFirst(thisdeck.replace("+", "\\+"), "");  //去掉取出来的卡牌的卡牌组
                            thisdeck = thisdeck.replace(",","");
                            if(thisdeck.indexOf('-') == -1){            //当本卡没有加等级的时候,加上等级15
                                thisdeck = thisdeck +"-15";
                            }else{                                      //当本卡有加等级的时候,把10级或14级换成15级, 应该不会有别的等级出现
                                thisdeck = thisdeck.replace("-10", "-15").replace("-14", "-15");
                            } 
                            cardsnocnt = thisdeck +","+ newdeck;
                        }
                        i++;
                    }
                    
                }

                
                writer.print("<div style='color: red'>" + result.getValidationResult() + "</div>");
                writer.append("<table>");
                writer.append("<tr><td>平均需要进攻次数: </td><td>" + result.getAvgBattleCount() + "</td></tr>");
                writer.append("<tr><td>不稳定度: </td><td>" + Math.round(result.getCvBattleCount() * 100) + "%</td></tr>");
                writer.append("<tr><td>平均每轮进攻对莉莉丝伤害: </td><td>" + Math.round(result.getAvgDamageToLilith()) + "</td></tr>");
                writer.append("<tr><td>不稳定度: </td><td>" + Math.round(result.getCvDamageToLilith() * 100) + "%</td></tr>");
                writer.append("<tr><td>卡组排序卡组数值: </td><td>" + cardswithcnt + "</td></tr>");
                writer.append("<tr><td>卡组排序卡组: </td><td>" + cardsnocnt + "</td></tr>");
                writer.append("</td></tr></table>");

                
                //int cardcnt = olddeck;

                //LilithGameResult result = null;
                /*writer.print("<div style='color: red'>" + ttest + "</div>");
                writer.append("<table>");
                
                writer.append("<tr><td>不稳定度: </td><td>" + ttest2 + "%</td></tr>");
                
                writer.append("</td></tr></table>");*/

            } else if(selectlevel <= -110 && selectlevel >= -139){              //在已获得的卡牌中选择出对本战胜率最高的
                
                String bestdeck = "";
                Double bestcnt = 0.0;

                InputStream cardFWStream;
                List<String> txturls=new ArrayList<>();

                //从文件中读取出卡牌列表
                if(selectlevel == -110){            //精选345
                    txturls.add("cfvbaibai/cardfantasy/data/MyCard5S.txt");
                    txturls.add("cfvbaibai/cardfantasy/data/MyCard5F.txt");
                    txturls.add("cfvbaibai/cardfantasy/data/MyCard5H.txt");
                    txturls.add("cfvbaibai/cardfantasy/data/MyCard5T.txt");
                    txturls.add("cfvbaibai/cardfantasy/data/MyCard4S.txt");
                    txturls.add("cfvbaibai/cardfantasy/data/MyCard4F.txt");
                    txturls.add("cfvbaibai/cardfantasy/data/MyCard4H.txt");
                    txturls.add("cfvbaibai/cardfantasy/data/MyCard4T.txt");
                    txturls.add("cfvbaibai/cardfantasy/data/MyCard3.txt");
                    txturls.add("cfvbaibai/cardfantasy/data/MyCard4M.txt");
                    txturls.add("cfvbaibai/cardfantasy/data/MyCard5M.txt");
               
                }else if(selectlevel == -120 || selectlevel == -125){      //符文精选或全符文时

                    txturls.add("cfvbaibai/cardfantasy/data/MyCardFS.txt");
                    txturls.add("cfvbaibai/cardfantasy/data/MyCardFF.txt");
                    txturls.add("cfvbaibai/cardfantasy/data/MyCardFH.txt");
                    txturls.add("cfvbaibai/cardfantasy/data/MyCardFT.txt");
    
                }else if(selectlevel == -111 ){             
                    txturls.add("cfvbaibai/cardfantasy/data/MyCard1.txt");
                }else if(selectlevel == -112){
                    txturls.add("cfvbaibai/cardfantasy/data/MyCard2.txt");
                }else if(selectlevel == -113){
                    txturls.add("cfvbaibai/cardfantasy/data/MyCard3.txt");
                }else if(selectlevel == -114){
                    txturls.add("cfvbaibai/cardfantasy/data/MyCard4S.txt");
                    txturls.add("cfvbaibai/cardfantasy/data/MyCard4F.txt");
                    txturls.add("cfvbaibai/cardfantasy/data/MyCard4H.txt");
                    txturls.add("cfvbaibai/cardfantasy/data/MyCard4T.txt");
                    txturls.add("cfvbaibai/cardfantasy/data/MyCard4M.txt");
                }else if(selectlevel == -115){
                    txturls.add("cfvbaibai/cardfantasy/data/MyCard5S.txt");
                    txturls.add("cfvbaibai/cardfantasy/data/MyCard5F.txt");
                    txturls.add("cfvbaibai/cardfantasy/data/MyCard5H.txt");
                    txturls.add("cfvbaibai/cardfantasy/data/MyCard5T.txt");
                    txturls.add("cfvbaibai/cardfantasy/data/MyCard5M.txt");
                }else if(selectlevel == -116){      //45星
                    txturls.add("cfvbaibai/cardfantasy/data/MyCard5S.txt");
                    txturls.add("cfvbaibai/cardfantasy/data/MyCard5F.txt");
                    txturls.add("cfvbaibai/cardfantasy/data/MyCard5H.txt");
                    txturls.add("cfvbaibai/cardfantasy/data/MyCard5T.txt");
                    txturls.add("cfvbaibai/cardfantasy/data/MyCard4S.txt");
                    txturls.add("cfvbaibai/cardfantasy/data/MyCard4F.txt");
                    txturls.add("cfvbaibai/cardfantasy/data/MyCard4H.txt");
                    txturls.add("cfvbaibai/cardfantasy/data/MyCard4T.txt");
                    txturls.add("cfvbaibai/cardfantasy/data/MyCard4M.txt");
                    txturls.add("cfvbaibai/cardfantasy/data/MyCard5M.txt");
                }else if(selectlevel == -121){       //水
                    txturls.add("cfvbaibai/cardfantasy/data/MyCardFS.txt");
                }else if(selectlevel == -122){       //风
                    txturls.add("cfvbaibai/cardfantasy/data/MyCardFF.txt");
                }else if(selectlevel == -123){       //火
                    txturls.add("cfvbaibai/cardfantasy/data/MyCardFH.txt");
                }else if(selectlevel == -124){       //土
                    txturls.add("cfvbaibai/cardfantasy/data/MyCardFT.txt");
                }else if(selectlevel == -131){       //王国
                    txturls.add("cfvbaibai/cardfantasy/data/MyCard5S.txt");
                    txturls.add("cfvbaibai/cardfantasy/data/MyCard4S.txt");
                }else if(selectlevel == -132){       //森林
                    txturls.add("cfvbaibai/cardfantasy/data/MyCard5F.txt");
                    txturls.add("cfvbaibai/cardfantasy/data/MyCard4F.txt");
                }else if(selectlevel == -133){       //蛮荒
                    txturls.add("cfvbaibai/cardfantasy/data/MyCard5T.txt");
                    txturls.add("cfvbaibai/cardfantasy/data/MyCard4T.txt");
                }else if(selectlevel == -134){       //地狱
                    txturls.add("cfvbaibai/cardfantasy/data/MyCard5H.txt");
                    txturls.add("cfvbaibai/cardfantasy/data/MyCard4H.txt");
                }
                
                //从mycard文件列表中取出卡牌
                for (int i = 0; i < txturls.size(); i++) {
                    
                    String txturl = txturls.get(i); 
                    cardFWStream = CardDataStore.class.getClassLoader().getResourceAsStream(txturl);
                    //将文件中的符文读进list里
                    BufferedReader br = new BufferedReader(new InputStreamReader(cardFWStream, "UTF-8"));//构造一个BufferedReader类来读取文件
                    String line = null;
                    while((line = br.readLine())!=null){//使用readLine方法，一次读一行
                        if(line.indexOf("//") == -1){      //当卡牌不为尚未收录的卡牌时
                            line = line.replace('，', ',');
                            if(selectlevel == -110 || selectlevel == -120){      //当选择类型为精选时
                                if(line.indexOf(',') != -1){      //当为标记卡牌时
                                    sortcards.add(new sortCard(line.replace("-",","), 0.0));
                                }
                            }else{                                              //当选择类型为非精选时
                                sortcards.add(new sortCard(line.replace("-",","), 0.0));
                            }
                        }
                    }
                    br.close();    
                }

            /*
            } else if(selectlevel <= -110 && selectlevel >= -129){              //在已获得的卡牌中选择出对本战胜率最高的
                //selectlevel = count;
                //count = 1000;
                //deck = deck.replace("，", ",");

                List<sortCard> sortcards = new ArrayList<>();
                //sortcards.add(new sortCard("a", 18.1));
                String cardswithcnt = "";
                LilithGameResult result = null;
                String errornote = "";
                String bestdeck = "";
                Double bestcnt = 999.9;

                InputStream cardFWStream;
                String txturl = "";

                //设定各类别下的卡牌文件目录
                
                //从文件中读取出卡牌列表
                if(selectlevel == -110){            //精选345

                    txturl = "cfvbaibai/cardfantasy/data/MyCard5.txt";
                    cardFWStream = CardDataStore.class.getClassLoader().getResourceAsStream(txturl);
                    //将文件中的符文读进list里
                    BufferedReader br = new BufferedReader(new InputStreamReader(cardFWStream, "UTF-8"));//构造一个BufferedReader类来读取文件
                    String line = null;
                    
                    while((line = br.readLine())!=null){//使用readLine方法，一次读一行
                        if(line.indexOf("//") == -1 && line.replace('，', ',').indexOf(',') != -1){      //当卡牌不为尚未收录的卡牌,并且为标记卡牌时
                            sortcards.add(new sortCard(line.replace("-",","), 0.0));
                        }
                    }
                    br.close();    

                    txturl = "cfvbaibai/cardfantasy/data/MyCard4.txt";
                    cardFWStream = CardDataStore.class.getClassLoader().getResourceAsStream(txturl);
                    //将文件中的符文读进list里
                    br = new BufferedReader(new InputStreamReader(cardFWStream, "UTF-8"));//构造一个BufferedReader类来读取文件
                    line = null;
                    
                    while((line = br.readLine())!=null){//使用readLine方法，一次读一行
                        if(line.indexOf("//") == -1 && line.replace('，', ',').indexOf(',') != -1){      //当卡牌不为尚未收录的卡牌,并且为标记卡牌时
                            sortcards.add(new sortCard(line.replace("-",","), 0.0));
                        }
                    }
                    br.close();    

                    txturl = "cfvbaibai/cardfantasy/data/MyCard3.txt";
                    cardFWStream = CardDataStore.class.getClassLoader().getResourceAsStream(txturl);
                    //将文件中的符文读进list里
                    br = new BufferedReader(new InputStreamReader(cardFWStream, "UTF-8"));//构造一个BufferedReader类来读取文件
                    line = null;
                    
                    while((line = br.readLine())!=null){//使用readLine方法，一次读一行
                        if(line.indexOf("//") == -1 && line.replace('，', ',').indexOf(',') != -1){      //当卡牌不为尚未收录的卡牌,并且为标记卡牌时
                            sortcards.add(new sortCard(line.replace("-",","), 0.0));
                        }
                    }
                    br.close();    


                }else if(selectlevel == -116){      //45星
                    
                    txturl = "cfvbaibai/cardfantasy/data/MyCard4.txt";
                    cardFWStream = CardDataStore.class.getClassLoader().getResourceAsStream(txturl);
                    //将文件中的符文读进list里
                    BufferedReader br = new BufferedReader(new InputStreamReader(cardFWStream, "UTF-8"));//构造一个BufferedReader类来读取文件
                    String line = null;
                    
                    while((line = br.readLine())!=null){//使用readLine方法，一次读一行
                        if(line.indexOf("//") == -1){      //当卡牌不为尚未收录的卡牌时
                            sortcards.add(new sortCard(line.replace("-",","), 0.0));
                        }
                    }
                    br.close();    

                    txturl = "cfvbaibai/cardfantasy/data/MyCard5.txt";
                    cardFWStream = CardDataStore.class.getClassLoader().getResourceAsStream(txturl);
                    //将文件中的符文读进list里
                    br = new BufferedReader(new InputStreamReader(cardFWStream, "UTF-8"));//构造一个BufferedReader类来读取文件
                    line = null;
                    
                    while((line = br.readLine())!=null){//使用readLine方法，一次读一行
                        if(line.indexOf("//") == -1){      //当卡牌不为尚未收录的卡牌时
                            sortcards.add(new sortCard(line.replace("-",","), 0.0));
                        }
                    }
                    br.close();    

                }else if(selectlevel == -120 || selectlevel == -125){      //符文精选或全符文时
                    
                    txturl = "cfvbaibai/cardfantasy/data/MyCardFWS.txt";
                    cardFWStream = CardDataStore.class.getClassLoader().getResourceAsStream(txturl);
                    //将文件中的符文读进list里
                    BufferedReader br = new BufferedReader(new InputStreamReader(cardFWStream, "UTF-8"));//构造一个BufferedReader类来读取文件
                    String line = null;
                    
                    while((line = br.readLine())!=null){//使用readLine方法，一次读一行
                        if(line.indexOf("//") == -1){      //当卡牌不为尚未收录的卡牌时
                            if(selectlevel != 120 || line.replace('，', ',').indexOf(",") != -1){      //当为全符文或者为精选标记符文时
                                sortcards.add(new sortCard(line.replace("-",","), 0.0));
                            }
                        }
                    }
                    br.close();    

                    txturl = "cfvbaibai/cardfantasy/data/MyCardFWF.txt";
                    cardFWStream = CardDataStore.class.getClassLoader().getResourceAsStream(txturl);
                    //将文件中的符文读进list里
                    br = new BufferedReader(new InputStreamReader(cardFWStream, "UTF-8"));//构造一个BufferedReader类来读取文件
                    line = null;
                    
                    while((line = br.readLine())!=null){//使用readLine方法，一次读一行
                        if(line.indexOf("//") == -1){      //当卡牌不为尚未收录的卡牌时
                            if(selectlevel != 120 || line.replace('，', ',').indexOf(",") != -1){      //当为全符文或者为精选标记符文时
                                sortcards.add(new sortCard(line.replace("-",","), 0.0));
                            }
                        }
                    }

                    txturl = "cfvbaibai/cardfantasy/data/MyCardFWH.txt";
                    cardFWStream = CardDataStore.class.getClassLoader().getResourceAsStream(txturl);
                    //将文件中的符文读进list里
                    br = new BufferedReader(new InputStreamReader(cardFWStream, "UTF-8"));//构造一个BufferedReader类来读取文件
                    line = null;
                    
                    while((line = br.readLine())!=null){//使用readLine方法，一次读一行
                        if(line.indexOf("//") == -1){      //当卡牌不为尚未收录的卡牌时
                            if(selectlevel != 120 || line.replace('，', ',').indexOf(",") != -1){      //当为全符文或者为精选标记符文时
                                sortcards.add(new sortCard(line.replace("-",","), 0.0));
                            }
                        }
                    }

                    txturl = "cfvbaibai/cardfantasy/data/MyCardFWT.txt";
                    cardFWStream = CardDataStore.class.getClassLoader().getResourceAsStream(txturl);
                    //将文件中的符文读进list里
                    br = new BufferedReader(new InputStreamReader(cardFWStream, "UTF-8"));//构造一个BufferedReader类来读取文件
                    line = null;
                    
                    while((line = br.readLine())!=null){//使用readLine方法，一次读一行
                        if(line.indexOf("//") == -1){      //当卡牌不为尚未收录的卡牌时
                            if(selectlevel != 120 || line.replace('，', ',').indexOf(",") != -1){      //当为全符文或者为精选标记符文时
                                sortcards.add(new sortCard(line.replace("-",","), 0.0));
                            }
                        }
                    }
                    br.close();
    

                }else{              //其它单个星级卡牌

                    if(selectlevel == -111 ){             
                        txturl = "cfvbaibai/cardfantasy/data/MyCard1.txt";
                    }else if(selectlevel == -112){
                        txturl = "cfvbaibai/cardfantasy/data/MyCard2.txt";
                    }else if(selectlevel == -113){
                        txturl = "cfvbaibai/cardfantasy/data/MyCard3.txt";
                    }else if(selectlevel == -114){
                        txturl = "cfvbaibai/cardfantasy/data/MyCard4.txt";
                    }else if(selectlevel == -115){
                        txturl = "cfvbaibai/cardfantasy/data/MyCard5.txt";
                    }else if(selectlevel == -121){       
                        txturl = "cfvbaibai/cardfantasy/data/MyCardFWS.txt";
                    }else if(selectlevel == -122){       
                        txturl = "cfvbaibai/cardfantasy/data/MyCardFWF.txt";
                    }else if(selectlevel == -123){       
                        txturl = "cfvbaibai/cardfantasy/data/MyCardFWH.txt";
                    }else if(selectlevel == -124){       
                        txturl = "cfvbaibai/cardfantasy/data/MyCardFWT.txt";
                    }

                    cardFWStream = CardDataStore.class.getClassLoader().getResourceAsStream(txturl);
                    
                    //cardname = cardname +"aaa";
                    
                        //将文件中的符文读进list里
                        BufferedReader br = new BufferedReader(new InputStreamReader(cardFWStream, "UTF-8"));//构造一个BufferedReader类来读取文件
                        String line = null;
                        //cardname = cardname +"bbb" + br.readLine();
                        while((line = br.readLine())!=null){//使用readLine方法，一次读一行
                            //result.append(System.lineSeparator()+s);
                            //cardname = cardname +"ccc"+ line;
                            if(line.indexOf("//") == -1){      //当卡牌不为尚未收录的卡牌时
                                if(selectlevel != -120 || line.indexOf(',') != -1){     //当选择条件不为精选，或者卡牌本身带精选标记时才放入列表
                                    sortcards.add(new sortCard(line.replace("-",","), 0.0));
                                }
                            }
                        
                        }
                        br.close();    
                }*/


                //用list里的符文分别模拟，将获得的分数写进list里
                for(sortCard sortcard:sortcards){
                    String olddeck = deck;
                    olddeck = olddeck.replace('，', ',');
                    olddeck = olddeck.substring(olddeck.indexOf(',')+1);
                    String thiscardname = sortcard.cardname.replace("，",",").replace(",", "");
                    /*if(thiscardname.indexOf("-") == -1){
                        if(selectlevel <= -120){     //为符文时
                            thiscardname = thiscardname +"-4, ";
                        }else{                      //为卡牌时
                            thiscardname = thiscardname +"-10, ";
                        }
                    }*/
                    //String newdeck = sortcard.cardname.replace("，",",").replace(",", "") +"-4, "+ olddeck;
                    //String newdeck = thiscardname + olddeck;
                    //errornote = errornote + newdeck +",";
                    String newdeck = thiscardname +", "+ olddeck;

                    try {
                        result = null;
                        GameUI ui = new DummyGameUI();
                        result = GameLauncher.playLilithGame(
                            newdeck, lilithName, heroLv, gameType, 
                                    targetRemainingGuardCount, remainingHP, eventCardNames, count, ui);

                        //sortcards.add(new sortCard(thisdeck,result.getAvgBattleCount()));
                        //sortcard.cnt = result.getAvgBattleCount();
                        Double thiscnt = result.getAvgBattleCount();
                        sortcard.cnt = thiscnt;
                        if(thiscnt < bestcnt){
                            bestcnt = thiscnt;
                            bestdeck = newdeck;
                        }
                        if(result.getValidationResult() != ""){
                            errornote = errornote + sortcard.cardname +":"+ result.getValidationResult() +",";
                        }
                        

                    } catch (PvlGameTimeoutException e) {
                        writer.append("进攻次数超过最大次数，你的卡组太弱了");
                    }

                    //cardswithcnt = cardswithcnt + sortcard.cardname;
                    //cardswithcnt = cardswithcnt +"("+ sortcard.cnt.toString() +"),";
                          
                    
                }

                //list排序
                Collections.sort(sortcards, new Comparator<sortCard>() {
                    @Override
                    public int compare(sortCard o1, sortCard o2) {
                        //return o1.getCnt()>o2.getCnt()? -1:(o1.getCnt()==o2.getCnt()? 0:1);
                        return o1.getCnt().compareTo(o2.getCnt());
                    }
                });

                //从list中将前20的卡牌和分数取出来
                int i = 0;
                for(sortCard sortcard:sortcards){
                    //System.out.println(student.cardname);
                    //System.out.println(student.cnt.toString());
                    cardswithcnt = cardswithcnt + sortcard.cardname.replace(",","");
                    cardswithcnt = cardswithcnt +"("+ sortcard.cnt.toString() +"),";

                    //cardsnocnt = cardsnocnt + sortcard.cardname +" ";
                    //ttest = ttest +"读取符文循环，";
                    i++;
                    if(i>=20){
                        break;
                    }
                    
                }


                //最后显示一遍原始卡组的分数
                try {
                    result = null;
                    GameUI ui = new DummyGameUI();
                    result = GameLauncher.playLilithGame(
                                deck, lilithName, heroLv, gameType, 
                                targetRemainingGuardCount, remainingHP, eventCardNames, count, ui);
                } catch (PvlGameTimeoutException e) {
                    writer.append("进攻次数超过最大次数，你的卡组太弱了");
                }

                writer.print("<div style='color: red'>" + result.getValidationResult() + "</div>");
                writer.print("<div style='color: red'>" + errornote + "</div>");
                writer.append("<table>");
                writer.append("<tr><td>平均需要进攻次数: </td><td>" + result.getAvgBattleCount() + "</td></tr>");
                writer.append("<tr><td>不稳定度: </td><td>" + Math.round(result.getCvBattleCount() * 100) + "%</td></tr>");
                writer.append("<tr><td>平均每轮进攻对莉莉丝伤害: </td><td>" + Math.round(result.getAvgDamageToLilith()) + "</td></tr>");
                writer.append("<tr><td>不稳定度: </td><td>" + Math.round(result.getCvDamageToLilith() * 100) + "%</td></tr>");
                writer.append("<tr><td>强度选卡卡牌数值: </td><td>" + cardswithcnt + "</td></tr>");
                writer.append("<tr><td>强度选卡卡组: </td><td>" + bestdeck + "</td></tr>");
                writer.append("</td></tr></table>");



                //writer.print("<div style='color: red'>" + selectlevel + cardname + "</div>");
            } else {    //作者原本的强度分析
                //本卡组胜率
                try {
                    //LilithGameResult result = null;
                    GameUI ui = new DummyGameUI();
                    if (enableCustomGuards && gameType == 0) {
                        result = GameLauncher.playCustomLilithGame(
                                deck, lilithName + "," + customGuards, heroLv, customGuardsAtBuff, customGuardsHpBuff,
                                gameType, targetRemainingGuardCount, remainingHP, eventCardNames, count, ui);
                    } else {
                        result = GameLauncher.playLilithGame(
                                deck, lilithName, heroLv, gameType, 
                                targetRemainingGuardCount, remainingHP, eventCardNames, count, ui);
                    }
                    writer.print("<div style='color: red'>" + result.getValidationResult() + "</div>");
                    writer.append("<table>");
                    writer.append("<tr><td>平均需要进攻次数: </td><td>" + result.getAvgBattleCount() + "</td></tr>");
                    writer.append("<tr><td>不稳定度: </td><td>" + Math.round(result.getCvBattleCount() * 100) + "%</td></tr>");
                    writer.append("<tr><td>平均每轮进攻对莉莉丝伤害: </td><td>" + Math.round(result.getAvgDamageToLilith()) + "</td></tr>");
                    writer.append("<tr><td>不稳定度: </td><td>" + Math.round(result.getCvDamageToLilith() * 100) + "%</td></tr>");
                    writer.append("</td></tr></table>");
                } catch (PvlGameTimeoutException e) {
                    writer.append("进攻次数超过最大次数，你的卡组太弱了");
                }
            }
        } catch (Exception e) {
            writer.print(errorHelper.handleError(e, false));
        }
    }

    @RequestMapping(value = "/PlayMap1MatchGame")
    public void playMap1MatchGame(HttpServletRequest request, HttpServletResponse response,
            @RequestParam("deck") String deck, @RequestParam("count") int count,
            @RequestParam("hlv") int heroLv, @RequestParam("map") String map) throws IOException {
        PrintWriter writer = response.getWriter();
        try {
            logger.info("PlayMap1MatchGame from " + request.getRemoteAddr() + ":");
            logger.info("Deck = " + deck);
            logger.info("Hero LV = " + heroLv + ", Map = " + map);
            this.userActionRecorder.addAction(new UserAction(new Date(), request.getRemoteAddr(), "Play Map 1Match Game",
                    String.format("Deck=%s<br />HeroLV=%d, Map=%s", deck, heroLv, map)));
            WebPlainTextGameUI ui = new WebPlainTextGameUI();
            MapGameResult result = GameLauncher.playMapGame(deck, map, heroLv, 1, ui);
            writer.print(Utils.getCurrentDateTime() + "<br />");
            writer.print("<div style='color: red'>" + result.getValidationResult() + "</div>");
            writer.print(ui.getAllText());
            logger.info("Result: " + result.getLastResultName());
        } catch (Exception e) {
            writer.print(errorHelper.handleError(e, false));
        }
    }
    
    @RequestMapping(value = "/SimulateMap1MatchGame", headers = "Accept=application/json")
    public void simulateMap1MatchGame(HttpServletRequest request, HttpServletResponse response,
            @RequestParam("deck") String deck, @RequestParam("count") int count,
            @RequestParam("hlv") int heroLv, @RequestParam("map") String map) throws IOException {
        response.setContentType("application/json");
        PrintWriter writer = response.getWriter();
        try {
            logger.info("SimulateMap1MatchGame from " + request.getRemoteAddr() + ":");
            logger.info("Deck = " + deck);
            logger.info("Hero LV = " + heroLv + ", Map = " + map);
            this.userActionRecorder.addAction(new UserAction(new Date(), request.getRemoteAddr(), "Simulate Map 1Match Game",
                    String.format("Deck=%s<br />HeroLV=%d, Map=%s", deck, heroLv, map)));
            PlayerInfo player = PlayerBuilder.build(true, "玩家", deck, heroLv);
            StructuredRecordGameUI ui = new StructuredRecordGameUI();
            PveEngine engine = new PveEngine(ui, this.maps);
            PveGameResult gameResult = engine.play(player, map);
            BattleRecord record = ui.getRecord();
            writer.println(jsonHandler.toJson(record));
            logger.info("Result: " + gameResult.name());
        } catch (Exception e) {
            writer.println(errorHelper.handleError(e, true));
        }
    }
    
    @RequestMapping(value = "/PlayMapMassiveGame")
    public void playMapMassiveGame(HttpServletRequest request, HttpServletResponse response,
            @RequestParam("deck") String deck, @RequestParam("hlv") int heroLv, @RequestParam("map") String map,
            @RequestParam("count") int count) throws IOException {
        PrintWriter writer = response.getWriter();
        GameUI ui = new DummyGameUI();
        try {
            logger.info("PlayMapMassiveGame from " + request.getRemoteAddr() + ":");
            logger.info(String.format("Lv = %d, Map = %s, Count = %d", heroLv, map, count));
            logger.info("Deck = " + deck);
            
            this.userActionRecorder.addAction(new UserAction(new Date(), request.getRemoteAddr(), "Play Map Massive Game",
                    String.format("Deck=%s<br />Lv=%d, Count=%d, Map=%s", deck, heroLv, count, map)));

            if (Global.isDebugging()) {
                ui = new WebPlainTextGameUI();
            }

            MapGameResult result = null;

            int selectlevel = 0;
            if(count < -10){        //取出跟着count带进来的排序类型和排序次数, 由于不想去改原作者的代码, 所以只好用麻烦的方法来传过来
                selectlevel = count/10;
                count = count - selectlevel*10;
                if(count == -1){
                    count = 10;
                }else if(count == -2){
                    count = 100;
                }else if(count == -3){
                    count = 1000;
                }else if(count == -4){
                    count = 10000;
                }
            }
            //虽然下面这些在官方千场里用不到, 但不想写在里面, 嫌麻烦
            String cardswithcnt = "";
            String cardsnocnt = "";
            List<sortCard> sortcards = new ArrayList<>();
            deck = deck.replace("，", ",");
            deck = deck + ",";    //在结尾加逗号是为了方便循环的时候不用写额外的判断语句，保证每个卡牌后都至少有一个逗号
            deck = deck.replace(" ", "");
            deck = deck.replace(",,", ",");
            deck = deck.replace(",,", ",");
            String subdeck = deck;
            int firstcnt = subdeck.indexOf(',');
            String errornote = "";
            String ttest = "";

            if (selectlevel == -100 || selectlevel == -101){        //-100:本卡组卡牌胜率排序;  -101:本卡组卡牌升15胜率排序
                while (firstcnt != -1){
                    //取卡牌
                    String thisdeck = subdeck.substring(0, firstcnt+1).replace(" ","");  //第一个取出来的卡牌
                    subdeck = subdeck.substring(firstcnt+1);            //剩下的卡牌组，用来接着循环取剩下的卡牌

                    if(thisdeck.replace(",", "") != ""){
                        String newdeck = "";
                        String thisdeck15 = "";
                        if(selectlevel == -100){        //当为卡组排序时,每次进行去掉本张卡的模拟
                            newdeck = deck.replaceFirst(thisdeck.replace("+", "\\+"), "");  //去掉取出来的卡牌的卡牌组
                            //ttest = ttest + thisdeck +"|"+ newdeck +"@\r\n";
                        }else if(selectlevel == -101){
                            
                            newdeck = deck.replaceFirst(thisdeck.replace("+", "\\+"), "");  //去掉取出来的卡牌的卡牌组
                            if(thisdeck.indexOf('-') == -1){            //当本卡没有加等级的时候,加上等级15
                                thisdeck15 = thisdeck +"-15";
                            }else{                                      //当本卡有加等级的时候,把10级或14级换成15级, 应该不会有别的等级出现
                                thisdeck15 = thisdeck.replace("-10", "-15").replace("-14", "-15");
                            } 
                            newdeck = thisdeck15 +","+ newdeck;
                        }
                        

                        //将卡牌胜率写进列表
                        //result = null;
                        //ui = new DummyGameUI();
                        result = GameLauncher.playMapGame(newdeck, map, heroLv, count, ui);
                        sortcards.add(new sortCard(thisdeck,Double.valueOf(result.getAdvWinCount())));
                    }
                    firstcnt = subdeck.indexOf(',');                    //下一个卡牌在哪里结束
                }
                //最后运行一次正常的, 好显示数据及对比
                result = null;
                ui = new DummyGameUI();
                result = GameLauncher.playMapGame(deck, map, heroLv, count, ui);
                //开始排序列表
                Collections.sort(sortcards, new Comparator<sortCard>() {
                    @Override
                    public int compare(sortCard o1, sortCard o2) {
                        return o2.getCnt().compareTo(o1.getCnt());
                    }
                });
                //从列表中取出来
                int i = 1;
                for(sortCard sortcard:sortcards){
                    cardswithcnt = cardswithcnt + sortcard.cardname.replace(",","");
                    cardswithcnt = cardswithcnt +"("+ sortcard.cnt.toString() +"),";

                    if(selectlevel == -100){            //当为卡组排序的时候, 最后按强度卡牌排序显示卡组
                        cardsnocnt = cardsnocnt + sortcard.cardname +" ";
                    }else if(selectlevel == -101){      //当为15排序的时候, 最后显示最强卡牌15级加其它卡牌来显示卡组
                        if(i ==1){
                            String thisdeck = sortcard.cardname;
                            String newdeck = deck.replaceFirst(thisdeck.replace("+", "\\+"), "");  //去掉取出来的卡牌的卡牌组
                            thisdeck = thisdeck.replace(",","");
                            if(thisdeck.indexOf('-') == -1){            //当本卡没有加等级的时候,加上等级15
                                thisdeck = thisdeck +"-15";
                            }else{                                      //当本卡有加等级的时候,把10级或14级换成15级, 应该不会有别的等级出现
                                thisdeck = thisdeck.replace("-10", "-15").replace("-14", "-15");
                            } 
                            cardsnocnt = thisdeck +","+ newdeck;
                        }
                        i++;
                    }
                }
            } else if(selectlevel <= -110 && selectlevel >= -139){              //在已获得的卡牌中选择出对本战胜率最高的
                
                String bestdeck = "";
                Double bestcnt = 0.0;

                InputStream cardFWStream;
                List<String> txturls=new ArrayList<>();

                //从文件中读取出卡牌列表
                if(selectlevel == -110){            //精选345
                    txturls.add("cfvbaibai/cardfantasy/data/MyCard5S.txt");
                    txturls.add("cfvbaibai/cardfantasy/data/MyCard5F.txt");
                    txturls.add("cfvbaibai/cardfantasy/data/MyCard5H.txt");
                    txturls.add("cfvbaibai/cardfantasy/data/MyCard5T.txt");
                    txturls.add("cfvbaibai/cardfantasy/data/MyCard4S.txt");
                    txturls.add("cfvbaibai/cardfantasy/data/MyCard4F.txt");
                    txturls.add("cfvbaibai/cardfantasy/data/MyCard4H.txt");
                    txturls.add("cfvbaibai/cardfantasy/data/MyCard4T.txt");
                    txturls.add("cfvbaibai/cardfantasy/data/MyCard3.txt");
                    txturls.add("cfvbaibai/cardfantasy/data/MyCard4M.txt");
                    txturls.add("cfvbaibai/cardfantasy/data/MyCard5M.txt");
               
                }else if(selectlevel == -120 || selectlevel == -125){      //符文精选或全符文时

                    txturls.add("cfvbaibai/cardfantasy/data/MyCardFS.txt");
                    txturls.add("cfvbaibai/cardfantasy/data/MyCardFF.txt");
                    txturls.add("cfvbaibai/cardfantasy/data/MyCardFH.txt");
                    txturls.add("cfvbaibai/cardfantasy/data/MyCardFT.txt");
    
                }else if(selectlevel == -111 ){             
                    txturls.add("cfvbaibai/cardfantasy/data/MyCard1.txt");
                }else if(selectlevel == -112){
                    txturls.add("cfvbaibai/cardfantasy/data/MyCard2.txt");
                }else if(selectlevel == -113){
                    txturls.add("cfvbaibai/cardfantasy/data/MyCard3.txt");
                }else if(selectlevel == -114){
                    txturls.add("cfvbaibai/cardfantasy/data/MyCard4S.txt");
                    txturls.add("cfvbaibai/cardfantasy/data/MyCard4F.txt");
                    txturls.add("cfvbaibai/cardfantasy/data/MyCard4H.txt");
                    txturls.add("cfvbaibai/cardfantasy/data/MyCard4T.txt");
                    txturls.add("cfvbaibai/cardfantasy/data/MyCard4M.txt");
                }else if(selectlevel == -115){
                    txturls.add("cfvbaibai/cardfantasy/data/MyCard5S.txt");
                    txturls.add("cfvbaibai/cardfantasy/data/MyCard5F.txt");
                    txturls.add("cfvbaibai/cardfantasy/data/MyCard5H.txt");
                    txturls.add("cfvbaibai/cardfantasy/data/MyCard5T.txt");
                    txturls.add("cfvbaibai/cardfantasy/data/MyCard5M.txt");
                }else if(selectlevel == -116){      //45星
                    txturls.add("cfvbaibai/cardfantasy/data/MyCard5S.txt");
                    txturls.add("cfvbaibai/cardfantasy/data/MyCard5F.txt");
                    txturls.add("cfvbaibai/cardfantasy/data/MyCard5H.txt");
                    txturls.add("cfvbaibai/cardfantasy/data/MyCard5T.txt");
                    txturls.add("cfvbaibai/cardfantasy/data/MyCard4S.txt");
                    txturls.add("cfvbaibai/cardfantasy/data/MyCard4F.txt");
                    txturls.add("cfvbaibai/cardfantasy/data/MyCard4H.txt");
                    txturls.add("cfvbaibai/cardfantasy/data/MyCard4T.txt");
                    txturls.add("cfvbaibai/cardfantasy/data/MyCard4M.txt");
                    txturls.add("cfvbaibai/cardfantasy/data/MyCard5M.txt");
                }else if(selectlevel == -121){       //水
                    txturls.add("cfvbaibai/cardfantasy/data/MyCardFS.txt");
                }else if(selectlevel == -122){       //风
                    txturls.add("cfvbaibai/cardfantasy/data/MyCardFF.txt");
                }else if(selectlevel == -123){       //火
                    txturls.add("cfvbaibai/cardfantasy/data/MyCardFH.txt");
                }else if(selectlevel == -124){       //土
                    txturls.add("cfvbaibai/cardfantasy/data/MyCardFT.txt");
                }else if(selectlevel == -131){       //王国
                    txturls.add("cfvbaibai/cardfantasy/data/MyCard5S.txt");
                    txturls.add("cfvbaibai/cardfantasy/data/MyCard4S.txt");
                }else if(selectlevel == -132){       //森林
                    txturls.add("cfvbaibai/cardfantasy/data/MyCard5F.txt");
                    txturls.add("cfvbaibai/cardfantasy/data/MyCard4F.txt");
                }else if(selectlevel == -133){       //蛮荒
                    txturls.add("cfvbaibai/cardfantasy/data/MyCard5T.txt");
                    txturls.add("cfvbaibai/cardfantasy/data/MyCard4T.txt");
                }else if(selectlevel == -134){       //地狱
                    txturls.add("cfvbaibai/cardfantasy/data/MyCard5H.txt");
                    txturls.add("cfvbaibai/cardfantasy/data/MyCard4H.txt");
                }
                
                //从mycard文件列表中取出卡牌
                for (int i = 0; i < txturls.size(); i++) {
                    
                    String txturl = txturls.get(i); 
                    cardFWStream = CardDataStore.class.getClassLoader().getResourceAsStream(txturl);
                    //将文件中的符文读进list里
                    BufferedReader br = new BufferedReader(new InputStreamReader(cardFWStream, "UTF-8"));//构造一个BufferedReader类来读取文件
                    String line = null;
                    while((line = br.readLine())!=null){//使用readLine方法，一次读一行
                        if(line.indexOf("//") == -1){      //当卡牌不为尚未收录的卡牌时
                            line = line.replace('，', ',');
                            if(selectlevel == -110 || selectlevel == -120){      //当选择类型为精选时
                                if(line.indexOf(',') != -1){      //当为标记卡牌时
                                    sortcards.add(new sortCard(line.replace("-",","), 0.0));
                                }
                            }else{                                              //当选择类型为非精选时
                                sortcards.add(new sortCard(line.replace("-",","), 0.0));
                            }
                        }
                    }
                    br.close();    
                }
/*
            } else if(selectlevel <= -110 && selectlevel >= -129){              //在已获得的卡牌中选择出对本战胜率最高的
                
                String bestdeck = "";
                Double bestcnt = 0.0;

                InputStream cardFWStream;
                String txturl = "";

                //从文件中读取出卡牌列表
                if(selectlevel == -110){            //精选345
                    txturl = "cfvbaibai/cardfantasy/data/MyCard5.txt";
                    cardFWStream = CardDataStore.class.getClassLoader().getResourceAsStream(txturl);
                    //将文件中的符文读进list里
                    BufferedReader br = new BufferedReader(new InputStreamReader(cardFWStream, "UTF-8"));//构造一个BufferedReader类来读取文件
                    String line = null;
                    while((line = br.readLine())!=null){//使用readLine方法，一次读一行
                        if(line.indexOf("//") == -1 && line.replace('，', ',').indexOf(',') != -1){      //当卡牌不为尚未收录的卡牌,并且为标记卡牌时
                            sortcards.add(new sortCard(line.replace("-",","), 0.0));
                        }
                    }
                    br.close();    

                    txturl = "cfvbaibai/cardfantasy/data/MyCard4.txt";
                    cardFWStream = CardDataStore.class.getClassLoader().getResourceAsStream(txturl);
                    //将文件中的符文读进list里
                    br = new BufferedReader(new InputStreamReader(cardFWStream, "UTF-8"));//构造一个BufferedReader类来读取文件
                    line = null;
                    while((line = br.readLine())!=null){//使用readLine方法，一次读一行
                        if(line.indexOf("//") == -1 && line.replace('，', ',').indexOf(',') != -1){      //当卡牌不为尚未收录的卡牌,并且为标记卡牌时
                            sortcards.add(new sortCard(line.replace("-",","), 0.0));
                        }
                    }
                    br.close();    

                    txturl = "cfvbaibai/cardfantasy/data/MyCard3.txt";
                    cardFWStream = CardDataStore.class.getClassLoader().getResourceAsStream(txturl);
                    //将文件中的符文读进list里
                    br = new BufferedReader(new InputStreamReader(cardFWStream, "UTF-8"));//构造一个BufferedReader类来读取文件
                    line = null;
                    while((line = br.readLine())!=null){//使用readLine方法，一次读一行
                        if(line.indexOf("//") == -1 && line.replace('，', ',').indexOf(',') != -1){      //当卡牌不为尚未收录的卡牌,并且为标记卡牌时
                            sortcards.add(new sortCard(line.replace("-",","), 0.0));
                        }
                    }
                    br.close();    

                }else if(selectlevel == -116){      //45星
                    
                    txturl = "cfvbaibai/cardfantasy/data/MyCard4.txt";
                    cardFWStream = CardDataStore.class.getClassLoader().getResourceAsStream(txturl);
                    //将文件中的符文读进list里
                    BufferedReader br = new BufferedReader(new InputStreamReader(cardFWStream, "UTF-8"));//构造一个BufferedReader类来读取文件
                    String line = null;
                    while((line = br.readLine())!=null){//使用readLine方法，一次读一行
                        if(line.indexOf("//") == -1){      //当卡牌不为尚未收录的卡牌时
                            sortcards.add(new sortCard(line.replace("-",","), 0.0));
                        }
                    }
                    br.close();    

                    txturl = "cfvbaibai/cardfantasy/data/MyCard5.txt";
                    cardFWStream = CardDataStore.class.getClassLoader().getResourceAsStream(txturl);
                    //将文件中的符文读进list里
                    br = new BufferedReader(new InputStreamReader(cardFWStream, "UTF-8"));//构造一个BufferedReader类来读取文件
                    line = null;
                    while((line = br.readLine())!=null){//使用readLine方法，一次读一行
                        if(line.indexOf("//") == -1){      //当卡牌不为尚未收录的卡牌时
                            sortcards.add(new sortCard(line.replace("-",","), 0.0));
                        }
                    }
                    br.close();    

                }else if(selectlevel == -120 || selectlevel == -125){      //符文精选或全符文时
                    
                    txturl = "cfvbaibai/cardfantasy/data/MyCardFWS.txt";
                    cardFWStream = CardDataStore.class.getClassLoader().getResourceAsStream(txturl);
                    //将文件中的符文读进list里
                    BufferedReader br = new BufferedReader(new InputStreamReader(cardFWStream, "UTF-8"));//构造一个BufferedReader类来读取文件
                    String line = null;
                    while((line = br.readLine())!=null){//使用readLine方法，一次读一行
                        if(line.indexOf("//") == -1){      //当卡牌不为尚未收录的卡牌时
                            if(selectlevel != 120 || line.replace('，', ',').indexOf(",") != -1){      //当为全符文或者为精选标记符文时
                                sortcards.add(new sortCard(line.replace("-",","), 0.0));
                                
                            }
                        }
                    }
                    br.close();    

                    txturl = "cfvbaibai/cardfantasy/data/MyCardFWF.txt";
                    cardFWStream = CardDataStore.class.getClassLoader().getResourceAsStream(txturl);
                    //将文件中的符文读进list里
                    br = new BufferedReader(new InputStreamReader(cardFWStream, "UTF-8"));//构造一个BufferedReader类来读取文件
                    line = null;
                    while((line = br.readLine())!=null){//使用readLine方法，一次读一行
                        if(line.indexOf("//") == -1){      //当卡牌不为尚未收录的卡牌时
                            if(selectlevel != 120 || line.replace('，', ',').indexOf(",") != -1){      //当为全符文或者为精选标记符文时
                                sortcards.add(new sortCard(line.replace("-",","), 0.0));
                            }
                        }
                    }

                    txturl = "cfvbaibai/cardfantasy/data/MyCardFWH.txt";
                    cardFWStream = CardDataStore.class.getClassLoader().getResourceAsStream(txturl);
                    //将文件中的符文读进list里
                    br = new BufferedReader(new InputStreamReader(cardFWStream, "UTF-8"));//构造一个BufferedReader类来读取文件
                    line = null;
                    while((line = br.readLine())!=null){//使用readLine方法，一次读一行
                        if(line.indexOf("//") == -1){      //当卡牌不为尚未收录的卡牌时
                            if(selectlevel != 120 || line.replace('，', ',').indexOf(",") != -1){      //当为全符文或者为精选标记符文时
                                sortcards.add(new sortCard(line.replace("-",","), 0.0));
                            }
                        }
                    }

                    txturl = "cfvbaibai/cardfantasy/data/MyCardFWT.txt";
                    cardFWStream = CardDataStore.class.getClassLoader().getResourceAsStream(txturl);
                    //将文件中的符文读进list里
                    br = new BufferedReader(new InputStreamReader(cardFWStream, "UTF-8"));//构造一个BufferedReader类来读取文件
                    line = null;
                    while((line = br.readLine())!=null){//使用readLine方法，一次读一行
                        if(line.indexOf("//") == -1){      //当卡牌不为尚未收录的卡牌时
                            if(selectlevel != 120 || line.replace('，', ',').indexOf(",") != -1){      //当为全符文或者为精选标记符文时
                                sortcards.add(new sortCard(line.replace("-",","), 0.0));
                            }
                        }
                    }
                    br.close();
    
                }else{              //其它单个星级卡牌

                    if(selectlevel == -111 ){             
                        txturl = "cfvbaibai/cardfantasy/data/MyCard1.txt";
                    }else if(selectlevel == -112){
                        txturl = "cfvbaibai/cardfantasy/data/MyCard2.txt";
                    }else if(selectlevel == -113){
                        txturl = "cfvbaibai/cardfantasy/data/MyCard3.txt";
                    }else if(selectlevel == -114){
                        txturl = "cfvbaibai/cardfantasy/data/MyCard4.txt";
                    }else if(selectlevel == -115){
                        txturl = "cfvbaibai/cardfantasy/data/MyCard5.txt";
                    }else if(selectlevel == -121){       
                        txturl = "cfvbaibai/cardfantasy/data/MyCardFWS.txt";
                    }else if(selectlevel == -122){       
                        txturl = "cfvbaibai/cardfantasy/data/MyCardFWF.txt";
                    }else if(selectlevel == -123){       
                        txturl = "cfvbaibai/cardfantasy/data/MyCardFWH.txt";
                    }else if(selectlevel == -124){       
                        txturl = "cfvbaibai/cardfantasy/data/MyCardFWT.txt";
                    }

                    cardFWStream = CardDataStore.class.getClassLoader().getResourceAsStream(txturl);
                    //将文件中的符文读进list里
                    BufferedReader br = new BufferedReader(new InputStreamReader(cardFWStream, "UTF-8"));//构造一个BufferedReader类来读取文件
                    String line = null;
                    //cardname = cardname +"bbb" + br.readLine();
                    while((line = br.readLine())!=null){//使用readLine方法，一次读一行
                        //result.append(System.lineSeparator()+s);
                        //cardname = cardname +"ccc"+ line;
                        if(line.indexOf("//") == -1){      //当卡牌不为尚未收录的卡牌时
                            if(selectlevel != -120 || line.indexOf(',') != -1){     //当选择条件不为精选，或者卡牌本身带精选标记时才放入列表
                                sortcards.add(new sortCard(line.replace("-",","), 0.0));
                            }
                        }
                    }
                    br.close();    
                }
*/
                //用list里的符文分别模拟，将获得的分数写进list里
                for(sortCard sortcard:sortcards){
                    String olddeck = deck;
                    olddeck = olddeck.replace('，', ',');
                    olddeck = olddeck.substring(olddeck.indexOf(',')+1);
                    String thiscardname = sortcard.cardname.replace("，",",").replace(",", "");
                    /*if(thiscardname.indexOf("-") == -1){
                        if(selectlevel <= -120){     //为符文时
                            thiscardname = thiscardname +"-4, ";
                        }else{                      //为卡牌时
                            thiscardname = thiscardname +"-10, ";
                        }
                    }
                    String newdeck = thiscardname + olddeck;*/
                    String newdeck = thiscardname +", "+ olddeck;
                    //进行模拟并取值
                    result = null;
                    ui = new DummyGameUI();
                    result = GameLauncher.playMapGame(newdeck, map, heroLv, count, ui);
                    Double thiscnt = Double.valueOf(result.getAdvWinCount());
                    sortcard.cnt = thiscnt;
                    if(thiscnt > bestcnt){
                        bestcnt = thiscnt;
                        bestdeck = newdeck;
                    }
                    if(result.getValidationResult() != ""){
                        errornote = errornote + sortcard.cardname +":"+ result.getValidationResult() +",";
                    }
                }
                cardsnocnt = bestdeck;

                //list排序
                Collections.sort(sortcards, new Comparator<sortCard>() {
                    @Override
                    public int compare(sortCard o1, sortCard o2) {
                        return o2.getCnt().compareTo(o1.getCnt());
                    }
                });

                //从list中将前20的卡牌和分数取出来
                int i = 0;
                for(sortCard sortcard:sortcards){
                    cardswithcnt = cardswithcnt + sortcard.cardname.replace(",","");
                    cardswithcnt = cardswithcnt +"("+ sortcard.cnt.toString() +"),";

                    i++;
                    if(i>=20){
                        break;
                    }
                    
                }

                //最后显示一遍原始卡组的分数
                result = null;
                ui = new DummyGameUI();
                result = GameLauncher.playMapGame(deck, map, heroLv, count, ui);

            } else {    //作者原本的强度分析
                result = GameLauncher.playMapGame(deck, map, heroLv, count, ui);
            }

            writer.append(Utils.getCurrentDateTime() + "<br />");
            writer.append("<div style='color: red'>" + result.getValidationResult() + "</div>");
            writer.print("<div style='color: red'>" + errornote + "</div>");
            writer.print("<div style='color: red'>" + ttest + "</div>");
            writer.append("<table>");
            writer.append(String.format("<tr><td>战斗出错: </td><td>%d</td></tr>", result.getUnknownCount()));
            writer.append(String.format("<tr><td>失败: </td><td>%d</td></tr>", result.getLostCount()));
            writer.append(String.format("<tr><td>战斗超时: </td><td>%d</td></tr>", result.getTimeoutCount()));
            writer.append(String.format("<tr><td>胜利，过关条件符合: </td><td>%d</td></tr>", result.getAdvWinCount()));
            writer.append(String.format("<tr><td>胜利，过关条件不符合: </td><td>%d</td></tr>", result.getWinCount()));
            writer.append("<tr><td>强度卡组排序: </td><td>"+ cardswithcnt +"</td></tr>");
            writer.append("<tr><td>卡组排序卡组: </td><td>"+ cardsnocnt +"</td></tr>");
            writer.append("</table>");
            writer.append(String.format("<input type=\"hidden\" value=\"basicrate%d\">", result.getWinCount()));
            writer.append(String.format("<input type=\"hidden\" value=\"advrate%d\">", result.getAdvWinCount()));
            logger.info(String.format("TO:LO:BW:AW:UN = %d:%d:%d:%d:%d",
                    result.getTimeoutCount(),
                    result.getLostCount(),
                    result.getWinCount(),
                    result.getAdvWinCount(),
                    result.getUnknownCount()));
        } catch (Exception e) {
            writer.print(errorHelper.handleError(e, false));
            if (Global.isDebugging()) {
                writer.print(((WebPlainTextGameUI)ui).getAllText());
            }
        }
    }

    @RequestMapping(value = "/PlayDungeons1MatchGame")
    public void playDungeons1MatchGame(HttpServletRequest request, HttpServletResponse response,
                                  @RequestParam("fa") int firstAttack, @RequestParam("do") int deckOrder,
                                  @RequestParam("p1hhpb") int p1HeroHpBuff, @RequestParam("p1catb") int p1CardAtBuff, @RequestParam("p1chpb") int p1CardHpBuff,
                                  @RequestParam("p2hhpb") int p2HeroHpBuff, @RequestParam("p2catb") int p2CardAtBuff, @RequestParam("p2chpb") int p2CardHpBuff,
                                  @RequestParam("vc1") String victoryConditionText1,
                                  @RequestParam("deck") String deck, @RequestParam("count") int count,
                                  @RequestParam("hlv") int heroLv, @RequestParam("map") String map) throws IOException {
        PrintWriter writer = response.getWriter();
        try {
            logger.info("PlayMap1MatchGame from " + request.getRemoteAddr() + ":");
            logger.info("Deck = " + deck);
            logger.info("Hero LV = " + heroLv + ", Map = " + map);
            this.userActionRecorder.addAction(new UserAction(new Date(), request.getRemoteAddr(), "Play Map 1Match Game",
                    String.format("Deck=%s<br />HeroLV=%d, Map=%s", deck, heroLv, map)));
            VictoryCondition vc1 = VictoryCondition.parse(victoryConditionText1);
            Rule rule = new Rule(5, 999, firstAttack, deckOrder, false, vc1);
            WebPlainTextGameUI ui = new WebPlainTextGameUI();
            MapGameResult result = GameLauncher.playDungeonsGame(p1HeroHpBuff,p1CardAtBuff,p1CardHpBuff,p2HeroHpBuff,p2CardAtBuff,p2CardHpBuff,deck, map, heroLv, 1,rule, ui);
            writer.print(Utils.getCurrentDateTime() + "<br />");
            writer.print("<div style='color: red'>" + result.getValidationResult() + "</div>");
            writer.print(ui.getAllText());
            logger.info("Result: " + result.getLastResultName());
        } catch (Exception e) {
            writer.print(errorHelper.handleError(e, false));
        }
    }

    @RequestMapping(value = "/SimulateDungeons1MatchGame", headers = "Accept=application/json")
    public void simulateDungeons1MatchGame(HttpServletRequest request, HttpServletResponse response,
                                      @RequestParam("fa") int firstAttack, @RequestParam("do") int deckOrder,
                                      @RequestParam("p1hhpb") int p1HeroHpBuff, @RequestParam("p1catb") int p1CardAtBuff, @RequestParam("p1chpb") int p1CardHpBuff,
                                      @RequestParam("p2hhpb") int p2HeroHpBuff, @RequestParam("p2catb") int p2CardAtBuff, @RequestParam("p2chpb") int p2CardHpBuff,
                                      @RequestParam("vc1") String victoryConditionText1,
                                      @RequestParam("deck") String deck, @RequestParam("count") int count,
                                      @RequestParam("hlv") int heroLv, @RequestParam("map") String map) throws IOException {
        response.setContentType("application/json");
        PrintWriter writer = response.getWriter();
        try {
            logger.info("SimulateMap1MatchGame from " + request.getRemoteAddr() + ":");
            logger.info("Deck = " + deck);
            logger.info("Hero LV = " + heroLv + ", Map = " + map);
            this.userActionRecorder.addAction(new UserAction(new Date(), request.getRemoteAddr(), "Simulate Map 1Match Game",
                    String.format("Deck=%s<br />HeroLV=%d, Map=%s", deck, heroLv, map)));
            VictoryCondition vc1 = VictoryCondition.parse(victoryConditionText1);
            Rule rule = new Rule(5, 999, firstAttack, deckOrder, false, vc1);
            List<Skill> p2CardBuffs = new ArrayList<Skill>();
            if (p2CardAtBuff != 100) {
                p2CardBuffs.add(new PlayerCardBuffSkill(SkillType.原始攻击调整, p2CardAtBuff - 100));
            }
            if (p2CardHpBuff != 100) {
                p2CardBuffs.add(new PlayerCardBuffSkill(SkillType.原始体力调整, p2CardHpBuff - 100));
            }
            PlayerInfo player = PlayerBuilder.build(true, "玩家", deck, heroLv,p2CardBuffs,p2CardHpBuff);
            StructuredRecordGameUI ui = new StructuredRecordGameUI();
            PvdEngine engine = new PvdEngine(ui, this.dungeons);
            PveGameResult gameResult = engine.play(player, map,rule,p1HeroHpBuff,p1CardAtBuff,p1CardHpBuff);
            BattleRecord record = ui.getRecord();
            writer.println(jsonHandler.toJson(record));
            logger.info("Result: " + gameResult.name());
        } catch (Exception e) {
            writer.println(errorHelper.handleError(e, true));
        }
    }

    @RequestMapping(value = "/PlayDungeonsMassiveGame")
    public void playDungeonsMassiveGame(HttpServletRequest request, HttpServletResponse response,
                                   @RequestParam("fa") int firstAttack, @RequestParam("do") int deckOrder,
                                   @RequestParam("p1hhpb") int p1HeroHpBuff, @RequestParam("p1catb") int p1CardAtBuff, @RequestParam("p1chpb") int p1CardHpBuff,
                                   @RequestParam("p2hhpb") int p2HeroHpBuff, @RequestParam("p2catb") int p2CardAtBuff, @RequestParam("p2chpb") int p2CardHpBuff,
                                   @RequestParam("vc1") String victoryConditionText1,
                                   @RequestParam("deck") String deck, @RequestParam("hlv") int heroLv, @RequestParam("map") String map,
                                   @RequestParam("count") int count) throws IOException {
        PrintWriter writer = response.getWriter();
        GameUI ui = new DummyGameUI();
        try {
            logger.info("PlayMapMassiveGame from " + request.getRemoteAddr() + ":");
            logger.info(String.format("Lv = %d, Map = %s, Count = %d", heroLv, map, count));
            logger.info("Deck = " + deck);
            this.userActionRecorder.addAction(new UserAction(new Date(), request.getRemoteAddr(), "Play Map Massive Game",
                    String.format("Deck=%s<br />Lv=%d, Count=%d, Map=%s", deck, heroLv, count, map)));
            VictoryCondition vc1 = VictoryCondition.parse(victoryConditionText1);
            Rule rule = new Rule(5, 999, firstAttack, deckOrder, false, vc1);
            if (Global.isDebugging()) {
                ui = new WebPlainTextGameUI();
            }

            MapGameResult result = null;

            int selectlevel = 0;
            if(count < -10){        //取出跟着count带进来的排序类型和排序次数, 由于不想去改原作者的代码, 所以只好用麻烦的方法来传过来
                selectlevel = count/10;
                count = count - selectlevel*10;
                if(count == -1){
                    count = 10;
                }else if(count == -2){
                    count = 100;
                }else if(count == -3){
                    count = 1000;
                }else if(count == -4){
                    count = 10000;
                }
            }
            //虽然下面这些在官方千场里用不到, 但不想写在里面, 嫌麻烦
            String cardswithcnt = "";
            String cardsnocnt = "";
            List<sortCard> sortcards = new ArrayList<>();
            deck = deck.replace("，", ",");
            deck = deck + ",";    //在结尾加逗号是为了方便循环的时候不用写额外的判断语句，保证每个卡牌后都至少有一个逗号
            deck = deck.replace(" ", "");
            deck = deck.replace(",,", ",");
            deck = deck.replace(",,", ",");
            String subdeck = deck;
            int firstcnt = subdeck.indexOf(',');
            String errornote = "";
            //String ttest = "";

            if (selectlevel == -100 || selectlevel == -101){        //-100:本卡组卡牌胜率排序;  -101:本卡组卡牌升15胜率排序
                while (firstcnt != -1){
                    //取卡牌
                    String thisdeck = subdeck.substring(0, firstcnt+1).replace(" ","");  //第一个取出来的卡牌
                    subdeck = subdeck.substring(firstcnt+1);            //剩下的卡牌组，用来接着循环取剩下的卡牌

                    if(thisdeck.replace(",", "") != ""){
                        String newdeck = "";
                        String thisdeck15 = "";
                        if(selectlevel == -100){        //当为卡组排序时,每次进行去掉本张卡的模拟
                            newdeck = deck.replaceFirst(thisdeck.replace("+", "\\+"), "");  //去掉取出来的卡牌的卡牌组
                            //ttest = ttest + thisdeck +"|"+ newdeck +"@\r\n";
                        }else if(selectlevel == -101){
                            
                            newdeck = deck.replaceFirst(thisdeck.replace("+", "\\+"), "");  //去掉取出来的卡牌的卡牌组
                            if(thisdeck.indexOf('-') == -1){            //当本卡没有加等级的时候,加上等级15
                                thisdeck15 = thisdeck +"-15";
                            }else{                                      //当本卡有加等级的时候,把10级或14级换成15级, 应该不会有别的等级出现
                                thisdeck15 = thisdeck.replace("-10", "-15").replace("-14", "-15");
                            } 
                            newdeck = thisdeck15 +","+ newdeck;
                        }
                        
                        //将卡牌胜率写进列表
                        //result = null;
                        //ui = new DummyGameUI();
                        result = GameLauncher.playDungeonsGame(p1HeroHpBuff,p1CardAtBuff,p1CardHpBuff,p2HeroHpBuff,p2CardAtBuff,p2CardHpBuff,newdeck, map, heroLv, count,rule, ui);
                        sortcards.add(new sortCard(thisdeck,Double.valueOf(result.getAdvWinCount())));
                    }

                    firstcnt = subdeck.indexOf(',');                    //下一个卡牌在哪里结束
                }
                //最后运行一次正常的, 好显示数据及对比
                //result = null;
                //ui = new DummyGameUI();
                result = GameLauncher.playDungeonsGame(p1HeroHpBuff,p1CardAtBuff,p1CardHpBuff,p2HeroHpBuff,p2CardAtBuff,p2CardHpBuff,deck, map, heroLv, count,rule, ui);
                //开始排序列表
                Collections.sort(sortcards, new Comparator<sortCard>() {
                    @Override
                    public int compare(sortCard o1, sortCard o2) {
                        return o2.getCnt().compareTo(o1.getCnt());
                    }
                });
                //从列表中取出来
                int i = 1;
                for(sortCard sortcard:sortcards){
                    cardswithcnt = cardswithcnt + sortcard.cardname.replace(",","");
                    cardswithcnt = cardswithcnt +"("+ sortcard.cnt.toString() +"),";

                    if(selectlevel == -100){            //当为卡组排序的时候, 最后按强度卡牌排序显示卡组
                        cardsnocnt = cardsnocnt + sortcard.cardname +" ";
                    }else if(selectlevel == -101){      //当为15排序的时候, 最后显示最强卡牌15级加其它卡牌来显示卡组
                        if(i ==1){
                            String thisdeck = sortcard.cardname;
                            String newdeck = deck.replaceFirst(thisdeck.replace("+", "\\+"), "");  //去掉取出来的卡牌的卡牌组
                            thisdeck = thisdeck.replace(",","");
                            if(thisdeck.indexOf('-') == -1){            //当本卡没有加等级的时候,加上等级15
                                thisdeck = thisdeck +"-15";
                            }else{                                      //当本卡有加等级的时候,把10级或14级换成15级, 应该不会有别的等级出现
                                thisdeck = thisdeck.replace("-10", "-15").replace("-14", "-15");
                            } 
                            cardsnocnt = thisdeck +","+ newdeck;
                        }
                        i++;
                    }
                }

            } else if(selectlevel <= -110 && selectlevel >= -139){              //在已获得的卡牌中选择出对本战胜率最高的
                
                String bestdeck = "";
                Double bestcnt = 0.0;

                InputStream cardFWStream;
                List<String> txturls=new ArrayList<>();

                //从文件中读取出卡牌列表
                if(selectlevel == -110){            //精选345
                    txturls.add("cfvbaibai/cardfantasy/data/MyCard5S.txt");
                    txturls.add("cfvbaibai/cardfantasy/data/MyCard5F.txt");
                    txturls.add("cfvbaibai/cardfantasy/data/MyCard5H.txt");
                    txturls.add("cfvbaibai/cardfantasy/data/MyCard5T.txt");
                    txturls.add("cfvbaibai/cardfantasy/data/MyCard4S.txt");
                    txturls.add("cfvbaibai/cardfantasy/data/MyCard4F.txt");
                    txturls.add("cfvbaibai/cardfantasy/data/MyCard4H.txt");
                    txturls.add("cfvbaibai/cardfantasy/data/MyCard4T.txt");
                    txturls.add("cfvbaibai/cardfantasy/data/MyCard3.txt");
                    txturls.add("cfvbaibai/cardfantasy/data/MyCard4M.txt");
                    txturls.add("cfvbaibai/cardfantasy/data/MyCard5M.txt");
               
                }else if(selectlevel == -120 || selectlevel == -125){      //符文精选或全符文时

                    txturls.add("cfvbaibai/cardfantasy/data/MyCardFS.txt");
                    txturls.add("cfvbaibai/cardfantasy/data/MyCardFF.txt");
                    txturls.add("cfvbaibai/cardfantasy/data/MyCardFH.txt");
                    txturls.add("cfvbaibai/cardfantasy/data/MyCardFT.txt");
    
                }else if(selectlevel == -111 ){             
                    txturls.add("cfvbaibai/cardfantasy/data/MyCard1.txt");
                }else if(selectlevel == -112){
                    txturls.add("cfvbaibai/cardfantasy/data/MyCard2.txt");
                }else if(selectlevel == -113){
                    txturls.add("cfvbaibai/cardfantasy/data/MyCard3.txt");
                }else if(selectlevel == -114){
                    txturls.add("cfvbaibai/cardfantasy/data/MyCard4S.txt");
                    txturls.add("cfvbaibai/cardfantasy/data/MyCard4F.txt");
                    txturls.add("cfvbaibai/cardfantasy/data/MyCard4H.txt");
                    txturls.add("cfvbaibai/cardfantasy/data/MyCard4T.txt");
                    txturls.add("cfvbaibai/cardfantasy/data/MyCard4M.txt");
                }else if(selectlevel == -115){
                    txturls.add("cfvbaibai/cardfantasy/data/MyCard5S.txt");
                    txturls.add("cfvbaibai/cardfantasy/data/MyCard5F.txt");
                    txturls.add("cfvbaibai/cardfantasy/data/MyCard5H.txt");
                    txturls.add("cfvbaibai/cardfantasy/data/MyCard5T.txt");
                    txturls.add("cfvbaibai/cardfantasy/data/MyCard5M.txt");
                }else if(selectlevel == -116){      //45星
                    txturls.add("cfvbaibai/cardfantasy/data/MyCard5S.txt");
                    txturls.add("cfvbaibai/cardfantasy/data/MyCard5F.txt");
                    txturls.add("cfvbaibai/cardfantasy/data/MyCard5H.txt");
                    txturls.add("cfvbaibai/cardfantasy/data/MyCard5T.txt");
                    txturls.add("cfvbaibai/cardfantasy/data/MyCard4S.txt");
                    txturls.add("cfvbaibai/cardfantasy/data/MyCard4F.txt");
                    txturls.add("cfvbaibai/cardfantasy/data/MyCard4H.txt");
                    txturls.add("cfvbaibai/cardfantasy/data/MyCard4T.txt");
                    txturls.add("cfvbaibai/cardfantasy/data/MyCard4M.txt");
                    txturls.add("cfvbaibai/cardfantasy/data/MyCard5M.txt");
                }else if(selectlevel == -121){       //水
                    txturls.add("cfvbaibai/cardfantasy/data/MyCardFS.txt");
                }else if(selectlevel == -122){       //风
                    txturls.add("cfvbaibai/cardfantasy/data/MyCardFF.txt");
                }else if(selectlevel == -123){       //火
                    txturls.add("cfvbaibai/cardfantasy/data/MyCardFH.txt");
                }else if(selectlevel == -124){       //土
                    txturls.add("cfvbaibai/cardfantasy/data/MyCardFT.txt");
                }else if(selectlevel == -131){       //王国
                    txturls.add("cfvbaibai/cardfantasy/data/MyCard5S.txt");
                    txturls.add("cfvbaibai/cardfantasy/data/MyCard4S.txt");
                }else if(selectlevel == -132){       //森林
                    txturls.add("cfvbaibai/cardfantasy/data/MyCard5F.txt");
                    txturls.add("cfvbaibai/cardfantasy/data/MyCard4F.txt");
                }else if(selectlevel == -133){       //蛮荒
                    txturls.add("cfvbaibai/cardfantasy/data/MyCard5T.txt");
                    txturls.add("cfvbaibai/cardfantasy/data/MyCard4T.txt");
                }else if(selectlevel == -134){       //地狱
                    txturls.add("cfvbaibai/cardfantasy/data/MyCard5H.txt");
                    txturls.add("cfvbaibai/cardfantasy/data/MyCard4H.txt");
                }
                
                //从mycard文件列表中取出卡牌
                for (int i = 0; i < txturls.size(); i++) {
                    
                    String txturl = txturls.get(i); 
                    cardFWStream = CardDataStore.class.getClassLoader().getResourceAsStream(txturl);
                    //将文件中的符文读进list里
                    BufferedReader br = new BufferedReader(new InputStreamReader(cardFWStream, "UTF-8"));//构造一个BufferedReader类来读取文件
                    String line = null;
                    while((line = br.readLine())!=null){//使用readLine方法，一次读一行
                        if(line.indexOf("//") == -1){      //当卡牌不为尚未收录的卡牌时
                            line = line.replace('，', ',');
                            if(selectlevel == -110 || selectlevel == -120){      //当选择类型为精选时
                                if(line.indexOf(',') != -1){      //当为标记卡牌时
                                    sortcards.add(new sortCard(line.replace("-",","), 0.0));
                                }
                            }else{                                              //当选择类型为非精选时
                                sortcards.add(new sortCard(line.replace("-",","), 0.0));
                            }
                        }
                    }
                    br.close();    
                }
/*
            } else if(selectlevel <= -110 && selectlevel >= -129){              //在已获得的卡牌中选择出对本战胜率最高的
                
                String bestdeck = "";
                Double bestcnt = 0.0;

                InputStream cardFWStream;
                String txturl = "";

                //从文件中读取出卡牌列表
                if(selectlevel == -110){            //精选345
                    txturl = "cfvbaibai/cardfantasy/data/MyCard5.txt";
                    cardFWStream = CardDataStore.class.getClassLoader().getResourceAsStream(txturl);
                    //将文件中的符文读进list里
                    BufferedReader br = new BufferedReader(new InputStreamReader(cardFWStream, "UTF-8"));//构造一个BufferedReader类来读取文件
                    String line = null;
                    while((line = br.readLine())!=null){//使用readLine方法，一次读一行
                        if(line.indexOf("//") == -1 && line.replace('，', ',').indexOf(',') != -1){      //当卡牌不为尚未收录的卡牌,并且为标记卡牌时
                            sortcards.add(new sortCard(line.replace("-",","), 0.0));
                        }
                    }
                    br.close();    

                    txturl = "cfvbaibai/cardfantasy/data/MyCard4.txt";
                    cardFWStream = CardDataStore.class.getClassLoader().getResourceAsStream(txturl);
                    //将文件中的符文读进list里
                    br = new BufferedReader(new InputStreamReader(cardFWStream, "UTF-8"));//构造一个BufferedReader类来读取文件
                    line = null;
                    while((line = br.readLine())!=null){//使用readLine方法，一次读一行
                        if(line.indexOf("//") == -1 && line.replace('，', ',').indexOf(',') != -1){      //当卡牌不为尚未收录的卡牌,并且为标记卡牌时
                            sortcards.add(new sortCard(line.replace("-",","), 0.0));
                        }
                    }
                    br.close();    

                    txturl = "cfvbaibai/cardfantasy/data/MyCard3.txt";
                    cardFWStream = CardDataStore.class.getClassLoader().getResourceAsStream(txturl);
                    //将文件中的符文读进list里
                    br = new BufferedReader(new InputStreamReader(cardFWStream, "UTF-8"));//构造一个BufferedReader类来读取文件
                    line = null;
                    while((line = br.readLine())!=null){//使用readLine方法，一次读一行
                        if(line.indexOf("//") == -1 && line.replace('，', ',').indexOf(',') != -1){      //当卡牌不为尚未收录的卡牌,并且为标记卡牌时
                            sortcards.add(new sortCard(line.replace("-",","), 0.0));
                        }
                    }
                    br.close();    

                }else if(selectlevel == -116){      //45星
                    
                    txturl = "cfvbaibai/cardfantasy/data/MyCard4.txt";
                    cardFWStream = CardDataStore.class.getClassLoader().getResourceAsStream(txturl);
                    //将文件中的符文读进list里
                    BufferedReader br = new BufferedReader(new InputStreamReader(cardFWStream, "UTF-8"));//构造一个BufferedReader类来读取文件
                    String line = null;
                    while((line = br.readLine())!=null){//使用readLine方法，一次读一行
                        if(line.indexOf("//") == -1){      //当卡牌不为尚未收录的卡牌时
                            sortcards.add(new sortCard(line.replace("-",","), 0.0));
                        }
                    }
                    br.close();    

                    txturl = "cfvbaibai/cardfantasy/data/MyCard5.txt";
                    cardFWStream = CardDataStore.class.getClassLoader().getResourceAsStream(txturl);
                    //将文件中的符文读进list里
                    br = new BufferedReader(new InputStreamReader(cardFWStream, "UTF-8"));//构造一个BufferedReader类来读取文件
                    line = null;
                    while((line = br.readLine())!=null){//使用readLine方法，一次读一行
                        if(line.indexOf("//") == -1){      //当卡牌不为尚未收录的卡牌时
                            sortcards.add(new sortCard(line.replace("-",","), 0.0));
                        }
                    }
                    br.close();    

                }else if(selectlevel == -120 || selectlevel == -125){      //符文精选或全符文时
                    
                    txturl = "cfvbaibai/cardfantasy/data/MyCardFWS.txt";
                    cardFWStream = CardDataStore.class.getClassLoader().getResourceAsStream(txturl);
                    //将文件中的符文读进list里
                    BufferedReader br = new BufferedReader(new InputStreamReader(cardFWStream, "UTF-8"));//构造一个BufferedReader类来读取文件
                    String line = null;
                    while((line = br.readLine())!=null){//使用readLine方法，一次读一行
                        if(line.indexOf("//") == -1){      //当卡牌不为尚未收录的卡牌时
                            if(selectlevel != 120 || line.replace('，', ',').indexOf(",") != -1){      //当为全符文或者为精选标记符文时
                                sortcards.add(new sortCard(line.replace("-",","), 0.0));
                                
                            }
                        }
                    }
                    br.close();    

                    txturl = "cfvbaibai/cardfantasy/data/MyCardFWF.txt";
                    cardFWStream = CardDataStore.class.getClassLoader().getResourceAsStream(txturl);
                    //将文件中的符文读进list里
                    br = new BufferedReader(new InputStreamReader(cardFWStream, "UTF-8"));//构造一个BufferedReader类来读取文件
                    line = null;
                    while((line = br.readLine())!=null){//使用readLine方法，一次读一行
                        if(line.indexOf("//") == -1){      //当卡牌不为尚未收录的卡牌时
                            if(selectlevel != 120 || line.replace('，', ',').indexOf(",") != -1){      //当为全符文或者为精选标记符文时
                                sortcards.add(new sortCard(line.replace("-",","), 0.0));
                            }
                        }
                    }

                    txturl = "cfvbaibai/cardfantasy/data/MyCardFWH.txt";
                    cardFWStream = CardDataStore.class.getClassLoader().getResourceAsStream(txturl);
                    //将文件中的符文读进list里
                    br = new BufferedReader(new InputStreamReader(cardFWStream, "UTF-8"));//构造一个BufferedReader类来读取文件
                    line = null;
                    while((line = br.readLine())!=null){//使用readLine方法，一次读一行
                        if(line.indexOf("//") == -1){      //当卡牌不为尚未收录的卡牌时
                            if(selectlevel != 120 || line.replace('，', ',').indexOf(",") != -1){      //当为全符文或者为精选标记符文时
                                sortcards.add(new sortCard(line.replace("-",","), 0.0));
                            }
                        }
                    }

                    txturl = "cfvbaibai/cardfantasy/data/MyCardFWT.txt";
                    cardFWStream = CardDataStore.class.getClassLoader().getResourceAsStream(txturl);
                    //将文件中的符文读进list里
                    br = new BufferedReader(new InputStreamReader(cardFWStream, "UTF-8"));//构造一个BufferedReader类来读取文件
                    line = null;
                    while((line = br.readLine())!=null){//使用readLine方法，一次读一行
                        if(line.indexOf("//") == -1){      //当卡牌不为尚未收录的卡牌时
                            if(selectlevel != 120 || line.replace('，', ',').indexOf(",") != -1){      //当为全符文或者为精选标记符文时
                                sortcards.add(new sortCard(line.replace("-",","), 0.0));
                            }
                        }
                    }
                    br.close(); 
    
                }else{              //其它单个星级卡牌

                    if(selectlevel == -111 ){             
                        txturl = "cfvbaibai/cardfantasy/data/MyCard1.txt";
                    }else if(selectlevel == -112){
                        txturl = "cfvbaibai/cardfantasy/data/MyCard2.txt";
                    }else if(selectlevel == -113){
                        txturl = "cfvbaibai/cardfantasy/data/MyCard3.txt";
                    }else if(selectlevel == -114){
                        txturl = "cfvbaibai/cardfantasy/data/MyCard4.txt";
                    }else if(selectlevel == -115){
                        txturl = "cfvbaibai/cardfantasy/data/MyCard5.txt";
                    }else if(selectlevel == -121){       
                        txturl = "cfvbaibai/cardfantasy/data/MyCardFWS.txt";
                    }else if(selectlevel == -122){       
                        txturl = "cfvbaibai/cardfantasy/data/MyCardFWF.txt";
                    }else if(selectlevel == -123){       
                        txturl = "cfvbaibai/cardfantasy/data/MyCardFWH.txt";
                    }else if(selectlevel == -124){       
                        txturl = "cfvbaibai/cardfantasy/data/MyCardFWT.txt";
                    }

                    cardFWStream = CardDataStore.class.getClassLoader().getResourceAsStream(txturl);
                    //将文件中的符文读进list里
                    BufferedReader br = new BufferedReader(new InputStreamReader(cardFWStream, "UTF-8"));//构造一个BufferedReader类来读取文件
                    String line = null;
                    //cardname = cardname +"bbb" + br.readLine();
                    while((line = br.readLine())!=null){//使用readLine方法，一次读一行
                        //result.append(System.lineSeparator()+s);
                        //cardname = cardname +"ccc"+ line;
                        if(line.indexOf("//") == -1){      //当卡牌不为尚未收录的卡牌时
                            if(selectlevel != -120 || line.indexOf(',') != -1){     //当选择条件不为精选，或者卡牌本身带精选标记时才放入列表
                                sortcards.add(new sortCard(line.replace("-",","), 0.0));
                            }
                        }
                    }
                    br.close();    
                }
*/
                //用list里的符文分别模拟，将获得的分数写进list里
                for(sortCard sortcard:sortcards){
                    String olddeck = deck;
                    olddeck = olddeck.replace('，', ',');
                    olddeck = olddeck.substring(olddeck.indexOf(',')+1);
                    String thiscardname = sortcard.cardname.replace("，",",").replace(",", "");
                    /*if(thiscardname.indexOf("-") == -1){
                        if(selectlevel <= -120){     //为符文时
                            thiscardname = thiscardname +"-4, ";
                        }else{                      //为卡牌时
                            thiscardname = thiscardname +"-10, ";
                        }
                    }*/
                    String newdeck = thiscardname +", "+ olddeck;
                    //进行模拟并取值
                    //result = null;
                    //ui = new DummyGameUI();
                    result = GameLauncher.playDungeonsGame(p1HeroHpBuff,p1CardAtBuff,p1CardHpBuff,p2HeroHpBuff,p2CardAtBuff,p2CardHpBuff,newdeck, map, heroLv, count,rule, ui);
                    Double thiscnt = Double.valueOf(result.getAdvWinCount());
                    sortcard.cnt = thiscnt;
                    if(thiscnt > bestcnt){
                        bestcnt = thiscnt;
                        bestdeck = newdeck;
                    }
                    if(result.getValidationResult() != ""){
                        errornote = errornote + sortcard.cardname +":"+ result.getValidationResult() +",";
                    }
                }
                cardsnocnt = bestdeck;

                //list排序
                Collections.sort(sortcards, new Comparator<sortCard>() {
                    @Override
                    public int compare(sortCard o1, sortCard o2) {
                        return o2.getCnt().compareTo(o1.getCnt());
                    }
                });

                //从list中将前20的卡牌和分数取出来
                int i = 0;
                for(sortCard sortcard:sortcards){
                    cardswithcnt = cardswithcnt + sortcard.cardname.replace(",","");
                    cardswithcnt = cardswithcnt +"("+ sortcard.cnt.toString() +"),";

                    i++;
                    if(i>=20){
                        break;
                    }
                    
                }

                //最后显示一遍原始卡组的分数
                //result = null;
                //ui = new DummyGameUI();
                result = GameLauncher.playDungeonsGame(p1HeroHpBuff,p1CardAtBuff,p1CardHpBuff,p2HeroHpBuff,p2CardAtBuff,p2CardHpBuff,deck, map, heroLv, count,rule, ui);
    
            }else{
                result = GameLauncher.playDungeonsGame(p1HeroHpBuff,p1CardAtBuff,p1CardHpBuff,p2HeroHpBuff,p2CardAtBuff,p2CardHpBuff,deck, map, heroLv, count,rule, ui);
            }
            
            writer.append(Utils.getCurrentDateTime() + "<br />");
            writer.append("<div style='color: red'>" + result.getValidationResult() + "</div>");
            writer.print("<div style='color: red'>" + errornote + "</div>");
            writer.append("<table>");
            writer.append(String.format("<tr><td>战斗出错: </td><td>%d</td></tr>", result.getUnknownCount()));
            writer.append(String.format("<tr><td>失败: </td><td>%d</td></tr>", result.getLostCount()));
            writer.append(String.format("<tr><td>战斗超时: </td><td>%d</td></tr>", result.getTimeoutCount()));
            writer.append(String.format("<tr><td>胜利，过关条件符合: </td><td>%d</td></tr>", result.getAdvWinCount()));
            writer.append(String.format("<tr><td>胜利，过关条件不符合: </td><td>%d</td></tr>", result.getWinCount()));
            writer.append("<tr><td>强度卡组排序: </td><td>"+ cardswithcnt +"</td></tr>");
            writer.append("<tr><td>卡组排序卡组: </td><td>"+ cardsnocnt +"</td></tr>");
            writer.append("</table>");
            writer.append(String.format("<input type=\"hidden\" value=\"basicrate%d\">", result.getWinCount()));
            writer.append(String.format("<input type=\"hidden\" value=\"advrate%d\">", result.getAdvWinCount()));
            logger.info(String.format("TO:LO:BW:AW:UN = %d:%d:%d:%d:%d",
                    result.getTimeoutCount(),
                    result.getLostCount(),
                    result.getWinCount(),
                    result.getAdvWinCount(),
                    result.getUnknownCount()));
        } catch (Exception e) {
            writer.print(errorHelper.handleError(e, false));
            if (Global.isDebugging()) {
                writer.print(((WebPlainTextGameUI)ui).getAllText());
            }
        }
    }
    
    @RequestMapping(value = "/GetCardDetail", headers = "Accept=application/json")
    public void getCardDetail(HttpServletRequest request, HttpServletResponse response,
            @RequestParam("playerId") String playerId, @RequestParam("uniqueName") String uniqueName,
            @RequestParam("type") String type) throws IOException {
    }
    
    @Autowired
    private CardDataStore store;
    
    @RequestMapping(value = "/GetDataStore", headers = "Accept=application/json")
    public void getDataStore(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        PrintWriter writer = response.getWriter();
        response.setContentType("application/json");
        try {
            logger.info("Getting card data store...");
            Map <String, Object> result = new HashMap <String, Object>();
            List<EntityDataRuntimeInfo> entities = new ArrayList<EntityDataRuntimeInfo>();
            List<CardData> cards = store.getAllCards();
            for (CardData card : cards) {
                if (!card.isMaterial()) {
                    entities.add(new EntityDataRuntimeInfo(card));
                }
            }
            RuneData[] runes = RuneData.values();
            for (RuneData rune : runes) {
                entities.add(new EntityDataRuntimeInfo(rune));
            }
            result.put("entities", entities);

            List<SkillTypeRuntimeInfo> skillList = new ArrayList<SkillTypeRuntimeInfo>(); 
            for (SkillType skillType : SkillType.values()) {
                if (!skillType.containsTag(SkillTag.不可洗炼)) {
                    skillList.add(new SkillTypeRuntimeInfo(skillType));
                }
            }

            Collections.sort(skillList, new Comparator<SkillTypeRuntimeInfo>() {
                private Comparator<Object> comparer = Collator.getInstance(Locale.CHINA);
                @Override
                public int compare(SkillTypeRuntimeInfo arg0, SkillTypeRuntimeInfo arg1) {
                    return comparer.compare(arg0.getName(), arg1.getName());
                }
            });

            result.put("skills", skillList);
            writer.print(jsonHandler.toJson(result));
        } catch (Exception e) {
            writer.print(errorHelper.handleError(e, true));
        }
    }
    
    @RequestMapping(value = "/GetMapVictoryCondition", headers = "Accept=application/json")
    public void getMapVictoryCondition(HttpServletRequest request, HttpServletResponse response,
            @RequestParam("map") String map) throws IOException {
        PrintWriter writer = response.getWriter();
        response.setContentType("application/json");
        try {
            logger.info("Getting map victory condition: " + map);
            String condition = "";
            MapInfo mapInfo = this.maps.getMap(map);
            if (mapInfo == null) {
                condition = "无效的地图：" + map;
            } else {
                condition = mapInfo.getCondition().getDescription();
            }
            writer.print(jsonHandler.toJson(condition));
        } catch (Exception e) {
            writer.print(errorHelper.handleError(e, true));
        }
    }

    @RequestMapping(value = "/GetDungeonsVictoryCondition", headers = "Accept=application/json")
    public void getDungeonsVictoryCondition(HttpServletRequest request, HttpServletResponse response,
                                       @RequestParam("map") String map) throws IOException {
        PrintWriter writer = response.getWriter();
        response.setContentType("application/json");
        try {
            logger.info("Getting map victory condition: " + map);
            String condition = "";
            MapInfo mapInfo = this.dungeons.getDungeons(map);
            if (mapInfo == null) {
                condition = "无效的地图：" + map;
            } else {
                condition = mapInfo.getCondition().getDescription();
            }
            writer.print(jsonHandler.toJson(condition));
        } catch (Exception e) {
            writer.print(errorHelper.handleError(e, true));
        }
    }

    @RequestMapping(value = "/CardSkills/{cardName}")
    public void getCardSkills(HttpServletRequest request, HttpServletResponse response,
        @PathVariable("cardName") String cardName) throws IOException {
        PrintWriter writer = response.getWriter();
        try {
            logger.info("Getting card skills: " + cardName);
            CardData cardData = this.store.getCard(cardName);
            if (cardData == null) {
                writer.println("无效的卡牌: " + cardName);
                response.setStatus(404);
                return;
            }
            for (Skill skill : cardData.getSkills()) {
                if (skill.getType() != SkillType.无效) {
                    writer.print(skill.getShortDesc());
                }
            }
        } catch (Exception e) {
            writer.print(errorHelper.handleError(e, true));
        }
    }
    
    @RequestMapping(value = "/GetMapDeckInfo", headers = "Accept=application/json")
    public void getMapDeckInfo(HttpServletRequest request, HttpServletResponse response,
        @RequestParam("map") String map) throws IOException {
        PrintWriter writer = response.getWriter();
        response.setContentType("application/json");
        try {
            logger.info("Getting map stage: " + map);
            String deckInfo = "";
            MapInfo mapInfo = this.maps.getMap(map);
            if (mapInfo == null) {
                deckInfo = "无效的地图：" + map;
            } else {
                deckInfo = mapInfo.getDeckInfo();
            }
            writer.print(jsonHandler.toJson(deckInfo));
        } catch (Exception e) {
            writer.print(errorHelper.handleError(e, true));
        }
    }

    //地下城模块数据
    @RequestMapping(value = "/GetDungeonsDeckInfo", headers = "Accept=application/json")
    public void getDungeonsDeckInfo(HttpServletRequest request, HttpServletResponse response,
                               @RequestParam("map") String map) throws IOException {
        PrintWriter writer = response.getWriter();
        response.setContentType("application/json");
        try {
            logger.info("Getting map stage: " + map);
            String deckInfo = "";
            MapInfo mapInfo = this.dungeons.getDungeons(map);
            if (mapInfo == null) {
                deckInfo = "无效的地图：" + map;
            } else {
                deckInfo = mapInfo.getDeckInfo();
            }
            writer.print(jsonHandler.toJson(deckInfo));
        } catch (Exception e) {
            writer.print(errorHelper.handleError(e, true));
        }
    }

    @RequestMapping(value = "/Video/{mode}", method = RequestMethod.POST, headers = "Accept=application/json")
    public void convertVideo(HttpServletRequest request, HttpServletResponse response,
        @PathVariable String mode, @RequestParam("videoData") String videoData) throws IOException {
        response.setContentType("plain/text");
        PrintWriter writer = response.getWriter();
        logger.info("Converting video: " + mode);
        if (videoData == null || videoData.length() == 0) {
            throw new CardFantasyRuntimeException("无效的录像数据");
        }
        if (mode.equalsIgnoreCase("compact")) {
            String compacted = Base64Encoder.encode(Compressor.compress(videoData));
            logger.info("Compacted: " + compacted);
            writer.print(compacted);
        } else if (mode.equalsIgnoreCase("decompact")) {
            String decompacted = Compressor.decompress(Base64Encoder.decode(videoData));
            logger.info("Decompacted: " + decompacted);
            writer.print(decompacted);
        }
    }
}
