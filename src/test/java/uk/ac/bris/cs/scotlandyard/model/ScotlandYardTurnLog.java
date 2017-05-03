package uk.ac.bris.cs.scotlandyard.model;

import java.util.ArrayList;

public class ScotlandYardTurnLog {
    ArrayList<ScotlandYardTurn> mContents = new ArrayList<ScotlandYardTurn>();

    public ArrayList<ScotlandYardTurn> getContents() {
        return mContents;
    }

    public ScotlandYardTurnLog() {
        System.out.println("        BEGIN LOG...    ");
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
