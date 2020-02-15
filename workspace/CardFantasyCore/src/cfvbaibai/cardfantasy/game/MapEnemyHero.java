package cfvbaibai.cardfantasy.game;

import java.util.Collection;

import cfvbaibai.cardfantasy.data.*;

public final class MapEnemyHero extends PlayerInfo {

    private int maxHP;
    public MapEnemyHero(String id, int maxHP, Collection<Rune> runes,  Collection <Indenture> indentures, Collection <Equipment> equipments, Collection<Card> cards) {
        super(false, id, 999, null, 100, runes,indentures,equipments, cards);
        this.maxHP = maxHP;
    }
    
    @Override
    public int getMaxHP() {
        return this.maxHP;
    }
}
