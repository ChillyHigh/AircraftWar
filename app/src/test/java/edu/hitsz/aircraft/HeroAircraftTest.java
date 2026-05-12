package edu.hitsz.aircraft;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class HeroAircraftTest {
    private HeroAircraft hero;

    @BeforeEach
    void setUp() {
        hero = HeroAircraft.getHeroAircraft();
    }

    @Test
    void getHeroAircraftShouldReturnSameInstance() {
        assertSame(hero, HeroAircraft.getHeroAircraft());
    }

    @Test
    void increaseHpShouldNotExceedMaxHp() {
        int initialHp = hero.getHp();
        hero.decreaseHp(500);
        hero.increaseHp(1000);
        assertEquals(initialHp, hero.getHp());
    }

    @Test
    void decreaseHpShouldReduceHp() {
        int hp = hero.getHp();
        hero.decreaseHp(300);
        assertEquals(hp - 300, hero.getHp());
    }

    @Test
    void decreaseHpToZeroShouldMakeHeroInvalid() {
        hero.decreaseHp(10000);
        assertTrue(hero.notValid());
    }
}