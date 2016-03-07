package Server;


public class Algorithm {
    private static final int BLACK = 1;     //player BLACK
    private static final int WHITE = -1;	//player WHITE
    private static final int AVAILABLE = 2;	//current player available place
    private int[][] piece = new int[8][8];	//main 2d-array to save all pieces
    private int curPiece = BLACK; // current piece

    Algorithm(){
        piece[3][3] = WHITE;
        piece[3][4] = BLACK;
        piece[4][3] = BLACK;
        piece[4][4] = WHITE;
        piece[3][2] = AVAILABLE;
        piece[2][3] = AVAILABLE;
        piece[5][4] = AVAILABLE;
        piece[4][5] = AVAILABLE;
    }

    int[][] map(){
        return piece;
    }

    int curPiece(){
        return curPiece;
    }
}
