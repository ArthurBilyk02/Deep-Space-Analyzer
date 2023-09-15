package com.example.deep_space_image_analyser;

import javafx.fxml.FXML;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.Slider;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;
import javafx.scene.paint.Color;
import javafx.stage.FileChooser;

import javafx.scene.shape.Circle;
import java.io.File;
import java.util.List;

public class Controller {

	@FXML
	ImageView selectedimage;
	@FXML
	ImageView grayscaleImage;
	@FXML
	ImageView returnedImage;
	@FXML
	Label minPixelValueLabel;
	@FXML
	Label maxPixelValueLabel;
	@FXML
	Label lightValueLabel;
	@FXML
	Slider minPixels;
	@FXML
	Slider maxPixels;
	@FXML
	Slider light;
	@FXML
	RadioButton numberSelection;

	private Image selectedImage;
	private ImageProcessor imageProcessor;

	// Initialize method that runs when the controller is created
	public void initialize() {
		// Attach event handlers to the sliders

		// Update the minPixelValueLabel as the minPixels slider is dragged
		minPixels.setOnMouseDragged(event -> {
			int minPixelsValue = (int) Math.round(minPixels.getValue());
			String slider1String = String.format("%s", minPixelsValue);
			minPixelValueLabel.setText(slider1String);
		});

		// Update the maxPixelValueLabel as the maxPixels slider is dragged
		maxPixels.setOnMouseDragged(event -> {
			int maxPixelsValue = (int) Math.round(maxPixels.getValue());
			String slider2String = String.format("%s", maxPixelsValue);
			maxPixelValueLabel.setText(slider2String);
		});

		// Update the lightValueLabel as the light slider is dragged
		light.setOnMouseDragged(event -> {
			String slider3String = String.format("%.2f", light.getValue());
			lightValueLabel.setText(slider3String);
		});
	}

	// Method to handle the "Choose Photo" button click
	@FXML
	private void choosePhoto() {
		FileChooser fileChooser = new FileChooser();
		fileChooser.setTitle("Choose Photo");
		fileChooser.getExtensionFilters().add(
				new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg", "*.gif", "*.jfif")
		);

		// Open a file dialog for the user to select an image
		File selectedFile = fileChooser.showOpenDialog(null);

		if (selectedFile != null) {
			// Load the selected image and update the UI
			selectedImage = new Image(selectedFile.toURI().toString());
			updateSliderMaxValues(selectedImage);
			selectedimage.setImage(selectedImage);
			imageProcessor = new ImageProcessor(selectedImage);
		}
	}

	// Method to handle the "Process Image" button click
	@FXML
	private void processImage() {
		// Display the grayscale version of the selected image
		grayscaleImage.setImage(imageProcessor.convertToBinaryMask(selectedImage));

		if (imageProcessor != null) {
			// Get values from sliders
			int minPixelSize = (int) Math.round(minPixels.getValue());
			int maxPixelSize = (int) Math.round(maxPixels.getValue());
			double lightThreshold = light.getValue();

			// Process the image and get bounding boxes
			List<Circle> boundingBoxes = imageProcessor.process(minPixelSize, maxPixelSize, lightThreshold, numberSelection.isSelected());

			// Display the processed image with bounding boxes
			Image processedImage = drawBoundingBoxes(selectedImage, boundingBoxes, numberSelection.isSelected());
			returnedImage.setImage(processedImage);
		}
	}

	// Helper method to set max values for minPixels and maxPixels based on image dimensions
	private void updateSliderMaxValues(Image image) {
		double imageWidth = image.getWidth();
		double imageHeight = image.getHeight();

		// Set the maximum values for minPixels and maxPixels sliders
		minPixels.setMax(Math.min(imageWidth, imageHeight)); // Set max to the smaller dimension
		maxPixels.setMax(Math.max(imageWidth, imageHeight)); // Set max to the larger dimension
	}

	// Helper method to draw bounding boxes on the image
	private Image drawBoundingBoxes(Image originalImage, List<Circle> boundingBoxes, boolean drawNumbers) {
		// Create a WritableImage with the same dimensions as the original image
		WritableImage resultImage = new WritableImage((int) originalImage.getWidth(), (int) originalImage.getHeight());

		// Create a Canvas to draw on the WritableImage
		Canvas canvas = new Canvas(resultImage.getWidth(), resultImage.getHeight());
		GraphicsContext gc = canvas.getGraphicsContext2D();

		// Draw the original image on the canvas
		gc.drawImage(originalImage, 0, 0);

		// Draw bounding boxes on the canvas
		gc.setStroke(Color.BLUE);
		gc.setLineWidth(2);

		for (Circle Circ : boundingBoxes) {
			double centerX = Circ.getCenterX();
			double centerY = Circ.getCenterY();
			double radius = Circ.getRadius();
			gc.strokeOval(centerX - radius, centerY - radius, radius * 2, radius * 2);
		}

		// Check if numbers should be drawn on the image
		if (drawNumbers) {
			// Call your createNumbers method here if numbers should be drawn
			imageProcessor.createNumbers(boundingBoxes, gc);
		}

		// Snapshot the canvas to a WritableImage and return it
		canvas.snapshot(null, resultImage);

		return resultImage;
	}
}
