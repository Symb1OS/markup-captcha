package ru.namibios;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;

public class DataMarkup extends JFrame {

    private static final String UNSORTED_IMG = "resources/captcha/unsorted/";
    private static final String SORTER_IMG = "resources/captcha/sorted/image/";
    private static final String SORTER_CSV = "resources/captcha/sorted/value/";

    private int count;

    private JLabel captchaImg = new JLabel();

    private JTextField captchaText = new JTextField();

    private JButton ok = new JButton("Разметить");

    private void loadCount() {
        File[] array = new File(SORTER_IMG).listFiles();

        if (array != null && array.length != 0) {

            Integer max = Arrays.stream(array)
                    .map(file ->
                            Integer.valueOf(file.getName().replace(".jpg", ""))
                    ).max((Integer::compareTo)).get();

            count = max + 1;

        } else {
            count = 0;
        }

    }

    private boolean next;

    public DataMarkup() {
        setTitle("Data markup");

        setLocationRelativeTo(null);
        setSize(500, 200);
        setAlwaysOnTop(true);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        Container contentPane = getContentPane();
        contentPane.setLayout(new BorderLayout());

        captchaText.setText("");
        captchaText.setFont(new Font(Font.SERIF,Font.PLAIN, 16));

        contentPane.add(BorderLayout.NORTH, captchaImg);
        contentPane.add(BorderLayout.CENTER,captchaText);
        contentPane.add(BorderLayout.SOUTH, ok);

        loadCount();

        ok.addActionListener(e -> {

            new Thread( () -> {

                String text = captchaText.getText();

                if (!text.matches("[w,a,s,d]{2,10}")) {
                    System.out.println("Картинка не размечена или используются запрещенные символы");
                    return;
                }

                try {

                    String csv = convertToCSV(text);
                    Files.writeString(Path.of(SORTER_CSV + count + ".csv"), csv);

                } catch (IOException ioException) {
                    ioException.printStackTrace();
                }

                captchaText.setText("");
                captchaText.grabFocus();
                captchaText.requestFocus();

                next = true;

            }).start();

        });

        setResizable(false);
        setVisible(true);

        File folder = new File(UNSORTED_IMG);
        for (File file: folder.listFiles()) {

            System.out.println(file.getName());

            if (file.isFile()) {
                captchaImg.setIcon(new ImageIcon(String.valueOf(file.getAbsoluteFile())));
            }

            while (true) {

                if (next) {

                    boolean renamed = file.renameTo(new File(SORTER_IMG + count + ".jpg"));
                    if (renamed) {
                        count++;
                    }

                    next = false;
                    break;
                }

                sleep(1000);

            }

        }

        System.out.println("Файлы закончились");
        System.exit(0);

    }

    private String convertToCSV(String text) {
        text = text.toLowerCase();
        int length = text.length();

        String value = text
                .replaceAll("w", "0,")
                .replaceAll("s", "1,")
                .replaceAll("a", "2,")
                .replaceAll("d", "3,");

        for (int i = 0; i < 10 - length; i++) {
            value += "4,";
        }

        value = value.substring(0, value.length() - 1);
        return value;
    }

    private void sleep(int ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
    public static void main(String[] args) {
        new DataMarkup();

    }

}