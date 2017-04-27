package uk.ac.bris.cs.scotlandyard.model;

import static java.util.Objects.requireNonNull;
import static uk.ac.bris.cs.scotlandyard.model.Colour.Black;
import static uk.ac.bris.cs.scotlandyard.model.Colour.Blue;
import static uk.ac.bris.cs.scotlandyard.model.Ticket.Bus;
import static uk.ac.bris.cs.scotlandyard.model.Ticket.Taxi;
import static uk.ac.bris.cs.scotlandyard.model.Ticket.Underground;
import static uk.ac.bris.cs.scotlandyard.model.Transport.Boat;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

import com.sun.javaws.exceptions.InvalidArgumentException;
import org.apache.commons.lang3.ObjectUtils;
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

				Set<Integer> locationsset = new HashSet<>();
				Set<Colour> coloursset = new HashSet<>();

				for (PlayerConfiguration config : mConfigurations) {
					// prevent duplicate locations
					if (locationsset.contains(config.location)) {throw new IllegalArgumentException("Duplicate location");}
					locationsset.add(config.location);

					// prevent duplicate colours
					if (coloursset.contains(config.colour)) {throw new IllegalArgumentException("Duplicate colour");}
					coloursset.add(config.colour);

					// ensure mapping for each Ticket exists
					if (!(config.tickets.containsKey(Ticket.Bus)
					   && config.tickets.containsKey(Taxi)
						 && config.tickets.containsKey(Ticket.Underground)
				  	 && config.tickets.containsKey(Ticket.Double)
					   && config.tickets.containsKey(Ticket.Secret))) {
							 throw new IllegalArgumentException("Player is missing a ticket type");
					}

					// prevent invalid tickets
					if (!config.colour.isMrX() && (config.tickets.get(Ticket.Secret) != 0 || config.tickets.get(Ticket.Double) != 0)) {
						throw new IllegalArgumentException("Detectives cannot have Secret or Double tickets");
					}
				}

				// construct players
				mScotlandYardPlayers = new ArrayList<ScotlandYardPlayer>();

				for (PlayerConfiguration config : mConfigurations) {
					ScotlandYardPlayer player = new ScotlandYardPlayer(config.player, config.colour, config.location, config.tickets);
					mScotlandYardPlayers.add(player);
					mTotalPlayers++;
				}

				// at initialisation, the rounds have not started
				mCurrentRound = ScotlandYardGame.NOT_STARTED;
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
	        TicketMove tm = (TicketMove) move;
            player.location(tm.destination());
            // modify player's tickets
            player.removeTicket(tm.ticket());
            if (!player.colour().isMrX()) {
            	getPlayerInstanceByColour(Black).addTicket(tm.ticket());
			}
            // recalculate occupied spaces
            getOccupiedLocations();
            goNextPlayer();
        } else if (move instanceof PassMove) { // pass moves
	    	goNextPlayer();
		} else if (move instanceof DoubleMove) {
			DoubleMove dm = (DoubleMove) move;
			player.location(dm.secondMove().destination());
			// modify player's tickets
			player.removeTicket(Ticket.Double);
			player.removeTicket(dm.firstMove().ticket());
			player.removeTicket(dm.secondMove().ticket());
			// recalculate occupied spaces
			getOccupiedLocations();
			goNextPlayer();
		} else {
	    	throw new IllegalArgumentException("Illegal Move");
		}
    }

    private void requestMove(ScotlandYardPlayer player) {
        requireNonNull(player);
        Set<Move> moves = getValidMoves(player);
        player.player().makeMove(this, player.location(), moves, this);
    }

    @Override
	public void startRotate() {
		mCurrentRound++;
		requestMove(getPlayerInstanceByColour(getCurrentPlayer()));
	}

	private int getRoundsRemaining() {
	    return mRounds.size() - mCurrentRound;
    }

    private ArrayList<Integer> getOccupiedLocations() {
	    mOccupied = new ArrayList();
        for (ScotlandYardPlayer p: mScotlandYardPlayers) {
            if(p.colour() != Colour.Black) {
                mOccupied.add(p.location());
            }
        }
        return mOccupied;
    }


	private Set<Move> getValidMoves(ScotlandYardPlayer player) {
		// Result
		Set<Move> moves = new HashSet<Move>();

		Ticket ticket;
		Colour colour = player.colour();
		int location = player.location();

		// Get currently occupied spaces to prevent offering collision-inducing moves
        ArrayList<Integer> occupied = getOccupiedLocations();


		// get the tickets of the player
		// get edges from graph
		Collection<Edge<Integer, Transport>> options = mGraph.getEdgesFrom(mGraph.getNode(location));

		for (Edge<Integer, Transport> edge : options) {
			Transport transport = edge.data();
			Node<Integer> destination = edge.destination();
			Integer dest = destination.value();
			ticket = Ticket.fromTransport(transport);
			Boolean canSecret = colour == Black && player.hasTickets(Ticket.fromTransport(Boat));
			Boolean canDouble = colour == Black && player.hasTickets(Ticket.Double) && getRoundsRemaining() >= 2;

			if ((player.hasTickets(ticket) || canSecret) && !occupied.contains(dest)) {
				TicketMove move = new TicketMove(colour, ticket, dest);
                moves.add(move);

                if (canSecret) {
                    TicketMove secretMove = new TicketMove(colour, Ticket.fromTransport(Boat), dest);
                    moves.add(secretMove);
                }

				if (canDouble) {
                	// generate collection of double-turn tickets (edges)
					Collection<Edge<Integer, Transport>> moreOptions = mGraph.getEdgesFrom(mGraph.getNode(dest));

					for (Edge<Integer, Transport> secondEdge : moreOptions) {
						Transport secondTransport = secondEdge.data();
						Node<Integer> secondDestination = secondEdge.destination();
						Integer sDest = secondDestination.value();
						Ticket sTicket = Ticket.fromTransport(secondTransport);

						if (!occupied.contains(sDest) || sDest == location && player.hasTickets(sTicket)) {
						    DoubleMove doubleMove;
                            boolean valid = ticket != sTicket || player.hasTickets(ticket, 2);

                            if (valid) {
                                doubleMove = new DoubleMove(colour, ticket, dest, sTicket, sDest);
                                moves.add(doubleMove);
                            }

                            if (canSecret) {
                                // add moves for use of a secret ticket first
                                DoubleMove doubleSecretFirstMove = new DoubleMove(colour, Ticket.fromTransport(Boat), dest, sTicket, sDest);
                                moves.add(doubleSecretFirstMove);
                                // add moves for use of a secret ticket second
                                DoubleMove doubleSecretSecondMove = new DoubleMove(colour, ticket, dest, Ticket.fromTransport(Boat), sDest);
                                moves.add(doubleSecretSecondMove);
                                // add moves for use of two secret tickets
                                if (player.hasTickets(Ticket.fromTransport(Boat), 2)) {
                                    DoubleMove doubleSecretCombo = new DoubleMove(colour, Ticket.fromTransport(Boat), dest, Ticket.fromTransport(Boat), sDest);
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
		for (ScotlandYardPlayer currentplayer : mScotlandYardPlayers) {
			players.add(currentplayer.colour());
		}
		return Collections.unmodifiableList(players);
	}

	@Override
	public Set<Colour> getWinningPlayers() {
		// TODO: Revisit this later
		return Collections.unmodifiableSet(Collections.<Colour>emptySet());
	}

	private int updateLastRevealed () {
		hasBeenRevealed = true;
		mLastRevealedBlack = getPlayerInstanceByColour(Black).location();
		return mLastRevealedBlack;
	}

	@Override
	public int getPlayerLocation(Colour colour) {
		if (colour == Black) {
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
		for (ScotlandYardPlayer currentplayer : mScotlandYardPlayers) {
			if (currentplayer.colour() == colour) {
				return currentplayer.tickets().get(ticket);
			}
		}
		return 0;
	}

	@Override
	public boolean isGameOver() {
		// TODO: Revisit this later
		return false;
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
		// ONE SEC - SWITCHING TO PC MIC
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
