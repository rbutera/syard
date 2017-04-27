package uk.ac.bris.cs.scotlandyard.model;

import static java.util.Objects.requireNonNull;
import static uk.ac.bris.cs.scotlandyard.model.Colour.Black;
import static uk.ac.bris.cs.scotlandyard.model.Ticket.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

import uk.ac.bris.cs.gamekit.graph.Graph;
import uk.ac.bris.cs.gamekit.graph.Edge;
import uk.ac.bris.cs.gamekit.graph.ImmutableGraph;
import uk.ac.bris.cs.gamekit.graph.Node;


// TODO implement all methods and pass all tests
public class ScotlandYardModel implements ScotlandYardGame, Consumer<Move> {

	private final List<Boolean> mRounds;
	private final Graph<Integer, Transport> mGraph;
	private ArrayList<ScotlandYardPlayer> mScotlandYardPlayers;
	private int mCurrentRound = 0;
	private int mCurrentRoundTurnsPlayed = 0;
	private int mTotalPlayers = 0;
	private int mTotalTurnsPlayed = 0;
	private boolean hasBeenRevealed = false;
    private ArrayList<Integer> mOccupied;
	private final ArrayList<PlayerConfiguration> mConfigurations;
    private int mLastRevealedBlack = 0;

	private List<Boolean> validateRounds(List<Boolean> rounds) {
		if (rounds == null){throw new NullPointerException();}
        if (rounds.isEmpty()) {throw new IllegalArgumentException("Empty rounds");}
		return requireNonNull(rounds);
	}

	private Graph<Integer, Transport> validateGraph(Graph<Integer, Transport> graph) {
		if (graph == null) {throw new NullPointerException();}
		if (graph.isEmpty()) {throw new IllegalArgumentException("Empty graph");}
		return requireNonNull(graph);
	}


	private ArrayList<PlayerConfiguration> configurePlayers(PlayerConfiguration mrX, PlayerConfiguration firstDetective, PlayerConfiguration... restOfTheDetectives) {
		if (mrX.colour != Black) {throw new IllegalArgumentException("MrX should be Black");}
		ArrayList<PlayerConfiguration> configurations = new ArrayList<>();
		configurations.add(requireNonNull(mrX));
		configurations.add(requireNonNull(firstDetective));

		for (PlayerConfiguration config : restOfTheDetectives) {
			configurations.add(requireNonNull(config));
		}
		return configurations;
	}

	public ScotlandYardModel(List<Boolean> rounds, Graph<Integer, Transport> graph,
			PlayerConfiguration mrX, PlayerConfiguration firstDetective,
			PlayerConfiguration... restOfTheDetectives) {

				mRounds = validateRounds(rounds);
				mGraph = validateGraph(graph);
			 	mConfigurations = configurePlayers(mrX, firstDetective, restOfTheDetectives);

				Set<Integer> setConfigLocations = new HashSet<>();
				Set<Colour> setConfigColours = new HashSet<>();

				for (PlayerConfiguration config : mConfigurations) {
					// prevent duplicate locations
					if (setConfigLocations.contains(config.location)) {throw new IllegalArgumentException("Duplicate location");}
					setConfigLocations.add(config.location);

					// prevent duplicate colours
					if (setConfigColours.contains(config.colour)) {throw new IllegalArgumentException("Duplicate colour");}
					setConfigColours.add(config.colour);

					// ensure mapping for each Ticket exists
					if (!(config.tickets.containsKey(Bus)
					   && config.tickets.containsKey(Taxi)
						 && config.tickets.containsKey(Underground)
				  	 && config.tickets.containsKey(Double)
					   && config.tickets.containsKey(Secret))) {
							 throw new IllegalArgumentException("Player is missing a ticket type");
					}

					// prevent invalid tickets
					if (!config.colour.isMrX() && (config.tickets.get(Secret) != 0 || config.tickets.get(Double) != 0)) {
						throw new IllegalArgumentException("Detectives cannot have Secret or Double tickets");
					}
				}

				// populate mScotlandYardPlayers with ScotlandYardPlayers derived from mConfigurations
				mScotlandYardPlayers = new ArrayList<ScotlandYardPlayer>();

				for (PlayerConfiguration config : mConfigurations) {
					ScotlandYardPlayer player = new ScotlandYardPlayer(config.player, config.colour, config.location, config.tickets);
					mScotlandYardPlayers.add(player);
					mTotalPlayers++;
				}

				// at initialisation, the rounds have not started
				mCurrentRound = NOT_STARTED;
	}

