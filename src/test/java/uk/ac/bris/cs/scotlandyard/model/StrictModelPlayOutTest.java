package uk.ac.bris.cs.scotlandyard.model;

import static java.lang.String.format;
import static java.util.Objects.hash;
import static java.util.Objects.nonNull;
import static java.util.Objects.requireNonNull;
import static java.util.function.Function.identity;
import static java.util.stream.Collectors.toMap;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anySet;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.spy;
import static uk.ac.bris.cs.scotlandyard.model.Colour.Black;
import static uk.ac.bris.cs.scotlandyard.model.Colour.Blue;
import static uk.ac.bris.cs.scotlandyard.model.Colour.Green;
import static uk.ac.bris.cs.scotlandyard.model.Colour.Red;
import static uk.ac.bris.cs.scotlandyard.model.Colour.White;
import static uk.ac.bris.cs.scotlandyard.model.Colour.Yellow;
import static uk.ac.bris.cs.scotlandyard.model.Ticket.Bus;
import static uk.ac.bris.cs.scotlandyard.model.Ticket.Secret;
import static uk.ac.bris.cs.scotlandyard.model.Ticket.Taxi;
import static uk.ac.bris.cs.scotlandyard.model.Ticket.Underground;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Stream;

import org.junit.Test;
import org.mockito.AdditionalAnswers;
import org.mockito.stubbing.Answer;
import org.mockito.stubbing.Stubber;

import uk.ac.bris.cs.gamekit.graph.Graph;
import uk.ac.bris.cs.gamekit.graph.ImmutableGraph;
import uk.ac.bris.cs.gamekit.graph.UndirectedGraph;

public class StrictModelPlayOutTest extends ModelTestBase {

