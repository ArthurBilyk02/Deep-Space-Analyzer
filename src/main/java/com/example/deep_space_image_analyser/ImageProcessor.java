package com.example.deep_space_image_analyser;

import javafx.scene.Group;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ImageProcessor {

	private Image originalImage;
	private double luminanceThreshold;

	// Constructor that initializes the ImageProcessor with the original image
	public ImageProcessor(Image originalImage) {
		this.originalImage = originalImage;
	}

	// Main method for processing the image
	public List<Circle> process(int minSize, int maxSize, double luminanceThreshold, boolean numberSelection) {
		this.luminanceThreshold = luminanceThreshold;

		// 1. Convert the original image to black and white
		WritableImage blackAndWhiteImage = convertToBinaryMask(originalImage);

		// 2. Detect celestial objects using a simple flood-fill algorithm
		List<Circle> boundingBoxes = detectCelestialObjects(blackAndWhiteImage, minSize, maxSize, luminanceThreshold);

		// Check if the "numberSelection" RadioButton is selected

		return boundingBoxes;
	}

	// Method to convert an input image to a black and white binary mask
	public WritableImage convertToBinaryMask(Image inputImage) {
		int width = (int) inputImage.getWidth();
		int height = (int) inputImage.getHeight();

		WritableImage maskImage = new WritableImage(width, height);
		for (int x = 0; x < width; x++) {
			for (int y = 0; y < height; y++) {
				Color color = inputImage.getPixelReader().getColor(x, y);
				double luminance = (0.299 * color.getRed() + 0.587 * color.getGreen() + 0.114 * color.getBlue());

				// Set pixel to white or black based on luminance threshold
				if (luminance >= luminanceThreshold) {
					maskImage.getPixelWriter().setColor(x, y, Color.WHITE);
				} else {
					maskImage.getPixelWriter().setColor(x, y, Color.BLACK);
				}
			}
		}

		return maskImage;
	}

	// Method to detect celestial objects using flood-fill algorithm
	private List<Circle> detectCelestialObjects(WritableImage blackAndWhiteImage, int minSize, int maxSize, double luminanceThreshold) {
		int width = (int) blackAndWhiteImage.getWidth();
		int height = (int) blackAndWhiteImage.getHeight();
		List<Circle> boundingBoxes = new ArrayList<>();
		boolean[][] visited = new boolean[width][height];
		int objectCount = 0;

		for (int x = 0; x < width; x++) {
			for (int y = 0; y < height; y++) {
				if (!visited[x][y]) {
					Color pixelColor = blackAndWhiteImage.getPixelReader().getColor(x, y);
					double luminance = (0.299 * pixelColor.getRed() + 0.587 * pixelColor.getGreen() + 0.114 * pixelColor.getBlue());

					// Start a flood-fill if the pixel luminance is above the threshold (representing a star)
					if (luminance >= luminanceThreshold) {
						List<Point> pixelsInObject = floodFill(x, y, blackAndWhiteImage, visited);

						// Debug: Print object size
						System.out.println("Object size: " + pixelsInObject.size());

						int objectSize = pixelsInObject.size();

						if (objectSize >= minSize && objectSize <= maxSize) {
							objectCount++;

							double centerX = 0;
							double centerY = 0;

							for (Point pixel : pixelsInObject) {
								centerX += pixel.x;
								centerY += pixel.y;
							}

							centerX /= objectSize;
							centerY /= objectSize;

							double maxDistance = 0;

							for (Point pixel : pixelsInObject) {
								double distance = Math.sqrt(
										Math.pow(pixel.x - centerX, 2) + Math.pow(pixel.y - centerY, 2)
								);

								if (distance > maxDistance) {
									maxDistance = distance;
								}
							}

							// Create a bounding box (Circle) for the detected object
							Circle circle = new Circle(centerX, centerY, maxDistance, Color.BLUE);
							boundingBoxes.add(circle);
							objectCount++;
						}
					}
				}
			}
		}

		System.out.println("Total Objects Detected: " + objectCount);
		return boundingBoxes;
	}

	// Flood-fill algorithm to find connected pixels of an object
	private List<Point> floodFill(int startX, int startY, WritableImage image, boolean[][] visited) {
		int width = (int) image.getWidth();
		int height = (int) image.getHeight();
		List<Point> pixelsInObject = new ArrayList<>();
		List<Point> stack = new ArrayList<>();

		stack.add(new Point(startX, startY));

		while (!stack.isEmpty()) {
			Point currentPoint = stack.remove(stack.size() - 1);
			int x = currentPoint.x;
			int y = currentPoint.y;

			if (x >= 0 && x < width && y >= 0 && y < height && !visited[x][y]) {
				visited[x][y] = true;
				pixelsInObject.add(currentPoint);

				// Continue to explore neighbors, regardless of color
				checkAndAddNeighbor(stack, x + 1, y, image, visited);
				checkAndAddNeighbor(stack, x - 1, y, image, visited);
				checkAndAddNeighbor(stack, x, y + 1, image, visited);
				checkAndAddNeighbor(stack, x, y - 1, image, visited);
			}
		}

		return pixelsInObject;
	}

	// Method to draw numbers on the detected objects
	public void createNumbers(List<Circle> boundingBoxes, GraphicsContext gc) {
		int counter = 1; // Initialize the counter for unique numbers

		// Set the font and color for the numbers
		gc.setFont(Font.font("Arial", FontWeight.BOLD, 6));
		gc.setFill(Color.CYAN);

		for (Circle circle : boundingBoxes) {
			// Get the coordinates for drawing the number
			double centerX = circle.getCenterX();
			double centerY = circle.getCenterY();
			double radius = circle.getRadius();
			double x1 = centerX - radius + 5; // Adjust the x-coordinate for proper positioning
			double y1 = centerY + radius - 5; // Adjust the y-coordinate for proper positioning

			// Draw the number on the canvas at the specified coordinates
			gc.fillText(String.valueOf(counter), x1, y1);

			counter++; // Increment the counter for the next object
		}
	}

	// Helper method to check and add a neighbor pixel to the stack
	private void checkAndAddNeighbor(List<Point> stack, int x, int y, WritableImage image, boolean[][] visited) {
		int width = (int) image.getWidth();
		int height = (int) image.getHeight();

		if (x >= 0 && x < width && y >= 0 && y < height && !visited[x][y]) {
			Color pixelColor = image.getPixelReader().getColor(x, y);
			if (pixelColor.equals(Color.WHITE)) {
				stack.add(new Point(x, y));
			}
		}
	}

	// Inner class to represent a point with x and y coordinates
	private class Point {
		int x;
		int y;

		Point(int x, int y) {
			this.x = x;
			this.y = y;
		}
	}
}