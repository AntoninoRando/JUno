import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;

import controller.Controller;
import controller.HumanController;
import model.MainLoop;
import model.Player;
import model.cards.Card;
import model.cards.Deck;
import model.cards.Suit;
import model.effects.Effect;
import model.effects.EffectBuilder;

public class TestGame {
    public static void main(String[] args) {
        EffectBuilder eb = new EffectBuilder(1);
        eb.addDraw(2);
        Effect draw2 = eb.build();

        EffectBuilder eb2 = new EffectBuilder(2);
        eb2.directTargetToFollowing(1).addBlockTurn();
        Effect blockNext = eb2.build();

        // Creating the deck and shuffling it
        List<Card> smallCardSet = new ArrayList<Card>();
        for (int i = 1; i <= 10; i++) {
            for (Suit color: Suit.values()) {
                Card c = new Card(color, i);
                if (i == 7)
                    c.addEffect(draw2);
                smallCardSet.add(c);
            }
        }
        Card blocker = new Card(Suit.WILD, 0);
        blocker.addEffect(blockNext);
        smallCardSet.add(blocker);
        Deck smallDeck = new Deck(smallCardSet);

        // New players with their controller
        Player p1 = new Player("Antonino", true);
        Player p2 = new Player("Bot Giovanni", false);
        Player p3 = new Player("Bot Luca", false);

        Controller c1 = new HumanController();
        c1.setSource(p1);

        TreeMap<Integer, Player> players = new TreeMap<>();
        players.put(0, p1);
        players.put(1, p2);
        players.put(2, p3);

        MainLoop.getInstance().play(players, smallDeck, c1);
    }
}
