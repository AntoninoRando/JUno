package model.players;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;
import java.util.function.Predicate;

/* --- JUno ------------------------------- */

import model.cards.Card;

/**
 * Implements the <em>Template Method Pattern</em>.
 * <p>
 * An abstract AI player. The concrete AI must define how to choose a card among
 * different options.
 */

/**
 * This class contains those player info (user or AI) that are also used during
 * the game: nickanem, icon, cards in hand.
 */
public abstract class Player {
    /* --- Fields ----------------------------- */

    protected String icon;
    protected String nickname;
    protected List<Card> hand;
    protected boolean playing;

    /* --- Constructors ----------------------- */

    /**
     * Creates a new player with the given icon and nickname, and an empty hand.
     * 
     * @param icon     The icon of the player.
     * @param nickname The nickname of the player.
     */
    public Player(String icon, String nickname) {
        this.icon = icon;
        this.nickname = nickname;
        hand = new LinkedList<Card>();
    }

    
    /** 
     * @return String
     */
    /* --- Getters and Setters ---------------- */

    public String getNickame() {
        return nickname;
    }

    
    /** 
     * @return String
     */
    public String getIcon() {
        return icon;
    }

    public List<Card> getHand() {
        return hand;
    }

    public boolean isPlaying() {
        return playing;
    }

    public void setPlayingState(boolean isPlaying) {
        this.playing = isPlaying;
    }

    /* --- Body ------------------------------- */

    /**
     * Wraps the player info and returns it.
     * 
     * @return The player data: "nickname" (String), "icon" (String) and "hand-size"
     *         (int).
     */
    public HashMap<String, Object> getData() {
        HashMap<String, Object> data = new HashMap<>();
        data.put("nickname", getNickame());
        data.put("icon", getIcon());
        data.put("hand-size", getHand().size());
        return data;
    }

    /**
     * Given different cards options, returns the type of action to perform and the
     * value choosen (if there is any).
     * 
     * @param cards The cards options.
     * @return The type of action to perform associated with its info.
     */
    public abstract Entry<String, Object> chooseFrom(Card[] cards);

    /**
     * It has the same effect as the
     * <code>chooseFrom(Collection<Card> cards)</code>, but only the cards that
     * respect the validate conditions can be choosed.
     * 
     * @param cards    The cards options.
     * @param validate The filter to reduce the card options only to the valid
     *                 cards.
     * @return The type of action to perform associated with its info.
     */
    public abstract Entry<String, Object> chooseFrom(Card[] cards, Predicate<Card> validate);
}
