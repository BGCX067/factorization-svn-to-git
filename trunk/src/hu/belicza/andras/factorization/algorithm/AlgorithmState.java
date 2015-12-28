package hu.belicza.andras.factorization.algorithm;

/**
 * A class representing a state of an {@link Algorithm}.
 * 
 * @author Andras Belicza
 */
public class AlgorithmState {
	
	/** The algorithm's execution time in nanoseconds.     */
	public final long   executionTimeNanos;
	/** Completion rate of the execution of the algorithm. */
	public final float  completionRate;
	/** The string representation of the internal state.   */
	public final String internalState;
	
	public AlgorithmState( final long executionTimeNanos, final float completionRate, final String internalState ) {
		this.executionTimeNanos = executionTimeNanos;
		this.completionRate     = completionRate;
		this.internalState      = internalState;
	}
	
}
