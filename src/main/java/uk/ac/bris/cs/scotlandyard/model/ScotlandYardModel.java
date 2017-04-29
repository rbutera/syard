package uk.ac.bris.cs.scotlandyard.model;

import uk.ac.bris.cs.gamekit.graph.Edge;
import uk.ac.bris.cs.gamekit.graph.Graph;
import uk.ac.bris.cs.gamekit.graph.ImmutableGraph;
import uk.ac.bris.cs.gamekit.graph.Node;

import java.util.*;
import java.util.function.Consumer;

import static java.util.Objects.requireNonNull;
import static uk.ac.bris.cs.scotlandyard.model.Colour.Black;
import static uk.ac.bris.cs.scotlandyard.model.Ticket.*;
import static uk.ac.bris.cs.scotlandyard.model.Ticket.Double;


// TODO implement all methods and pass all tests
public class ScotlandYardModel implements ScotlandYardGame, Consumer<Move> {

	private final List<Boolean> mRounds;
	private final Graph<Integer, Transport> mGraph;
	private ArrayList<ScotlandYardPlayer> mScotlandYardPlayers;
	private Set<Colour> mWinningPlayers = new HashSet<>();
	private int mCurrentRound = NOT_STARTED;
	private int mTotalTurnsPlayed = 0;
	private int mCurrentRoundTurnsPlayed = 0;
	private int mTotalPlayers = 0;
	private int mLastRevealedBlack = 0;
	private boolean mHasBeenRevealed = false;
	private boolean gameOverNotified = false;

	private boolean DEBUG_ENABLED = true;
	private void DEBUG_PRINT (String s) {
		if(DEBUG_ENABLED) {
			System.out.println(s);
		}
	}

	private void setRevealed() {
		mHasBeenRevealed = true;
	}
	private boolean checkRevealed() {
		return mHasBeenRevealed;
	}


	private List<Boolean> validateRounds(List<Boolean> rounds) {
		requireNonNull(rounds);
		if (rounds.isEmpty()) {
			throw new IllegalArgumentException("Empty rounds");
		}
		return rounds;
	}

	private Graph<Integer, Transport> validateGraph(Graph<Integer, Transport> graph) {
		requireNonNull(graph);
		if (graph.isEmpty()) {
			throw new IllegalArgumentException("Empty graph");
		}
		return graph;
	}


