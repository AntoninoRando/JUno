package Controllers;

import CardsTools.Card;
import CardsTools.Hand;

public class Player {
    private String nickname;
    private Hand hand;

    public Player(String nickname, Card... cards) {
        this.nickname = nickname;
        hand = new Hand(cards);
    }

    public String getNickname() {
        return nickname;
    }

    public Hand getHand() {
        return hand;
    }

    public Card getCard(int index) {
        return hand.get(index);
    }

    protected Card removeCard(int index) {
        return hand.remove(index);
    }

    protected void addCard(Card card) {
        hand.add(card);

        // !Potrebbe essere inefficiente e spiacevole sortare ad ogni pescata
        hand.arrange();
    }
}