	/**
	 * This test is exactly the same as
	 * {@link ModelSixPlayerPlayOutTest#testDetectiveWon()} but with extra print
	 * testing statements
	 *
	 */
	@Test
	public void testStrictlyDetectiveWon() throws Exception {
		PlayerConfiguration black = of(Black, 127);
		PlayerConfiguration blue = of(Blue, 53);
		PlayerConfiguration green = of(Green, 94);
		PlayerConfiguration red = of(Red, 155);
		PlayerConfiguration white = of(White, 29);
		PlayerConfiguration yellow = of(Yellow, 123);

		List<Boolean> rounds = Arrays.asList(
				false, false, true,
				false, false, false, false, true,
				false, false, false, false, true,
				false, false, false, false, true,
				false, false, false, false, true);

		ScotlandYardGame game = createGame(rounds, defaultGraph(),
				black, blue, green, red, white, yellow);
		StrictSoftEventAssert softly = new StrictSoftEventAssert();
		softly.bindSpectator(game);

		softly.givenPlayerWillSelect(black,
				x2(Black, Secret, 116, Secret, 127),
				x2(Black, Bus, 133, Secret, 141),
				ticket(Black, Taxi, 158),
				ticket(Black, Taxi, 142),
				ticket(Black, Bus, 157),
				ticket(Black, Taxi, 170),
				ticket(Black, Taxi, 159),
				ticket(Black, Secret, 172),
				ticket(Black, Secret, 187),
				ticket(Black, Taxi, 172),
				ticket(Black, Taxi, 128));

		softly.givenPlayerWillSelect(blue,
				ticket(Blue, Taxi, 69),
				ticket(Blue, Taxi, 86),
				ticket(Blue, Bus, 116),
				ticket(Blue, Taxi, 127),
				ticket(Blue, Bus, 133),
				ticket(Blue, Bus, 157),
				ticket(Blue, Bus, 133),
				ticket(Blue, Taxi, 140),
				ticket(Blue, Taxi, 156),
				ticket(Blue, Taxi, 140),
				ticket(Blue, Underground, 128));

		softly.givenPlayerWillSelect(green,
				ticket(Green, Bus, 77),
				ticket(Green, Taxi, 78),
				ticket(Green, Bus, 79),
				ticket(Green, Underground, 67),
				ticket(Green, Underground, 89),
				ticket(Green, Underground, 128),
				ticket(Green, Bus, 187),
				ticket(Green, Bus, 185),
				ticket(Green, Taxi, 170),
				ticket(Green, Taxi, 159));

		softly.givenPlayerWillSelect(red,
				ticket(Red, Taxi, 156),
				ticket(Red, Taxi, 140),
				ticket(Red, Bus, 133),
				ticket(Red, Taxi, 141),
				ticket(Red, Taxi, 158),
				ticket(Red, Taxi, 142),
				ticket(Red, Bus, 157),
				ticket(Red, Bus, 133),
				ticket(Red, Taxi, 141),
				ticket(Red, Taxi, 158));

		softly.givenPlayerWillSelect(white,
				ticket(White, Taxi, 42),
				ticket(White, Taxi, 72),
				ticket(White, Bus, 105),
				ticket(White, Taxi, 108),
				ticket(White, Taxi, 119),
				ticket(White, Taxi, 108),
				ticket(White, Taxi, 119),
				ticket(White, Taxi, 108),
				ticket(White, Taxi, 119),
				ticket(White, Taxi, 136));

		softly.givenPlayerWillSelect(yellow,
				ticket(Yellow, Taxi, 137),
				ticket(Yellow, Taxi, 123),
				ticket(Yellow, Taxi, 124),
				ticket(Yellow, Bus, 111),
				ticket(Yellow, Underground, 153),
				ticket(Yellow, Underground, 185),
				ticket(Yellow, Taxi, 170),
				ticket(Yellow, Taxi, 157),
				ticket(Yellow, Bus, 185),
				ticket(Yellow, Bus, 187));

		softly.startRotateUntilGameIsOver(game);
		softly.printEvents();

		// IMPORTANT: these assertions are smoke tests only, ideally, you would
		// compare your model's output with the correct one down below to see if
		// there are any obvious missing/wrongly-ordered events. Please use a diff tool
		// for this task as comparing by hand is too error-prone and painful

		assertThat(game.getCurrentRound()).isEqualTo(13);
		assertThat(game.isGameOver()).isTrue();
		assertThat(game.getWinningPlayers()).containsOnly(Green, Yellow, White, Red, Blue);

		// The output of a correct implementation looks like this:
		// NOTE: The order of players might be different but that shouldn't
		// matter much. Pay special attention towards the end of the sequence,
		// quite a lot of students either forgot to call Spectator#onGameOver or
		// has an extra Player#makeMove, both are wrong.

		// START OF SEQUENCE

		//		Player.makeMove(view={round=0, current=Black, reveal=true, over=false, winning=[]}, location=127, expected=Double[Black-(Secret)->116-(Secret)->127], actual=165)
		// -> Spectator.onMoveMade(view={round=0, current=Black, reveal=true, over=false, winning=[]}, move=Double[Black-(Secret)->0-(Secret)->0])
		// -> Spectator.onRoundStarted(view={round=1, current=Black, reveal=true, over=false, winning=[]}, round=1)
		// -> Spectator.onMoveMade(view={round=1, current=Black, reveal=true, over=false, winning=[]}, move=Ticket[Black-(Secret)->0])
		// -> Spectator.onRoundStarted(view={round=2, current=Black, reveal=true, over=false, winning=[]}, round=2)
		// -> Spectator.onMoveMade(view={round=2, current=Black, reveal=true, over=false, winning=[]}, move=Ticket[Black-(Secret)->0])
		//		Player.makeMove(view={round=2, current=Blue, reveal=true, over=false, winning=[]}, location=53, expected=Ticket[Blue-(Taxi)->69], actual=3)
		// -> Spectator.onMoveMade(view={round=2, current=Blue, reveal=true, over=false, winning=[]}, move=Ticket[Blue-(Taxi)->69])
		//		Player.makeMove(view={round=2, current=Green, reveal=true, over=false, winning=[]}, location=94, expected=Ticket[Green-(Bus)->77], actual=6)
		// -> Spectator.onMoveMade(view={round=2, current=Green, reveal=true, over=false, winning=[]}, move=Ticket[Green-(Bus)->77])
		//		Player.makeMove(view={round=2, current=Red, reveal=true, over=false, winning=[]}, location=155, expected=Ticket[Red-(Taxi)->156], actual=4)
		// -> Spectator.onMoveMade(view={round=2, current=Red, reveal=true, over=false, winning=[]}, move=Ticket[Red-(Taxi)->156])
		//		Player.makeMove(view={round=2, current=White, reveal=true, over=false, winning=[]}, location=29, expected=Ticket[White-(Taxi)->42], actual=9)
		// -> Spectator.onMoveMade(view={round=2, current=White, reveal=true, over=false, winning=[]}, move=Ticket[White-(Taxi)->42])
		//		Player.makeMove(view={round=2, current=Yellow, reveal=true, over=false, winning=[]}, location=123, expected=Ticket[Yellow-(Taxi)->137], actual=9)
		// -> Spectator.onMoveMade(view={round=2, current=Yellow, reveal=true, over=false, winning=[]}, move=Ticket[Yellow-(Taxi)->137])
		// -> Spectator.onRotationComplete(view={round=2, current=Black, reveal=true, over=false, winning=[]})
		//		Player.makeMove(view={round=2, current=Black, reveal=true, over=false, winning=[]}, location=127, expected=Double[Black-(Bus)->133-(Secret)->141], actual=165)
		// -> Spectator.onMoveMade(view={round=2, current=Black, reveal=true, over=false, winning=[]}, move=Double[Black-(Bus)->133-(Secret)->133])
		// -> Spectator.onRoundStarted(view={round=3, current=Black, reveal=true, over=false, winning=[]}, round=3)
		// -> Spectator.onMoveMade(view={round=3, current=Black, reveal=true, over=false, winning=[]}, move=Ticket[Black-(Bus)->133])
		// -> Spectator.onRoundStarted(view={round=4, current=Black, reveal=true, over=false, winning=[]}, round=4)
		// -> Spectator.onMoveMade(view={round=4, current=Black, reveal=true, over=false, winning=[]}, move=Ticket[Black-(Secret)->133])
		//		Player.makeMove(view={round=4, current=Blue, reveal=true, over=false, winning=[]}, location=69, expected=Ticket[Blue-(Taxi)->86], actual=4)
		// -> Spectator.onMoveMade(view={round=4, current=Blue, reveal=true, over=false, winning=[]}, move=Ticket[Blue-(Taxi)->86])
		//		Player.makeMove(view={round=4, current=Green, reveal=true, over=false, winning=[]}, location=77, expected=Ticket[Green-(Taxi)->78], actual=8)
		// -> Spectator.onMoveMade(view={round=4, current=Green, reveal=true, over=false, winning=[]}, move=Ticket[Green-(Taxi)->78])
		//		Player.makeMove(view={round=4, current=Red, reveal=true, over=false, winning=[]}, location=156, expected=Ticket[Red-(Taxi)->140], actual=8)
		// -> Spectator.onMoveMade(view={round=4, current=Red, reveal=true, over=false, winning=[]}, move=Ticket[Red-(Taxi)->140])
		//		Player.makeMove(view={round=4, current=White, reveal=true, over=false, winning=[]}, location=42, expected=Ticket[White-(Taxi)->72], actual=7)
		// -> Spectator.onMoveMade(view={round=4, current=White, reveal=true, over=false, winning=[]}, move=Ticket[White-(Taxi)->72])
		//		Player.makeMove(view={round=4, current=Yellow, reveal=true, over=false, winning=[]}, location=137, expected=Ticket[Yellow-(Taxi)->123], actual=2)
		// -> Spectator.onMoveMade(view={round=4, current=Yellow, reveal=true, over=false, winning=[]}, move=Ticket[Yellow-(Taxi)->123])
		// -> Spectator.onRotationComplete(view={round=4, current=Black, reveal=true, over=false, winning=[]})
		//		Player.makeMove(view={round=4, current=Black, reveal=true, over=false, winning=[]}, location=141, expected=Ticket[Black-(Taxi)->158], actual=8)
		// -> Spectator.onRoundStarted(view={round=5, current=Black, reveal=true, over=false, winning=[]}, round=5)
		// -> Spectator.onMoveMade(view={round=5, current=Black, reveal=true, over=false, winning=[]}, move=Ticket[Black-(Taxi)->133])
		//		Player.makeMove(view={round=5, current=Blue, reveal=true, over=false, winning=[]}, location=86, expected=Ticket[Blue-(Bus)->116], actual=7)
		// -> Spectator.onMoveMade(view={round=5, current=Blue, reveal=true, over=false, winning=[]}, move=Ticket[Blue-(Bus)->116])
		//		Player.makeMove(view={round=5, current=Green, reveal=true, over=false, winning=[]}, location=78, expected=Ticket[Green-(Bus)->79], actual=7)
		// -> Spectator.onMoveMade(view={round=5, current=Green, reveal=true, over=false, winning=[]}, move=Ticket[Green-(Bus)->79])
		//		Player.makeMove(view={round=5, current=Red, reveal=true, over=false, winning=[]}, location=140, expected=Ticket[Red-(Bus)->133], actual=13)
		// -> Spectator.onMoveMade(view={round=5, current=Red, reveal=true, over=false, winning=[]}, move=Ticket[Red-(Bus)->133])
		//		Player.makeMove(view={round=5, current=White, reveal=true, over=false, winning=[]}, location=72, expected=Ticket[White-(Bus)->105], actual=7)
		// -> Spectator.onMoveMade(view={round=5, current=White, reveal=true, over=false, winning=[]}, move=Ticket[White-(Bus)->105])
		//		Player.makeMove(view={round=5, current=Yellow, reveal=true, over=false, winning=[]}, location=123, expected=Ticket[Yellow-(Taxi)->124], actual=9)
		// -> Spectator.onMoveMade(view={round=5, current=Yellow, reveal=true, over=false, winning=[]}, move=Ticket[Yellow-(Taxi)->124])
		// -> Spectator.onRotationComplete(view={round=5, current=Black, reveal=true, over=false, winning=[]})
		//		Player.makeMove(view={round=5, current=Black, reveal=true, over=false, winning=[]}, location=158, expected=Ticket[Black-(Taxi)->142], actual=8)
		// -> Spectator.onRoundStarted(view={round=6, current=Black, reveal=true, over=false, winning=[]}, round=6)
		// -> Spectator.onMoveMade(view={round=6, current=Black, reveal=true, over=false, winning=[]}, move=Ticket[Black-(Taxi)->133])
		//		Player.makeMove(view={round=6, current=Blue, reveal=true, over=false, winning=[]}, location=116, expected=Ticket[Blue-(Taxi)->127], actual=8)
		// -> Spectator.onMoveMade(view={round=6, current=Blue, reveal=true, over=false, winning=[]}, move=Ticket[Blue-(Taxi)->127])
		//		Player.makeMove(view={round=6, current=Green, reveal=true, over=false, winning=[]}, location=79, expected=Ticket[Green-(Underground)->67], actual=10)
		// -> Spectator.onMoveMade(view={round=6, current=Green, reveal=true, over=false, winning=[]}, move=Ticket[Green-(Underground)->67])
		//		Player.makeMove(view={round=6, current=Red, reveal=true, over=false, winning=[]}, location=133, expected=Ticket[Red-(Taxi)->141], actual=4)
		// -> Spectator.onMoveMade(view={round=6, current=Red, reveal=true, over=false, winning=[]}, move=Ticket[Red-(Taxi)->141])
		//		Player.makeMove(view={round=6, current=White, reveal=true, over=false, winning=[]}, location=105, expected=Ticket[White-(Taxi)->108], actual=10)
		// -> Spectator.onMoveMade(view={round=6, current=White, reveal=true, over=false, winning=[]}, move=Ticket[White-(Taxi)->108])
		//		Player.makeMove(view={round=6, current=Yellow, reveal=true, over=false, winning=[]}, location=124, expected=Ticket[Yellow-(Bus)->111], actual=9)
		// -> Spectator.onMoveMade(view={round=6, current=Yellow, reveal=true, over=false, winning=[]}, move=Ticket[Yellow-(Bus)->111])
		// -> Spectator.onRotationComplete(view={round=6, current=Black, reveal=true, over=false, winning=[]})
		//		Player.makeMove(view={round=6, current=Black, reveal=true, over=false, winning=[]}, location=142, expected=Ticket[Black-(Bus)->157], actual=17)
		// -> Spectator.onRoundStarted(view={round=7, current=Black, reveal=true, over=false, winning=[]}, round=7)
		// -> Spectator.onMoveMade(view={round=7, current=Black, reveal=true, over=false, winning=[]}, move=Ticket[Black-(Bus)->133])
		//		Player.makeMove(view={round=7, current=Blue, reveal=true, over=false, winning=[]}, location=127, expected=Ticket[Blue-(Bus)->133], actual=8)
		// -> Spectator.onMoveMade(view={round=7, current=Blue, reveal=true, over=false, winning=[]}, move=Ticket[Blue-(Bus)->133])
		//		Player.makeMove(view={round=7, current=Green, reveal=true, over=false, winning=[]}, location=67, expected=Ticket[Green-(Underground)->89], actual=12)
		// -> Spectator.onMoveMade(view={round=7, current=Green, reveal=true, over=false, winning=[]}, move=Ticket[Green-(Underground)->89])
		//		Player.makeMove(view={round=7, current=Red, reveal=true, over=false, winning=[]}, location=141, expected=Ticket[Red-(Taxi)->158], actual=3)
		// -> Spectator.onMoveMade(view={round=7, current=Red, reveal=true, over=false, winning=[]}, move=Ticket[Red-(Taxi)->158])
		//		Player.makeMove(view={round=7, current=White, reveal=true, over=false, winning=[]}, location=108, expected=Ticket[White-(Taxi)->119], actual=6)
		// -> Spectator.onMoveMade(view={round=7, current=White, reveal=true, over=false, winning=[]}, move=Ticket[White-(Taxi)->119])
		//		Player.makeMove(view={round=7, current=Yellow, reveal=true, over=false, winning=[]}, location=111, expected=Ticket[Yellow-(Underground)->153], actual=9)
		// -> Spectator.onMoveMade(view={round=7, current=Yellow, reveal=true, over=false, winning=[]}, move=Ticket[Yellow-(Underground)->153])
		// -> Spectator.onRotationComplete(view={round=7, current=Black, reveal=true, over=false, winning=[]})
		//		Player.makeMove(view={round=7, current=Black, reveal=true, over=false, winning=[]}, location=157, expected=Ticket[Black-(Taxi)->170], actual=11)
		// -> Spectator.onRoundStarted(view={round=8, current=Black, reveal=true, over=false, winning=[]}, round=8)
		// -> Spectator.onMoveMade(view={round=8, current=Black, reveal=true, over=false, winning=[]}, move=Ticket[Black-(Taxi)->170])
		//		Player.makeMove(view={round=8, current=Blue, reveal=true, over=false, winning=[]}, location=133, expected=Ticket[Blue-(Bus)->157], actual=6)
		// -> Spectator.onMoveMade(view={round=8, current=Blue, reveal=true, over=false, winning=[]}, move=Ticket[Blue-(Bus)->157])
		//		Player.makeMove(view={round=8, current=Green, reveal=true, over=false, winning=[]}, location=89, expected=Ticket[Green-(Underground)->128], actual=9)
		// -> Spectator.onMoveMade(view={round=8, current=Green, reveal=true, over=false, winning=[]}, move=Ticket[Green-(Underground)->128])
		//		Player.makeMove(view={round=8, current=Red, reveal=true, over=false, winning=[]}, location=158, expected=Ticket[Red-(Taxi)->142], actual=3)
		// -> Spectator.onMoveMade(view={round=8, current=Red, reveal=true, over=false, winning=[]}, move=Ticket[Red-(Taxi)->142])
		//		Player.makeMove(view={round=8, current=White, reveal=true, over=false, winning=[]}, location=119, expected=Ticket[White-(Taxi)->108], actual=3)
		// -> Spectator.onMoveMade(view={round=8, current=White, reveal=true, over=false, winning=[]}, move=Ticket[White-(Taxi)->108])
		//		Player.makeMove(view={round=8, current=Yellow, reveal=true, over=false, winning=[]}, location=153, expected=Ticket[Yellow-(Underground)->185], actual=13)
		// -> Spectator.onMoveMade(view={round=8, current=Yellow, reveal=true, over=false, winning=[]}, move=Ticket[Yellow-(Underground)->185])
		// -> Spectator.onRotationComplete(view={round=8, current=Black, reveal=true, over=false, winning=[]})
		//		Player.makeMove(view={round=8, current=Black, reveal=true, over=false, winning=[]}, location=170, expected=Ticket[Black-(Taxi)->159], actual=2)
		// -> Spectator.onRoundStarted(view={round=9, current=Black, reveal=true, over=false, winning=[]}, round=9)
		// -> Spectator.onMoveMade(view={round=9, current=Black, reveal=true, over=false, winning=[]}, move=Ticket[Black-(Taxi)->170])
		//		Player.makeMove(view={round=9, current=Blue, reveal=true, over=false, winning=[]}, location=157, expected=Ticket[Blue-(Bus)->133], actual=5)
		// -> Spectator.onMoveMade(view={round=9, current=Blue, reveal=true, over=false, winning=[]}, move=Ticket[Blue-(Bus)->133])
		//		Player.makeMove(view={round=9, current=Green, reveal=true, over=false, winning=[]}, location=128, expected=Ticket[Green-(Bus)->187], actual=10)
		// -> Spectator.onMoveMade(view={round=9, current=Green, reveal=true, over=false, winning=[]}, move=Ticket[Green-(Bus)->187])
		//		Player.makeMove(view={round=9, current=Red, reveal=true, over=false, winning=[]}, location=142, expected=Ticket[Red-(Bus)->157], actual=10)
		// -> Spectator.onMoveMade(view={round=9, current=Red, reveal=true, over=false, winning=[]}, move=Ticket[Red-(Bus)->157])
		//		Player.makeMove(view={round=9, current=White, reveal=true, over=false, winning=[]}, location=108, expected=Ticket[White-(Taxi)->119], actual=6)
		// -> Spectator.onMoveMade(view={round=9, current=White, reveal=true, over=false, winning=[]}, move=Ticket[White-(Taxi)->119])
		//		Player.makeMove(view={round=9, current=Yellow, reveal=true, over=false, winning=[]}, location=185, expected=Ticket[Yellow-(Taxi)->170], actual=6)
		// -> Spectator.onMoveMade(view={round=9, current=Yellow, reveal=true, over=false, winning=[]}, move=Ticket[Yellow-(Taxi)->170])
		// -> Spectator.onRotationComplete(view={round=9, current=Black, reveal=true, over=false, winning=[]})
		//		Player.makeMove(view={round=9, current=Black, reveal=true, over=false, winning=[]}, location=159, expected=Ticket[Black-(Secret)->172], actual=8)
		// -> Spectator.onRoundStarted(view={round=10, current=Black, reveal=true, over=false, winning=[]}, round=10)
		// -> Spectator.onMoveMade(view={round=10, current=Black, reveal=true, over=false, winning=[]}, move=Ticket[Black-(Secret)->170])
		//		Player.makeMove(view={round=10, current=Blue, reveal=true, over=false, winning=[]}, location=133, expected=Ticket[Blue-(Taxi)->140], actual=5)
		// -> Spectator.onMoveMade(view={round=10, current=Blue, reveal=true, over=false, winning=[]}, move=Ticket[Blue-(Taxi)->140])
		//		Player.makeMove(view={round=10, current=Green, reveal=true, over=false, winning=[]}, location=187, expected=Ticket[Green-(Bus)->185], actual=6)
		// -> Spectator.onMoveMade(view={round=10, current=Green, reveal=true, over=false, winning=[]}, move=Ticket[Green-(Bus)->185])
		//		Player.makeMove(view={round=10, current=Red, reveal=true, over=false, winning=[]}, location=157, expected=Ticket[Red-(Bus)->133], actual=5)
		// -> Spectator.onMoveMade(view={round=10, current=Red, reveal=true, over=false, winning=[]}, move=Ticket[Red-(Bus)->133])
		//		Player.makeMove(view={round=10, current=White, reveal=true, over=false, winning=[]}, location=119, expected=Ticket[White-(Taxi)->108], actual=3)
		// -> Spectator.onMoveMade(view={round=10, current=White, reveal=true, over=false, winning=[]}, move=Ticket[White-(Taxi)->108])
		//		Player.makeMove(view={round=10, current=Yellow, reveal=true, over=false, winning=[]}, location=170, expected=Ticket[Yellow-(Taxi)->157], actual=2)
		// -> Spectator.onMoveMade(view={round=10, current=Yellow, reveal=true, over=false, winning=[]}, move=Ticket[Yellow-(Taxi)->157])
		// -> Spectator.onRotationComplete(view={round=10, current=Black, reveal=true, over=false, winning=[]})
		//		Player.makeMove(view={round=10, current=Black, reveal=true, over=false, winning=[]}, location=172, expected=Ticket[Black-(Secret)->187], actual=6)
		// -> Spectator.onRoundStarted(view={round=11, current=Black, reveal=true, over=false, winning=[]}, round=11)
		// -> Spectator.onMoveMade(view={round=11, current=Black, reveal=true, over=false, winning=[]}, move=Ticket[Black-(Secret)->170])
		//		Player.makeMove(view={round=11, current=Blue, reveal=true, over=false, winning=[]}, location=140, expected=Ticket[Blue-(Taxi)->156], actual=11)
		// -> Spectator.onMoveMade(view={round=11, current=Blue, reveal=true, over=false, winning=[]}, move=Ticket[Blue-(Taxi)->156])
		//		Player.makeMove(view={round=11, current=Green, reveal=true, over=false, winning=[]}, location=185, expected=Ticket[Green-(Taxi)->170], actual=7)
		// -> Spectator.onMoveMade(view={round=11, current=Green, reveal=true, over=false, winning=[]}, move=Ticket[Green-(Taxi)->170])
		//		Player.makeMove(view={round=11, current=Red, reveal=true, over=false, winning=[]}, location=133, expected=Ticket[Red-(Taxi)->141], actual=5)
		// -> Spectator.onMoveMade(view={round=11, current=Red, reveal=true, over=false, winning=[]}, move=Ticket[Red-(Taxi)->141])
		//		Player.makeMove(view={round=11, current=White, reveal=true, over=false, winning=[]}, location=108, expected=Ticket[White-(Taxi)->119], actual=6)
		// -> Spectator.onMoveMade(view={round=11, current=White, reveal=true, over=false, winning=[]}, move=Ticket[White-(Taxi)->119])
		//		Player.makeMove(view={round=11, current=Yellow, reveal=true, over=false, winning=[]}, location=157, expected=Ticket[Yellow-(Bus)->185], actual=4)
		// -> Spectator.onMoveMade(view={round=11, current=Yellow, reveal=true, over=false, winning=[]}, move=Ticket[Yellow-(Bus)->185])
		// -> Spectator.onRotationComplete(view={round=11, current=Black, reveal=true, over=false, winning=[]})
		//		Player.makeMove(view={round=11, current=Black, reveal=true, over=false, winning=[]}, location=187, expected=Ticket[Black-(Taxi)->172], actual=5)
		// -> Spectator.onRoundStarted(view={round=12, current=Black, reveal=true, over=false, winning=[]}, round=12)
		// -> Spectator.onMoveMade(view={round=12, current=Black, reveal=true, over=false, winning=[]}, move=Ticket[Black-(Taxi)->170])
		//		Player.makeMove(view={round=12, current=Blue, reveal=true, over=false, winning=[]}, location=156, expected=Ticket[Blue-(Taxi)->140], actual=8)
		// -> Spectator.onMoveMade(view={round=12, current=Blue, reveal=true, over=false, winning=[]}, move=Ticket[Blue-(Taxi)->140])
		//		Player.makeMove(view={round=12, current=Green, reveal=true, over=false, winning=[]}, location=170, expected=Ticket[Green-(Taxi)->159], actual=2)
		// -> Spectator.onMoveMade(view={round=12, current=Green, reveal=true, over=false, winning=[]}, move=Ticket[Green-(Taxi)->159])
		//		Player.makeMove(view={round=12, current=Red, reveal=true, over=false, winning=[]}, location=141, expected=Ticket[Red-(Taxi)->158], actual=4)
		// -> Spectator.onMoveMade(view={round=12, current=Red, reveal=true, over=false, winning=[]}, move=Ticket[Red-(Taxi)->158])
		//		Player.makeMove(view={round=12, current=White, reveal=true, over=false, winning=[]}, location=119, expected=Ticket[White-(Taxi)->136], actual=3)
		// -> Spectator.onMoveMade(view={round=12, current=White, reveal=true, over=false, winning=[]}, move=Ticket[White-(Taxi)->136])
		//		Player.makeMove(view={round=12, current=Yellow, reveal=true, over=false, winning=[]}, location=185, expected=Ticket[Yellow-(Bus)->187], actual=8)
		// -> Spectator.onMoveMade(view={round=12, current=Yellow, reveal=true, over=false, winning=[]}, move=Ticket[Yellow-(Bus)->187])
		// -> Spectator.onRotationComplete(view={round=12, current=Black, reveal=true, over=false, winning=[]})
		//		Player.makeMove(view={round=12, current=Black, reveal=true, over=false, winning=[]}, location=172, expected=Ticket[Black-(Taxi)->128], actual=1)
		// -> Spectator.onRoundStarted(view={round=13, current=Black, reveal=true, over=false, winning=[]}, round=13)
		// -> Spectator.onMoveMade(view={round=13, current=Black, reveal=true, over=false, winning=[]}, move=Ticket[Black-(Taxi)->128])
		//		Player.makeMove(view={round=13, current=Blue, reveal=true, over=false, winning=[]}, location=140, expected=Ticket[Blue-(Underground)->128], actual=13)
		// -> Spectator.onMoveMade(view={round=13, current=Blue, reveal=true, over=true, winning=[White, Red, Blue, Yellow, Green]}, move=Ticket[Blue-(Underground)->128])
		// -> Spectator.onGameOver(view={round=13, current=Green, reveal=true, over=true, winning=[White, Red, Blue, Yellow, Green]}, winner=[White, Red, Blue, Yellow, Green])

		// END OF SEQUENCE

	}

