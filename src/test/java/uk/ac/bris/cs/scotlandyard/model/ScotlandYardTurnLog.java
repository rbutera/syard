package uk.ac.bris.cs.scotlandyard.model;

import java.util.ArrayList;

public class ScotlandYardTurnLog {
    ArrayList<ScotlandYardTurn> mContents = new ArrayList<ScotlandYardTurn>();
    ArrayList<Colour> mOrder = new ArrayList<Colour>();

    private int mPlayersTotal = 0;

    public ArrayList<ScotlandYardTurn> getContents() {
        return mContents;
    }

    public ScotlandYardTurnLog(ArrayList<Colour> playerOrder) {
        requireNonNull(playerOrder);
        if(playerOrder.size() > 0) {
            mPlayersTotal = playerOrder.size();
            System.out.println("        BEGIN LOG...    (" + playerOrder.size() + " players");
        } else {
            throw new IllegalArgumentException("Cannot create a ScotlandYardTurnLog for a non-positive quantity of players! (was given" + playerOrder.size() + " )");
        }
    }

    /**
     * finds the last DoubleMove in the log and returns it
     * @return
     */
    public ScotlandYardTurn latestDouble() {
        //TODO(rb): implement this!!
        throw new IllegalArgumentException("implement latestDouble");
    }

    /**
     * finds the latest entry in the log and returns it
     * @return
     */
    public ScotlandYardTurn latest() {
        //TODO(rb): implement this!!
        throw new IllegalArgumentException("implement latestTurn!")
    }

    /**
     * adds a new entry to the log
     * @param input the new entry to be added
     * @return
     */
    public int add(ScotlandYardTurn input) {
        int index = 0;
        if(true){
            //TODO(rb): implement this!!
            throw new IllegalArgumentException("implement SYTL.add!");
        } else {
            return index;
        }
    }

    public ScotlandYardTurn nextColour() {
        //TODO(rb): implement this!!
        throw new IllegalArgumentException("implement SYTL.nextPlayer!");
    }

    public ScotlandYardTurn nextRound() {
        //TODO(rb): implement this!!
        throw new IllegalArgumentException("implement SYTL.nextRound!");
    }

    public ScotlandYardTurn nextTurn() {
        //TODO(rb): implement this!!
        throw new IllegalArgumentException("implement SYTL.nextTurn!");
    }

    public ArrayList<ScotlandYardTurn> getRound(int round){
        //TODO(rb): implement this!!
        throw new IllegalArgumentException("implement SYTL.getRound()");
    }
}
