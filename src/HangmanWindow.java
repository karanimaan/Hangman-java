import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.event.MouseInputAdapter;
import javax.swing.text.AbstractDocument;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DocumentFilter;
import java.awt.event.*;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.awt.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Stream;


public class HangmanWindow extends JFrame {

    private final JComboBox difficultyComboBox;
    private double drawingState;
    ImageIcon[] images;
    JButton restartButton;
    private JPanel outputPanel, inputPanel;
    private JLabel wordLabel, imageLabel, guessesRemainingLabel;
    private JTextField textField;
    private double guessesRemaining;
    private JTextArea wrongGuessesTextArea;
    Word word;
    private String hiddenWord, wrongGuesses;
    private char[] charArray, hiddenCharArray;
    private Font textFont;
    private JButton guessButton;
    private HangmanDialog dialog;
    private char guessedLetter;
    private String line;
    private int difficulty;
    private int totalGuesses;

    private  class MyWindowListener extends WindowAdapter {
        @Override
        public void windowOpened(WindowEvent e) {
            textField.grabFocus();
            textField.selectAll();
        }
    }

    private class HangmanMouseListener extends MouseInputAdapter {
        @Override
        public void mouseClicked(MouseEvent e) {
            super.mouseClicked(e);
            textField.grabFocus();
            textField.selectAll();
        }
    }