	/**
	 * Internal event recording rig for {@link ScotlandYardGame}, you do not
	 * need to understand any of this
	 */
	static class StrictSoftEventAssert {

		private final List<Event> events = new ArrayList<>();
		private final EventRecordingSpectator spectator = spy(new EventRecordingSpectator(events));

		void givenPlayerWillSelect(PlayerConfiguration configuration, Move first, Move... rest) {
			Stubber stubber = doAnswer(recordThenSelect(first));
			for (Move move : rest)
				stubber.doAnswer(recordThenSelect(move));
			stubber.doAnswer(recordUnwanted());
			stubber.when(configuration.player).makeMove(any(), anyInt(), anySet(), any());
		}

		Spectator bindSpectator(ScotlandYardGame game) {
			game.registerSpectator(spectator);
			return spectator;
		}

		void startRotateUntilGameIsOver(ScotlandYardGame game) {
			game.startRotate();
		}

		void printEvents() {
			events.forEach(System.out::println);
		}

		private Answer<Void> recordThenSelect(Move move) {
			nonNull(move);
			return AdditionalAnswers.<ScotlandYardView, Integer, Set<Move>,
					Consumer<Move>> answerVoid((
							view, location, moves, callback) -> {
						events.add(new PlayerMakeMoveEvent(view, location, move, moves));
						assertThat(moves)
								.as("[Location %s] trying to select %s from given valid moves of %s",
										location, moves, move)
								.contains(move);
						callback.accept(move);
					});
		}

