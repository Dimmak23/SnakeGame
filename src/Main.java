import frame.GameFrame;

import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;
import java.io.IOException;

public class Main {
	public static void main(String[] args) throws UnsupportedAudioFileException, IOException, LineUnavailableException {
//		GameFrame frame = new GameFrame();
		new GameFrame();
//		java.net.URL url = ClassLoader.getSystemResource("../assets/icons/appIcon.png");
//		Toolkit kit = Toolkit.getDefaultToolkit();
//		Image img = kit.createImage(url);
//		frame.setIconImage(img);
	}
}
