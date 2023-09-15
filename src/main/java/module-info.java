module com.example.deep_space_image_analyser {
	requires javafx.controls;
	requires javafx.fxml;
	requires javafx.web;

	requires org.controlsfx.controls;
	requires com.dlsc.formsfx;
	requires org.kordamp.ikonli.javafx;
	requires org.kordamp.bootstrapfx.core;
	requires com.almasb.fxgl.all;
	requires java.desktop;

	opens com.example.deep_space_image_analyser to javafx.fxml;
	exports com.example.deep_space_image_analyser;
}