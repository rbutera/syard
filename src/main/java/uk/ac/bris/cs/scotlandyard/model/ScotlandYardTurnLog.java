package uk.ac.bris.cs.scotlandyard.model;

import com.google.common.base.Predicate;
import com.google.common.collect.Collections2;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

import java.util.ArrayList;
import java.util.List;

import static java.util.Objects.requireNonNull;

public class ScotlandYardTurnLog {
    List<ScotlandYardTurn> mContents = new ArrayList<>();
    List<Colour> mOrder = new ArrayList<>();

    public void DEBUG_PRINTORDER() {
        System.out.println("LOG BEGIN (" + mOrder.size() + " players)");
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
        if (!(playerOrder.size() > 0)) throw new IllegalArgumentException("Positive quantity of players required! Got: " + playerOrder.size());
        if (!playerOrder.get(0).isMrX()) throw new IllegalArgumentException("First player should be Mr. X!");

        mOrder.addAll(playerOrder);

        DEBUG_PRINTORDER();
    }

    public ScotlandYardTurn latestX(Predicate<ScotlandYardTurn> filterFn) {
        List<ScotlandYardTurn> reversed = Lists.reverse(mContents);
        Iterable<ScotlandYardTurn> filtered = Iterables.filter(reversed, filterFn);

        return Iterables.getFirst(filtered, new ScotlandYardTurn(this, -1,"PassMove", Colour.Black,-1, -1, -1,-1,-1, false));
    }

    /**
     * finds the last DoubleMove in the log and returns it
     * @return
     */
    public ScotlandYardTurn latestDouble() {
        return latestX((x) -> x.getMoveType() == "DoubleMove");
    }

    public ScotlandYardTurn lastOfColour(Colour colour) {
        return latestX((x) -> x.getColour() == colour);
    }

    private int getOrigin(Colour colour) {
        if (!mContents.isEmpty()) {
            return lastOfColour(colour).getDestination();
        }

        return 0;
    }

    /**
     * finds the latest entry in the log and returns it
     * @return
     */
    public ScotlandYardTurn latest() {
        return Lists.reverse(mContents).get(0);
    }

    private int generateDoubleIndex (){
        int lastDoubleIndex = latest().getDoubleIndex();
        boolean lastPartOfDouble = latest().isPartOfDouble();

        if(lastPartOfDouble && lastDoubleIndex < 2){
            return lastDoubleIndex + 1;
        }

        return 0;
    }
    /**
     * adds a new entry to the log
     * @param move the new entry to be added
     * @return the index of the new log entry, if appropriate
     */
    public int add(Move move, int round) {
        int index = mContents.size();
        String moveType;
        int origin;
        int destination;

        requireNonNull(round);
        Colour colour = move.colour();

        origin = getOrigin(colour);
        if (origin < 0) {
            origin = 0;
        }
        if(move instanceof TicketMove){
            moveType = "TicketMove";
            TicketMove casted = (TicketMove) move;
            destination = casted.destination();
        } else {
            destination = origin;
            if (move instanceof PassMove){
                moveType = "PassMove";
            } else if (move instanceof DoubleMove){
                moveType = "DoubleMove";
            } else {
                moveType = "Unknown";
                throw new IllegalArgumentException("ScotlandYardTurnLog.add must be passed a TicketMove,PassMove or DoubleMove. Received " + moveType);
            }
        }
        int turn = Collections2.filter(mContents, (ScotlandYardTurn x) -> !x.getMoveType().equals("DoubleMove")).size();

        boolean partOfDouble = false;
        int doubleIndex = 0;
        if (!mContents.isEmpty()) {
            doubleIndex = generateDoubleIndex();
            partOfDouble = ((latest().isPartOfDouble() && doubleIndex > 0) || (move instanceof DoubleMove));
        }

        ScotlandYardTurn added = new ScotlandYardTurn(this, index, moveType, colour, origin, destination, turn, round, doubleIndex, partOfDouble);
        mContents.add(added);

        return index;
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

    public List<ScotlandYardTurn> getContents() {
        return mContents;
    }

    /**
     * returns all the ScotlandYardTurns for a given round.
     * @param round
     * @return
     */
    public List<ScotlandYardTurn> getTurnsInRound(int round){
        //TODO(rb): implement this!!
        throw new IllegalArgumentException("implement SYTL.getTurnsInRound()");
    }
}
