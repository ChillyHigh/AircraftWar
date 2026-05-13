package edu.hitsz.fxml;

import edu.hitsz.application.AudioManager;
import edu.hitsz.application.Main;
import edu.hitsz.application.game.AbstractGame;
import edu.hitsz.application.game.EasyGame;
import edu.hitsz.application.game.HardGame;
import edu.hitsz.application.game.NormalGame;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;

public class DifficultySwitchCtrl {

    @FXML
    private AnchorPane root;

    @FXML
    private Button easyButton;

    @FXML
    private Button normalButton;

    @FXML
    private Button hardButton;

    private Difficulty difficulty = Difficulty.EASY;

    @FXML
    public void initialize() {
        root.setPrefSize(Main.WINDOW_WIDTH, Main.WINDOW_HEIGHT);
        selectDifficulty(difficulty);
    }

    @FXML
    private void selectEasy() {
        selectDifficulty(Difficulty.EASY);
    }

    @FXML
    private void selectNormal() {
        selectDifficulty(Difficulty.NORMAL);
    }

    @FXML
    private void selectHard() {
        selectDifficulty(Difficulty.HARD);
    }

    @FXML
    private void confirmSelection() {
        try {
            AbstractGame game = switch (difficulty) {
                case EASY -> new EasyGame();
                case NORMAL -> new NormalGame();
                case HARD -> new HardGame();
            };
            Scene gameScene = new Scene(game, Main.WINDOW_WIDTH, Main.WINDOW_HEIGHT);

            Stage stage = (Stage) root.getScene().getWindow();
            stage.setScene(gameScene);

            AudioManager.playBGM(AudioManager.BGM);
            game.action();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void selectDifficulty(Difficulty difficulty) {
        this.difficulty = difficulty;
        easyButton.getStyleClass().remove("selected-btn");
        normalButton.getStyleClass().remove("selected-btn");
        hardButton.getStyleClass().remove("selected-btn");

        switch (difficulty) {
            case EASY -> easyButton.getStyleClass().add("selected-btn");
            case NORMAL -> normalButton.getStyleClass().add("selected-btn");
            case HARD -> hardButton.getStyleClass().add("selected-btn");
        }

    }

    private enum Difficulty {
        EASY, NORMAL, HARD
    }
}
