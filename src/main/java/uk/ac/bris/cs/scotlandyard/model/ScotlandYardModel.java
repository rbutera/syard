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

				// players cannot have duplicate locations, colours, and there are
				// limitations for which tickets certain players can have
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
					if (config.colour != Black && (config.tickets.get(Ticket.Secret) != 0 || config.tickets.get(Ticket.Double) != 0)) {
						throw new IllegalArgumentException("Detectives cannot have Secret or Double tickets");
					}
				}

				// verify member variables are not empty
				if (rounds.isEmpty()) {throw new IllegalArgumentException("Empty rounds");}
				if (graph.isEmpty()) {throw new IllegalArgumentException("Empty graph");}
				if (graph.isEmpty()) {throw new IllegalArgumentException("Empty graph");}
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
		// TODO
		throw new RuntimeException("Implement me");
	}

	@Override
	public Collection<Spectator> getSpectators() {
		// TODO
		throw new RuntimeException("Implement me");
	}

	@Override
	public List<Colour> getPlayers() {
		// TODO
		throw new RuntimeException("Implement me");
	}

	@Override
	public Set<Colour> getWinningPlayers() {
		// TODO
		throw new RuntimeException("Implement me");
	}

	@Override
	public int getPlayerLocation(Colour colour) {
		// TODO
		throw new RuntimeException("Implement me");
	}

	@Override
	public int getPlayerTickets(Colour colour, Ticket ticket) {
		// TODO
		throw new RuntimeException("Implement me");
	}

	@Override
	public boolean isGameOver() {
		// TODO
		throw new RuntimeException("Implement me");
	}

	@Override
	public Colour getCurrentPlayer() {
		// TODO
		throw new RuntimeException("Implement me");
	}

	@Override
	public int getCurrentRound() {
		// TODO
		throw new RuntimeException("Implement me");
	}

	@Override
	public boolean isRevealRound() {
		// TODO
		throw new RuntimeException("Implement me");
	}

	@Override
	public List<Boolean> getRounds() {
		// TODO
		throw new RuntimeException("Implement me");
	}

	@Override
	public Graph<Integer, Transport> getGraph() {
		// TODO
		throw new RuntimeException("Implement me");
	}

}
