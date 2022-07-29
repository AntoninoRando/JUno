package model;

import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;

import controller.Controller;
import controller.ControllerFX;
import model.cards.Card;
import model.cards.Deck;
import model.cards.Suit;
import model.effects.Effect;
import model.effects.EffectBuilder;

public class JUno extends Thread {
    /* SINGLETON */
    /* --------- */
    private static JUno instance;

    public static JUno getInstance() {
        if (instance == null)
            instance = new JUno();
        return instance;
    }

    private JUno() {

    }

    /* ---------------------------------------- */

    @Override
    public void run() {
        setName("JUNO");
        test();
    }

    public void test() {
        /* STANDARD DECK */
        /* ------------- */
        List<Card> standardSet = new ArrayList<Card>(108);
        Effect blockTurn = new EffectBuilder().directTargetToFollowing(1).skipTargetTurn().build();
        Effect draw2 = new EffectBuilder().directTargetToFollowing(1).targetDraws(2).skipTargetTurn().build();
        Effect reverseTurn = new EffectBuilder().reverseTurnOrder().build();
        Effect pickColor = new EffectBuilder().selectOneCardOf(new Card(Suit.RED, -5), new Card(Suit.BLUE, -5),
                new Card(Suit.YELLOW, -5), new Card(Suit.GREEN, -5)).transformIntoTarget().build();
        Effect draw4 = new EffectBuilder().directTargetToFollowing(1).targetDraws(4).skipTargetTurn()
                .selectOneCardOf(new Card(Suit.RED, -4), new Card(Suit.BLUE, -4),
                        new Card(Suit.YELLOW, -4), new Card(Suit.GREEN, -4))
                .transformIntoTarget().build();

        for (Suit color : Suit.values()) {
            if (color == Suit.WILD)
                continue;

            for (int i = 1; i < 10; i++) {
                standardSet.add(new Card(color, i));
                standardSet.add(new Card(color, i));
            }

            standardSet.add(new Card(color, 0));
            standardSet.add(new Card(color, 0));
            standardSet.add(new Card(color, -1, blockTurn));
            standardSet.add(new Card(color, -1, blockTurn));
            standardSet.add(new Card(color, -2, draw2));
            standardSet.add(new Card(color, -2, draw2));
            standardSet.add(new Card(color, -3, reverseTurn));
            standardSet.add(new Card(color, -3, reverseTurn));
            standardSet.add(new Card(Suit.WILD, -5, pickColor));
            standardSet.add(new Card(Suit.WILD, -4, draw4));
        }

        Deck standardDeck = new Deck(standardSet);

        // New players with their controller
        Player p1 = new Player("Antonino", true);
        Player p2 = new Player("Top Princessess", false);
        Player p3 = new Player("Bot Luca", false);

        Controller c1 = new ControllerFX();
        c1.setSource(p1);

        TreeMap<Integer, Player> players = new TreeMap<>();
        players.put(0, p1);
        players.put(1, p2);
        players.put(2, p3);

        try {
            Loop match = Loop.getInstance();
            match.setupGame(players, standardDeck, c1);
            match.play();
        } catch (InterruptedException e) {
        }
    }
}
