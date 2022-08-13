package model.gameLogic;

import model.events.EventManager;
import model.events.InputListener;
import view.Displayer;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.TreeMap;
import java.util.function.Supplier;

import controller.Controller;
import model.data.UserInfo;

public class Loop implements InputListener {
    /* SINGLETON */
    /* ------------------------------ */
    private static Loop instance;

    public static Loop getInstance() {
        if (instance == null)
            instance = new Loop();
        return instance;
    }

    private Loop() {
        choiceTypes = new HashMap<>();

        choiceTypes.put("card", () -> {
            Card c = (Card) choice;
            if (g.isPlayable(c)) {
                Actions.changeCurrentCard(c);
                g.getPlayer().hand.remove(c);
                c.getEffect().ifPresent(effect -> effect.cast(g.getPlayer(), c));
                events.notify("cardPlayed", c);

                if (g.getPlayer().isHuman() && g.getPlayer().getHand().size() == 1 && !unoDeclared) 
                    Actions.dealFromDeck(g.getPlayer(), 2);

                events.notify("playerHandChanged", g.getPlayer());
                return true;
            } else {
                events.notify("warning", "Can't play it now!", c);
                return false;
            }
        });
        choiceTypes.put("draw", () -> {
            Actions.dealFromDeck(g.getPlayer());
            return true;
        });
        choiceTypes.put("unoDeclared", () -> {
            startUnoTimer();
            return false;
        });
        choiceTypes.put("cardPosition", () -> {
            try {
                choice = g.getPlayer().getHand().get((int) choice);
                return choiceTypes.get("card").get();
            } catch (IndexOutOfBoundsException e) {
                events.notify("warning", "Invalid selection!");
                return false;
            }
        });
    }

    /* ------------------------------ */

    public static EventManager events = new EventManager();

    static HashMap<String, Supplier<Boolean>> choiceTypes;
    private Displayer disp;

    private static Game g;
    private static Player player;
    static Object choice;
    static String choiceType;

    private static Controller[] users;

    static boolean unoDeclared;

    LinkedList<Phase> phases = new LinkedList<>();
    static int currentPhase;

    private static long timeStart;

    public void setupView(Displayer displayer) {
        disp = displayer;
        events.subscribe(disp, disp.getEventsListening().stream().toArray(String[]::new));
    }

    public void setupGame(TreeMap<Integer, Player> players, Controller... users) {
        Game.reset();
        g = Game.getInstance();
        g.setPlayers(players);

        Loop.users = users;
        for (Controller c : users) {
            c.setInputListener(this);
            c.start();
        }

        g.restoreTurnOrder();
        phases.add(Phases.START_TURN);
        phases.add(Phases.MAKE_CHOICE);
        phases.add(Phases.PARSE_CHOICE);
        phases.add(Phases.RESOLVE_CHOICE);
        phases.add(Phases.END_TURN);
    }

    /* ------------------------------ */

    public void play() {
        setupFirstTurn();
        timeStart = System.currentTimeMillis();
        try {
            while (!g.isOver()) {
                boolean validChoice = phases.get(currentPhase).apply(this, Game.getInstance());

                if (g.didPlayerWin(g.getPlayer())) {
                    endGame(false);
                    return;
                }

                if (phases.get(currentPhase) == Phases.RESOLVE_CHOICE && !validChoice)
                    currentPhase = 1;
                else
                    currentPhase = (++currentPhase) % phases.size();
            }
        } catch (NullPointerException e) {
            // May be here because of the "reset()" method
            events.notify("gameIsOver", (Object[]) null);
        }
    }

    private void setupFirstTurn() {
        events.notify("gameStart", g.getPlayers().toArray());

        Actions.shuffleDeck();
        Card firstCard = Actions.takeFromDeck();
        Actions.changeCurrentCard(firstCard);
        events.notify("firstCard", firstCard);

        for (Player p : g.getPlayers())
            Actions.dealFromDeck(p, 1); //TODO change back to 7

        player = g.getPlayer(0);

        for (Controller c : users)
            c.setupControls();

    }

    public void endGame(boolean interrupted) {
        int minutesElapsed = (int) ((System.currentTimeMillis() - timeStart) / 60000F);
        int xpEarned = minutesElapsed;
        UserInfo.addXp(xpEarned);

        if (!interrupted) {
            Player winner = g.getPlayer();
            if (winner.isHuman())
                UserInfo.addXp(5);
            UserInfo.addGamePlayed(winner.isHuman());
            events.notify("playerWon", g.getPlayer(), xpEarned);
        }

        Game.reset();
        
        g = null;
        player = null;
        choice = null;
        choiceType = null;
        users = null;
        unoDeclared = false;
        currentPhase = 0;
        timeStart = 0;

        events.notify("reset");
    }

    /* INPUTLISTENER */
    /* ------------- */
    @Override
    public void accept(Object choice, Player source) {
        synchronized (this) {
            // We use == instead of equals because they have to be the same object
            if (source != player) {
                events.notify("warning", "This is not your turn!");
                return;
            }
            Loop.choice = choice;
            notify();
        }
    }

    /* -------------------------------------- */
    private void startUnoTimer() {
        Player unoer = player;
        events.notify("unoDeclared", unoer);
        unoDeclared = true;

        new Thread() {
            @Override
            public void run() {
                try {
                    sleep(5000);
                } catch (InterruptedException e) {
                }
                // We use == because they have to be the same. We check if it is still the unoer
                // turn because we don't want to modify the unoDeclared for others player.
                if (unoer == player)
                    unoDeclared = false;
            }
        }.start();
    }
}