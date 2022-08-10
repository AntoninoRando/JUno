import java.util.TreeMap;

import javafx.application.Platform;
import javafx.geometry.Bounds;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import model.gameLogic.Loop;
import model.gameLogic.Player;
import model.gameLogic.Card;
import model.data.UserInfo;

import view.Displayer;
import view.animations.Animation;
import view.animations.Animations;
import view.animations.ResetTranslate;
import view.gameElements.PlayerPane;
import view.gameElements.HandPane;
import view.gameElements.PlayerLabel;
import view.gameElements.PlayzonePane;
import view.gameElements.SelectionPane;
import view.gameElements.TerrainPane;
import view.home.HomeMenu;
import view.home.Homes;
import view.settings.Settings;
import view.sounds.Sounds;

import controller.Controller;
import controller.ControllerFX;
import controller.Controls;

public class App extends Displayer {
    private Scene scene;
    private StackPane root;
    private HomeMenu home;
    private StackPane gameElements;
    private Thread gameThread;

    public App() {
        super("gameStart", "unoDeclared", "turnBlocked", "enemyTurn cardPlayed", "warning", "turnStart", "turnEnd");

        root = new StackPane();
        root.setId("background");

        scene = new Scene(root, 1000, 600);
        scene.getStylesheets().add(getClass().getResource("resources\\Style.css").toExternalForm());

        UserInfo.loadData("resources\\data\\userInfo.txt");
        Runtime.getRuntime().addShutdownHook(new Thread(() -> UserInfo.writeData("resources\\data\\userInfo.txt")));
    }

    private void arrangeLayers() {
        addGameContents();
        addHomePageContents();
        addSettingsContents();
    }

    private void addGameContents() {
        gameElements = new StackPane();
        gameElements.setVisible(false);

        BorderPane borderPane = new BorderPane();
        borderPane.setLeft(PlayerPane.getInstance());
        borderPane.setCenter(TerrainPane.getInstance());
        borderPane.setBottom(HandPane.getInstance());

        // Add padding to the borderPane right to absolute center the node in the
        // borderPane center region
        Region padderRegionRight = new Region();
        padderRegionRight.prefWidthProperty().bind(PlayerPane.getInstance().widthProperty());
        borderPane.setRight(padderRegionRight);

        gameElements.getChildren().addAll(borderPane, PlayzonePane.getInstance(),
                SelectionPane.getInstance());

        PlayzonePane.getInstance().setOnMouseClicked(e -> {
            if (e.getButton().equals(MouseButton.SECONDARY))
                Controls.DECLARE_UNO.getAction().handle(e);
            else
                Controls.DRAW.getAction().handle(e);
        });

        root.getChildren().add(gameElements);
    }

    private void addHomePageContents() {
        home = HomeMenu.getInstance();

        root.getChildren().add(home);
        StackPane.setAlignment(home, Pos.TOP_LEFT);

        Homes.setPlayButtonAction(e -> newGame());
    }

    private void addSettingsContents() {
        root.getChildren().addAll(Settings.MENU, Settings.SETTINGS_BUTTON);
        StackPane.setAlignment(Settings.SETTINGS_BUTTON, Pos.TOP_RIGHT);

        Settings.setRestartButtonAction(e -> {
            endGame();
            newGame();
        });
        Settings.setQuitButtonAction(e -> {
            endGame();
            gameElements.setVisible(false);
            home.setVisible(true);
        });
        Settings.setNickFieldAction(__ -> UserInfo.setNick(__));
        Settings.setDeleteAccountAction(e -> UserInfo.reset());
        Settings.setAvatarClickAction(e -> Settings.openAvatarPicker());
    }

    private Animation unoAnimation;
    private Animation blockTurnAnimation;
    private Animation playingCardAnimation;
    private Animation focusPlayerOnTurnAnimation;