		private Answer<Void> recordUnwanted() {
			return AdditionalAnswers.<ScotlandYardView, Integer, Set<Move>,
					Consumer<Move>> answerVoid((
							view,
							location,
							moves,
							callback) -> events
									.add(new PlayerMakeMoveEvent(view, location, null, moves)));
		}

		private static abstract class Event {

			final ImmutableScotlandYardView view;

			Event(ScotlandYardView view) {
				this.view = ImmutableScotlandYardView.snapshot(view);
			}

			@Override
			public boolean equals(Object o) {
				if (this == o) return true;
				if (o == null || getClass() != o.getClass()) return false;
				Event event = (Event) o;
				return Objects.equals(view, event.view);
			}

			@Override
			public int hashCode() {
				return hash(view);
			}

		}

		private static final class ClientStartRotateEvent extends Event {

			ClientStartRotateEvent(ScotlandYardView view) {
				super(view);
			}

			@Override
			public String toString() {
				return format("ClientStartRotateEvent{view=%s}", view);
			}
		}

		private static final class PlayerMakeMoveEvent extends Event {

			private final int location;
			private final Move expected;
			private final Set<Move> actual;

			PlayerMakeMoveEvent(ScotlandYardView view, int location, Move expected,
					Set<Move> actual) {
				super(view);
				this.location = location;
				this.expected = expected;
				this.actual = actual;
			}

