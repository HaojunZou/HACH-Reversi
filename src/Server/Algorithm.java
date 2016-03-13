package Server;

class Algorithm {
    private static final int BLACK = 1;     //player BLACK
    private static final int WHITE = -1;	//player WHITE
    private static final int AVAILABLE = 2;	//current player available place
    private int[][] piece = new int[8][8];	//main 2d-array to save all pieces
    private int currentPlayer = 1; // current piece
    private boolean legalMove = false;  //if the current move is legal

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

    int[][] getCurrentMap(){
        return piece;
    }

    int getCurrentPlayer(){
        return currentPlayer;
    }

    int getCountBlack(){ return countScore(1); }

    int getCountWhite(){ return countScore(-1); }

    boolean checkLegal() { return legalMove; }

    //counter the score
    private int countScore(int cur){
        int score = 0;
        for(int xe=0; xe<8; xe++){
            for(int ye=0; ye<8; ye++){
                if(piece[xe][ye] == cur){
                    score++;
                }
            }
        }
        return score;
    }

    /**
     * move function
     * @param x
     * @param y
     * @return 0 if no one can move, 1 if opponent player should pass, 2 if can switch user, 3 if move is illegal
     */
    int move(int x, int y){
        //using current piece face and grille position check the location
        if (checkLocation(currentPlayer, x, y, true)) {    //if the location is ok
//            if(piece[x][y] == AVAILABLE)
//                return 3;
            piece[x][y] = currentPlayer;
            currentPlayer *= -1;    //switch the player
            if (shouldPass(currentPlayer)) {  //if the opponent player has not available place
                if (shouldPass(-currentPlayer)) {    //check self should pass
                    currentPlayer = 64;
                    return 0;
                } else {    //if not, switch player
                    System.out.println("Pass!");
                    currentPlayer *= -1;
                    return 1;
                }
            }else
                return 2;
        }
//        else
//            legalMove = false;
        return 3;
    }

    /**
     * draw available place
     * @param cur: player face
     * @return true if current player has no available place
     */
    private boolean shouldPass(int cur){
        //restore all available places from last round to empty
        for(int xe=0; xe<8; xe++){
            for(int ye=0; ye<8; ye++){
                if(piece[xe][ye]!=1 && piece[xe][ye]!=-1){
                    piece[xe][ye] = 0;
                }
            }
        }
        //check if the current player has available places
        int curAva = 0;
        for(int xa=0; xa<8; xa++){
            for(int ya=0; ya<8; ya++){
                if(piece[xa][ya]==0){
                    if (checkLocation(cur, xa, ya, false)){
                        piece[xa][ya] = 2;
                        curAva++;
                    }
                }
            }
        }
        //if the current player has not available places, then return true;
        return curAva == 0;
    }

    private boolean checkLocation(int cur, int i, int j, boolean execute) {
        return !(piece[i][j] == BLACK || piece[i][j] == WHITE) && availablePlace(cur, i, j, execute);
//        return availablePlace(cur, i, j, execute);
    }

