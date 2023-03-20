package panel;

import javax.sound.sampled.*;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.IOException;
import java.util.AbstractMap;
import java.util.Map;
import java.util.Random;

/**
 * The JPanel class provides general-purpose containers for lightweight components.
 * By default, panels do not add colors to anything except their own background;
 * however, you can easily add borders to them and otherwise customize their painting.
 * Details can be found in Performing Custom Painting.
 * In many types of look and feel, panels are opaque by default.
 * Opaque panels work well as content panes and can help with painting efficiently,
 * as described in Using Top-Level Containers.
 * You can change a panel's transparency by invoking the setOpaque method.
 * A transparent panel draws no background, so that any components underneath show through.
 * <p>
 * public interface ActionListener
 * extends EventListener
 * The listener interface for receiving action events.
 * The class that is interested in processing an action event implements this interface,
 * and the object created with that class is registered with a component,
 * using the component's addActionListener method.
 * When the action event occurs, that object's actionPerformed method is invoked.
 */

public class GamePanel extends JPanel implements ActionListener {

	/**
	 * Invoked when an action occurs.
	 *
	 * @param event the event to be processed
	 */
	@Override
	public void actionPerformed(ActionEvent event) {
		if (running) {
			move();
			try {
				checkApple();
			} catch (IOException | LineUnavailableException | UnsupportedAudioFileException e) {
				throw new RuntimeException(e);
			}
			try {
				checkCollisions();
			} catch (LineUnavailableException | IOException e) {
				throw new RuntimeException(e);
			}
		}
		repaint();
	}

	public GamePanel() throws UnsupportedAudioFileException, IOException, LineUnavailableException {
		//
		random = new Random();
		this.setPreferredSize(new Dimension(
				_FIELD_.get("width"),
				_WIDGET_.get("height") + _FIELD_.get("height"))
		);
		this.setBackground(Color.black);
		this.setFocusable(true);
		this.addKeyListener(new SnakeKeyAdapter());

//		startGame();
		// Prepare sounds
		themePlayer = AudioSystem.getClip();
		greetInputStream = AudioSystem.getAudioInputStream(new File("assets/sounds/greet.wav"));
		themeInputStream = AudioSystem.getAudioInputStream(new File("assets/sounds/theme.wav"));
		gameOverInputStream = AudioSystem.getAudioInputStream(new File("assets/sounds/game_over.wav"));

		themePlayer.open(greetInputStream);
		themePlayer.setFramePosition(0);
		themePlayer.start();
	}

	public void startGame() throws LineUnavailableException, IOException, InterruptedException {
//		this.running = true;
		newApple();
		timer = new Timer(_DELAY_, this);
		timer.start();

		themePlayer.stop();
		themePlayer.close();

		themePlayer.open(themeInputStream);
		themePlayer.setFramePosition(0);
		themePlayer.loop(Clip.LOOP_CONTINUOUSLY);
		FloatControl gainControl =
				(FloatControl) themePlayer.getControl(FloatControl.Type.MASTER_GAIN);
		gainControl.setValue(-20.0f); // Reduce volume by 20 decibels.
		themePlayer.start();
	}

	public void newApple() {
		appleX = random.nextInt((int) (_FIELD_.get("width") / _UNIT_.get("size"))) * _UNIT_.get("size");
		appleY = random.nextInt((int) (_FIELD_.get("height") / _UNIT_.get("size"))) * _UNIT_.get("size");
	}

	public void paintComponent(Graphics graph) {
		super.paintComponent(graph);
		draw(graph);
	}

	public void move() {
		for (int part = bodyParts-1; part > 0; part--) {
			snakeX[part] = snakeX[part - 1];
			snakeY[part] = snakeY[part - 1];
		}

		switch (direction) {
			case 'U':
				snakeY[0] = snakeY[0] - _UNIT_.get("size");
				break;
			case 'D':
				snakeY[0] = snakeY[0] + _UNIT_.get("size");
				break;
			case 'L':
				snakeX[0] = snakeX[0] - _UNIT_.get("size");
				break;
			case 'R':
				snakeX[0] = snakeX[0] + _UNIT_.get("size");
				break;
			default:
				System.out.println("Unpredicted error");
		}
	}