			@Override
			public boolean equals(Object o) {
				if (this == o) return true;
				if (o == null || getClass() != o.getClass()) return false;
				if (!super.equals(o)) return false;
				PlayerMakeMoveEvent that = (PlayerMakeMoveEvent) o;
				return location == that.location &&
						Objects.equals(expected, that.expected) &&
						Objects.equals(actual, that.actual);
			}

			@Override
			public int hashCode() {
				return hash(super.hashCode(), location, expected, actual);
			}

			@Override
			public String toString() {
				return format(
						"Player.makeMove(view=%s, location=%d, expected=%s, actual=%s)",
						view, location, expected, actual.size());
			}
		}

		private static final class MoveMadeEvent extends Event {

			private final Move move;

			MoveMadeEvent(ImmutableScotlandYardView view, Move move) {
				super(view);
				this.move = move;
			}

			@Override
			public boolean equals(Object o) {
				if (this == o) return true;
				if (o == null || getClass() != o.getClass()) return false;
				if (!super.equals(o)) return false;
				MoveMadeEvent that = (MoveMadeEvent) o;
				return Objects.equals(move, that.move);
			}

			@Override
			public int hashCode() {
				return hash(super.hashCode(), move);
			}

			@Override
			public String toString() {
				return format(" -> Spectator.onMoveMade(view=%s, move=%s)", view, move);
			}
		}

