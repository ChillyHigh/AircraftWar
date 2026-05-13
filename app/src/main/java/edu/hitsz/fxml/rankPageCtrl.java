package edu.hitsz.fxml;

import edu.hitsz.DAO.UserData;
import edu.hitsz.DAO.UserDataDaoImpl;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Optional;

public class rankPageCtrl {
    private UserDataDaoImpl userDataDao;
    private int currentScore;
    private String currentTime;
    private String difficulty = "easy";

    @FXML
    private Label scoreLabel;

    @FXML
    private Label difficultyLabel;

    @FXML
    private TableView<UserData> rankTable;

    @FXML
    private TableColumn<UserData, String> nameCol;

    @FXML
    private TableColumn<UserData, Integer> scoreCol;

    @FXML
    private TableColumn<UserData, String> timeCol;

    private final ObservableList<UserData> data = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        LocalDate today = LocalDate.now();
        LocalTime now = LocalTime.now();
        currentTime = today + " " + now;
        rankTable.setItems(data);
        nameCol.setCellValueFactory(new PropertyValueFactory<>("username"));
        scoreCol.setCellValueFactory(new PropertyValueFactory<>("score"));
        timeCol.setCellValueFactory(new PropertyValueFactory<>("time"));
        rankTable.setRowFactory(tableView -> {
            TableRow<UserData> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 1 && !row.isEmpty()) {
                    confirmDelete(row.getItem());
                }
            });
            return row;
        });
    }

    public void setCurrentScore(int score, String difficulty) {
        this.currentScore = score;
        this.difficulty = difficulty.toLowerCase();
        this.userDataDao = new UserDataDaoImpl(this.difficulty);
        scoreLabel.setText("本局得分: " + score);
        difficultyLabel.setText("当前榜单: " + difficulty.toUpperCase());
        refreshRankList();
        Platform.runLater(this::showNameDialog);
    }

    private void showNameDialog() {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("记录成绩");
        dialog.setHeaderText("游戏结束");
        dialog.setContentText("请输入玩家姓名：");

        Optional<String> result = dialog.showAndWait();
        result.ifPresent(name -> {
            String username = name.trim();
            if (!username.isEmpty()) {
                userDataDao.addUserData(new UserData(userDataDao.nextId(), username, currentScore, currentTime));
                refreshRankList();
            }
        });
    }

    private void confirmDelete(UserData userData) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("删除记录");
        alert.setHeaderText("删除排行榜记录");
        alert.setContentText("确认删除 " + userData.getUsername() + " 的成绩吗？");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            userDataDao.deleteUserData(userData.getId());
            refreshRankList();
        }
    }

    private void refreshRankList() {
        data.clear();
        data.addAll(userDataDao.getAllUserData());
    }

}