    /**
     * validate the place beside the current position
     * @param cur: player face
     * @param i: x-coordinate
     * @param j: y-coordinate
     * @param execute: if need to be executed
     * @return true if this place is available for current player
     */
    private boolean availablePlace(int cur, int i, int j, boolean execute) {
        if(execute)
            System.out.println("Player: " + (cur==1 ? "Black" : "White"));

        boolean result = false;

        //NW corner
        //those places in map called north west corner
        if(i>=0 && i<2 && j>=0 && j<2){
            //times of search actions been executed
            int nw_corner_search_action = 0;
            //follow this direction, if the piece beside the current place is opponent piece
            if(piece[i+1][j]==-cur){
                //do a piece search in this direction. if at least one friend has been found,
                //it will retrun true, if not, it will return false.
                if(searchEnd(cur, i, j, "TO_EAST", execute))
                    //if search has been executed, search actions increasing by one.
                    nw_corner_search_action ++;
            }
            if(piece[i+1][j+1]==-cur){
                if(searchEnd(cur, i, j, "TO_SE", execute))
                    nw_corner_search_action ++;
            }
            if(piece[i][j+1]==-cur){
                if(searchEnd(cur, i, j, "TO_SOUTH", execute))
                    nw_corner_search_action ++;
            }
            //at least one search actions has been executed, it will return true, it means this place is available.
            result = nw_corner_search_action > 0;
        }

        //NE corner
        if(i>5 && i<=7 && j>=0 && j<2){
            int ne_corner_search_action = 0;
            if(piece[i][j+1]==-cur){
                if(searchEnd(cur, i, j, "TO_SOUTH", execute))
                    ne_corner_search_action ++;
            }
            if(piece[i-1][j+1]==-cur){
                if(searchEnd(cur, i, j, "TO_SW", execute))
                    ne_corner_search_action ++;
            }
            if(piece[i-1][j]==-cur){
                if(searchEnd(cur, i, j, "TO_WEST", execute))
                    ne_corner_search_action ++;
            }
            result = ne_corner_search_action > 0;
        }

        //SE corner
        if(i>5 && i<=7 && j>5 && j<=7){
            int se_corner_search_action = 0;
            if(piece[i-1][j]==-cur){
                if(searchEnd(cur, i, j, "TO_WEST", execute))
                    se_corner_search_action ++;
            }
            if(piece[i-1][j-1]==-cur){
                if(searchEnd(cur, i, j, "TO_NW", execute))
                    se_corner_search_action ++;
            }
            if(piece[i][j-1]==-cur){
                if(searchEnd(cur, i, j, "TO_NORTH", execute))
                    se_corner_search_action ++;
            }
            result = se_corner_search_action > 0;
        }

        //SW corner
        if(i>=0 && i<2 && j>5 && j<=7){
            int sw_corner_search_action = 0;
            if(piece[i][j-1]==-cur){
                if(searchEnd(cur, i, j, "TO_NORTH", execute))
                    sw_corner_search_action ++;
            }
            if(piece[i+1][j-1]==-cur){
                if(searchEnd(cur, i, j, "TO_NE", execute))
                    sw_corner_search_action ++;
            }
            if(piece[i+1][j]==-cur){
                if(searchEnd(cur, i, j, "TO_EAST", execute))
                    sw_corner_search_action ++;
            }
            result = sw_corner_search_action > 0;
        }

        //NORTH edge
        if(i>1 && i<6 && j>=0 && j<2){
            int north_edge_search_action = 0;
            if(piece[i+1][j]==-cur){
                if(searchEnd(cur, i, j, "TO_EAST", execute))
                    north_edge_search_action ++;
            }
            if(piece[i+1][j+1]==-cur){
                if(searchEnd(cur, i, j, "TO_SE", execute))
                    north_edge_search_action ++;
            }
            if(piece[i][j+1]==-cur){
                if(searchEnd(cur, i, j, "TO_SOUTH", execute))
                    north_edge_search_action ++;
            }
            if(piece[i-1][j+1]==-cur){
                if(searchEnd(cur, i, j, "TO_SW", execute))
                    north_edge_search_action ++;
            }
            if(piece[i-1][j]==-cur){
                if(searchEnd(cur, i, j, "TO_WEST", execute))
                    north_edge_search_action ++;
            }
            result = north_edge_search_action > 0;
        }

        //EAST edge
        if(i>5 && i<=7 && j>1 && j<6){
            int east_edge_search_action = 0;
            if(piece[i][j+1]==-cur){
                if(searchEnd(cur, i, j, "TO_SOUTH", execute))
                    east_edge_search_action ++;
            }
            if(piece[i-1][j+1]==-cur){
                if(searchEnd(cur, i, j, "TO_SW", execute))
                    east_edge_search_action ++;
            }
            if(piece[i-1][j]==-cur){
                if(searchEnd(cur, i, j, "TO_WEST", execute))
                    east_edge_search_action ++;
            }
            if(piece[i-1][j-1]==-cur){
                if(searchEnd(cur, i, j, "TO_NW", execute))
                    east_edge_search_action ++;
            }
            if(piece[i][j-1]==-cur){
                if(searchEnd(cur, i, j, "TO_NORTH", execute))
                    east_edge_search_action ++;
            }
            result = east_edge_search_action > 0;
        }

        //SOUTH edge
        if(i>1 && i<6 && j>5 && j<=7){
            int south_edge_search_action = 0;
            if(piece[i-1][j]==-cur){
                if(searchEnd(cur, i, j, "TO_WEST", execute))
                    south_edge_search_action ++;
            }
            if(piece[i-1][j-1]==-cur){
                if(searchEnd(cur, i, j, "TO_NW", execute))
                    south_edge_search_action ++;
            }
            if(piece[i][j-1]==-cur){
                if(searchEnd(cur, i, j, "TO_NORTH", execute))
                    south_edge_search_action ++;
            }
            if(piece[i+1][j-1]==-cur){
                if(searchEnd(cur, i, j, "TO_NE", execute))
                    south_edge_search_action ++;
            }
            if(piece[i+1][j]==-cur){
                if(searchEnd(cur, i, j, "TO_EAST", execute))
                    south_edge_search_action ++;
            }
            result = south_edge_search_action > 0;
        }

        //WEST edge
        if(i<2 && j>1 && j<6){
            int west_edge_search_action = 0;
            if(piece[i][j-1]==-cur){
                if(searchEnd(cur, i, j, "TO_NORTH", execute))
                    west_edge_search_action ++;
            }
            if(piece[i+1][j-1]==-cur){
                if(searchEnd(cur, i, j, "TO_NE", execute))
                    west_edge_search_action ++;
            }
            if(piece[i+1][j]==-cur){
                if(searchEnd(cur, i, j, "TO_EAST", execute))
                    west_edge_search_action ++;
            }
            if(piece[i+1][j+1]==-cur){
                if(searchEnd(cur, i, j, "TO_SE", execute))
                    west_edge_search_action ++;
            }
            if(piece[i][j+1]==-cur){
                if(searchEnd(cur, i, j, "TO_SOUTH", execute))
                    west_edge_search_action ++;
            }
            result = west_edge_search_action > 0;
        }

        //Middle pieces
        if(i>1 && i<6 && j>1 && j<6){
            int search_action = 0;
            if(piece[i][j-1]==-cur){
                if(searchEnd(cur, i, j, "TO_NORTH", execute))
                    search_action ++;
            }
            if(piece[i+1][j-1]==-cur){
                if(searchEnd(cur, i, j, "TO_NE", execute))
                    search_action ++;
            }
            if(piece[i+1][j]==-cur){
                if(searchEnd(cur, i, j, "TO_EAST", execute))
                    search_action ++;
            }
            if(piece[i+1][j+1]==-cur){
                if(searchEnd(cur, i, j, "TO_SE", execute))
                    search_action ++;
            }
            if(piece[i][j+1]==-cur){
                if(searchEnd(cur, i, j, "TO_SOUTH", execute))
                    search_action ++;
            }
            if(piece[i-1][j+1]==-cur){
                if(searchEnd(cur, i, j, "TO_SW", execute))
                    search_action ++;
            }
            if(piece[i-1][j]==-cur){
                if(searchEnd(cur, i, j, "TO_WEST", execute))
                    search_action ++;
            }
            if(piece[i-1][j-1]==-cur){
                if(searchEnd(cur, i, j, "TO_NW", execute))
                    search_action ++;
            }
            result = search_action > 0;
        }

        if(result && execute)
            System.out.println("Player is on an available place");
        else if(execute)
            System.out.println("Player is on an unavailable place");
        return result;
    }

