package view;

import model.Player;
import model.JUno;

import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;

public class GameFX extends Displayer {
    /* SINGLETON */
    /* --------- */
    private static GameFX instance;

    public static GameFX getInstance() {
        if (instance == null)
            instance = new GameFX();
        return instance;
    }

    public GameFX() {
        super("gameStart");
        if (instance == null)
            instance = this;
    }

    /* ---------------------------------------- */
    private BorderPane background;
    private HandPane hand;
    private PlayzonePane playzone;

    private Pane createContent() {
        background = new BorderPane();
        playzone = new PlayzonePane();
        background.setCenter(playzone);

        // TODO non ho capito come funziona tutto cio... in logica doveva far fittare l'handPane nel background
        AnchorPane anchorPane = new AnchorPane();
        background.setBottom(anchorPane);
        hand = new HandPane();
        anchorPane.getChildren().add(hand);
        AnchorPane.setTopAnchor(hand, 0.0);
        AnchorPane.setBottomAnchor(hand, 0.0);
        AnchorPane.setLeftAnchor(hand, 0.0);
        AnchorPane.setRightAnchor(hand, 0.0);

        return background;
    }

    @Override
    public void start(Stage stage) throws Exception {
        Scene scene = new Scene(createContent(), 1380, 760);
        scene.getStylesheets().add(getClass().getResource("Style.css").toExternalForm());
        stage.setScene(scene);
        stage.show();

        CardContainer.playzoneBounds = playzone.localToScene(playzone.getBoundsInLocal());
    }

    public static void main(String[] args) {
        new JUno().start();
        launch(args);
    }

    @Override
    public void update(String eventType, Object data) {
        if (eventType.equals("gameStart"))
            Platform.runLater(() -> {
                Player player = (Player) data;
                while(player.getHand() == null) {
                    try {
                        wait(100);
                    } catch (InterruptedException e) {
                        // TODO non so se sia giusto quello che ho fatto ma faccio aspettare nel caso la logica di gioco stia caricando la mano del giocatore mentre la scena sia già pronta.
                        e.printStackTrace();
                    }
                }
                for (int i = 0; i < player.getHand().size(); i++)
                    hand.addCard(new CardContainer(player.getHand().get(i)));
            });
    }
}
