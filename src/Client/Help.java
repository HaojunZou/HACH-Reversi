package Client;

import javax.swing.*;
import java.io.*;

public class Help extends JFrame{
    public Help(){
        this.setVisible(true);
        this.setSize(700, 500);
        this.setTitle("Help");
        this.setResizable(false);
        JTextArea helpText = new JTextArea();

        String path = new File("").getAbsolutePath() + "/Help.txt";
        File file = new File(path);

        try {
            FileReader fr = new FileReader(file);
            BufferedReader bw = new BufferedReader(fr);
            String line;
            while((line=bw.readLine()) != null) {
                helpText.append(line + "\n");
            }
            fr.close();
            bw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        helpText.setEditable(false);
        this.add(helpText);
    }
}
