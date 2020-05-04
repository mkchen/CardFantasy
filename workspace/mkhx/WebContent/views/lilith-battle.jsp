<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<div id="lilith-battle" class="main-page" data-role="page" data-title="莉莉丝战" data-mini="true" data-theme="${theme}">
    <div data-role="content">
        <div data-role="collapsible" data-collapsed="false" data-mini="true">
            <h3>设置阵容</h3>
            <div>
                <fieldset class="select-6-4" data-role="controlgroup" data-type="horizontal">
                    <select id="lilith-name" data-mini="true" data-native-menu="true">
                    <c:forEach items="${lilithDatas}" var="data">
                        <option value="<c:out value="${data.bossId}" />"> <c:out value="${data.bossId}" /></option>
                    </c:forEach>
                    </select>
                    <select id="lilith-game-type" data-mini="true" data-native-menu="true">
                        <option value="0">清怪模式</option>
                        <option value="1">尾刀模式</option>
                    </select>
                </fieldset>
                <div class="ui-grid-a">
                    <div class="ui-block-a ui-block-label-number">活动卡牌</div>
                    <div class="ui-block-b">
                        <input type="text" id="lilith-event-cards" data-mini="true" value="梦魇猎手·胧,魂灵猎鹰,魂灵疾风" />
                    </div>
                </div>
                <div id="lilith-config-0" class="ui-grid-a">
                    <div class="ui-block-a ui-block-label-number">清怪至剩余怪数(包括莉莉丝)：</div>
                    <div class="ui-block-b">
                        <input type="number" id="lilith-target-remaining-guard-count" data-mini="true" value="1" />
                    </div>
                </div>
                <div id="lilith-config-1" class="ui-grid-a">
                    <div class="ui-block-a ui-block-label-number">莉莉丝剩余HP：</div>
                    <div class="ui-block-b">
                        <input type="number" id="lilith-remaining-hp" data-mini="true" value="300000" />
                    </div>
                </div>
                <div style="display: none">
                    <input type="checkbox" id="enable-custom-lilith-guards" data-mini="true" />
                    <label for="enable-custom-lilith-guards">自定义小兵</label>
                </div>
                <div id="custom-lilith-guards" style="display: none">
                    <table class="avg-group-4">
                        <tr>
                            <td>攻击BUFF%</td>
                            <td><input type="number" id="custom-lilith-guards-atbuff" data-mini="true" value="100" /></td>
                            <td>体力BUFF%</td>
                            <td><input type="number" id="custom-lilith-guards-hpbuff" data-mini="true" value="100" /></td>
                        </tr>
                    </table>
                    <textarea id="custom-lilith-guards-deck" rows="5" cols="40" data-mini="true">岩壁-4,死域-4,石林-4,飞岩-4,毁灭之龙-10,巫妖领主-10,黑甲铁骑士+战意5-15,震源岩蟾-10,熊人武士+不动-15,牛头人酋长-10,战斗猛犸象-10,金属巨龙-10,战神+不动-15</textarea>
                </div>
                <div id="lilith-player" class="player ui-grid-c">
                    <div class="ui-block-a ui-block-label-number">
                        <span>玩家等级: </span>
                    </div>
                    <div class="ui-block-b">
                        <input type="number" id="lilith-player-heroLv" name="lilith-player-heroLv" data-mini="true" value="75" />
                    </div>
                    <div class="ui-block-c ui-block-label-number">
                        <span>玩家卡组: </span>
                    </div>
                    <div class="ui-block-d">
                        <a id="build-lilith-deck-button" data-role="button" data-rel="dialog" data-mini="true">组卡</a>
                    </div>
                </div>
                <div>
                    <textarea id="lilith-player-deck" name="lilith-player-deck" rows="5" cols="40" data-mini="true">熊人武士+蛮荒之力3-15,熊人武士+不动-12,蜘蛛人女王+不动-15,蜘蛛人女王+暴击5-12,水源制造者+森林之力4-15,水源制造者+森林守护4-14,元素灵龙+不动-15,小矮人狙击者+森林守护3-15,雷兽+格挡8-11,暴怒霸龙+吸血6-15,石林-3,扬旗-3,雷盾-3,赤谷-3</textarea>
                </div>
            </div>
        </div>
        <div id="lilith-command" data-mini="true" data-role="controlgroup" data-type="horizontal" data-disabled="false">
            <a id="play-lilith-1-game-button" class="battle-button" data-role="button" data-mini="true">文字战斗</a>
            <a id="simulate-lilith-1-game-button" class="battle-button" data-role="button" data-mini="true">动画战斗</a>
            <a id="play-lilith-massive-game-button" class="battle-button" data-role="button" data-mini="true">强度分析</a>
            <a id="sort-lilith-massive-game-button" class="battle-button" data-role="button" data-mini="true">卡组排序</a>
            <select id="cnt-lilith-game-level" data-mini="true" data-native-menu="true">
                <option value="10">10次</option>
                <option value="100" selected="selected">100次</option>
                <option value="1000">1000次</option>
                <option value="10000">10000次</option>
                <option value="-100">TOP百次</option>
                <option value="-1000">TOP千次</option>
                <option value="-10000">TOP万次</option>
            </select>
            <a id="select-lilith-massive-game-button" class="battle-button" data-role="button" data-mini="true">强度选卡</a>
            <select id="select-lilith-game-level" data-mini="true" data-native-menu="true">
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
                <option value="-141">全契约</option>
                
                <option value="-150">装备</option>
                    <option value="-151">武器</option>
                <option value="-152">防具</option>
                <option value="-153">饰品</option>
            </select>
        <a data-role="button" data-mini="true" data-type="bug" href="#">提BUG</a>
            
            
        </div>
        <div id="lilith-battle-div" data-mini="true" data-role="collapsible" data-collapsed="false">
            <h3>战斗记录</h3>
            <div id="lilith-battle-output" class="battle-output">没有战斗</div>
        </div>
    </div>
</div>