import java.util.List;
import java.util.Map;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;

/**
 * This class manage the game. A game is an istance of GameManager.
 */
public class GameManager {
    // VARIABLES
    private Deck drawingDeck;
    private CardGroup discardPile = new CardGroup();
    private Card terrainCard;
    private int turns;

    // We use LinkedList to define the order in which players play.
    private List<Controller> controllers = new LinkedList<Controller>();

    // CONSTRUCTORS
    public GameManager(Deck drawingDeck, List<Controller> controllers) {
        this.drawingDeck = drawingDeck;
        this.controllers = controllers;
    }

    public GameManager(Deck drawingDeck, Controller... controllers) {
        this.drawingDeck = drawingDeck;
        this.controllers = Arrays.asList(controllers);
    }

    // METHODS
    public void setup() {
        drawingDeck.shuffle();
        terrainCard = this.drawingDeck.remove(0);
        turns = 1;

        for (Controller controller : controllers) {
            controller.setGame(this);
            controller.drawFromDeck(5);
        }
    }

    public void playTurns() {
        for (Controller controller : controllers)
            controller.makePlay();
        turns++;
    }

    public void playGame() {
        setup();
        while (!controllers.isEmpty()) {
            playTurns();
        }
    }

    public boolean reShuffle() {
        boolean hasChanghed = drawingDeck.addAll(discardPile.shuffle().getCards());
        discardPile.clear();
        return hasChanghed;
    }

    public Card drawFromDeck() {
        if (drawingDeck.isEmpty())
            reShuffle();
        return drawingDeck.remove(0);
    }

    public void putCard(Card card) {
        discardPile.add(terrainCard);
        terrainCard = card;
    }

    public boolean playCard(Card card) {
        if (!card.isPlayable(terrainCard))
            return false;

        putCard(card);
        return true;
    }

    // !Questo metodo fa schifo
    public boolean checkWin(Controller controller) {
        Player bringer = controller.getBringer();

        if (bringer.getHand().getSize() != 0)
            return false;

        System.out.println("Well done " + bringer.getNickname() + ", you won!");
        // !Anche se da questo punto in poi la partita finisce, questa istruzione darà
        // errore (penso perché la lista che gli passo è immutabile)
        controllers.clear(); // Game will end because controllers is empty
        return true;

    }

    // GETTERS AND SETTERS
    public Deck getDrawingDeck() {
        return drawingDeck;
    }

    // !Potrei fare che ti da la prima carta della discard pile, e che quindi non
    // eiste la variabile terrainCard
    public Card getTerrainCard() {
        return terrainCard;
    }

    public int getTurns() {
        return turns;
    }

    public List<Controller> getControllers() {
        return controllers;
    }

    // MAIN
    public static void main(String[] args) {
        // Creating the deck and shuffling it
        List<Card> smallCardSet = new ArrayList<Card>();
        for (int i = 1; i <= 9; i++) {
            for (Suit suit : Suit.values()) {
                if (suit == Suit.WILD) 
                    continue;
                smallCardSet.add(new Card(suit, i));
            }
            // Special card
            Card draw2 = new Card(Suit.WILD, 0);
            Map<String, String> draw2e = new HashMap<String,String>() ;
            draw2e.put("play", "draw(2, next)");
            draw2.addEffect(new Effect(draw2e));
            smallCardSet.add(draw2);
        }

        Deck smallDeck = new Deck(smallCardSet);

        // New players with their controller
        Player p1 = new Player("Antonino");
        HumanController controllerP1 = new HumanController(p1);

        Player p2 = new Player("Bot Giovanni");
        AIController bot1 = new AIController(p2);

        Player p3 = new Player("Bot Luca");
        AIController bot2 = new AIController(p3);

        GameManager g1 = new GameManager(smallDeck, controllerP1, bot1, bot2);

        g1.playGame();
    }
}