    /**
     * search if there's same face on the other head from the current position
     * @param cur: player face
     * @param i: x-coordinate
     * @param j: y-coordinate
     * @param direction: direction
     * @param execute: if need to be executed
     * @return true if found friend
     */
    private boolean searchEnd(int cur, int i, int j, String direction, boolean execute) {
        switch(direction){
            case "TO_NORTH":
                int end_to_north;	//save the end point of searching
                boolean north_friend = false;	//if found friend
                //begin search from the place beside the current place
                for(end_to_north=j-1; end_to_north>=0; end_to_north--){
                    //if found friend, break out
                    if(piece[i][end_to_north]==cur){
                        north_friend = true;
                        break;
                    }
                    //if not found, break out
                    if(piece[i][end_to_north]==0){
                        end_to_north = 0;
                        break;
                    }
                }
                //if found friend
                if(north_friend){
                    if(execute){	//if need to be executed, then it will reverse all pieces between the current place and friend place
                        for(int y=j-1; y>=end_to_north; y--){
                            if(piece[i][y]==-cur){
                                piece[i][y]=cur;
                            }
                        }
                    }
                    return true;
                }
                else
                    return false;
            case "TO_EAST":
                int end_to_east;
                boolean east_friend = false;
                for(end_to_east=i+1; end_to_east<=7; end_to_east++){
                    if(piece[end_to_east][j]==cur){
                        east_friend = true;
                        break;
                    }
                    if(piece[end_to_east][j]==0){
                        end_to_east = 0;
                        break;
                    }
                }
                if(east_friend){
                    if(execute){
                        for(int x=i+1; x<=end_to_east; x++){
                            if(piece[x][j]==-cur){
                                piece[x][j]=cur;
                            }
                        }
                    }
                    return true;
                }
                else
                    return false;
            case "TO_SOUTH":
                int end_to_south;
                boolean south_friend = false;
                for(end_to_south=j+1; end_to_south<=7; end_to_south++){
                    if(piece[i][end_to_south]==cur){
                        south_friend = true;
                        break;
                    }
                    if(piece[i][end_to_south]==0){
                        end_to_south = 0;
                        break;
                    }
                }
                if(south_friend){
                    if(execute){
                        for(int y=j+1; y<=end_to_south; y++){
                            if(piece[i][y]==-cur){
                                piece[i][y]=cur;
                            }
                        }
                    }
                    return true;
                }
                else
                    return false;
            case "TO_WEST":
                int end_to_west;
                boolean west_friend = false;
                for(end_to_west=i-1; end_to_west>=0; end_to_west--){
                    if(piece[end_to_west][j]==cur){
                        west_friend = true;
                        break;
                    }
                    if(piece[end_to_west][j]==0){
                        end_to_west = 0;
                        break;
                    }
                }
                if(west_friend){
                    if(execute){
                        for(int x=i-1; x>=end_to_west; x--){
                            if(piece[x][j]==-cur){
                                piece[x][j]=cur;
                            }
                        }
                    }
                    return true;
                }
                else
                    return false;
            case "TO_NE":
                int end_to_ne = 0;
                int rang_to_ne = 0;	//this range keeps x and y increase or decrease same rate
                boolean ne_friend = false;
                for(int x=i+1, y=j-1; x<=7 && y>=0; x++, y--, end_to_ne++){
                    if(piece[x][y]==cur){
                        ne_friend = true;
                        break;
                    }
                    if(piece[x][y]==0){
                        end_to_ne = 0;
                        break;
                    }
                }
                if(ne_friend){
                    if(execute){
                        for(int x=i+1, y=j-1; rang_to_ne<=end_to_ne; x++, y--, rang_to_ne++){
                            if(piece[x][y]==-cur){
                                piece[x][y]=cur;
                            }
                        }
                    }
                    return true;
                }
                else
                    return false;
            case "TO_SE":
                int end_to_se = 0;
                int rang_to_se = 0;
                boolean se_friend = false;
                for(int x=i+1, y=j+1; x<=7 && y<=7; x++, y++, end_to_se++){
                    if(piece[x][y]==cur){
                        se_friend = true;
                        break;
                    }
                    if(piece[x][y]==0){
                        end_to_se = 0;
                        break;
                    }
                }
                if(se_friend){
                    if(execute){
                        for(int x=i+1, y=j+1; rang_to_se<=end_to_se; x++, y++, rang_to_se++){
                            if(piece[x][y]==-cur){
                                piece[x][y]=cur;
                            }
                        }
                    }
                    return true;
                }
                else
                    return false;
            case "TO_SW":
                int end_to_sw = 0;
                int rang_to_sw = 0;
                boolean sw_friend = false;
                for(int x=i-1, y=j+1; x>=0 && y<=7; x--, y++, end_to_sw++){
                    if(piece[x][y]==cur){
                        sw_friend = true;
                        break;
                    }
                    if(piece[x][y]==0){
                        end_to_sw = 0;
                        break;
                    }
                }
                if(sw_friend){
                    if(execute){
                        for(int x=i-1, y=j+1; rang_to_sw<=end_to_sw; x--, y++, rang_to_sw++){
                            if(piece[x][y]==-cur){
                                piece[x][y]=cur;
                            }
                        }
                    }
                    return true;
                }
                else
                    return false;
            case "TO_NW":
                int end_to_nw = 0;
                int rang_to_nw = 0;
                boolean nw_friend = false;
                for(int x=i-1, y=j-1; x>=0 && y>=0; x--, y--, end_to_nw++){
                    if(piece[x][y]==cur){
                        nw_friend = true;
                        break;
                    }
                    if(piece[x][y]==0){
                        end_to_nw = 0;
                        break;
                    }
                }
                if(nw_friend){
                    if(execute){
                        for(int x=i-1, y=j-1; rang_to_nw<=end_to_nw; x--, y--, rang_to_nw++){
                            if(piece[x][y]==-cur){
                                piece[x][y]=cur;
                            }
                        }
                    }
                    return true;
                }
                else
                    return false;
            default:
                throw new IllegalArgumentException("Invalid direction: " + direction);
        }
    }
}
