<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
    <div id="dungeons-battle" class="main-page" data-role="page" data-title="地下城" data-mini="true" data-theme="${theme}">
        <div data-role="content">
            <div data-role="collapsible" data-collapsed="false" data-mini="true">
                <h3>设置阵容</h3>
                <div>
                    <a id="show-dungeons-battle-options-button" data-role="button" data-rel="dialog" data-mini="true">点击设置战斗规则</a>
                    <table class="form">
                        <tr>
                            <td>地下城关卡</td>
                            <td>
                                <select name="dungeons-id" id="dungeons-id" class="dungeons-select" data-mini="true" data-native-menu="true">
                                    <optgroup label="地下城">
                                        <option value="d-1">十二星座关90</option>
                                        <option value="d-2">免疫神风关90</option>
                                        <option value="d-3">传送圣枪关90</option>
                                        <option value="d-4">传送叹惋关90</option>
                                        <option value="d-5">转生龙灵关90</option>
                                        <option value="d-6">免疫森主关90</option>
                                        <option value="d-7">涅盘魅魔关</option>
                                        <option value="d-8">洪荒青龙关</option>
                                        <option value="d-9">不动朱雀关</option>
                                        <option value="d-10">三国英魂关</option>
                                        <option value="d-11">遗回银喵关</option>
                                        <option value="d-12">镜面仲颖关</option>
                                        <option value="d-13">制衡炼金关</option>
                                        <option value="d-14">狼顾魂乐关</option>
                                        <option value="d-15">传送兽王关</option>
                                        <option value="d-16">鬼才镜姬关</option>
                                        <option value="d-17">原素家族关111</option>
                                        <option value="d-18">晴空风樱关111</option>
                                        <option value="d-19">绯炎厨娘关111</option>
                                        <option value="d-20">斩羽机车关111</option>
                                        <option value="d-21">钻石炼金关111</option>
                                        <option value="d-22">公嗣君主关111</option>
                                        <option value="d-23">伯符君主111</option>
                                        <option value="d-24">子桓君主关111</option>
                                        <option value="d-25">单四神关111</option>
                                        <option value="d-26">双四神关111</option>
                                        <option value="d-27">月光妖兽关121</option>
                                        <option value="d-28">幻影凤凰关121</option>
                                        <option value="d-29">满地孙猴关121(s)</option>
                                        <option value="d-31"> 烈火南华关121</option>
                                        <option value="d-32">秘术投影关121</option>
                                        <option value="d-33">泳池派对关121</option>
                                        <option value="d-34">魔法季关121</option>
                                        <option value="d-35">坚韧九霄关121</option>
                                        <option value="d-36">日本妖怪关121</option>
                                        <option value="d-30">中国妖怪关121(s)</option>
                                        <option value="d-30n">中国妖怪关121(n)</option>
                                        <option value="d-29n"> 满地孙猴关121（n）</option>
                                        <option value="d-110">地下城110</option>
                                        <option value="d-110m">地下城110全灭</option>
                                        <option value="d-120">地下城120</option>
                                        <option value="d-11020">地下城110和120层</option>
                                        <option value="d-127">地下城127</option>
                                    </optgroup>
                                    <optgroup label="噩梦1图">
                                        <option value="1-1">1-1 森林入口</option>
                                        <option value="1-2">1-2 森林小径</option>
                                        <option value="1-3">1-3 守林人小屋</option>
                                        <option value="1-4">1-4 小镜湖</option>
                                        <option value="1-5">1-5 密林深处</option>
                                        <option value="1-6">1-6 废弃兽穴</option>
                                    </optgroup>
                                    <optgroup label="噩梦2图">
                                        <option value="2-1">2-1 泰坦山道</option>
                                        <option value="2-2">2-2 荒蛮古道</option>
                                        <option value="2-3">2-3 部落遗迹</option>
                                        <option value="2-4">2-4 余晖渡口</option>
                                        <option value="2-5">2-5 黄昏镇</option>
                                        <option value="2-6">2-6 银月港</option>
                                    </optgroup>
                                    <optgroup label="噩梦3图">
                                        <option value="3-1">3-1 风暴岛</option>
                                        <option value="3-2">3-2 南港</option>
                                        <option value="3-3">3-3 星辰学院</option>
                                        <option value="3-4">3-4 竞技场</option>
                                        <option value="3-5">3-5 星象塔</option>
                                        <option value="3-6">3-6 龙牙山</option>
                                        <option value="3-7">3-7 神秘山洞</option>
                                        <option value="3-8">3-8 地下图书馆</option>
                                    </optgroup>
                                    <optgroup label="噩梦4图">
                                        <option value="4-1">4-1 微风湾</option>
                                        <option value="4-2">4-2 巨木村</option>
                                        <option value="4-3">4-3 坠星湖</option>
                                        <option value="4-4">4-4 蓝鹰瀑布</option>
                                        <option value="4-5">4-5 月神祭坛</option>
                                        <option value="4-6">4-6 月影之井</option>
                                        <option value="4-7">4-7 耳语渡口</option>
                                        <option value="4-8">4-8 迷雾之谷</option>
                                    </optgroup>
                                    <optgroup label="噩梦5图">
                                        <option value="5-1">5-1 冒险者营地</option>
                                        <option value="5-2">5-2 冒险者岗哨</option>
                                        <option value="5-2n">5-2 冒险者岗哨(n服)</option>
                                        <option value="5-3">5-3 黑石矿坑</option>
                                        <option value="5-4">5-4 灰烬之谷</option>
                                        <option value="5-5">5-5 黑炭洞窟</option>
                                        <option value="5-6">5-6 遗忘神庙</option>
                                        <option value="5-6n">5-6 遗忘神庙(n服)</option>
                                        <option value="5-7">5-7 废弃墓园</option>
                                        <option value="5-7n">5-7 废弃墓园(n服)</option>
                                        <option value="5-8">5-8 灼热小径</option>
                                        <option value="5-9">5-H 焦炭遗迹</option>
                                    </optgroup>
                                    <optgroup label="星辰远征">
                                        <option value="Y-1">远征第一层</option>
                                        <option value="Y-2">远征第二层</option>
                                        <option value="Y-3">远征第三层</option>
                                        <option value="Y-4">远征第四层</option>
                                        <option value="Y-5">远征第五层</option>
                                        <option value="Y-6">远征第六层</option>
                                        <option value="Y-7">远征第七层</option>
                                        <option value="Y-8">远征第八层</option>
                                        <option value="Y-9">远征第九层</option>
                                        <option value="Y-10">远征第十层</option>
                                        <option value="Y-11">远征第十一层</option>
                                        <option value="Y-12">远征第十二层</option>
                                        <option value="Y-13">远征第十三层</option>
                                        <option value="Y-14">远征第十四层</option>
                                        <option value="Y-15">远征第十五层</option>
                                    </optgroup>
                                </select>
                            </td>
                        </tr>
                        <tr>
                            <td>难度</td>
                            <td>
                                <select name="dungeons-difficulty" id="dungeons-difficulty" class="map-select"  data-mini="true" data-native-menu="true">
                                    <option value="1" selected="selected">简单</option>
                                    <option value="2">普通</option>
                                    <option value="3">困难</option>
                                </select>
                            </td>
                        </tr>
                        <tr>
                            <td>层数/噩梦加成</td>
                            <td>
                                <select name="layer-select" id="layer-select" class="layer-select"  data-mini="true" data-native-menu="true">
                                    <option value="0" selected="selected">不选择层数/地图</option>
                                    <option value="-1">星辰远征</option>
                                    <option value="1">噩梦地图1图</option>
                                    <option value="2">噩梦地图2图</option>
                                    <option value="3">噩梦地图3图</option>
                                    <option value="4">噩梦地图4图</option>
                                    <option value="5">噩梦地图5图</option>
                                    <option value="90" >地下城90层</option>
                                    <option value="91" >地下城91层</option>
                                    <option value="92" >地下城92层</option>
                                    <option value="93" >地下城93层</option>
                                    <option value="94" >地下城94层</option>
                                    <option value="95" >地下城95层</option>
                                    <option value="96" >地下城96层</option>
                                    <option value="97" >地下城97层</option>
                                    <option value="98" >地下城98层</option>
                                    <option value="99" >地下城99层</option>
                                    <option value="100" >地下城100层</option>
                                    <option value="101">地下城101层</option>
                                    <option value="102">地下城102层</option>
                                    <option value="103">地下城103层</option>
                                    <option value="104">地下城104层</option>
                                    <option value="105">地下城105层</option>
                                    <option value="106">地下城106层</option>
                                    <option value="107">地下城107层</option>
                                    <option value="108">地下城108层</option>
                                    <option value="109">地下城109层</option>
                                    <option value="110">地下城110层</option>
                                    <option value="111">地下城111层</option>
                                    <option value="112">地下城112层</option>
                                    <option value="113">地下城113层</option>
                                    <option value="114">地下城114层</option>
                                    <option value="115">地下城115层</option>
                                    <option value="116">地下城116层</option>
                                    <option value="117">地下城117层</option>
                                    <option value="118">地下城118层</option>
                                    <option value="119">地下城119层</option>
                                    <option value="120">地下城120层</option>
                                    <option value="121">地下城121层</option>
                                    <option value="122">地下城122层</option>
                                    <option value="123">地下城123层</option>
                                    <option value="124">地下城124层</option>
                                    <option value="125">地下城125层</option>
                                    <option value="126">地下城126层</option>
                                    <option value="127">地下城127层</option>
                                    <option value="128">地下城128层</option>
                                    <option value="129">地下城129层</option>
                                    <option value="130">地下城130层</option>
                                </select>
                            </td>
                        </tr>
                        <tr>
                            <td>过关条件</td>
                            <td>
                                <span id="dungeons-victory-condition">未知</span>
                                <a id="view-dungeons-deck-link" data-rel="dialog" data-mini="true">查看关卡阵容</a>
                            </td>
                        </tr>
                    </table>
                    <div id="player" class="player ui-grid-c">
                        <div class="ui-block-a ui-block-label-number">
                            <span>玩家等级: </span>
                        </div>
                        <div class="ui-block-b">
                            <input type="number" id="dungeons-hero-lv" name="dungeons-hero-lv" data-mini="true" value="75" />
                        </div>
                        <div class="ui-block-c ui-block-label-number">
                            <span>玩家卡组: </span>
                        </div>
                        <div class="ui-block-d">
                            <a id="build-dungeons-deck-button" data-role="button" data-rel="dialog" data-mini="true">组卡</a>
                        </div>
                    </div>
                    <div>
                        <textarea id="dungeons-deck" name="dungeons-deck" rows="5" cols="40" data-mini="true">熊人武士+蛮荒之力3-15,熊人武士+不动-12,蜘蛛人女王+不动-15,蜘蛛人女王+暴击5-12,水源制造者+森林之力4-15,水源制造者+森林守护4-14,元素灵龙+不动-15,小矮人狙击者+森林守护3-15,雷兽+格挡8-11,暴怒霸龙+吸血6-15,石林-3,扬旗-3,雷盾-3,赤谷-3</textarea>
                    </div>
                </div>
            </div>
            <div data-mini="true" data-role="controlgroup" data-type="horizontal" data-disabled="false">
                <a id="play-dungeons-1-game-button" class="battle-button" data-role="button" data-mini="true">文字战斗</a>
                <a id="simulate-dungeons-1-game-button" class="battle-button" data-role="button" data-mini="true">动画战斗</a>
                <a id="play-dungeons-massive-game-button" class="battle-button" data-role="button" data-mini="true">连续千场</a>
                <a id="sort-dungeons-massive-game-button" class="battle-button" data-role="button" data-mini="true">卡组排序</a>
                <select id="cnt-dungeons-game-level" data-mini="true" data-native-menu="true">
                    <option value="10">10次</option>
                    <option value="100" selected="selected">100次</option>
                    <option value="1000">1000次</option>
                <option value="10000">10000次</option>
                <option value="-100">TOP百次</option>
                <option value="-1000">TOP千次</option>
                <option value="-10000">TOP万次</option>
                </select>
                <a id="select-dungeons-massive-game-button" class="battle-button" data-role="button" data-mini="true">强度选卡</a>
                <select id="select-dungeons-game-level" data-mini="true" data-native-menu="true">
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
            <div id="dungeons-battle-div" data-mini="true" data-role="collapsible" data-collapsed="false">
                <h3>战斗记录</h3>
                <div id="dungeons-battle-output" class="battle-output">没有战斗</div>
            </div>
        </div>
    </div>
    <div data-role="page" data-title="查看关卡阵容" data-mini="true" id="view-dungeons-deck-page" class="fixed-width">
        <div data-role="header" data-position="fixed">
            <h3 style="text-align: center">查看关卡阵容</h3>
        </div>
        <div data-role="content">
            <div id="dungeons-deck-info" style="height: 200px; padding: 10px"></div>
            <div style="width: 100%">
                <a data-role="button" data-mini="true" href="javascript:history.go(-1)">返回</a>
            </div>
        </div>
    </div>