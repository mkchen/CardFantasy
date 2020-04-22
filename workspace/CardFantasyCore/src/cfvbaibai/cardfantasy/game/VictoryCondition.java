package cfvbaibai.cardfantasy.game;

import java.util.List;

import cfvbaibai.cardfantasy.CardFantasyRuntimeException;
import cfvbaibai.cardfantasy.data.Race;
import cfvbaibai.cardfantasy.data.RuneClass;
import cfvbaibai.cardfantasy.engine.CardInfo;
import cfvbaibai.cardfantasy.engine.GameEndCause;
import cfvbaibai.cardfantasy.engine.GameResult;
import cfvbaibai.cardfantasy.engine.Player;
import cfvbaibai.cardfantasy.engine.RuneInfo;

public abstract class VictoryCondition {
    public abstract boolean meetCriteria(GameResult result);
    
    public static VictoryCondition parse(String desc) {
        if (desc == null) {
            throw new CardFantasyRuntimeException("desc should not be null");
        }
        if (desc.length() == 0) {
            return null;
        }
        if (desc.equalsIgnoreCase("Any")) {
            return new DummyVictoryCondition();
        } else if (desc.equalsIgnoreCase("EnemyAllCardsDie")) {
            return new CardsAllDieVictoryCondition();
        } else if (desc.startsWith("MyHeroHP:")) {
            int hpThreshold = Integer.parseInt(desc.substring(9));
            return new HeroHPVictoryCondition(hpThreshold);
        } else if (desc.startsWith("Round:")) {
            int maxRound = Integer.parseInt(desc.substring(6));
            return new RoundVictoryCondition(maxRound);
        } else if (desc.startsWith("MyAllCardRound:")) {
            int maxRound = Integer.parseInt(desc.substring(15));
            return new AllCardRoundVictoryCondition(maxRound);
        } else if (desc.equals("EnemyHeroDie")) {
            return new EnemyHeroDieVictoryCondition();
        } else if (desc.startsWith("MyDeadCard:")) {
            int maxDeadCard = Integer.parseInt(desc.substring(11));
            return new MaxDeadCardVictoryCondition(maxDeadCard);
        } else if (desc.startsWith("MyAllCardHP:")) {
            int hpThreshold = Integer.parseInt(desc.substring(12));
            return new AllCardHPVictoryCondition(hpThreshold);
        } else if (desc.startsWith("CardOfStar:")) {
            String rest = desc.substring(11);
            String[] parts = rest.split(":");
            int star = Integer.parseInt(parts[0]);
            int minCount = Integer.parseInt(parts[1]);
            return new CardOfStarVictoryCondition(star, minCount);
        } else if (desc.startsWith("CardOfRace:")) {
            String rest = desc.substring(11);
            String[] parts = rest.split(":");
            Race race = Race.BOSS;
            if (parts[0].equals("K")) {
                race = Race.KINGDOM;
            } else if (parts[0].equals("F")) {
                race = Race.FOREST;
            } else if (parts[0].equals("S")) {
                race = Race.SAVAGE;
            } else if (parts[0].equals("H")) {
                race = Race.HELL;
            } else {
                throw new CardFantasyRuntimeException("胜利条件中发现非法的种族: " + parts[0]);
            }
            int minCount = Integer.parseInt(parts[1]);
            return new CardOfRaceVictoryCondition(race, minCount);
        } else if (desc.startsWith("NoRune:")) {
            String rest = desc.substring(7);
            return new NoRuneVictoryCondition(toRuneClass(rest));
        } else if (desc.startsWith("HasRune:")) {
            String rest = desc.substring(8);
            return new HasRuneVictoryCondition(toRuneClass(rest));
        } else {
            throw new CardFantasyRuntimeException("无效的胜利条件: " + desc);
        }
    }
    
    private static RuneClass toRuneClass(String shorthand) {
        if (shorthand == null) {
            return null;
        }
        RuneClass runeClass = null;
        if (shorthand.equals("A")) {
            runeClass = null;
        } else if (shorthand.equals("G")) {
            runeClass = RuneClass.GROUND;
        } else if (shorthand.equals("F")) {
            runeClass = RuneClass.FIRE;
        } else if (shorthand.equals("I")) {
            runeClass = RuneClass.WATER;
        } else if (shorthand.equals("W")) {
            runeClass = RuneClass.WIND;
        }
        return runeClass;
    }

    public abstract String getDescription();
}

class HeroHPVictoryCondition extends VictoryCondition {
    private int threshold;
    
    public HeroHPVictoryCondition(int threshold) {
        this.threshold = threshold;
    }
    
    @Override
    public boolean meetCriteria(GameResult result) {
        Player winner = result.getWinner();
        return winner.getHP() * 100 / winner.getMaxHP() >= threshold;
    }

    @Override
    public String getDescription() {
        return "胜利时，己方英雄生命值不低于" + threshold + "%";
    }
}
class AllCardHPVictoryCondition extends VictoryCondition {
    //private int maxDeadCard;
    private int threshold;
    
