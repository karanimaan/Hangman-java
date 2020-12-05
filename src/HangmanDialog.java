import javax.swing.*;
import java.awt.*;

class HangmanDialog extends JDialog {

    JButton restartButton;

    HangmanDialog (HangmanWindow frame, String title) {
        super(frame, title);
        setLayout(new GridLayout(3, 1));
        setIconImage(frame.images[0].getImage());


        String message = null;
        JLabel answerLabel = new JLabel();
        switch (title) {
            case "Win" -> message = "You win!";
            case "Loss" -> {
                message = "Game over.";
                answerLabel.setText("The word is: " + frame.word);
            }
        }
        JLabel gameOverLabel = new JLabel(message, SwingConstants.CENTER);
        gameOverLabel.setFont(new Font(Font.SERIF, Font.PLAIN, 30));
        answerLabel.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 20));
        setSize(300, 200);
        setLocationRelativeTo(null);
        restartButton = new JButton("Restart Game");
        restartButton.setActionCommand(frame.restartButton.getActionCommand());
        restartButton.addActionListener(frame.new OnClickListener());
        restartButton.setPreferredSize(new Dimension(100, 50));
        add(gameOverLabel);
        add(answerLabel);
        add(restartButton);
        getRootPane().setDefaultButton(restartButton);
    }
}