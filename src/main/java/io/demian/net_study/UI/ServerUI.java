package io.demian.net_study.UI;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

import io.demian.net_study.Constant;

import java.net.URL;
import java.nio.file.Paths;

public abstract class ServerUI extends Application {

    public abstract void startServer();
    public abstract void stopServer();

    public TextArea txtDisplay;
    public Button btnStartStop;

    @Override
    public void start(Stage primaryStage) throws Exception {
        BorderPane root = new BorderPane();
        root.setPrefSize(500, 300);

        txtDisplay = new TextArea();
        txtDisplay.setEditable(false);
        BorderPane.setMargin(txtDisplay, new Insets(0, 0, 2, 0));
        root.setCenter(txtDisplay);

        btnStartStop = new Button("start");
        btnStartStop.setPrefHeight(30);
        btnStartStop.setMaxWidth(Double.MAX_VALUE);
        btnStartStop.setOnAction(e -> {
            if (btnStartStop.getText().equals("start")) {
                startServer();
            } else if (btnStartStop.getText().equals("stop")) {
                stopServer();
            }
        });
        root.setBottom(btnStartStop);

        Scene scene = new Scene(root);
        scene.getStylesheets().add(getClass().getResource(Constant.APP_STYLE_PATH).toString());
        primaryStage.setScene(scene);
        primaryStage.setTitle("Server");
        primaryStage.setOnCloseRequest(event -> stopServer());
        primaryStage.show();
    }

    public void displayText(String text) {
        txtDisplay.appendText(text + "\n");
    }

    public TextArea getTxtDisplay() {
        return txtDisplay;
    }

    public void setTxtDisplay(TextArea txtDisplay) {
        this.txtDisplay = txtDisplay;
    }

    public Button getBtnStartStop() {
        return btnStartStop;
    }

    public void setBtnStartStop(Button btnStartStop) {
        this.btnStartStop = btnStartStop;
    }
}