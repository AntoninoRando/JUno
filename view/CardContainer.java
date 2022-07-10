package view;

import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import javafx.animation.ScaleTransition;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.util.Duration;
import model.cards.Card;

public class CardContainer extends ImageView {
    // TODO non so se vanno bene per qualsiasi sistema operativo quei separatori
    private static Path imgFolder = Paths.get("C:\\Users\\anton\\OneDrive\\Desktop\\AllUnoCards");
    private Path imgPath;

    public CardContainer(Card card) {
        getStyleClass().add("card");
        imgPath = imgFolder.resolve(card.toString().concat(".png"));
        if (Files.notExists(imgPath))
            imgPath = imgFolder.resolve("MISSING.png");
        try {
            loadImage();
        } catch (MalformedURLException e) {
            throw new Error("Error while loading the image of: " + card.toString());
        }
    }

    private void loadImage() throws MalformedURLException {
        Image img = new Image(imgPath.toUri().toURL().toExternalForm());
        setImage(img);
        setPreserveRatio(true);
        setFitWidth(150);
        //makeDraggable();
        makeZommable(0.5);
    }

    /* -------------------------------- */
    private final ScaleTransition zoomIn = new ScaleTransition(Duration.millis(100.0), this);
    private final ScaleTransition zoomOut = new ScaleTransition(Duration.millis(100.0), this);

    // TODO migliorare perche' e' un po' lagghi e ogni tanto si bugga
    private void makeZommable(Double scalingFactor) {
        setOnMouseEntered(e -> {
            if (getScaleX() <= 1.0 || getScaleY() <= 1.0) {
                // 1 + scalingFactor  = getScaleX() + x
                zoomIn.setByX(1 + scalingFactor - getScaleX());
                zoomIn.setByY(1 + scalingFactor - getScaleY());
                zoomIn.play();
            }
        });

        setOnMouseExited(e -> {
            if (getScaleX() != 1.0 || getScaleY() != 1.0) {
                // 1 = getScaleX() - x 
                zoomOut.setByX(1 - getScaleX());
                zoomOut.setByY(1 - getScaleY());
                zoomOut.play();
            }
        });
    }
}
