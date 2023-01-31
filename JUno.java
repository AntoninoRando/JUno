import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import model.players.UserData;
import view.GUIContainer;
import view.SpriteFactory;
import view.media.Animations;
import view.media.Sound;
import view.settings.ProfileMenu;
import view.settings.SettingsMenu;
import events.EventListener;

public class JUno extends Application implements EventListener, GUIContainer {
    /* --- Fields ----------------------------- */

    private AppState state;
    private Scene scene;
    private StackPane root;
    private SettingsMenu settings;
    private Button settingsButton;

    /* ---.--- Getters and Setters ------------ */

    public Scene getScene() {
        return scene;
    }

    /* --- Constructors ----------------------- */

    public JUno() {
        initialize();
    }

    /* --- Body ------------------------------- */

    private void loadMedia() {
        Animations.FOCUS_PLAYER.get().load();
        Animations.NEW_GAME.get().load();
        Animations.UNO_TEXT.get().load();
        Animations.BLOCK_TURN.get().load();
        Animations.CARD_PLAYED.get().load();

        // Serve solo per far vedere la classe
        System.out.print("");
    }

    public static void main(String[] args) {
        String userDataPath = "resources\\Data\\userInfo.txt";
        ProfileMenu.getInstance(); // Iscrive il profile menu all'info change.
        UserData.load(userDataPath);
        Runtime.getRuntime().addShutdownHook(new Thread(() -> UserData.write(userDataPath)));
        launch(args);
    }

    /* --- Visible ---------------------------- */

    @Override
    public void createElements() {
        root = new StackPane();
        scene = new Scene(root, 1000, 600);
        settings = SettingsMenu.getInstance();
        settingsButton = new Button();
    }

    @Override
    public void arrangeElements() {
        scene.getStylesheets().add(getClass().getResource("resources\\Style.css").toExternalForm());

        settings.setVisible(false);

        settingsButton.setId("settings-button");
        ImageView buttonIcon = new ImageView();
        SpriteFactory.getButtonSprite("settings").draw(70.0, buttonIcon);
        settingsButton.setGraphic(buttonIcon);
        StackPane.setAlignment(settingsButton, Pos.TOP_RIGHT);

        root.getChildren().addAll(Home.getInstance(), settings, settingsButton);
        Home.getInstance().setContext(this);
    }

    @Override
    public void applyBehaviors() {
        settingsButton.setOnMouseClicked(__ -> {
            Sound.BUTTON_CLICK.play(false);
            settings.setVisible(!settings.isVisible());
        });
        settingsButton.setOnMouseEntered(__ -> {
            if (Animations.rotate360(settingsButton))
                Sound.GEAR.play(false);
        });
    }

    /* --- State ------------------------------ */

    public void changeState(AppState arrivalState) {
        this.state = arrivalState;
        Platform.runLater(() -> {
            root.getChildren().remove(0);
            root.getChildren().add(0, (Pane) state);
            state.display();
        });
    }

    /* --- Application ------------------------ */

    @Override
    public void start(Stage stage) throws Exception {
        stage.setTitle("JUno");
        stage.setFullScreen(true);
        stage.setFullScreenExitHint("");
        stage.setOnCloseRequest(e -> System.exit(0));
        stage.setScene(scene);
        loadMedia();
        stage.show();
    }
}
