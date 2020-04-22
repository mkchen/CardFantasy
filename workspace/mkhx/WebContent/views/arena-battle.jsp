<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
    <div id="arena-battle" class="main-page" data-role="page" data-title="竞技场战" data-mini="true" data-theme="${theme}">
        <div data-role="content">
            <div data-role="collapsible" data-mini="true" data-collapsed="false">
                <h3>设置双方阵容</h3>
                <div>
                    <a id="show-arena-battle-options-button" data-role="button" data-rel="dialog" data-mini="true">点击设置战斗规则</a>
                    <div id="arena-battle-options-text"></div>
                    <div id="player2" class="player ui-grid-c">
                        <div class="ui-block-a ui-block-label-number">
                            <span>玩家2等级: </span>
                        </div>
                        <div class="ui-block-b">
                            <input type="number" id="hero2Lv" data-mini="true" value="75" />
                        </div>
                        <div class="ui-block-c ui-block-label-number">
                            <span>玩家2卡组: </span>
                        </div>
                        <div class="ui-block-d">
                            <a id="build-deck2-button" data-role="button" data-rel="dialog" data-mini="true">组卡</a>
                        </div>
                    </div>
                    <div>
                        <textarea id="deck2" rows="5" cols="40" data-mini="true">熊人武士+蛮荒之力3-15,熊人武士+不动-12,蜘蛛人女王+不动-15,蜘蛛人女王+暴击5-12,水源制造者+森林之力4-15,水源制造者+森林守护4-14,元素灵龙+不动-15,小矮人狙击者+森林守护3-15,雷兽+格挡8-11,暴怒霸龙+吸血6-15,石林-3,扬旗-3,雷盾-3,赤谷-3</textarea>
                    </div>
                    <div id="player1" class="player ui-grid-c">
                        <div class="ui-block-a ui-block-label-number">
                            <span>玩家1等级: </span>
                        </div>
                        <div class="ui-block-b">
                            <input type="number" id="hero1Lv" data-mini="true" value="75" />
                        </div>
                        <div class="ui-block-c ui-block-label-number">
                            <span>玩家1卡组: </span>
                        </div>
                        <div class="ui-block-d">
                            <a id="build-deck1-button" data-role="button" data-rel="dialog" data-mini="true">组卡</a>
                        </div>
                    </div>
                    <div>
                        <textarea id="deck1" rows="5" cols="40" data-mini="true">降临天使-10,羽蛇神-10,恶魔猎人-10,圣剑持有者-10,幻想炼金士-10,福音乐师-10,铁血剑豪-10,元素灵龙-10,震源岩蟾-10,冰封,清泉,赤谷,霜冻</textarea>
                    </div>
                </div>
            </div>
            <div id="command" data-mini="true" data-role="controlgroup" data-type="horizontal">
                <a id="play-auto-1-game-button" class="battle-button" data-role="button" data-mini="true">文字战斗</a>
                <a id="simulate-auto-1-game-button" class="battle-button" data-role="button" data-mini="true">动画战斗</a>
                <a id="play-auto-massive-game-button" class="battle-button" data-role="button" data-mini="true">连续千场</a>
                <a id="sort-auto-massive-game-button" class="battle-button" data-role="button" data-mini="true">卡组排序</a>
                <a id="sort15-auto-massive-game-button" class="battle-button" data-role="button" data-mini="true">15级排序</a>
                <select id="cnt-auto-game-level" data-mini="true" data-native-menu="true">
                    <option value="1">10次</option>
                    <option value="2" selected="selected">100次</option>
                    <option value="3">1000次</option>
                    <option value="4">10000次</option>
                    <option value="5">TOP百次</option>
                    <option value="6">TOP千次</option>
                    <option value="7">TOP万次</option>
                </select>
                <a id="select-auto-massive-game-button" class="battle-button" data-role="button" data-mini="true">强度选卡</a>
                <select id="select-auto-game-level" data-mini="true" data-native-menu="true">
                    <option value="-110" selected="selected">精选卡牌</option>
                    <option value="-111">1星</option>
                    <option value="-112">2星</option>
                    <option value="-113">3星</option>
                    <option value="-114">4星</option>
                    <option value="-115">5星</option>
                    <option value="-116">45星</option>
                    <option value="-1160">45星错</option>
                    <option value="-131">王国</option>
                    <option value="-132">森林</option>
                    <option value="-133">蛮荒</option>
                    <option value="-134">地狱</option>
                    <option value="-120">精选符文</option>
                    <option value="-121">水符文</option>
                    <option value="-122">风符文</option>
                    <option value="-123">火符文</option>
                    <option value="-124">土符文</option>
                    <option value="-125">全符文</option>
                    <option value="-140">精选契约</option>
                    <option value="-141">契约</option>
                    
                    <option value="-151">武器</option>
                <option value="-152">防具</option>
                <option value="-153">饰品</option>
                    <option value="-161">星辰远征</option>
            </select>
                <a data-role="button" data-mini="true" data-type="bug" href="#">提BUG</a>
            </div>
            <div id="arena-battle-div" data-mini="true" data-role="collapsible" data-collapsed="false">
                <h3>战斗记录</h3>
                <div id="battle-output" class="battle-output">没有战斗</div>
            </div>
        </div>
    </div>