	private ArrayList<PlayerConfiguration> configurePlayers(PlayerConfiguration mrX, PlayerConfiguration firstDetective, PlayerConfiguration... restOfTheDetectives) {
		if (mrX.colour != Black) {
			throw new IllegalArgumentException("MrX should be Black");
		}
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
		ArrayList<PlayerConfiguration> configurations = configurePlayers(mrX, firstDetective, restOfTheDetectives);

		Set<Integer> setConfigLocations = new HashSet<>();
		Set<Colour> setConfigColours = new HashSet<>();

		for (PlayerConfiguration config : configurations) {
			// prevent duplicate locations
			if (setConfigLocations.contains(config.location)) {
				throw new IllegalArgumentException("Duplicate location");
			}
			setConfigLocations.add(config.location);

			// prevent duplicate colours
			if (setConfigColours.contains(config.colour)) {
				throw new IllegalArgumentException("Duplicate colour");
			}
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

		// populate mScotlandYardPlayers with ScotlandYardPlayers derived from configurations
		mScotlandYardPlayers = new ArrayList<>();

		for (PlayerConfiguration config : configurations) {
			ScotlandYardPlayer player = new ScotlandYardPlayer(config.player, config.colour, config.location, config.tickets);
			mScotlandYardPlayers.add(player);
			mTotalPlayers++;
		}
	}

	private ScotlandYardPlayer getPlayerInstanceByColour (Colour colour){
		requireNonNull(colour);
		ScotlandYardPlayer result = null;

		for (ScotlandYardPlayer player : mScotlandYardPlayers)
			if (player.colour() == colour) result = player;

		if (result == null){
			throw new IllegalArgumentException("Could not find player for colour");
		}

		return result;
	}


	private void goNextPlayer(){
		if (++mCurrentRoundTurnsPlayed >= mTotalPlayers){
			mCurrentRoundTurnsPlayed = 0;
			if(!gameOverNotified) {
				notifyRotationComplete();
			}
		} else {
			requestMove(getPlayerInstanceByColour(getCurrentPlayer()));
		}
		mTotalTurnsPlayed++;
	}

	private boolean revealNextRound() {
		return getRounds().size() > mCurrentRound && getRounds().get(mCurrentRound);
	}

	private boolean revealRoundAfterNext() {
		return getRounds().size() > mCurrentRound + 1 && getRounds().get(mCurrentRound + 1);
	}

	private void makeSingleMrXTicketMove(Ticket ticket, int destination) {
		ScotlandYardPlayer mrX = getMrX();

		mrX.removeTicket(ticket);
		mrX.location(destination);
		notifyRound(++mCurrentRound);

		notifyMove(new TicketMove(mrX.colour(), ticket, destination));
	}

	private void maskedDoubleMove(Ticket firstTicket, int firstDestination, Ticket secondTicket, int secondDestination) {
		notifyMove(new DoubleMove(Black, firstTicket, firstDestination, secondTicket, secondDestination));

		makeSingleMrXTicketMove(firstTicket,firstDestination);
		makeSingleMrXTicketMove(secondTicket,secondDestination);
		mTotalTurnsPlayed++;
	}

	private boolean haveSpectators() {
		return getSpectators().size() > 0;
	}

	private Move modifyMoveForRevealRounds(Move move) {
		if (move instanceof TicketMove) {
			if (!isRevealRound()) {
				TicketMove casted = (TicketMove) move;
				move = new TicketMove(casted.colour(), casted.ticket(), getPlayerLocation(Black));
			}
		} else if (move instanceof DoubleMove) {
			DoubleMove casted = (DoubleMove) move;

			Ticket firstTicket = casted.firstMove().ticket();
			int firstDestination = casted.firstMove().destination();

			Ticket secondTicket = casted.secondMove().ticket();
			int secondDestination = casted.secondMove().destination();

			if (revealNextRound() && revealRoundAfterNext()) {
				maskedDoubleMove(firstTicket, firstDestination, secondTicket, secondDestination);
			} else if (revealNextRound() && !revealRoundAfterNext()) {
				maskedDoubleMove(firstTicket, firstDestination, secondTicket, firstDestination);
			} else if (!revealNextRound() && revealRoundAfterNext()) {
				maskedDoubleMove(firstTicket, getPlayerLocation(Black), secondTicket, secondDestination);
			} else {
				maskedDoubleMove(firstTicket, getPlayerLocation(Black), secondTicket, getPlayerLocation(Black));
			}
		}

		return move;
	}

	private void notifyMove(Move move) {
		if (haveSpectators() && !gameOverNotified) {
			for (Spectator spectator : getSpectators()) {
				spectator.onMoveMade(this, move);
				String colour;
				switch(move.colour()){
					case Black:
						colour = "Black";
						break;
					case Red:
						colour = "Red";
						break;
					case Yellow:
						colour = "Yellow";
						break;
					case Green:
						colour = "Green";
						break;
					case Blue:
						colour = "Blue";
						break;
					case White:
						colour = "Privileged";
						break;
					default:
						colour = "unknown";
						break;
				}
				DEBUG_PRINT(colour);
			}
		} else if (gameOverNotified) {
			DEBUG_PRINT("X");
		}
	}

	private void notifyMoves(Move... moves){
		requireNonNull(moves);

		for (Move move : moves) {
			if (move.colour().isMrX()) {
				notifyRound(mCurrentRound);
				move = modifyMoveForRevealRounds(move);
			} else {
				notifyMove(move);
				if (mrXIsCaptured()) {
					isGameOver();
				}
			}
		}

	}
	private void processMove(ScotlandYardPlayer player, Move move) {
		if (move instanceof TicketMove) {
			TicketMove casted = (TicketMove) move;

			if (player.isMrX()) {
				makeSingleMrXTicketMove(casted.ticket(),casted.destination());
			} else {
				player.location(casted.destination());
				player.removeTicket(casted.ticket());

				// detectives' consumed tickets are given to Mr. X
				getMrX().addTicket(casted.ticket());
			}

		} else if (move instanceof DoubleMove) {
			player.removeTicket(Double);
		} else if (move instanceof PassMove) {
			// do nothing for PassMoves
		} else {
			throw new IllegalArgumentException("Illegal move");
		}

		notifyMoves(move);
	}

	private void requestMove(ScotlandYardPlayer player) {
		requireNonNull(player);

		Set<Move> moves = getValidMoves(player);
		player.player().makeMove(this, player.location(), moves, this);
	}

	@Override
	public void accept(Move move) {
		requireNonNull(move);

		ScotlandYardPlayer player = getPlayerInstanceByColour(move.colour());
		Set<Move> valid = getValidMoves(player);
		if (valid.contains(move)){
			processMove(player, move);

			// recalculate occupied spaces
			getOccupiedLocations();

			goNextPlayer();
		} else {
			throw new IllegalArgumentException("Illegal move");
		}
	}

	private void notifyRotationComplete() {
		if (haveSpectators()) {
			for (Spectator spectator : getSpectators()) {
				spectator.onRotationComplete(this);
			}
		}
		DEBUG_PRINT("rotation complete");
	}

	@Override
	public void startRotate() {
		if (isGameOver()) {
			throw new IllegalStateException("Game's already over");
		}

		notifyRound(mCurrentRound);
		requestMove(getPlayerInstanceByColour(getCurrentPlayer()));

		if (getRoundsRemaining() == 0) {
			isGameOver(); // ?
		}
	}

	private void notifyRound(int round) {
		for (Spectator spectator : getSpectators()) {
			spectator.onRoundStarted(this,round);
			DEBUG_PRINT("start " + round);
		}
	}

	private int getRoundsRemaining() {
		return mRounds.size() - mCurrentRound;
	}

	private ArrayList<Integer> getOccupiedLocations() {
		ArrayList<Integer> result = new ArrayList();

		for (ScotlandYardPlayer p: mScotlandYardPlayers) {
			if(!p.colour().isMrX()) {
				result.add(p.location());
			}
		}

		return result;
	}


	private Set<Move> getValidMoves(ScotlandYardPlayer player) {
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
	public List<Colour> getPlayers() {
		ArrayList<Colour> players = new ArrayList<Colour>();

		for (ScotlandYardPlayer player : mScotlandYardPlayers) {
			players.add(player.colour());
		}

		return Collections.unmodifiableList(players);
	}

	private int updateLastRevealed () {
		setRevealed();
		mLastRevealedBlack = getMrX().location();

		return mLastRevealedBlack;
	}


	@Override
	public boolean isRevealRound() {
		if (checkRevealed()) {
			return getRounds().get(getCurrentRound() - 1);
		} else {
			int currentRound = getCurrentRound();
			return 	(
					mTotalTurnsPlayed != 0
					&& currentRound > 0
					&& currentRound - 1 < getRounds().size()
					&& getRounds().get(currentRound - 1)
			);
		}
	}

	@Override
	public int getPlayerLocation(Colour colour) {
		if (colour.isMrX()) {
			if (isRevealRound())
				updateLastRevealed();

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
			result = result || (player.colour() != Black && mrX.location() == player.location()); // if this is true once, then Mr. X has been captured
		}

		return result;
	}

	@Override
	public Set<Colour> getWinningPlayers() {
		return Collections.unmodifiableSet(mWinningPlayers);
	}


	@Override
	public boolean isGameOver() {
		boolean detectivesWin = false;
		boolean mrXWins = false;

		// MR.X WINS
		if (allDetectivesAreTicketless() || noDetectivesHaveValidMoves() || allRoundsAreUsedUp()) {
			mrXWins = true;

			mWinningPlayers.add(Black);
		}

		// DETECTIVES WIN
		if (mrXHasNoValidMoves() || mrXIsCaptured()){
			detectivesWin = true;

			for (ScotlandYardPlayer player : mScotlandYardPlayers) {
				if (player.isDetective()) {
					mWinningPlayers.add(player.colour());
				}
			}
		}

		boolean result = mrXWins || detectivesWin;
		if(result && haveSpectators() && !gameOverNotified){
			for (Spectator spectator : getSpectators()) {
				spectator.onGameOver(this, getWinningPlayers());
				DEBUG_PRINT("GAME OVER");
			}
			gameOverNotified = true;
		}
		return result;
	}

	@Override
	public Colour getCurrentPlayer() {
		return mScotlandYardPlayers.get(mCurrentRoundTurnsPlayed).colour();
	}

	@Override
	public int getCurrentRound() {
		// TODO: check comment no longer needed here
//		int result = checkRevealed() ? mCurrentRound : 0;
//		return result;
		return mCurrentRound;
	}

	@Override
	public List<Boolean> getRounds() {
		return Collections.unmodifiableList(mRounds);
	}

	@Override
	public Graph<Integer, Transport> getGraph() {
		return new ImmutableGraph<>(mGraph);
	}

	/// SPECTATORS

	private List<Spectator> mSpectators = new ArrayList<>();
	// Refer to:
	// https://www.ole.bris.ac.uk/bbcswebdav/courses/COMS10001_2016/students/model/index.html#Game_sequence
	// for order of spectator notifications

	@Override
	public Collection<Spectator> getSpectators() {
		// TODO: implement

		return Collections.unmodifiableList(requireNonNull(mSpectators));
	}

	@Override
	public void registerSpectator(Spectator spectator) {
		// TODO: implement
		requireNonNull(spectator);

		if (mSpectators.contains(spectator)) {
			throw new IllegalArgumentException("Spectator already registered");
		}

		mSpectators.add(spectator);
	}

	@Override
	public void unregisterSpectator(Spectator spectator) {
		// TODO: implement
		requireNonNull(spectator);

		if (!mSpectators.contains(spectator)) {
			throw new IllegalArgumentException("Spectator has not been registered");
		}

		mSpectators.remove(spectator);
	}

}