	public void checkApple() throws LineUnavailableException, IOException, UnsupportedAudioFileException {
		if ((snakeX[0] == appleX) && (snakeY[0] == appleY)) {
			if (bodyParts < _QTY_.get("units")) {

				AudioInputStream catchInputStream = AudioSystem.getAudioInputStream(
						new File("assets/sounds/catch.wav"));
				Clip clip = AudioSystem.getClip();
				// Play catch sound
				clip.open(catchInputStream);
				clip.setFramePosition(0);
				clip.start();

				bodyParts++;
				switch (direction) {
					case 'U':
						snakeY[bodyParts-1] = snakeY[bodyParts-2] + _UNIT_.get("size");
						snakeX[bodyParts-1] = snakeX[bodyParts-2];
						break;
					case 'D':
						snakeY[bodyParts-1] = snakeY[bodyParts-2] - _UNIT_.get("size");
						snakeX[bodyParts-1] = snakeX[bodyParts-2];
						break;
					case 'L':
						snakeY[bodyParts-1] = snakeY[bodyParts-2];
						snakeX[bodyParts-1] = snakeX[bodyParts-2] + _UNIT_.get("size");
						break;
					case 'R':
						snakeY[bodyParts-1] = snakeY[bodyParts-2];
						snakeX[bodyParts-1] = snakeX[bodyParts-2] - _UNIT_.get("size");
						break;
					default:
						System.out.println("Unpredicted error");
				}
			}
			appleEaten++;
			newApple();
		}
	}

	public void checkCollisions() throws LineUnavailableException, IOException {

		// Collision snake head with the body
		for (int part = bodyParts-1; part > 0; part--) {
			if ((snakeX[0] == snakeX[part]) && (snakeY[0] == snakeY[part])) {
				running = false;
				gameOver = true;
				break;
			}
		}

		// Collision head with borders
		if (
				(snakeX[0] < 0)
						|| (snakeX[0] > _FIELD_.get("width"))
						|| (snakeY[0] < 0)                     // no need offset from roof widget
						|| (snakeY[0] > _FIELD_.get("height")) // no need to set offset from roof widget
		) {
			running = false;
			gameOver = true;
		}

		//
		if (!running) {
			timer.stop();

			themePlayer.stop();
			themePlayer.close();

			themePlayer.open(gameOverInputStream);
			themePlayer.setFramePosition(0);
			FloatControl gainControl =
					(FloatControl) themePlayer.getControl(FloatControl.Type.MASTER_GAIN);
			gainControl.setValue(-20.0f); // Reduce volume by 20 decibels.
			themePlayer.start();
		}
	}

	public void draw(Graphics drawing) {
		if (!running && !gameOver){
			welcomePageRender(drawing);
		}
		else if (running){
			gamePlayRender(drawing);
		}
		else{
			gameOverRender(drawing);
		}
	}

	public void welcomePageRender(Graphics drawing) {
		//
		drawTextLine(
				drawing,
				Color.blue,
				Font.BOLD | Font.ITALIC,
				50,
				"Welcome to the Snake game",
				_FIELD_.get("width"),
				_FIELD_.get("height") + _WIDGET_.get("height")
		);
		//
		final int fontSize = 24;
		drawTextLine(
				drawing,
				Color.red,
				Font.BOLD | Font.ITALIC,
				fontSize,
				"Please, press ENTER to start the game...",
				_FIELD_.get("width"),
				_FIELD_.get("height") + _WIDGET_.get("height") + 3 * fontSize / 2 + _WIDGET_.get("height")
		);
	}

	public void gamePlayRender(Graphics drawing) {

		// Create a copy of the Graphics instance
		Graphics2D drawing2D = (Graphics2D) drawing.create();

		// Set the stroke of the copy, not the original
		Stroke dashed = new BasicStroke(1, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL,
				0, new float[]{4}, 0);
		drawing2D.setStroke(dashed);

		// Draw grid of the game
		for (int heightIndex = 0; heightIndex < _FIELD_.get("height") / _UNIT_.get("size"); heightIndex++) {
			drawing2D.drawLine(
					0,
					_WIDGET_.get("height") + heightIndex * _UNIT_.get("size"), // offset from roof widget
					_FIELD_.get("width"),
					_WIDGET_.get("height") + heightIndex * _UNIT_.get("size")  // offset from roof widget
			);
		}
		for (int widthIndex = 0; widthIndex < _FIELD_.get("width") / _UNIT_.get("size"); widthIndex++) {
			drawing2D.drawLine(
					widthIndex * _UNIT_.get("size"),
					_WIDGET_.get("height"),                        // offset from roof widget
					widthIndex * _UNIT_.get("size"),
					_WIDGET_.get("height") + _FIELD_.get("height") // offset from roof widget
			);
		}

		// Get rid of the drawing copy
		drawing2D.dispose();

		// Draw apple
		drawing.setColor(new Color(164, 90, 82));
		drawing.fillOval(
				appleX, appleY + _WIDGET_.get("height"),
				_UNIT_.get("size"), _UNIT_.get("size")
		);

		// Draw snake
		for (int part = 0; part < bodyParts; part++) {
			if (part == 0) drawing.setColor(new Color(80, 220, 120));
			else drawing.setColor(new Color(168, 228, 160));
			drawing.fillRect(
					snakeX[part], snakeY[part] + _WIDGET_.get("height"),
					_UNIT_.get("size"), _UNIT_.get("size")
			);
		}

		// And also score of the game
		final int fontSize = 36;
		drawTextLine(
				drawing,
				new Color(168, 228, 160),
				Font.BOLD | Font.ITALIC,
				fontSize,
				"Score: " + appleEaten,
				_FIELD_.get("width"),
				fontSize / 2 + _WIDGET_.get("height")
		);
	}