		private static final class RoundStartedEvent extends Event {

			private final int round;

			RoundStartedEvent(ScotlandYardView view, int round) {
				super(view);
				this.round = round;
			}

			@Override
			public boolean equals(Object o) {
				if (this == o) return true;
				if (o == null || getClass() != o.getClass()) return false;
				if (!super.equals(o)) return false;
				RoundStartedEvent that = (RoundStartedEvent) o;
				return round == that.round;
			}

			@Override
			public int hashCode() {
				return hash(super.hashCode(), round);
			}

			@Override
			public String toString() {
				return format(" -> Spectator.onRoundStarted(view=%s, round=%d)", view, round);
			}
		}

		private static final class RotationCompleteEvent extends Event {

			RotationCompleteEvent(ScotlandYardView view) {
				super(view);
			}

			@Override
			public String toString() {
				return format(" -> Spectator.onRotationComplete(view=%s)", view);
			}
		}

		private static final class GameOverEvent extends Event {

			private final Set<Colour> winner;

			GameOverEvent(ScotlandYardView view, Set<Colour> winner) {
				super(view);
				this.winner = winner;
			}

			@Override
			public boolean equals(Object o) {
				if (this == o) return true;
				if (o == null || getClass() != o.getClass()) return false;
				if (!super.equals(o)) return false;
				GameOverEvent that = (GameOverEvent) o;
				return Objects.equals(winner, that.winner);
			}