	@Override
	public void registerSpectator(Spectator spectator) {
		// TODO
		// throw new RuntimeException("Implement me");
	}

	@Override
	public void unregisterSpectator(Spectator spectator) {
		// TODO
		// throw new RuntimeException("Implement me");
	}

    @Override
    public void accept(Move move) {
        // do something with the Move the current Player wants to play
        requireNonNull(move);
        ScotlandYardPlayer player = getPlayerInstanceByColour(move.colour());
        Set<Move> valid = getValidMoves(player);
        if (valid.contains(move)){
			processMove(player, move);
		} else {
        	throw new IllegalArgumentException("Illegal move passed to callback");
		}
    }

    private ScotlandYardPlayer getPlayerInstanceByColour (Colour colour){
	    requireNonNull(colour);
	    ScotlandYardPlayer p = null;
        for (ScotlandYardPlayer player :
                mScotlandYardPlayers)
            if (player.colour() == colour) p = player;

        if (p == null){
            throw new IllegalArgumentException("Could not find player for colour");
        }
        return p;
    }

	private void goNextPlayer(){
		if (++mCurrentRoundTurnsPlayed >= mTotalPlayers){
			mCurrentRoundTurnsPlayed = 0;
		} else {
			requestMove(getPlayerInstanceByColour(getCurrentPlayer()));
		}
		mTotalTurnsPlayed++;
	}

    private void processMove(ScotlandYardPlayer player, Move move) {
	    if (move instanceof TicketMove) {
	        TicketMove ticketMove = (TicketMove) move;
            player.location(ticketMove.destination());
            // modify player's tickets
            player.removeTicket(ticketMove.ticket());
            if (player.colour().isDetective()) {
            	getPlayerInstanceByColour(Black).addTicket(ticketMove.ticket());
			}
            // recalculate occupied spaces
            getOccupiedLocations();
            goNextPlayer();
        } else if (move instanceof PassMove) { // pass moves
	    	goNextPlayer();
		} else if (move instanceof DoubleMove) {
			DoubleMove doubleMove = (DoubleMove) move;
			player.location(doubleMove.secondMove().destination());
			// modify player's tickets
			player.removeTicket(Ticket.Double);
			player.removeTicket(doubleMove.firstMove().ticket());
			player.removeTicket(doubleMove.secondMove().ticket());
			// recalculate occupied spaces
			getOccupiedLocations();
			goNextPlayer();
		} else {
	    	throw new IllegalArgumentException("Illegal move");
		}
    }

    private void requestMove(ScotlandYardPlayer player) {
        requireNonNull(player);
        Set<Move> moves = getValidMoves(player);
        player.player().makeMove(this, player.location(), moves, this);
    }

    @Override
	public void startRotate() {
	    if (isGameOver()) throw new IllegalStateException("Game's already over");
		mCurrentRound++;
		requestMove(getPlayerInstanceByColour(getCurrentPlayer()));
	}

	private int getRoundsRemaining() {
	    return mRounds.size() - mCurrentRound;
    }

    private ArrayList<Integer> getOccupiedLocations() {
	    mOccupied = new ArrayList();
        for (ScotlandYardPlayer p: mScotlandYardPlayers) {
            if(!p.colour().isMrX()) {
                mOccupied.add(p.location());
            }
        }
        return mOccupied;
    }