	public void gameOverRender(Graphics drawing) {
		//
		drawTextLine(
				drawing,
				Color.red,
				Font.BOLD | Font.ITALIC,
				75,
				"Game over",
				_FIELD_.get("width"),
				_FIELD_.get("height") + _WIDGET_.get("height")
		);
		//
		final int fontSize = 24;
		drawTextLine(
				drawing,
				Color.red,
				Font.BOLD | Font.ITALIC,
				fontSize,
				"Your final score is: " + appleEaten + ".",
				_FIELD_.get("width"),
				_FIELD_.get("height") + _WIDGET_.get("height") + 3 * fontSize / 2 + _WIDGET_.get("height")
		);
	}

	public void drawTextLine(
			Graphics drawing,
			Color textColor,
			int fontOption,
			int fontSize,
			String text,
			int boxWidth, int boxHeight) {

		// Game over text
		drawing.setColor(textColor);

		//		URL fontUrl = new URL(
//				"https://github.com/indvd00m/
//				graphics2d-drawstring-test/blob/master/src/test/resources/
//				fonts/DejaVuSansMono/DejaVuSansMono.ttf?raw=true");
//		Font font = Font.createFont(Font.TRUETYPE_FONT, fontUrl.openStream());
//		float fontSizeInPixels = 90f;
//		font = font.deriveFont(fontSizeInPixels);
//		drawing.setFont(font);

		// TODO: Maybe find some other font
		drawing.setFont(new Font("Monospaced", fontOption, fontSize));
		FontMetrics metrics = getFontMetrics(drawing.getFont());
		drawing.drawString(
				text,
				(boxWidth - metrics.stringWidth(text)) / 2,
				(boxHeight) / 2
		);
	}

	public class SnakeKeyAdapter extends KeyAdapter {
		@Override
		public void keyPressed(KeyEvent event) {
			switch (event.getKeyCode()) {
				case KeyEvent.VK_ENTER:
					if (!running && !gameOver) {
						running = true;
						try {
							startGame();
						} catch (LineUnavailableException | IOException | InterruptedException e) {
							throw new RuntimeException(e);
						}
					}
					break;
				case KeyEvent.VK_LEFT:
					if (direction != 'R') direction = 'L';
					break;
				case KeyEvent.VK_RIGHT:
					if (direction != 'L') direction = 'R';
					break;
				case KeyEvent.VK_UP:
					if (direction != 'D') direction = 'U';
					break;
				case KeyEvent.VK_DOWN:
					if (direction != 'U') direction = 'D';
					break;
				case KeyEvent.VK_ESCAPE: {
					//TODO: find any better solution to terminate
					System.exit(0);
					break;
				}
				default:
					break;
			}
		}
	}

	//	static final Pair<Integer, Integer> SCREEN_SIZE = new Pair<>(800, 600);
	static final Map<String, Integer> _WIDGET_ = Map.ofEntries(
			new AbstractMap.SimpleEntry<>("height", 50)
	);

	static final Map<String, Integer> _FIELD_ = Map.ofEntries(
			new AbstractMap.SimpleEntry<>("width", 800),
			new AbstractMap.SimpleEntry<>("height", 600)
	);
	static final Map<String, Integer> _UNIT_ = Map.ofEntries(
			new AbstractMap.SimpleEntry<>("size", 20)
	);
	static final Map<String, Integer> _QTY_ = Map.ofEntries(
			new AbstractMap.SimpleEntry<>(
					"units",
					_FIELD_.get("width") * _FIELD_.get("height") / _UNIT_.get("size") * _UNIT_.get("size"))
	);

	static final int _DELAY_ = 80;

	int[] snakeX = new int[_FIELD_.get("width") / _UNIT_.get("size")];
	int[] snakeY = new int[_FIELD_.get("height") / _UNIT_.get("size")];

	int bodyParts = 6;
	int appleEaten;
	int appleX;
	int appleY;

	char direction = 'R';
	boolean running = false;
	boolean gameOver = false;
	Timer timer;
	Random random;

	// SOUNDS
	AudioInputStream greetInputStream;
	AudioInputStream themeInputStream;
	AudioInputStream gameOverInputStream;
	Clip themePlayer;
}