			@Override
			public int hashCode() {
				return hash(super.hashCode(), winner);
			}

			@Override
			public String toString() {
				return format(" -> Spectator.onGameOver(view=%s, winner=%s)", view, winner);
			}
		}

		private static final class ImmutableScotlandYardView implements ScotlandYardView {

			private static class Player {

				private final int location;
				private final Map<Ticket, Integer> tickets;

				Player(int location,
						Map<Ticket, Integer> tickets) {
					this.location = location;
					this.tickets = tickets;
				}
			}

			private final List<Colour> colours;
			private final Map<Colour, Player> players;
			private final Set<Colour> winning;
			private final boolean gameOver;
			private final Colour currentPlayer;
			private final int currentRound;
			private final boolean revealRound;
			private final List<Boolean> rounds;
			private final Graph<Integer, Transport> graph;

			static ImmutableScotlandYardView snapshot(ScotlandYardView view) {
				return new ImmutableScotlandYardView(view);
			}

			private ImmutableScotlandYardView(ScotlandYardView view) {
				colours = view.getPlayers();
				players = colours
						.stream()
						.collect(toMap(identity(), p -> new Player(
								view.getPlayerLocation(p),
								Stream.of(Ticket.values()).collect(
										toMap(identity(), t -> view.getPlayerTickets(p, t)))),
								(l, r) -> {
									throw new AssertionError();
								}, LinkedHashMap::new));

				winning = Collections.unmodifiableSet(new HashSet<>(view.getWinningPlayers()));
				gameOver = view.isGameOver();
				currentRound = view.getCurrentRound();
				revealRound = true;
				currentPlayer = view.getCurrentPlayer();
				rounds = Collections.unmodifiableList(new ArrayList<>(view.getRounds()));
				graph = new ImmutableGraph<>(new UndirectedGraph<>(view.getGraph()));
			}

