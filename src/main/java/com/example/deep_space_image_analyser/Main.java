package com.example.deep_space_image_analyser;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class Main extends Application {
	@Override
	public void start(Stage stage) throws IOException {
		FXMLLoader fxmlLoader = new FXMLLoader(Main.class.getResource("View.fxml"));
		Scene scene = new Scene(fxmlLoader.load(), 835, 550);

		stage.setTitle("Galaxy Image Analyser");
		stage.setResizable(false); //set the scene as a set size
		stage.setScene(scene);
		stage.show();
	}

	public static void main(String[] args) {
		launch();
	}
}