package uk.ac.bris.cs.scotlandyard.model;

import sun.plugin.dom.exception.InvalidStateException;

/**
 * Allowed colour of the players in Scotland Yard game
 */
public enum Colour {
	Black, Blue, Green, Red, White, Yellow;

	/**
	 * Checks whether a colour is Mr.X
	 * 
	 * @return true if colour is Mr.X otherwise false
	 */
	public boolean isMrX() {
		return this == Black;
	}

	/**
	 * Checks whether a colour is a detective
	 * 
	 * @return true if colour is a detective otherwise false
	 */
	public boolean isDetective() {
		return !isMrX();
	}

	public String toString () {
		String colour;

		switch (this) {
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
				colour = "White";
				break;
			default:
				throw new InvalidStateException("Colour should be one of Black/Red/Yellow/Green/Blue or White, but tried to call toString on " + this);
		}

		return colour;
	}

}
