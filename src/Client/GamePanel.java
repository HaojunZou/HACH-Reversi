package Client;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

class GamePanel extends JPanel implements MouseListener{
    private static final Color BACKGROUND_COLOR = new Color(60, 150, 60);	//set background color;
    private static final Color AVAILABLE_PLACE_COLOR = new Color(50, 50, 50);	//set available places color;
    private int[][] piece = new int[8][8];	//main 2d-array to save all pieces
    private Algorithm algorithm = new Algorithm();

    //Constructor
    GamePanel(){
        //initial pieces and available places for BLACK player
        addMouseListener(this);
        setBackground(BACKGROUND_COLOR);
    }

    /**
     * draw pieces
     */
    private void drawPieces() {
        Graphics g = this.getGraphics();
        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 8; j++) {
                if(piece[i][j] == 2){
                    g.setColor(AVAILABLE_PLACE_COLOR);
                    g.fillOval(72 + 50 * i, 72 + 50 * j, 6, 6);
                }
                else {
                    if (piece[i][j] == 0) {    //if there's piece in place
                        g.setColor(BACKGROUND_COLOR);
                    }
                    else if (piece[i][j] == 1) {    //BLACK
                        g.setColor(Color.BLACK);
                    } else if (piece[i][j] == -1) {    //WHITE
                        g.setColor(Color.WHITE);
                    }
                    g.fillOval(55 + 50 * i, 55 + 50 * j, 40, 40);
                }
            }
        }
    }

    @Override
    public void paint(Graphics g) {	//override the paint method
        super.paint(g);
        piece = algorithm.map();
        //initial player color in the corner
        if(algorithm.curPiece() == 1) {
            g.setColor(Color.BLACK);
            g.fillOval(5, 5, 40, 40);
        }
        if(algorithm.curPiece() == -1) {
            g.setColor(Color.WHITE);
            g.fillOval(5, 5, 40, 40);
        }

        //initial available places for BLACK player
        g.setColor(AVAILABLE_PLACE_COLOR);
        g.fillOval(172, 222, 6, 6);
        g.fillOval(222, 172, 6, 6);
        g.fillOval(322, 272, 6, 6);
        g.fillOval(272, 322, 6, 6);

        //draw horizontal lines
        for (int i = 0; i <= 8; i++) {
            g.drawLine(50, 50 * i + 50, 450, 50 * i + 50);
        }
        //draw vertical lines
        for (int i = 0; i <= 8; i++) {
            g.drawLine(50 * i + 50, 50, 50 * i + 50, 450);
        }
        //draw mark
        String[] xl = { "A", "B", "C", "D", "E", "F", "G", "H" };
        String[] yl = { "1", "2", "3", "4", "5", "6", "7", "8" };
        for (int i = 0; i < yl.length; i++) {
            g.drawString(xl[i], 50 * i + 72, 40);
            g.drawString(yl[i], 40, 50 * i + 80);
        }

        //draw pieces
        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 8; j++) {
                if (piece[i][j] != 0) {	//if there's piece in place
                    if (piece[i][j] == 1) {             //BLACK
                        g.setColor(Color.BLACK);
                        g.fillOval(55 + 50 * i, 55 + 50 * j, 40, 40);
                    } else if (piece[i][j] == -1) { 	//WHITE
                        g.setColor(Color.WHITE);
                        g.fillOval(55 + 50 * i, 55 + 50 * j, 40, 40);
                    } else if (piece[i][j] == 2) { 	    //available place
                        g.setColor(AVAILABLE_PLACE_COLOR);
                        g.fillOval(72 + 50 * i, 72 + 50 * j, 6, 6);
                    }
                }
            }
        }
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        int x = e.getX();	//get the x-coordinate when mouse clicked
        int y = e.getY();	//get the y-coordinate when mouse clicked
        // get the grille position as index
        int i = (x - 50) / 50;
        int j = (y - 50) / 50;
        System.out.println("Player is at: (" + i + ", " + j + ")");	// the index
        if (i < 0 || i >= 8 || j < 0 || j >= 8)
            System.out.println("Place out of range");
        else{
            algorithm.move(i, j);
            drawPieces();
        }
    }

    @Override
    public void mousePressed(MouseEvent e) {

    }

    @Override
    public void mouseReleased(MouseEvent e) {

    }

    @Override
    public void mouseEntered(MouseEvent e) {

    }

    @Override
    public void mouseExited(MouseEvent e) {

    }
}