    public AllCardHPVictoryCondition(int threshold) {
        this.threshold = threshold;
        //this.maxDeadCard = maxDeadCard;
    }
    
    @Override
    public boolean meetCriteria(GameResult result) {
        Player winner = result.getWinner();
        return winner.getHP() * 100 / winner.getMaxHP() >= threshold && winner.getGrave().size() < 1;

    }

    @Override
    public String getDescription() {
        return "胜利时，己方英雄生命值不低于"+ threshold +"%，并且己方无卡牌阵亡";
    }
}
class AllCardRoundVictoryCondition extends VictoryCondition {
    //private int maxDeadCard;
    private int maxRound;
    
    public AllCardRoundVictoryCondition(int maxRound) {
        this.maxRound = maxRound;
    }
    
    @Override
    public boolean meetCriteria(GameResult result) {
        Player winner = result.getWinner();
        return winner.getHP() * 100 / winner.getMaxHP() >= 80 && winner.getGrave().size() < 1 && result.getRound() < this.maxRound;

    }

    @Override
    public String getDescription() {
        return maxRound + "回合数内取得胜利，并且胜利时，己方英雄生命值不低于80%，己方无卡牌阵亡";
    }
}



class RoundVictoryCondition extends VictoryCondition {
    private int maxRound;
    
    public RoundVictoryCondition(int maxRound) {
        this.maxRound = maxRound;
    }
    
    @Override
    public boolean meetCriteria(GameResult result) {
        return result.getRound() < this.maxRound;
    }

    @Override
    public String getDescription() {
        return maxRound + "回合数内取得胜利";
    }
}

class EnemyHeroDieVictoryCondition extends VictoryCondition {
    @Override
    public boolean meetCriteria(GameResult result) {
        return result.getCause() == GameEndCause.英雄死亡;
    }

    @Override
    public String getDescription() {
        return "对方英雄阵亡";
    }
}

class MaxDeadCardVictoryCondition extends VictoryCondition {
    private int maxDeadCard;
    public MaxDeadCardVictoryCondition(int maxDeadCard) {
        this.maxDeadCard = maxDeadCard;
    }
    public boolean meetCriteria(GameResult result) {
        return result.getWinner().getGrave().size() < this.maxDeadCard;
    }
    @Override
    public String getDescription() {
        return "己方阵亡卡牌小于" + maxDeadCard + "张";
    }
}

class CardOfStarVictoryCondition extends VictoryCondition {
    private int star;
    private int minCount;
    public CardOfStarVictoryCondition(int star, int minCount) {
        this.star = star;
        this.minCount = minCount;
    }
    public boolean meetCriteria(GameResult result) {
        int count = 0;
        for (CardInfo card : result.getWinner().getAllPrimaryCards()) {
            if (card.getStar() == this.star) {
                ++count;
            }
        }
        return count >= minCount;
    }
    @Override
    public String getDescription() {
        return "卡组中" + star + "星卡牌不小于" + minCount + "张";
    }
}

class CardOfRaceVictoryCondition extends VictoryCondition {
    private Race race;
    private int minCount;
    public CardOfRaceVictoryCondition(Race race, int minCount) {
        this.race = race;
        this.minCount = minCount;
    }
    public boolean meetCriteria(GameResult result) {
        int count = 0;
        List<CardInfo> primaryCards = result.getWinner().getAllPrimaryCards();
        for (CardInfo card : primaryCards) {
            if (card.getOriginalRace() == this.race) {
                ++count;
            }
        }
        return count >= minCount;
    }
    @Override
    public String getDescription() {
        return "卡组中" + race.getDisplayName() + "种族卡牌不小于" + minCount + "张";
    }
}

class NoRuneVictoryCondition extends VictoryCondition {
    private RuneClass runeClass;
    public NoRuneVictoryCondition(RuneClass runeClass) {
        this.runeClass = runeClass;
    }
    public boolean meetCriteria(GameResult result) {
        List<RuneInfo> runes = result.getWinner().getRuneBox().getRunes();
        if (runeClass == null) {
            return runes.size() == 0;
        }
        for (RuneInfo rune : result.getWinner().getRuneBox().getRunes()) {
            if (rune.getRuneClass() == runeClass) {
                return false;
            }
        }
        return true;
    }
    @Override
    public String getDescription() {
        if (runeClass == null) {
            return "卡组中无符文";
        } else {
            return "卡组中不包含" + runeClass.getDisplayName() + "属性符文";
        }
    }
}

class HasRuneVictoryCondition extends VictoryCondition {
    private RuneClass runeClass;
    public HasRuneVictoryCondition(RuneClass runeClass) {
        if (runeClass == null) {
            throw new IllegalArgumentException("胜利条件中缺少符文属性");
        }
        this.runeClass = runeClass;
    }
    public boolean meetCriteria(GameResult result) {
        for (RuneInfo rune : result.getWinner().getRuneBox().getRunes()) {
            if (rune.getRuneClass() == runeClass) {
                return true;
            }
        }
        return false;
    }
    @Override
    public String getDescription() {
        return "卡组中包含" + runeClass.getDisplayName() + "属性符文";
    }
}