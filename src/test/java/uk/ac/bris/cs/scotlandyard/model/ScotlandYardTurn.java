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
    private int mDoubleIndex = 0;
    private ScotlandYardTurn mStartedBy;

    public void setPartOfDouble(boolean partOfDouble) {
        mPartOfDouble = partOfDouble;
    }

    private boolean mPartOfDouble = false;

    public ScotlandYardTurn(int index, String moveType, int colour, int turn, int round, int doubleIndex, boolean partOfDouble) {
        mIndex = index;
        mMoveType = moveType;
        mColour = colour;
        mTurn = turn;
        mRound = round;
        mDoubleIndex = doubleIndex;
        mPartOfDouble = partOfDouble;

        if(mPartOfDouble){
            // validate sane combinations
            if(mDoubleIndex != 0 && mDoubleIndex != 1 && mDoubleIndex != 2){
                throw new IllegalArgumentException("invalid mDoubleIndex. Must be 0, 1 or 2. Got: " + mDoubleIndex);
            } else if (mDoubleIndex == 0) {
                if(!getMoveType().equals("DoubleMove")){
                    throw new IllegalArgumentException("invalid movetype for double index of 0. Got: " + getMoveType());
                }
            } else {
                if(!getMoveType().equals("TicketMove")){
                    throw new IllegalArgumentException("invalid movetype for double index of 0. Got: " + getMoveType());
                }
            }
        }
    }

    public ScotlandYardTurn details() {
        return this;
    }

    @Override
    public String toString() {
        return "ScotlandYardTurn{" +
                "Index=" + mIndex +
                ", moveType='" + mMoveType + '\'' +
                ", colour=" + mColour +
                ", turn=" + mTurn +
                ", round=" + mRound +
                ", doubleIndex=" + mDoubleIndex +
                ", startedBy=" + mStartedBy +
                ", partOfDouble=" + mPartOfDouble +
                '}';
    }

    /**
     * during logging this is used to tie all elements of a doublemove together for reference.
     * @param origin the original ScotlandYardTurn created by the DoubleMove
     */
    public void setStartedBy(ScotlandYardTurn origin) {
        requireNonNull(origin);
        if(origin !== null){
            if(origin.getMoveType().equals("DoubleMove")){
                int di = getDoubleIndex();
                if(di != 0 && di != 1 && di != 2) {
                    throw new IllegalStateException("ScotlandYardTurn.startedBy encountered a DoubleMove or child ticket");
                }
                mStartedBy = origin;
                setPartOfDouble(true);

            } else {
                throw new IllegalArgumentException("ScotlandYardTurn.startedBy should be passed a double move but was passed a " + origin.getMoveType())
            }
        }
    }
}
