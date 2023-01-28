import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

/* --- Mine ------------------------------- */

import view.GameResults;
import view.GUIContainer;
import view.animations.Animation;
import view.animations.Animations;
import view.sounds.Sounds;

public class EndGame extends VBox implements AppState, GUIContainer {
    /* --- Singleton -------------------------- */

    private static EndGame instance;

    public static EndGame getInstance() {
        if (instance == null)
            instance = new EndGame();
        return instance;
    }

    private EndGame() {
        initialize();
    }

    /* --- Fields ----------------------------- */

    private JUno app; // context
    private Button playAgain;
    private Button backHome;

    /* --- Body ------------------------------- */

    private void playAgain() {
        Animation closing = Animations.NEW_GAME.get();
        closing.setStopFrame(5);
        closing.setDimensions(app.getScene().getWidth(), app.getScene().getHeight());

        Sounds.BUTTON_CLICK.play();
        closing.play(this);

        app.changeState(InGame.getInstance());
    }

    private void goHome() {
        Sounds.BUTTON_CLICK.play();
        app.changeState(Home.getInstance());
    }

    /* --- Visible ---------------------------- */

    @Override
    public void createElements() {
        playAgain = new Button();
        backHome = new Button();
    }

    @Override
    public void arrangeElements() {
        playAgain.setStyle("-fx-background-color: none");
        backHome.setStyle("-fx-background-color: none");

        ImageView playAgainImage = new ImageView(new Image("resources\\BlueButton.png"));
        playAgainImage.setPreserveRatio(true);
        playAgainImage.setFitWidth(150.0);
        playAgain.setGraphic(playAgainImage);

        ImageView backHomeImage = new ImageView(new Image("resources\\HomeButton.png"));
        backHomeImage.setPreserveRatio(true);
        backHomeImage.setFitWidth(150.0);
        backHome.setGraphic(backHomeImage);

        HBox buttonsContainer = new HBox(playAgain, backHome);
        buttonsContainer.setAlignment(Pos.CENTER);

        setId("game-results");
        getChildren().addAll(GameResults.getInstance(), buttonsContainer);
        setAlignment(Pos.CENTER);
    }

    @Override
    public void applyBehaviors() {
        playAgain.setOnMouseClicked(e -> playAgain());
        backHome.setOnMouseClicked(e -> goHome());
    }

    /* --- State ------------------------------ */

    public void setContext(JUno app) {
        this.app = app;
    }

    @Override
    public void display() {
    }
}