			@Override
			public List<Colour> getPlayers() {
				return colours;
			}

			@Override
			public Set<Colour> getWinningPlayers() {
				return winning;
			}

			@Override
			public int getPlayerLocation(Colour colour) {
				return players.get(colour).location;
			}

			@Override
			public int getPlayerTickets(Colour colour, Ticket ticket) {
				return players.get(colour).tickets.get(ticket);
			}

			@Override
			public boolean isGameOver() {
				return gameOver;
			}

			@Override
			public Colour getCurrentPlayer() {
				return currentPlayer;
			}

			@Override
			public int getCurrentRound() {
				return currentRound;
			}

			@Override
			public boolean isRevealRound() {
				return revealRound;
			}

			@Override
			public List<Boolean> getRounds() {
				return rounds;
			}

			@Override
			public Graph<Integer, Transport> getGraph() {
				return graph;
			}

			String describeSimple() {
				return format("{round=%s, current=%s, reveal=%s, over=%s, winning=%s}",
						getCurrentRound(),
						getCurrentPlayer(),
						isRevealRound(),
						isGameOver(),
						getWinningPlayers());
			}

			@Override
			public boolean equals(Object o) {
				if (this == o) return true;
				if (o == null || getClass() != o.getClass()) return false;
				ImmutableScotlandYardView that = (ImmutableScotlandYardView) o;
				return gameOver == that.gameOver &&
						currentRound == that.currentRound &&
						revealRound == that.revealRound &&
						Objects.equals(players, that.players) &&
						Objects.equals(winning, that.winning) &&
						currentPlayer == that.currentPlayer &&
						Objects.equals(rounds, that.rounds) &&
						Objects.equals(graph, that.graph);
			}

			@Override
			public int hashCode() {
				return hash(players, winning, gameOver, currentPlayer, currentRound,
						revealRound,
						rounds,
						graph);
			}

			@Override
			public String toString() {
				return describeSimple();
			}
		}

		static class EventRecordingSpectator implements Spectator {

			private final List<Event> events;

			EventRecordingSpectator(List<Event> events) {
				this.events = requireNonNull(events);
			}

			@Override
			public void onMoveMade(ScotlandYardView view, Move move) {
				events.add(new MoveMadeEvent(new ImmutableScotlandYardView(view), move));
			}

			@Override
			public void onRoundStarted(ScotlandYardView view, int round) {
				events.add(new RoundStartedEvent(new ImmutableScotlandYardView(view), round));
			}

			@Override
			public void onRotationComplete(ScotlandYardView view) {
				events.add(new RotationCompleteEvent(new ImmutableScotlandYardView(view)));
				if (!view.isGameOver()) ((ScotlandYardGame) (view)).startRotate();
			}

			@Override
			public void onGameOver(ScotlandYardView view, Set<Colour> winningPlayers) {
				events.add(new GameOverEvent(new ImmutableScotlandYardView(view), winningPlayers));

			}
		}
	}
}
