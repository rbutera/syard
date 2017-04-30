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

	private List<Boolean> mRounds;
	private Graph<Integer, Transport> mGraph;
	private ArrayList<ScotlandYardPlayer> mScotlandYardPlayers = new ArrayList<>();;
	private Set<Colour> mWinningPlayers = new HashSet<>();
	private int mCurrentRound = NOT_STARTED;
	private int mCurrentTurn = 0;
	private int mTotalPlayers = 0;
	private int mLastRevealedBlack = 0;
	private boolean mGameOverNotificationSent = false;
	private List<Spectator> mSpectators = new ArrayList<>();

	private boolean DEBUG_ENABLED = true;
	private void DEBUG_PRINT (String s) {
		if(DEBUG_ENABLED) {
			System.out.println(s);
		}
	}

	private boolean isGameOverNotificationSent() {
		return mGameOverNotificationSent;
	}

	private void sentGameOverNotification() {
		this.mGameOverNotificationSent = true;
	}

	private ArrayList<ScotlandYardPlayer> getScotlandYardPlayers() {
		return mScotlandYardPlayers;
	}

	private void validateAndImportRounds(List<Boolean> rounds) {
		requireNonNull(rounds);

		if (rounds.isEmpty()) {
			throw new IllegalArgumentException("Empty rounds");
		}

		this.mRounds = rounds;
	}

	private void validateAndImportGraph(Graph<Integer, Transport> graph) {
		requireNonNull(graph);

		if (graph.isEmpty()) {
			throw new IllegalArgumentException("Empty graph");
		}

		this.mGraph = graph;
	}

	private ArrayList<PlayerConfiguration> configurePlayers(PlayerConfiguration mrX, PlayerConfiguration firstDetective, PlayerConfiguration... restOfTheDetectives) {
		if (mrX.colour != Black) throw new IllegalArgumentException("MrX should be Black");

		ArrayList<PlayerConfiguration> configurations = new ArrayList<>();
		configurations.add(requireNonNull(mrX));
		configurations.add(requireNonNull(firstDetective));

		for (PlayerConfiguration config : restOfTheDetectives) {
			configurations.add(config);
		}
		return configurations;
	}

	public ScotlandYardModel(List<Boolean> rounds, Graph<Integer, Transport> graph,
							 PlayerConfiguration mrX, PlayerConfiguration firstDetective,
							 PlayerConfiguration... restOfTheDetectives) {

		validateAndImportRounds(rounds);
		validateAndImportGraph(graph);
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
				throw new IllegalArgumentException("Only Mr.X can have Secret or Double tickets");
			}

			// populate mScotlandYardPlayers with ScotlandYardPlayers derived from configurations
			addScotlandYardPlayer(new ScotlandYardPlayer(config.player, config.colour, config.location, config.tickets));
			incrementTotalPlayers();
		}
	}

	private void addScotlandYardPlayer(ScotlandYardPlayer player) {
		this.mScotlandYardPlayers.add(player);
	}

	private ScotlandYardPlayer getPlayerInstanceByColour (Colour colour){
		for (ScotlandYardPlayer player : getScotlandYardPlayers())
			if (player.colour() == colour) return player;

		return null;
	}

	private void setCurrentTurn(int n) {
		this.mCurrentTurn = n;
	}

	private void endTurn(){
		setCurrentTurn(getCurrentTurn() + 1);

		if (getCurrentTurn() >= getTotalPlayers()){
			notifyRotationComplete();

			if (getRoundsRemaining() == 0) {
				isGameOver();
			}

			setCurrentTurn(0);
		} else {
			requestNextMove();
		}
	}

	private int getTotalPlayers() {
		return this.mTotalPlayers;
	}

	private void incrementTotalPlayers() {
		this.mTotalPlayers++;
	}

	private void maskedDoubleMove(ScotlandYardPlayer player, Ticket firstTicket, int firstDestination, int trueFirstDestination, Ticket secondTicket, int secondDestination, int trueSecondDestination) {
		DoubleMove doubleMove = new DoubleMove(player.colour(), firstTicket, firstDestination, secondTicket, secondDestination);

		notifyMove(doubleMove);

		performTicketMove(player,doubleMove.firstMove(),trueFirstDestination);
		performTicketMove(player,doubleMove.secondMove(),trueSecondDestination);
	}

	// this overloaded method is called when only the second move's destination is not masked
	private void maskedDoubleMove(ScotlandYardPlayer player, Ticket firstTicket, int firstDestination, int trueFirstDestination, Ticket secondTicket, int secondDestination) {
		maskedDoubleMove(player,firstTicket,firstDestination,trueFirstDestination,secondTicket,secondDestination,secondDestination);
	}

	// this overloaded method is called when only the first move's destination is not masked
	private void maskedDoubleMove(ScotlandYardPlayer player, Ticket firstTicket, int firstDestination, Ticket secondTicket, int secondDestination, int trueSecondDestination) {
		maskedDoubleMove(player,firstTicket,firstDestination,firstDestination,secondTicket,secondDestination,trueSecondDestination);
	}

	// this overloaded method is called when both the first and second moves' destinations are not masked
	private void maskedDoubleMove(ScotlandYardPlayer player, Ticket firstTicket, int firstDestination, Ticket secondTicket, int secondDestination) {
		maskedDoubleMove(player,firstTicket,firstDestination,firstDestination,secondTicket,secondDestination,secondDestination);
	}

	private boolean canNotifySpectators() {
		return !getSpectators().isEmpty() && !isGameOverNotificationSent();
	}

	private void notifyMove(Move move) {
		if (canNotifySpectators()) {
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
				DEBUG_PRINT(" -> " + colour + "'s turn");
			}
		}
	}
	private void performDoubleMove(ScotlandYardPlayer player, DoubleMove doubleMove){
		player.removeTicket(Double);
		int currentLocation = getPlayerLocation(player.colour());

		Ticket firstTicket = doubleMove.firstMove().ticket();
		int firstDestination = doubleMove.firstMove().destination();

		Ticket secondTicket = doubleMove.secondMove().ticket();
		int secondDestination = doubleMove.secondMove().destination();

		boolean isRevealRoundInOneRound = isRevealRound(1);
		boolean isRevealRoundInTwoRounds = isRevealRound(2);

		if (isRevealRoundInOneRound && isRevealRoundInTwoRounds) {
			// the next round and the around after are both reveal rounds
			maskedDoubleMove(player, firstTicket, firstDestination, secondTicket, secondDestination);
		} else if (isRevealRoundInOneRound && !isRevealRoundInTwoRounds) {
			// the next round is a reveal round and the round after is a hidden round
			maskedDoubleMove(player, firstTicket, firstDestination, secondTicket, firstDestination, secondDestination);
		} else if (!isRevealRoundInOneRound && isRevealRoundInTwoRounds) {
			// the next round is a hidden round and the round after is a reveal round
			maskedDoubleMove(player, firstTicket, currentLocation, firstDestination, secondTicket, secondDestination);
		} else {
			// the next round and the round after are both hidden rounds
			maskedDoubleMove(player, firstTicket, currentLocation, firstDestination, secondTicket, currentLocation, secondDestination);
		}
	}

	private void performTicketMove(ScotlandYardPlayer player, TicketMove move, int trueDestination) {
		Ticket ticket = move.ticket();

		player.removeTicket(ticket);
		player.location(trueDestination);

		if(player.isMrX()) notifyRound(incrementCurrentRound());
		if(player.isDetective()) getMrX().addTicket(ticket);

		notifyMove(move);
		if (mrXIsCaptured()) isGameOver();
	}

	// overloaded version to simplify calls where the destination hasn't been masked
	private void performTicketMove(ScotlandYardPlayer player, TicketMove move) {
		performTicketMove(player,move,move.destination());
	}

	private void performMove(ScotlandYardPlayer player, Move move) {
		if      (move instanceof TicketMove) performTicketMove(player,(TicketMove) move);
		else if (move instanceof DoubleMove) performDoubleMove(player,(DoubleMove) move);
		else if (move instanceof PassMove)   notifyMove(move);
	}

	private void requestNextMove() {
		ScotlandYardPlayer player = getPlayerInstanceByColour(getCurrentPlayer());
		player.player().makeMove(this, player.location(), getValidMoves(player), this);
	}

	@Override
	public void accept(Move move) {
		requireNonNull(move);

		ScotlandYardPlayer player = getPlayerInstanceByColour(move.colour());

		if (getValidMoves(player).contains(move)){
			performMove(player, move);
		} else {
			throw new IllegalArgumentException("Illegal move");
		}

		endTurn();
	}

	private void notifyRotationComplete() {
		if (canNotifySpectators()) {
			for (Spectator spectator : getSpectators()) {
				spectator.onRotationComplete(this);
			}
		}
		DEBUG_PRINT("====== END ROUND ======");
	}

	@Override
	public void startRotate() {
		if (isGameOver()) {
			throw new IllegalStateException("Game's already over");
		}

		requestNextMove();
	}

	private void notifyRound(int round) {
		for (Spectator spectator : getSpectators()) {
			spectator.onRoundStarted(this,round);
		}
		DEBUG_PRINT("====== ROUND:  " + round + " ======");
	}

	private int getRoundsRemaining() {
		return getRounds().size() - getCurrentRound();
	}

	private ArrayList<Integer> getOccupiedLocations() {
		ArrayList<Integer> result = new ArrayList<>();

		for (ScotlandYardPlayer player: getScotlandYardPlayers()) {
			if (player.isDetective()) {
				result.add(player.location());
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
			Boolean canSecret = player.isMrX() && player.hasTickets(Secret);
			Boolean canDouble = player.isMrX() && player.hasTickets(Double) && getRoundsRemaining() >= 2;

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

		if (colour.isDetective() && moves.isEmpty()) {
			moves.add(new PassMove(colour));
		}

		return moves;
	}

	@Override
	public List<Colour> getPlayers() {
		ArrayList<Colour> players = new ArrayList<>();

		for (ScotlandYardPlayer player : getScotlandYardPlayers()) {
			players.add(player.colour());
		}

		return Collections.unmodifiableList(players);
	}

	// overloaded version to check if it will be a reveal round in x rounds from now
	public boolean isRevealRound(int x) {
		x--; // accommodate zero-indexing of getRounds()
		int currentRound = getCurrentRound();
		return getRounds().size() > (currentRound + x)
				&& getRounds().get(currentRound + x);
	}

	@Override
	public boolean isRevealRound() {
		return isRevealRound(0);
	}

	@Override
	public int getPlayerLocation(Colour colour) {
		ScotlandYardPlayer player = getPlayerInstanceByColour(colour);
		int location = player.location();

		if (player.isMrX()) {
			if (getCurrentRound() == 0 || !isRevealRound()) return getLastRevealedBlack();
			else setLastRevealedBlack(location);
		}

		return location;
	}

	private int getLastRevealedBlack(){
		return this.mLastRevealedBlack;
	}

	private void setLastRevealedBlack(int n){
		this.mLastRevealedBlack = n;
	}

	@Override
	public int getPlayerTickets(Colour colour, Ticket ticket) {
		return getPlayerInstanceByColour(colour).tickets().get(ticket);
	}

	private boolean allDetectivesAreTicketless() {
		for (ScotlandYardPlayer player : getScotlandYardPlayers()) {
			if (player.isDetective() && !player.tickets().isEmpty()) return false;
		}

		return true;
	}

	private boolean noDetectivesHaveValidMoves() {
		boolean result = true;

		for (ScotlandYardPlayer player : getScotlandYardPlayers()) {
			if (player.isDetective()) {
				PassMove passMove = new PassMove(player.colour());

				// if this is false once, then at least one Detective isn't stuck
				result &= getValidMoves(player).contains(passMove);
			}
		}

		return result;
	}

	private boolean allRoundsAreUsedUp(){
		return getCurrentRound() >= getRounds().size();
	}

	private ScotlandYardPlayer getMrX(){
		return getPlayerInstanceByColour(Black);
	}

	private boolean mrXIsStuck(){
		return getValidMoves(getMrX()).isEmpty();
	}

	private boolean mrXIsCaptured() {
		boolean result = false;

		ScotlandYardPlayer mrX = getMrX();
		for (ScotlandYardPlayer player : getScotlandYardPlayers()) {
			result |= (player.colour() != mrX.colour() && mrX.location() == player.location()); // if this is true once, then Mr. X has been captured
		}

		return result;
	}

	@Override
	public Set<Colour> getWinningPlayers() {
		return Collections.unmodifiableSet(mWinningPlayers);
	}

	private void populateWinningPlayersSet(boolean winnerIsMrX) {
		for (Colour player : getPlayers())
			if ((player.isDetective() && !winnerIsMrX) || (player.isMrX() && winnerIsMrX))
				this.mWinningPlayers.add(player);
	}

	private void notifyGameOver() {
		if(canNotifySpectators()){
			for (Spectator spectator : getSpectators()) {
				spectator.onGameOver(this, getWinningPlayers());
			}
			DEBUG_PRINT("GAME OVER");
			sentGameOverNotification();
		}
	}

	@Override
	public boolean isGameOver() {
		boolean mrXWon;

		if (allDetectivesAreTicketless() || noDetectivesHaveValidMoves() || allRoundsAreUsedUp()) {
			mrXWon = true;
		} else if (mrXIsStuck() || mrXIsCaptured()){
			mrXWon = false;
		} else {
			return false;
		}

		populateWinningPlayersSet(mrXWon);

		notifyGameOver();

		return true;
	}

	@Override
	public Colour getCurrentPlayer() {
		return getPlayers().get(getCurrentTurn());
	}

	public int getCurrentTurn() {
		return this.mCurrentTurn;
	}

	@Override
	public int getCurrentRound() {
		return this.mCurrentRound;
	}

	private int incrementCurrentRound() {
		this.mCurrentRound = getCurrentRound() + 1;
		return getCurrentRound();
	}

	@Override
	public List<Boolean> getRounds() {
		return Collections.unmodifiableList(mRounds);
	}

	@Override
	public Graph<Integer, Transport> getGraph() {
		return new ImmutableGraph<>(mGraph);
	}

	@Override
	public Collection<Spectator> getSpectators() {
		return Collections.unmodifiableList(requireNonNull(mSpectators));
	}

	@Override
	public void registerSpectator(Spectator spectator) {
		requireNonNull(spectator);

		if (getSpectators().contains(spectator)) {
			throw new IllegalArgumentException("Spectator already registered");
		}

		this.mSpectators.add(spectator);
	}

	@Override
	public void unregisterSpectator(Spectator spectator) {
		requireNonNull(spectator);

		if (!getSpectators().contains(spectator)) {
			throw new IllegalArgumentException("Spectator has not been registered");
		}

		this.mSpectators.remove(spectator);
	}

}