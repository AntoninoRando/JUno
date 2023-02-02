package model.gameLogic;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Stream;

/* --- JUno ------------------------------- */

import events.Event;

import model.CUModel;
import model.cards.Card;
import model.cards.CardBuilder;
import model.cards.Suit;
import model.players.Enemy;
import model.players.GameAI;
import model.players.Player;
import model.players.User;
import model.players.UserData;

/**
 * Gathers all game info and contains the logic to execute a match.
 * <p>
 * It is not a singleton, since it is easier to think of a new match like a new
 * Game object, instead of the same game object that resets.
 */
public class Game {
    /* --- Fields ----------------------------- */

    private Player[] players;
    private Card terrainCard;
    private final int firstHandSize = 2;
    private List<Card> deck, discardPile;
    private int turn; // current turn
    private Predicate<Card> playCondition;
    private Predicate<Player> winCondition;
    private long timeStart;
    private boolean interrupted;
    private GameState state; // Context

    /* --- Constructors ----------------------- */

    /**
     * Creates a new match with initial settings, but do not starts it.
     */
    public Game() {
        discardPile = new LinkedList<>();
        deck = new LinkedList<>(CardBuilder.getCards("resources/cards/Small.json"));
        playCondition = card -> {
            Suit aS = terrainCard.getSuit();
            Suit bS = card.getSuit();
            return aS == bS || terrainCard.getValue() == card.getValue() || aS == Suit.WILD || bS == Suit.WILD;
        };
        winCondition = player -> player.getHand().isEmpty();
        players = Stream.concat(
                Stream.of(User.getInstance()),
                Stream.of(Enemy.values()).map(en -> en.get())).toArray(Player[]::new);
    }

    /* ---.--- Getters and Setters ------------ */

    public Player[] getPlayers() {
        return players;
    }

    public void setTurnOrder(Player[] newOrder) {
        players = newOrder;
    }

    public Predicate<Card> getPlayCondition() {
        return playCondition;
    }

    public int getTurn() {
        return turn;
    }

    public void setTurn(int newTurn) {
        turn = newTurn;
    }

    public Player getCurrentPlayer() {
        return players[turn];
    }

    /* --- State ------------------------------ */

    public void changeState(GameState newState) {
        state = newState;
    }

    /* --- Game Loop -------------------------- */

    /**
     * Sets the first turn, deals cards to the players, draws the first terrain card
     * and notifies that the game is about to start.
     */
    private void setupGame() {
        PlayerTurn initialState = new PlayerTurn();
        initialState.setContext(getCurrentPlayer(), this);
        getCurrentPlayer().setState(1);
        changeState(initialState);

        // Notify
        HashMap<String, Object> data = new HashMap<>();

        data.put("all-nicknames", Stream.of(getPlayers()).map(Player::getNickame).toArray(String[]::new));
        data.put("all-icons", Stream.of(getPlayers()).map(Player::getIcon).toArray(String[]::new));
        notifyToCU(Event.GAME_READY, data);

        // First card
        shuffleDeck();
        Card firstCard = takeFromDeck();
        changeCurrentCard(firstCard);
        notifyToCU(Event.CARD_CHANGE, firstCard.getData()); // Notify

        // Give cards to players
        for (Player player : players) {
            player.getHand().clear();
            dealFromDeck(player, firstHandSize);
        }
        notifyToCU(Event.GAME_START, null); // Notify
    }

    /**
     * Setup the game and starts the game loop for this match. After someone won,
     * ends the game.
     */
    public void play() {
        setupGame();
        timeStart = System.currentTimeMillis();

        while (!interrupted && !winCondition.test(getCurrentPlayer()))
            state.resolve();

        if (!interrupted)
            end();
    }

    /**
     * Interrupts this match, blocking all incoming and outcoming communications and
     * making the <code>play</code> loop end.
     */
    public void block() {
        interrupted = true;
    }

    /**
     * After the game loop ends, notifies that the game has ended and updates user
     * info, giving him xp (additional xp if they are also the winner).
     */
    private void end() {
        Player winner = getCurrentPlayer();
        boolean humanWon = !(winner instanceof GameAI);
        int xpEarned = (int) ((System.currentTimeMillis() - timeStart) / 60000F) + (humanWon ? 3 : 0);

        UserData.addXp(xpEarned);
        UserData.addGamePlayed(humanWon);

        HashMap<String, Object> data = UserData.wrapData();
        data.put("xp-earned", xpEarned);
        notifyToCU(Event.INFO_CHANGE, data);
        notifyToCU(Event.PLAYER_WON, winner.getData());
    }

    /* --- Body ------------------------------- */

    public void playerCard(Player player, Card card) {
        changeCurrentCard(card);
        player.getHand().remove(card);

        HashMap<String, Object> data = player.getData();
        data.putAll(card.getData());
        boolean userTurn = getCurrentPlayer() instanceof User;
        notifyToCU(userTurn ? Event.USER_PLAYED_CARD : Event.AI_PLAYED_CARD, data);
        notifyToCU(Event.CARD_CHANGE, data);
    }

    /**
     * Replace the current terrain card with the given card.
     * 
     * @param card The new terrain card.
     */
    public void changeCurrentCard(Card card) {
        if (terrainCard != null)
            discardPile.add(terrainCard);
        terrainCard = card;
    }

    /**
     * 
     * @return The first card in the deck pile.
     */
    public Card takeFromDeck() {
        if (deck.isEmpty())
            shuffleDeck();
        return deck.remove(0);
    }

    /**
     * Shuffles the deck.
     */
    public void shuffleDeck() {
        discardPile.forEach(card -> card.shuffleIn(deck));
        Collections.shuffle(deck);
        discardPile.clear();
    }

    /**
     * A player draws from deck the choosen amount of cards.
     * 
     * @param player The player that will draw the cards.
     * @param times  The amount of cards to be drawn.
     */
    public void dealFromDeck(Player player, int times) {
        while (times-- > 0) {
            // Add card
            Card card = takeFromDeck();
            player.getHand().add(card);

            // Notify
            HashMap<String, Object> data = card.getData();
            data.putAll(player.getData());

            if (!(player instanceof GameAI))
                notifyToCU(Event.USER_DREW, data);
            else
                notifyToCU(Event.AI_DREW, data);
        }
    }

    /**
     * Jumps to the turn ahead by the given amount.
     * 
     * @param ahead The amount of turns to skip.
     */
    public void advanceTurn(int ahead) {
        int newTurn = (getTurn() + ahead) % players.length;
        setTurn(newTurn);
    }

    /**
     * Sends the message to the CUModel. It has the same effect of
     * <p>
     * 
     * <pre>
     * CUModel.communicate(event, data)
     * </pre>
     * </p>
     * but it stops the message to be sent if the game is dead (i.e., interrupted)
     * 
     * @param event
     * @param data
     */
    public void notifyToCU(Event event, HashMap<String, Object> data) {
        if (!interrupted)
            CUModel.communicate(event, data);
    }
}