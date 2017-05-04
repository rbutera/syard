package uk.ac.bris.cs.scotlandyard.model;

import com.google.common.base.Predicate;
import com.google.common.collect.Collections2;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static java.util.Objects.requireNonNull;

public class ScotlandYardTurnLog {
    List<ScotlandYardTurn> mContents = new ArrayList<>();
    List<Colour> mOrder = new ArrayList<>();

    public void setMrXOrigin(int mrXOrigin) {
        mMrXOrigin = mrXOrigin;
    }

    int mMrXOrigin = 0;

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

    public ScotlandYardTurnLog(ArrayList<Colour> playerOrder, int mrXOrigin) {
        requireNonNull(playerOrder);
        if (!(playerOrder.size() > 0)) throw new IllegalArgumentException("Positive quantity of players required! Got: " + playerOrder.size());
        if (!playerOrder.get(0).isMrX()) throw new IllegalArgumentException("First player should be Mr. X!");

        mOrder.addAll(playerOrder);
        setMrXOrigin(mrXOrigin);
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
        int origin = 0;
        if (!mContents.isEmpty()) {
            origin = lastOfColour(colour).getDestination();
            if (origin < 0) origin = 0;
        } else {
            origin = mMrXOrigin;
        }

        return origin;
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
        requireNonNull(round);

        int index = mContents.size();
        String moveType;
        Colour colour = move.colour();
        int origin = getOrigin(colour);
        int destination;

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
        ScotlandYardTurn latestTurn;
        if (getContents().size() > 0) latestTurn = latest();
        else return Colour.Black;
        if (latestTurn.isPartOfDouble()) {
            return latestTurn.getColour();
        } else {
            int index = mOrder.indexOf(latestTurn);

            // if we're at the last element of mOrders
            // then the next colour should be the first element
            // e.g. mOrders = (Black, Red, Blue)
            // nextColour for Blue is Black

            if (mOrder.size() <= (index + 1)) {
                index = 0;
            }

            return mOrder.get(index + 1);
        }
    }

    public int nextRound() {
        // derive the round from the log
        // filter out the double moves from the log's mContents
        // count the number of black turns
        // this is the previous round
        // increment this by one
        // return

        if (mContents.size() > 0) {
            Predicate<ScotlandYardTurn> isTrueBlack =
                    x ->  !x.getMoveType().equals("DoubleMove")
                                    && x.getColour().equals(Colour.Black);

            int turn = Collections2.filter(mContents, isTrueBlack).size();

            return turn+1;
        } else {
            return ScotlandYardView.NOT_STARTED;
        }
    }

    public int nextTurn() {
        int result = -1337;

        if(mContents.size() > 0) {
            ScotlandYardTurn last = latest();

            // if last was part of a double move
                // if the double index was less than 2
                    // return last.getTurn();
                // else
                 /* the double index == 2 so we are done adding child turns to the log and can safely move to the next turn */
            // else
                // get a reference to the last colour in the order
                // if the last was the last colour in the order
                    // black should be next, so nextTurn will be 0.

            if (last.isPartOfDouble()) {
                if (last.getDoubleIndex() < 2) {
                    result = last.getTurn();
                } else { // double index is 2
                    result = last.getTurn() + 1;
                }
            } else { // not part of double
                int indexOfLastInOrder = mOrder.size() - 1;
                if (last.getColour() == mOrder.get(indexOfLastInOrder)) {
                    result = 0;
                } else {
                    result = last.getTurn() + 1;
                }
            }
        }

        return result;
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
