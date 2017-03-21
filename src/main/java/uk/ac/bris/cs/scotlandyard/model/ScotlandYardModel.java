package uk.ac.bris.cs.scotlandyard.model;

import static java.util.Arrays.asList;
import static java.util.Collections.emptySet;
import static java.util.Collections.singletonList;
import static java.util.Collections.unmodifiableCollection;
import static java.util.Collections.unmodifiableList;
import static java.util.Collections.unmodifiableSet;
import static java.util.Objects.requireNonNull;
import static uk.ac.bris.cs.scotlandyard.model.Colour.Black;
import static uk.ac.bris.cs.scotlandyard.model.Ticket.Double;
import static uk.ac.bris.cs.scotlandyard.model.Ticket.Secret;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;
import uk.ac.bris.cs.gamekit.graph.Edge;
import uk.ac.bris.cs.gamekit.graph.Graph;
import uk.ac.bris.cs.gamekit.graph.ImmutableGraph;

// TODO implement all methods and pass all tests
public class ScotlandYardModel implements ScotlandYardGame {

	public List<Boolean> rounds;
	public Graph<Integer, Transport> graph;
	public List<ScotlandYardPlayer> syardplayers;
	public int currentround;

	public ScotlandYardModel(List<Boolean> rounds, Graph<Integer, Transport> graph,
			PlayerConfiguration mrX, PlayerConfiguration firstDetective,
			PlayerConfiguration... restOfTheDetectives) {
				// construct member variables, validating they're non-null
				this.rounds = requireNonNull(rounds);
				this.graph = requireNonNull(graph);

				ArrayList<PlayerConfiguration> configurations = new ArrayList<>();
				for (PlayerConfiguration config : restOfTheDetectives) {
					configurations.add(requireNonNull(config));
				}
				configurations.add(0, requireNonNull(firstDetective));
				configurations.add(0, requireNonNull(mrX));

				// mrX should be Black
				if (mrX.colour != Black) {throw new IllegalArgumentException("MrX should be Black");}

				// player validation
				Set<Integer> locationsset = new HashSet<>();
				Set<Colour> coloursset = new HashSet<>();
				for (PlayerConfiguration config : configurations) {
					// prevent duplicate locations
					if (locationsset.contains(config.location)) {throw new IllegalArgumentException("Duplicate location");}
					locationsset.add(config.location);

					// prevent duplicate colours
					if (coloursset.contains(config.colour)) {throw new IllegalArgumentException("Duplicate colour");}
					coloursset.add(config.colour);

					// ensure mapping for each Ticket exists
					if (!(config.tickets.containsKey(Ticket.Bus)
					   && config.tickets.containsKey(Ticket.Taxi)
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

				// verify member variables are not empty
				if (rounds.isEmpty()) {throw new IllegalArgumentException("Empty rounds");}
				if (graph.isEmpty()) {throw new IllegalArgumentException("Empty graph");}

				// construct players
				this.syardplayers = new ArrayList<ScotlandYardPlayer>();
				for (PlayerConfiguration config : configurations) {
					ScotlandYardPlayer syardplayer = new ScotlandYardPlayer(config.player, config.colour, config.location, config.tickets);
					this.syardplayers.add(syardplayer);
				}

				// at initialisation, the rounds have not started
				this.currentround = ScotlandYardGame.NOT_STARTED;
	}

	@Override
	public void registerSpectator(Spectator spectator) {
		// TODO
		throw new RuntimeException("Implement me");
	}

	@Override
	public void unregisterSpectator(Spectator spectator) {
		// TODO
		throw new RuntimeException("Implement me");
	}

	@Override
	public void startRotate() {
		// TODO: Complete this method

		// Workflow for implementation at:
		// https://www.ole.bris.ac.uk/bbcswebdav/courses/COMS10001_2016/students/model/index.html#example_workflow
		// (Scroll down to '3. Our next task is...')

		// Before Mr.X makes a move, the round number increments once so that Mr.X
		// will start on the first round.
		throw new RuntimeException("Implement me");
	}

	@Override
	public Collection<Spectator> getSpectators() {
		// TODO
		throw new RuntimeException("Implement me");
	}

	@Override
	public List<Colour> getPlayers() {
		ArrayList<Colour> players = new ArrayList<Colour>();
		for (ScotlandYardPlayer currentplayer : syardplayers) {
			players.add(currentplayer.colour());
		}
		return Collections.unmodifiableList(players);
	}

	@Override
	public Set<Colour> getWinningPlayers() {
		// TODO: Revisit this later
		return Collections.unmodifiableSet(Collections.<Colour>emptySet());
	}

	@Override
	public int getPlayerLocation(Colour colour) {
		if (!colour.isMrX()) {
			for (ScotlandYardPlayer currentplayer : syardplayers) {
				if (currentplayer.colour() == colour) {return currentplayer.location();}
			}
		}
		return 0;
	}

	@Override
	public int getPlayerTickets(Colour colour, Ticket ticket) {
		for (ScotlandYardPlayer currentplayer : syardplayers) {
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
		return syardplayers.get(currentround).colour();
	}

	@Override
	public int getCurrentRound() {
		return currentround;
	}

	@Override
	public boolean isRevealRound() {
		// TODO
		throw new RuntimeException("Implement me");
	}

	@Override
	public List<Boolean> getRounds() {
		return Collections.unmodifiableList(rounds);
	}

	@Override
	public Graph<Integer, Transport> getGraph() {
		return new ImmutableGraph<Integer, Transport>(graph);
	}

}