	private Set<Move> getValidMoves(ScotlandYardPlayer player) {
		// Result
		Set<Move> moves = new HashSet<>();

		Colour colour = player.colour();
		int location = player.location();

		// get currently occupied spaces to prevent offering collision-inducing moves
        ArrayList<Integer> occupied = getOccupiedLocations();

		// use the node of the player's current location to find potential move options
		Collection<Edge<Integer, Transport>> options = mGraph.getEdgesFrom(mGraph.getNode(location));

		// iterate through the potential move options and add ones considered valid
		for (Edge<Integer, Transport> edge : options) {
		    // extract the current edge's transport and destination node
			Transport transport = edge.data();
			Node<Integer> destinationNode = edge.destination();

			// extract the destination value from the destination node of this edge
			Integer destination = destinationNode.value();
			// extract the ticket from the given transport of this edge
			Ticket ticket = Ticket.fromTransport(transport);

			// enable special flags for Mr. X
			Boolean canSecret = colour.isMrX() && player.hasTickets(Secret);
			Boolean canDouble = colour.isMrX() && player.hasTickets(Double) && getRoundsRemaining() >= 2;

			// TODO: refactor the repetitive code here
			if ((player.hasTickets(ticket) || canSecret) && !occupied.contains(destination)) {
				TicketMove move = new TicketMove(colour, ticket, destination);
                moves.add(move);

                if (canSecret) {
                    TicketMove secretMove = new TicketMove(colour, Secret, destination);
                    moves.add(secretMove);
                }

				if (canDouble) {
                	// generate collection of double-turn tickets (edges)
					Collection<Edge<Integer, Transport>> moreOptions = mGraph.getEdgesFrom(mGraph.getNode(destination));

					for (Edge<Integer, Transport> secondEdge : moreOptions) {
						Transport secondTransport = secondEdge.data();
						Node<Integer> secondDestinationNode = secondEdge.destination();
						Integer secondDestination = secondDestinationNode.value();
						Ticket secondTicket = Ticket.fromTransport(secondTransport);

						if (!occupied.contains(secondDestination) || secondDestination == location && player.hasTickets(secondTicket)) {
                            if (ticket != secondTicket || player.hasTickets(ticket, 2)) {
                                DoubleMove doubleMove = new DoubleMove(colour, ticket, destination, secondTicket, secondDestination);
                                moves.add(doubleMove);
                            }

                            if (canSecret) {
                                // add moves for use of a secret ticket first
                                DoubleMove doubleSecretFirstMove = new DoubleMove(colour, Secret, destination, secondTicket, secondDestination);
                                moves.add(doubleSecretFirstMove);

                                // add moves for use of a secret ticket second
                                DoubleMove doubleSecretSecondMove = new DoubleMove(colour, ticket, destination, Secret, secondDestination);
                                moves.add(doubleSecretSecondMove);

                                // add moves for use of two secret tickets
                                if (player.hasTickets(Secret, 2)) {
                                    DoubleMove doubleSecretCombo = new DoubleMove(colour, Secret, destination, Secret, secondDestination);
                                    moves.add(doubleSecretCombo);
                                }
                            }
                        }
					}
				}
 			}
		}

		if (moves.isEmpty()) {
		    moves.add(new PassMove(colour));
        }
		return moves;
	}



	@Override
	public Collection<Spectator> getSpectators() {
		// TODO
		throw new RuntimeException("Implement me");
	}

	@Override
	public List<Colour> getPlayers() {
		ArrayList<Colour> players = new ArrayList<Colour>();
		for (ScotlandYardPlayer player : mScotlandYardPlayers) {
			players.add(player.colour());
		}
		return Collections.unmodifiableList(players);
	}

	private int updateLastRevealed () {
		hasBeenRevealed = true;
		mLastRevealedBlack = getPlayerInstanceByColour(Black).location();
		return mLastRevealedBlack;
	}

