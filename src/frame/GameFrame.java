package frame;

import panel.GamePanel;

import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;
import javax.swing.*;
import java.io.IOException;

/**
 * The JFrame class is slightly incompatible with Frame.
 * Like all other JFC/Swing top-level containers, a JFrame contains a JRootPane as its only child.
 * The content pane provided by the root pane should, as a rule,
 * contain all the non-menu components displayed by the JFrame.
 * This is different from the AWT Frame case. As a convenience add and its variants,
 * remove and setLayout have been overridden to forward to the contentPane as necessary.
 */

public class GameFrame extends JFrame {
	public GameFrame() throws UnsupportedAudioFileException, IOException, LineUnavailableException {

		this.add(new GamePanel());
		this.setTitle("Snake Game by DimmaK, 18 march 2023, all rights reserved.");
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		this.setResizable(false);
		this.pack();
		this.setVisible(true);
		this.setLocationRelativeTo(null);

		ImageIcon applicationIcon = new ImageIcon("assets/icons/appIcon.png");
//		ImageIcon applicationIcon = new ImageIcon(
//				Objects.requireNonNull(getClass().getClassLoader().getResource(
//						"assets/icons/appIcon.png")));
		this.setIconImage(applicationIcon.getImage());
	}
}
