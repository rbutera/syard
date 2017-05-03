package uk.ac.bris.cs.scotlandyard.model;

import java.util.ArrayList;

import static uk.ac.bris.cs.scotlandyard.model.Colour.Black;

public class ScotlandYardTurnLog {
    List<ScotlandYardTurn> mContents = Lists.newArrayList();
    List<Colour> mOrder = Lists.newArrayList();

    private int mPlayersTotal = 0;

    public ArrayList<ScotlandYardTurn> getContents() {
        return mContents;
    }

    public int printOrder () {
        System.out.printf("Turn order is ");
        for (int i = 0; i < mOrder.size(); i++) {
            System.out.printf("%s", mOrder.get(i));
            if(i < (mOrder.size() - 1)){
                System.out.printf(", ");
            } else {
                System.out.printf(".\n");
            }
        }
    }

    public ScotlandYardTurnLog(ArrayList<Colour> playerOrder) {
        requireNonNull(playerOrder);
        if(playerOrder.size() > 0) {
            mPlayersTotal = playerOrder.size();
            System.out.println("        BEGIN LOG...    (" + playerOrder.size() + " players");
            if(playerOrder.get(0) != Black){
                throw new IllegalArgumentException("Invalid player order - should start with Black");
            } else {
                mOrder.addAll(playerOrder);
                printOrder();
            }
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
        throw new IllegalArgumentException("implement latestTurn!");
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

    public ScotlandYardTurn scanForLast () {
        //TODO(rb): implement this!
        throw new IllegalArgumentException("implement scanForLast");
    }

    public Colour nextColour() {
        //TODO(rb): implement this!!
        throw new IllegalArgumentException("implement SYTL.nextPlayer!");
    }

    public int nextRound() {
        //TODO(rb): implement this!!
        throw new IllegalArgumentException("implement SYTL.nextRound!");
    }

    public int nextTurn() {
        //TODO(rb): implement this!!
        throw new IllegalArgumentException("implement SYTL.nextTurn!");
    }

    public ArrayList<ScotlandYardTurn> getRound(int round){
        //TODO(rb): implement this!!
        throw new IllegalArgumentException("implement SYTL.getRound()");
    }
}