    class OnClickListener implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            if (e.getActionCommand().equals(guessButton.getActionCommand())) {
                guessedLetter = textField.getText().toLowerCase().charAt(0);
                if (Word.legalCharacter(guessedLetter))
                    guess();
            }
            else if (e.getActionCommand().equals(restartButton.getActionCommand())) {
                restart();
            }
        }


        private void guess() {
            boolean correctGuess = false;
            for (int i = 0; i < word.length(); i++) {
                if (guessedLetter == charArray[i]) {
                    hiddenCharArray[i] = word.charAt(i);
                    correctGuess = true;
                }
            }
            if (correctGuess) {
                wordLabel.setText(String.valueOf(hiddenCharArray));
                if (wordLabel.getText().indexOf('-') == -1) {
                    dialog = new HangmanDialog(HangmanWindow.this, "Win");
                    dialog.setVisible(true);
                }
            }
            else if (guessesRemaining > 0) {
                wrongGuesses += " " + guessedLetter;
                wrongGuessesTextArea.setText(wrongGuesses);

                guessesRemainingLabel.setText(String.valueOf((int) --guessesRemaining));

                drawingState += 10.0 / totalGuesses;
                if (drawingState >= 10) {
                    if (guessesRemaining > 0)
                        drawingState = 9;
                    else
                        drawingState = 10;
                }
                imageLabel.setIcon(images[(int) Math.round(drawingState)]);

                if (guessesRemaining == 0) {
                    dialog = new HangmanDialog(HangmanWindow.this, "Loss");
                    dialog.setVisible(true);
                }
            }
            else {
                guessButton.setEnabled(false);
            }
        }
    }

    public HangmanWindow() {
        super();
        setTitle("Hangman");
        setSize(1000, 700);
        setLayout(new BorderLayout());
        setLocationRelativeTo(null);
        images = new ImageIcon[11];
        for (int i = 0; i < images.length; i++) {
            images[i] = new ImageIcon("hangmandrawings/state" + (i+1) + ".GIF");
        }
        setIconImage(images[10].getImage());

        addOutputPanel();
        addInputPanel();

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        addWindowListener(new MyWindowListener());
        addMouseListener(new HangmanMouseListener());

        Border border = BorderFactory.createEmptyBorder(10, 10, 10, 10);

        JPanel leftPanel = new JPanel(new BorderLayout());
        textFont = new Font(Font.SANS_SERIF, Font.PLAIN, 20);
        guessesRemainingLabel = new JLabel(String.valueOf(guessesRemaining), SwingConstants.CENTER);
        JLabel guessesRemainingStringLabel = new JLabel("Guesses Remaining: ");
        JPanel leftCenterPanel = new JPanel(new BorderLayout());
        leftCenterPanel.add(guessesRemainingStringLabel, BorderLayout.CENTER);
        leftCenterPanel.add(guessesRemainingLabel, BorderLayout.EAST);
        guessesRemainingStringLabel.setFont(textFont);
        guessesRemainingLabel.setFont(textFont);
        leftPanel.setBorder(border);
        leftPanel.add(leftCenterPanel, BorderLayout.CENTER);

        difficultyComboBox = new JComboBox(new String[] {"Amateur", "Semi-pro", "Professional", "Legendary"});
        ImageIcon restartIcon = new ImageIcon(new ImageIcon("restart.jpg").getImage().getScaledInstance(50, 50,
                Image.SCALE_SMOOTH));
        restartButton = new JButton(restartIcon);
        restartButton.setActionCommand("Restart");
        restartButton.addActionListener(new OnClickListener());
        restartButton.setPreferredSize(new Dimension(50, 50));
        JPanel topLeftPanel = new JPanel(new FlowLayout());
        topLeftPanel.add(new JPanel());
        topLeftPanel.add(difficultyComboBox);
        topLeftPanel.add(restartButton);
        leftPanel.add(topLeftPanel, BorderLayout.NORTH);
        add(leftPanel, BorderLayout.WEST);

        wrongGuesses = "";
        wrongGuessesTextArea = new JTextArea(1, 1); //ignores size?
        wrongGuessesTextArea.setEditable(false);
        wrongGuessesTextArea.setLineWrap(true);
        wrongGuessesTextArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 40));
        JPanel rightPanel = new JPanel(new BorderLayout());
        rightPanel.setBorder(border);
        rightPanel.add(wrongGuessesTextArea, BorderLayout.CENTER);
        add(rightPanel, BorderLayout.EAST);

        restart();
        difficulty = 0;

        difficultyComboBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                guessesRemaining += 2*difficulty;
                difficulty = difficultyComboBox.getSelectedIndex();
                totalGuesses = totalGuesses - 2*difficulty;
                guessesRemaining -= 2*difficulty;
                if (guessesRemaining <= 0)
                    guessesRemaining = 1;
                guessesRemainingLabel.setText(String.valueOf((int) Math.ceil(guessesRemaining)));
            }
        });

        textField.setInputVerifier(new InputVerifier() {
            @Override
            public boolean verify(JComponent input) {
                return false;
            }
        });

    }

    private void restart() {
        if (dialog != null)
            dialog.dispose();
        generateWord();
        wordLabel.setText(hiddenWord);
        difficulty = difficultyComboBox.getSelectedIndex();
        totalGuesses = 10 - 2*difficulty;
        guessesRemaining = totalGuesses;
        guessesRemainingLabel.setText(String.valueOf((int) guessesRemaining));
        drawingState = 0;
        imageLabel.setIcon(images[0]);
        wrongGuesses = "";
        wrongGuessesTextArea.setText(wrongGuesses);
        guessButton.setEnabled(true);
        textField.setText("Enter Letter");
        textField.setColumns(10);

        textField.grabFocus();
        textField.selectAll();
    }


    private void addOutputPanel() {
        outputPanel = new JPanel(new GridLayout(2, 1)); //Initialize in main contructor?

        ImageIcon hangmanImage = new ImageIcon("hangmandrawings/state1.GIF");
        imageLabel = new JLabel(hangmanImage);
        outputPanel.add(imageLabel);

        wordLabel = new JLabel(hiddenWord, SwingConstants.CENTER);
        wordLabel.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 50));
        outputPanel.add(wordLabel);

        add(outputPanel, BorderLayout.CENTER);

       // outputPanel.setPreferredSize(new Dimension(500, 400));
    }

    private void addInputPanel() {
        inputPanel = new JPanel(new FlowLayout());

        textField = new JTextField();
        textField.setFont(new Font(Font.SERIF, 0, 20));
        ((AbstractDocument) textField.getDocument()).setDocumentFilter(new DocumentFilter(){
            @Override
            public void replace(FilterBypass fb, int offset, int length, String text, AttributeSet attrs) throws BadLocationException {
                super.replace(fb, 0, fb.getDocument().getLength(), text, attrs);
            }

        });

        inputPanel.add(textField);

        guessButton = new JButton("Guess");
        guessButton.addActionListener(new OnClickListener());
        inputPanel.add(guessButton);

        add(inputPanel, BorderLayout.SOUTH);
       // inputPanel.setPreferredSize(new Dimension(500, 100));
        getRootPane().setDefaultButton(guessButton);
    }

    private void generateWord() {
        final int FIRST_LINE_NUMBER = 66;
        final int LAST_LINE_NUMBER = 77678;
        do {
            int randomLineNumber = ThreadLocalRandom.current().nextInt(FIRST_LINE_NUMBER, LAST_LINE_NUMBER + 1);
            File file = new File("Dictionary.txt");
            line = null;
            try (Stream<String> fileStream = Files.lines(file.toPath())) {
                line = fileStream.skip(randomLineNumber).findFirst().get();
            } catch (IOException exception) {
                exception.printStackTrace();
            }
        } while (!Word.legalWordPhrase(line));

        word = new Word(line);
        hiddenWord = "-".repeat(word.length());
        charArray = word.toString().toLowerCase().toCharArray();
        hiddenCharArray = hiddenWord.toCharArray();
    }


}
