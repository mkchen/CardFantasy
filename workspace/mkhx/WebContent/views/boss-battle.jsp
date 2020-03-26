<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
    <div id="boss-battle" class="main-page" data-role="page" data-title="魔神战" data-mini="true" data-theme="${theme}">
        <div id="boss-battle-content" data-role="content">
            <div data-role="collapsible" data-collapsed="false" data-mini="true">
                <h3>设置阵容</h3>
                <div>
                    <fieldset class="select-2" data-role="controlgroup" data-type="horizontal">
                        <select name="boss-name" class="boss-name" id="boss-name" data-mini="true" data-native-menu="true">
                            <option value="强魔刃复仇女神">强魔刃复仇女神</option>
                            <option value="狙击复仇女神">狙击复仇女神</option>
                            <option value="送还复仇女神">送还复仇女神</option>
                            <option value="连锁复仇女神">连锁复仇女神</option>
                            <option value="网页版复仇女神">网页版复仇女神</option>
                            <option value="强魔咒邪龙之神">强魔咒邪龙之神</option>
                            <option value="迷魂邪龙之神">迷魂邪龙之神</option>
                            <option value="盾刺邪龙之神">盾刺邪龙之神</option>
                            <option value="网页版邪龙之神">网页版邪龙之神</option>
                            <option value="强魔甲噩梦之主">强魔甲噩梦之主</option>
                            <option value="振奋噩梦之主">振奋噩梦之主</option>
                            <option value="格挡噩梦之主">格挡噩梦之主</option>
                            <option value="网页版噩梦之主">网页版噩梦之主</option>
                            <option value="强魔咒毁灭之神">强魔咒毁灭之神</option>
                            <option value="邪吸毁灭之神">邪吸毁灭之神</option>
                            <option value="焚神毁灭之神">焚神毁灭之神</option>
                            <option value="网页版毁灭之神">网页版毁灭之神</option>
                            <option value="强魔甲深渊影魔">强魔甲深渊影魔</option>
                            <option value="弱点深渊影魔">弱点深渊影魔</option>
                            <option value="诅咒深渊影魔">诅咒深渊影魔</option>
                            <option value="网页版深渊影魔">网页版深渊影魔</option>
                            <option value="强魔刃万蛛之后">强魔刃万蛛之后</option>
                            <option value="回魂万蛛之后">回魂万蛛之后</option>
                            <option value="无回魂万蛛之后">无回魂万蛛之后</option>
                            <option value="网页版万蛛之后">网页版万蛛之后</option>
                        </select>
                        <select name="guard-type" id="guard-type" data-mini="true" data-native-menu="true">
                            <option value="0">没有杂兵</option>
                            <option value="1">普通杂兵</option>
                            <option value="2">强力杂兵</option>
                            <option value="3" selected="selected">五星杂兵</option>
                        </select>
                    </fieldset>
                    <table class="form">
                        <tr><td>技能: </td><td><div id="boss-skills" style="font-size: smaller"></div></td></tr>
                    </table>
                    <!-- +：10级， -->
                    <fieldset class="select-4" data-role="controlgroup" data-type="horizontal">
                        <select name="buff-kingdom" id="buff-kingdom" data-mini="true" data-native-menu="true">
                            <option value="0">王+0</option>
                            <option value="1">王+1</option>
                            <option value="2">王+2</option>
                            <option value="3">王+3</option>
                            <option value="4">王+4</option>
                            <option value="5">王+5</option>
                            <option value="6">王+6</option>
                            <option value="7">王+7</option>
                            <option value="8">王+8</option>
                            <option value="9">王+9</option>
                            <option value="10" selected="selected">王+10</option>
                        </select>
                        <select name="buff-savage" id="buff-savage" data-mini="true" data-native-menu="true">
                            <option value="0">蛮+0</option>
                            <option value="1">蛮+1</option>
                            <option value="2">蛮+2</option>
                            <option value="3">蛮+3</option>
                            <option value="4">蛮+4</option>
                            <option value="5">蛮+5</option>
                            <option value="6">蛮+6</option>
                            <option value="7">蛮+7</option>
                            <option value="8">蛮+8</option>
                            <option value="9">蛮+9</option>
                            <option value="10" selected="selected">蛮+10</option>
                        </select>
                        <select name="buff-forest" id="buff-forest" data-mini="true" data-native-menu="true">
                            <option value="0">森+0</option>
                            <option value="1">森+1</option>
                            <option value="2">森+2</option>
                            <option value="3">森+3</option>
                            <option value="4">森+4</option>
                            <option value="5">森+5</option>
                            <option value="6">森+6</option>
                            <option value="7">森+7</option>
                            <option value="8">森+8</option>
                            <option value="9">森+9</option>
                            <option value="10" selected="selected">森+10</option>
                        </select>
                        <select name="buff-hell" id="buff-hell" data-mini="true" data-native-menu="true">
                            <option value="0">地+0</option>
                            <option value="1">地+1</option>
                            <option value="2">地+2</option>
                            <option value="3">地+3</option>
                            <option value="4">地+4</option>
                            <option value="5">地+5</option>
                            <option value="6">地+6</option>
                            <option value="7">地+7</option>
                            <option value="8">地+8</option>
                            <option value="9">地+9</option>
                            <option value="10" selected="selected">地+10</option>
                        </select>
                    </fieldset>
                    <!-- 
                    <div id="boss-guards" class="player ui-grid-c">
                        <div class="ui-block-a ui-block-label-number">
                            <span>BOSS杂兵: </span>
                        </div>
                        <div class="ui-block-b"></div>
                        <div class="ui-block-c">
                            <a id="random-boss-guards-button" data-role="button" data-mini="true">随机</a>
                        </div>
                        <div class="ui-block-d">
                            <a id="build-boss-deck-button" data-role="button" data-rel="dialog" data-mini="true">设定</a>
                        </div>
                    </div>
                    <div>
                        <textarea id="boss-guards" name="boss-guards" rows="5" cols="40" data-mini="true"></textarea>
                    </div>
                    -->
                    <div id="player" class="player ui-grid-c">
                        <div class="ui-block-a ui-block-label-number">
                            <span>玩家等级: </span>
                        </div>
                        <div class="ui-block-b">
                            <input type="number" id="heroLv" name="heroLv" data-mini="true" value="75" />
                        </div>
                        <div class="ui-block-c ui-block-label-number">
                            <span>玩家卡组: </span>
                        </div>
                        <div class="ui-block-d">
                            <a id="build-boss-deck-button" data-role="button" data-rel="dialog" data-mini="true">组卡</a>
                        </div>
                    </div>
                    <div>
                        <textarea id="deck" name="deck" rows="5" cols="40" data-mini="true">熊人武士+蛮荒之力3-15,熊人武士+不动-12,蜘蛛人女王+不动-15,蜘蛛人女王+暴击5-12,水源制造者+森林之力4-15,水源制造者+森林守护4-14,元素灵龙+不动-15,小矮人狙击者+森林守护3-15,雷兽+格挡8-11,暴怒霸龙+吸血6-15,石林-3,扬旗-3,雷盾-3,赤谷-3</textarea>
                    </div>
                </div>
            </div>
            <div id="boss-command" data-mini="true" data-role="controlgroup" data-type="horizontal" data-disabled="false">
                <a id="play-boss-1-game-button" class="battle-button" data-role="button" data-mini="true">文字战斗</a>
                <a id="simulate-boss-1-game-button" class="battle-button" data-role="button" data-mini="true">动画战斗</a>
                <a id="play-boss-massive-game-button" class="battle-button" data-role="button" data-mini="true">强度分析</a>
                <a id="sort-boss-massive-game-button" class="battle-button" data-role="button" data-mini="true">卡组排序</a>
                <a id="sort15-boss-massive-game-button" class="battle-button" data-role="button" data-mini="true">15级排序</a>
                <select id="cnt-boss-game-level" data-mini="true" data-native-menu="true">
                    <option value="1">10次</option>
                    <option value="2">100次</option>
                    <option value="3" selected="selected">1000次</option>
                    <option value="4">10000次</option>
                    <!--option value="5">TOP百次</option>
                    <option value="6">TOP千次</option>
                    <option value="7">TOP万次</option-->
                </select>
                <a id="select-boss-massive-game-button" class="battle-button" data-role="button" data-mini="true">强度选卡</a>
                <select id="select-boss-game-level" data-mini="true" data-native-menu="true">
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
                    <option value="-150">精选装备</option>
                    <option value="-151">装备</option>
                </select>
                <a data-role="button" data-mini="true" data-type="bug" href="#">提BUG</a>
            </div>
            <div id="boss-battle-div" data-mini="true" data-role="collapsible" data-collapsed="false">
                <h3>战斗记录</h3>
                <div id="boss-battle-output" class="battle-output">没有战斗</div>
                <div id="boss-battle-massive-output" style="display: none">
                    <div id="boss-battle-massive-stat">
                        <table style="font-size: smaller" class="boss-battle-massive">
                            <tr>
                                <td>战斗次数</td><td id="boss-battle-game-count"></td>
                                <td>冷却时间</td><td id="boss-battle-cooldown"></td>
                                <td>总COST</td><td id="boss-battle-deck-cost"></td>
                            </tr>
                            <tr>
                                <td>不稳定度</td><td id="boss-battle-cv-damage"></td>
                                <td>平均伤害</td><td id="boss-battle-avg-damage" style="color: red"></td>
                                <td>最小伤害</td><td id="boss-battle-min-damage"></td>
                            </tr>
                            <tr>
                                <td>超时次数</td><td id="boss-battle-timeout-count"></td>
                                <td>60秒伤害</td><td id="boss-battle-avg-damage-per-minute" style="color: red"></td>
                                <td>最大伤害</td><td id="boss-battle-max-damage"></td>
                            </tr>
                            <tr>
                                <td colspan="6">
                                    <div id="boss-battle-deck-validation-result"></div>
                                </td>
                            </tr>
                            <tr><td>强度卡组排序: </td><td id="boss-battle-cardswithcnt" colspan="5"></td></tr>
                            <tr><td>卡组排序卡组: </td><td id="boss-battle-cardsnocnt" colspan="5"></td></tr>
                        </table>
                    </div>
                    <div id="boss-battle-chart-wrapper">
                        <canvas id="boss-battle-chart" width="300" height="150"></canvas>
                    </div>
                </div>
            </div>
        </div>
    </div>