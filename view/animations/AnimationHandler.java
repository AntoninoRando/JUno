package view.animations;

import java.util.HashMap;
import java.util.Map.Entry;

import events.Event;
import events.EventListener;
import javafx.application.Platform;
import javafx.scene.layout.Pane;
import view.CUView;

public class AnimationHandler implements EventListener {
    /* --- Singleton -------------------------- */

    private static AnimationHandler instance;

    public static AnimationHandler getInstance() {
        if (instance == null)
            instance = new AnimationHandler();
        return instance;
    }

    private AnimationHandler() {
        layers = new HashMap<>();
    }

    /* --- Fields ----------------------------- */

    private HashMap<Event, AnimationLayer> layers;

    /* --- Body ------------------------------- */

    public static void subscribe(AnimationLayer animationLayer, Event... events) {
        for (Event event : events) {
            getInstance().layers.put(event, animationLayer); // Subscribe AnimationLayer to this
            CUView.getInstance().subscribe(getInstance(), event); // Subssribe this to CUView
        }
    }

    private void playAnimation(Animation animation, Pane layer) {
        Platform.runLater(() -> animation.play(layer));

        if (!animation.willCountdown())
            return;

        try {
            animation.latch.await();
        } catch (InterruptedException e) {
        }
        animation.resetLatch();
    }

    @Override
    public void update(Event event, HashMap<String, Object> data) {
        Entry<Pane, Double[]> points = layers.get(event).getPoints(event);
        Pane layer = points.getKey();
        Double x = points.getValue()[0];
        Double y = points.getValue()[1];
        Double w = points.getValue()[2];
        Double h = points.getValue()[3];

        Animation animation;

        switch (event) {
            case UNO_DECLARED:
                boolean said = (boolean) data.get("said");
                animation = said ? Animations.UNO_TEXT.get() : null;
                break;
            case PLAYER_PLAYED_CARD:
                animation = Animations.CARD_PLAYED.get();
                animation.setWillCountdown(true);
                break;
            case TURN_START:
                animation = Animations.FOCUS_PLAYER.get();
                animation.setWillCountdown(true);
                animation.setWillStay(true);
                break;
            case TURN_BLOCKED:
                animation = Animations.BLOCK_TURN.get();
                animation.setWillCountdown(true);
                break;
            default:
                throwUnimplementedError(event);
                return;
        }

        animation.setSceneCoordinates(x, y);
        animation.setDimensions(w, h);

        playAnimation(animation, layer);
    }

    @Override
    public int getEventPriority(Event event) {
        switch (event) {
            case PLAYER_PLAYED_CARD:
                return 2;
            case TURN_START:
                return -1;
            default:
                return 1;
        }
    }
}
