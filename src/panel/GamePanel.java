package panel;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
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
			checkApple();
			checkCollisions();
		}
		repaint();
	}

	public GamePanel() {
		random = new Random();
		this.setPreferredSize(new Dimension(_SCREEN_.get("width"), _SCREEN_.get("height")));
		this.setBackground(Color.black);
		this.setFocusable(true);
		this.addKeyListener(new SnakeKeyAdapter());

		startGame();
	}

	public void startGame() {
		newApple();
		this.running = true;
		timer = new Timer(_DELAY_, this);
		timer.start();
	}

	public void newApple() {
		appleX = random.nextInt((int) (_SCREEN_.get("width") / _UNIT_.get("size"))) * _UNIT_.get("size");
		appleY = random.nextInt((int) (_SCREEN_.get("height") / _UNIT_.get("size"))) * _UNIT_.get("size");
	}

	public void paintComponent(Graphics graph) {
		super.paintComponent(graph);
		draw(graph);
	}

	public void move() {
		for (int part = bodyParts; part > 0; part--) {
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

	public void checkApple() {
		if ((snakeX[0] == appleX) && (snakeY[0] == appleY)) {
			if (bodyParts < _FIELD_.get("units")) bodyParts++;
			appleEaten++;
			newApple();
		}
	}

	public void checkCollisions() {

		// Collision snake head with the body
		for (int part = bodyParts; part > 0; part--) {
			if ((snakeX[0] == snakeX[part]) && (snakeY[0] == snakeY[part])) {
				running = false;
				break;
			}
		}

		// Collision head with borders
		if (
				(snakeX[0] < 0)
						|| (snakeX[0] > _SCREEN_.get("width"))
						|| (snakeY[0] < 0)
						|| (snakeY[0] > _SCREEN_.get("height"))
		) running = false;

		if (!running) timer.stop();
	}

	public void gameOver(Graphics graph) {
	}

	public void draw(Graphics drawing) {

		// Create a copy of the Graphics instance
		Graphics2D drawing2D = (Graphics2D) drawing.create();

		// Set the stroke of the copy, not the original
		Stroke dashed = new BasicStroke(1, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL,
				0, new float[]{4}, 0);
		drawing2D.setStroke(dashed);

		for (int heightIndex = 0; heightIndex < _SCREEN_.get("height") / _UNIT_.get("size"); heightIndex++) {
			drawing2D.drawLine(
					0,
					heightIndex * _UNIT_.get("size"),
					_SCREEN_.get("width"),
					heightIndex * _UNIT_.get("size")
			);
		}
		for (int widthIndex = 0; widthIndex < _SCREEN_.get("width") / _UNIT_.get("size"); widthIndex++) {
			drawing2D.drawLine(
					widthIndex * _UNIT_.get("size"),
					0,
					widthIndex * _UNIT_.get("size"),
					_SCREEN_.get("height")
			);
		}
		// Get rid of the copy
		drawing2D.dispose();

		// Draw apple
		drawing.setColor(new Color(164, 90, 82));
		drawing.fillOval(appleX, appleY, _UNIT_.get("size"), _UNIT_.get("size"));

		// Draw snake
		for (int part = 0; part < bodyParts; part++) {
			if (part == 0) drawing.setColor(new Color(130, 220, 163));
			else drawing.setColor(new Color(168, 228, 160));
			drawing.fillRect(snakeX[part], snakeY[part], _UNIT_.get("size"), _UNIT_.get("size"));
		}
	}

	public class SnakeKeyAdapter extends KeyAdapter {
		@Override
		public void keyPressed(KeyEvent event) {
			switch (event.getKeyCode()) {
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
				default:
					break;
			}
		}
	}

	//	static final Pair<Integer, Integer> SCREEN_SIZE = new Pair<>(800, 600);
	static final Map<String, Integer> _SCREEN_ = Map.ofEntries(
			new AbstractMap.SimpleEntry<>("width", 800),
			new AbstractMap.SimpleEntry<>("height", 600)
	);
	static final Map<String, Integer> _UNIT_ = Map.ofEntries(
			new AbstractMap.SimpleEntry<>("size", 20)
	);
	static final Map<String, Integer> _FIELD_ = Map.ofEntries(
			new AbstractMap.SimpleEntry<>(
					"units",
					_SCREEN_.get("width") * _SCREEN_.get("height") / _UNIT_.get("size") * _UNIT_.get("size"))
	);

	static final int _DELAY_ = 85;

	int[] snakeX = new int[_SCREEN_.get("width") / _UNIT_.get("size")];
	int[] snakeY = new int[_SCREEN_.get("height") / _UNIT_.get("size")];

	int bodyParts = 6;
	int appleEaten;
	int appleX;
	int appleY;

	char direction = 'R';
	boolean running = false;

	Timer timer;
	Random random;
}