    private void loadAnimations() {
        Animations.FOCUS_PLAYER.get().load();

        unoAnimation = Animations.UNO_TEXT.get();
        unoAnimation.setDimensions(400.0, null);

        blockTurnAnimation = Animations.BLOCK_TURN.get();
        blockTurnAnimation.setDimensions(200.0, null);
        blockTurnAnimation.setWillCountdown(true);

        playingCardAnimation = Animations.CARD_PLAYED.get();
        playingCardAnimation.setDimensions(400.0, null);
        playingCardAnimation.setWillCountdown(true);

        focusPlayerOnTurnAnimation = Animations.FOCUS_PLAYER.get();
        focusPlayerOnTurnAnimation.setWillCountdown(true);
        focusPlayerOnTurnAnimation.setWillStay(true);
    }

    private void newGame() {
        Player p1 = new Player(UserInfo.getNick(), true);
        Player p2 = new Player("Top Princessess", false);
        Player p3 = new Player("Bot Luca", false);
        Player p4 = new Player("Bot Giovanni", false);

        Controller c1 = new ControllerFX();
        c1.setSource(p1);

        TreeMap<Integer, Player> players = new TreeMap<>();
        players.put(0, p1);
        players.put(1, p2);
        players.put(2, p3);
        players.put(3, p4);

        Loop.getInstance().setupGame(players, c1);
        Loop.getInstance().setupView(this);

        gameElements.setVisible(true);
        home.setVisible(false);

        gameThread = new Thread(() -> Loop.getInstance().play());
        gameThread.start();
    }

    private void endGame() {
        Sounds.IN_GAME_SOUNDTRACK.stop();
        Loop.reset();
        gameThread.interrupt();
    }

    /* ---------------------------------------------------- */

    @Override
    public void start(Stage stage) throws Exception {
        stage.setTitle("JUno");

        stage.setFullScreen(true);
        stage.setFullScreenExitHint("");

        stage.setOnCloseRequest(e -> System.exit(0));

        arrangeLayers();
        loadAnimations();

        stage.setScene(scene);
        stage.show();
    }

    /* EVENT LISTENER METHODS */

    @Override
    public void update(String eventType, Object... data) {
        // TODO le animazioni le gioca toppo on the top: stanno sopra al menù di pausa
        switch (eventType) {
            case "gameStart":
                Platform.runLater(() -> Sounds.IN_GAME_SOUNDTRACK.play());
                break;
            case "warning":
                Platform.runLater(() -> ResetTranslate.resetTranslate(((Card) data[1]).getGuiContainer()));
                break;
            case "unoDeclared":
                Platform.runLater(() -> unoAnimation.play(root));
                break;
            case "turnBlocked":
                Platform.runLater(() -> blockTurnAnimation.play(root));
                try {
                    blockTurnAnimation.latch.await();
                } catch (InterruptedException e) {
                }
                blockTurnAnimation.resetLatch();
                break;
            case "enemyTurn cardPlayed":
                // TODO Aggiungere priorità all'animazione altrimenti parte dopo che è avvenuta
                // la modifica
                Platform.runLater(() -> playingCardAnimation.play(root));
                try {
                    playingCardAnimation.latch.await();
                } catch (InterruptedException e) {
                }
                playingCardAnimation.resetLatch();
                break;
            case "turnStart":
                PlayerLabel pl = PlayerPane.getInstance().getPlayerLabel((Player) data[0]);
                Bounds b = pl.localToScene(pl.getBoundsInLocal());

                focusPlayerOnTurnAnimation.setDimensions(PlayerPane.getInstance().getWidth(), b.getHeight() + 10);
                focusPlayerOnTurnAnimation.setSceneCoordinates(0.0, b.getMinY() - 5);

                Platform.runLater(() -> focusPlayerOnTurnAnimation.play(root, 0).getValue());
                try {
                    focusPlayerOnTurnAnimation.latch.await();
                } catch (InterruptedException e) {
                }
                focusPlayerOnTurnAnimation.resetLatch();
                break;
            case "turnEnd":
                Platform.runLater(() -> root.getChildren().remove(0));
                break;
        }
    }

    @Override
    public int getEventPriority(String eventLabel) {
        switch (eventLabel) {
            case "enemyTurn cardPlayed":
                return 2;
            default:
                return super.getEventPriority(eventLabel);
        }
    }

    /* MAIN */

    public static void main(String[] args) {
        launch(args);
    }

}
