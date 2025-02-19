import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import view.GUIContainer;
import view.media.Sound;

/* --- Mine ------------------------------- */

import view.settings.ProfileMenu;

/**
 * The home page of the application.
 */
public class Home extends StackPane implements AppState, GUIContainer {
    /* --- Singleton -------------------------- */

    private static Home instance;

    
    /** 
     * @return Home
     */
    public static Home getInstance() {
        if (instance == null)
            instance = new Home();
        return instance;
    }

    private Home() {
        createElements();
        arrangeElements();
        applyBehaviors();
    }

    /* --- Fields ----------------------------- */

    private JUno app; // context
    private Label title;
    private Button playButton;
    private Button profile;
    private Button exit;
    private ProfileMenu profileMenu;

    /* --- Body ------------------------------- */

    private void play() {
        Sound.BUTTON_CLICK.play(false);
        playButton.setDisable(false);
        InGame.getInstance().setContext(app);
        app.changeState(InGame.getInstance());

    }

    private void openProfile() {
        Sound.BUTTON_CLICK.play(false);
        profileMenu.setVisible(!profileMenu.isVisible());
    }

    private void exit() {
        Sound.BUTTON_CLICK.play(false);
        System.exit(0);
    }

    /* --- Visible ---------------------------- */

    @Override
    public void createElements() {
        title = new Label("JUno");
        playButton = new Button("Play");
        profile = new Button("Profile");
        exit = new Button("Exit");
        profileMenu = ProfileMenu.getInstance();
        profileMenu.setVisible(false);

        title.getStyleClass().add("title");
        playButton.getStyleClass().add("button");
        profile.getStyleClass().add("button");
        exit.getStyleClass().add("button");

    }

    @Override
    public void arrangeElements() {
        VBox buttons = new VBox(title, playButton, profile, exit);
        buttons.getStyleClass().add("home-menu");
        buttons.setSpacing(10.0);
        buttons.setAlignment(Pos.CENTER_LEFT);

        getChildren().addAll(buttons, profileMenu);
        StackPane.setAlignment(buttons, Pos.TOP_LEFT);
    }

    @Override
    public void applyBehaviors() {
        playButton.setOnMouseClicked(e -> {
            playButton.setDisable(true); // Avoid clicking play multiple times before game starts
            play();
        });
        profile.setOnMouseClicked(e -> openProfile());
        exit.setOnMouseClicked(e -> exit());
    }

    /* --- State ------------------------------ */

    public void setContext(JUno app) {
        this.app = app;
    }

    @Override
    public void display() {
    }
}