	@Override
	public int getPlayerLocation(Colour colour) {
		if (colour.isMrX()) {
			if (hasBeenRevealed && isRevealRound()) {
				updateLastRevealed();
			} else if (!hasBeenRevealed){
				if((mTotalTurnsPlayed != 0 && mCurrentRound > 0 && mCurrentRound < getRounds().size() && getRounds().get(mCurrentRound))){
					updateLastRevealed();
				}
				hasBeenRevealed = true;
			}

			return mLastRevealedBlack;
		}

		return getPlayerInstanceByColour(colour).location();
	}

	@Override
	public int getPlayerTickets(Colour colour, Ticket ticket) {
		for (ScotlandYardPlayer player : mScotlandYardPlayers) {
			if (player.colour() == colour) {
				return player.tickets().get(ticket);
			}
		}

		return 0;
	}

    private boolean allDetectivesAreTicketless() {
        for (ScotlandYardPlayer player : mScotlandYardPlayers) {
            if (player.colour().isDetective()) {
                if (player.hasTickets(Bus)) return false;
                if (player.hasTickets(Taxi)) return false;
                if (player.hasTickets(Underground)) return false;
                if (player.hasTickets(Double)) return false;
                if (player.hasTickets(Secret)) return false;
            }
        }

        return true;
    }

	private boolean noDetectivesHaveValidMoves() {
        boolean result = true;

        for (ScotlandYardPlayer player : mScotlandYardPlayers) {
            PassMove playerPassMove = new PassMove(player.colour());
            result &= getValidMoves(player).contains(playerPassMove); // if this is false once, then at least one Detective isn't stuck
        }

        return result;
    }

    private boolean allRoundsAreUsedUp(){
	    return getCurrentRound() >= getRounds().size();
    }

    private ScotlandYardPlayer getMrX(){
        return getPlayerInstanceByColour(Black);
    }

    private boolean mrXHasNoValidMoves(){
        PassMove mrXPassMove = new PassMove(Black);
        return getValidMoves(getMrX()).contains(mrXPassMove);
    }

    private boolean mrXIsCaptured() {
        boolean result = false;

        ScotlandYardPlayer mrX = getMrX();
        for (ScotlandYardPlayer player : mScotlandYardPlayers) {
            result |= player.location() == mrX.location()
                    && !player.colour().isMrX(); // if this is true once, then Mr. X has been captured
        }

        return result;
    }

    private Set<Colour> mWinners;

    @Override
    public Set<Colour> getWinningPlayers() {
        return mWinners;
    }


	@Override
	public boolean isGameOver() {
        boolean detectivesWin = false;
        boolean mrXWins = false;
        mWinners = new HashSet<>();

        // MR.X WINS
        if (allDetectivesAreTicketless() || noDetectivesHaveValidMoves() || allRoundsAreUsedUp()) {
            mrXWins = true;

            mWinners.add(Black);
        }

        // DETECTIVES WIN
        if (mrXHasNoValidMoves() || mrXIsCaptured()){
            detectivesWin = true;

            for (ScotlandYardPlayer player : mScotlandYardPlayers) {
                if (player.isDetective()) {
                    mWinners.add(player.colour());
                }
            }
        }

        return mrXWins || detectivesWin;
	}

	@Override
	public Colour getCurrentPlayer() {
		return mScotlandYardPlayers.get(mCurrentRoundTurnsPlayed).colour();
	}

	@Override
	public int getCurrentRound() {
		return mCurrentRound;
	}

	@Override
	public boolean isRevealRound() {
		return getRounds().get(getCurrentRound() - 1);
	}

	@Override
	public List<Boolean> getRounds() {
		return Collections.unmodifiableList(mRounds);
	}

	@Override
	public Graph<Integer, Transport> getGraph() {
		return new ImmutableGraph<Integer, Transport>(mGraph);
	}

}
