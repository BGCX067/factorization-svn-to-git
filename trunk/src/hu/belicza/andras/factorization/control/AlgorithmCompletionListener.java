package hu.belicza.andras.factorization.control;

import java.math.BigInteger;

/**
 * Defines an interface for algorithm completion listeners.
 * 
 * @author Andras Belicza
 */
public interface AlgorithmCompletionListener {
	
	/**
	 * Called when the execution of an algorithm finishes.
	 * @param factor the result of the algorithm, a factor of the input number
	 */
	void algorithmCompleted( final BigInteger factor );
	
}
