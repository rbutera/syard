package uk.ac.bris.cs.scotlandyard.model;

/**
 * Created by rai on 03/05/2017.
 */
public class ScotlandYardTurn {
    public String getMoveType() {
        return mMoveType;
    }

    private int mIndex = 0;
    private String mMoveType;

    public int getIndex() {
        return mIndex;
    }

    public int getColour() {
        return mColour;
    }

    public int getTurn() {
        return mTurn;
    }

    public int getRound() {
        return mRound;
    }

    public int getDoubleIndex() {
        return mDoubleIndex;
    }

    public ScotlandYardTurn getStartedBy() {
        return mStartedBy;
    }

    public boolean isPartOfDouble() {
        return mPartOfDouble;
    }

    private int mColour = 0;
    private int mTurn = 0;
    private int mRound = 0;
    // TODO: enum
    /**
     *  every single turn has a doubleIndex (0, 1 or 2)
     *  A DoubleMove's children TicketMOves will have doubleIndex 1 and 2 respectively.
     */
    private int mDoubleIndex = 0;
    private int mDestination = 0;
    private int mOrigin = 0;
    private int mStartedBy = 0; // index of the parent double move, if appropriate.

    public void setPartOfDouble(boolean partOfDouble) {
        mPartOfDouble = partOfDouble;
    }

    private boolean mPartOfDouble = false;

    public ScotlandYardTurn(int index, String moveType, int colour, int origin, int destination, int turn, int round, int doubleIndex, boolean partOfDouble) {
        mIndex = index;
        mMoveType = moveType;
        mColour = colour;
        mTurn = turn;
        mRound = round;
        mDoubleIndex = doubleIndex;
        mPartOfDouble = partOfDouble;
        mOrigin = origin;
        mDestination = destination;

        if(mPartOfDouble){
            // validate sane combinations
            if(mDoubleIndex != 0 && mDoubleIndex != 1 && mDoubleIndex != 2){
                throw new IllegalArgumentException("invalid mDoubleIndex. Must be 0, 1 or 2. Got: " + mDoubleIndex);
            } else if (mDoubleIndex == 0) {
                if(!getMoveType().equals("DoubleMove") && mPartOfDouble){
                    throw new IllegalArgumentException("invalid movetype for double index of 0. Got: " + getMoveType());
                } else if (!mPartOfDouble) {
                    if(!getMoveType().equals("PassMove") && mOrigin !== mDestination) {
                        throw new IllegalArgumentException("Pass moves should log with origin and destination equal to each other");
                    }
                }
            } else {
                if(!getMoveType().equals("TicketMove")){
                    throw new IllegalArgumentException("invalid movetype for non-zero doubleIndex. Got: " + getMoveType());
                }
            }
        }
    }

    public ScotlandYardTurn details() {
        return this;
    }

    @Override
    public String toString() {
        if(mMoveType != "DoubleMove"){
            String output = "ScotlandYardTurn{" +
                    "Index=" + mIndex +
                    ", moveType='" + mMoveType + '\'' +
                    ", colour=" + mColour +
                    ", action=" + mOrigin +
                    "-> " + mDestination +
                    ", turn=" + mTurn +
                    ", round=" + mRound +
                    ", doubleIndex=" + mDoubleIndex +
                    ", partOfDouble=" + mPartOfDouble;

                    if(mPartOfDouble){
                        output += ", startedBy=" + mStartedBy;
                    }
                    output += '}';
                    return output;
        } else {
            return "ScotlandYardTurn{" +
                    "Index=" + mIndex +
                    ", moveType='" + mMoveType + '\'' +
                    ", colour=" + mColour +
                    ", turn=" + mTurn +
                    ", round=" + mRound +
                    ", doubleIndex=" + mDoubleIndex +
                    ", partOfDouble=" + mPartOfDouble +
                    '}';
        }
    }

    /**
     * stores a reference to THE INDEX IN THE LOG of a ticketmove's parent doublemove when part of a doublemove
     * during logging this is used to tie all elements of a doublemove together for reference.
     * @param origin the original ScotlandYardTurn created by the DoubleMove
     */
    public void setStartedBy(ScotlandYardTurn parentDoubleMove) {
        requireNonNull(parentDoubleMove);
        if(parentDoubleMove != null){
            if(parentDoubleMove.getMoveType().equals("DoubleMove")){
                int di = getDoubleIndex();
                if(di != 0 && di != 1 && di != 2) {
                    throw new IllegalStateException("ScotlandYardTurn.startedBy encountered a DoubleMove or child ticket");
                }
                mStartedBy = parentDoubleMove.getIndex();
                setPartOfDouble(true);

            } else {
                throw new IllegalArgumentException("ScotlandYardTurn.startedBy should be passed a double move but was passed a " + origin.getMoveType())
            }
        }
    }
}
