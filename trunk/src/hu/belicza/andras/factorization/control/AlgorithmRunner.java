package hu.belicza.andras.factorization.control;

import hu.belicza.andras.factorization.algorithm.Algorithm;
import hu.belicza.andras.factorization.algorithm.AlgorithmState;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

/**
 * A runner class which can run, control and monitor a factorization algorithm.<br>
 * The algorithm execution is done asynchronously in a new thread.
 * 
 * @author Andras Belicza
 */
public class AlgorithmRunner extends Thread {
	
	/**
	 * The internal states of the algorithm runner.
	 * 
	 * @author Andras Belicza
	 */
	private static enum RunnerStates {
		/** Initial state, the algorithm has not yet been started.     */
		INITIAL,
		/** The algorithm is being executed, the algorithm is running. */
		RUNNING,
		/** The algorithm is suspended.                                */
		SUSPENDED,
		/** The algorithm is stopped.                                  */
		STOPPED,
		/** The algorithm has finished.                                */
		FINISHED
	};
	
	/** Algorithm instance to be handled.       */
	private final Algorithm  algorithm;
	/** The internal state of the runner.       */
	private RunnerStates     runnerState;
	
	/** The number to be factorized.            */
	private final BigInteger n;
	/** The state to resume the algorithm from. */
	private final String     resumeFromState;
	/** The result of the algorithm.            */
	private BigInteger       factor;
	
	/** List of algorithm completion listeners. */
	private final List< AlgorithmCompletionListener > completionListeners = new ArrayList< AlgorithmCompletionListener >( 2 );
	
	/**
	 * Creates a new AlgorithmRunner.
	 * @param algorithmClass class of the algorithm
	 * @param n the number to be factorized
	 */
	public AlgorithmRunner( final Class< ? extends Algorithm > algorithmClass, final BigInteger n ) {
		this( algorithmClass, n, null );
	}
	
	/**
	 * Creates a new AlgorithmRunner.
	 * @param algorithmClass class of the algorithm
	 * @param resumeFromState state to resume the algorithm from
	 */
	public AlgorithmRunner( final Class< ? extends Algorithm > algorithmClass, final String resumeFromState ) {
		this( algorithmClass, null, resumeFromState );
	}
	
	/**
	 * Instantiates the algorithm.
	 * @param algorithmClass class of the algorithm
	 * @param n the number to be factorized
	 * @param resumeFromState state to resume the algorithm from
	 */
	private AlgorithmRunner( final Class< ? extends Algorithm > algorithmClass, final BigInteger n, final String resumeFromState ) {
		if ( n == null && resumeFromState == null || n != null && resumeFromState != null )
			throw new IllegalArgumentException( "One, and only one of n or the resume state must be specified!" );
		this.n               = n;
		this.resumeFromState = resumeFromState;
		
		try {
			algorithm = algorithmClass.newInstance();
		} catch ( final Exception e ) {
			throw new RuntimeException( "Could not instantiate algorithm!" );
		}
		
		// This might be run form a swing dispatcher thread which has a higher priority than the normal,
		// we don't wanna inherit it...
		setPriority( NORM_PRIORITY );
		runnerState = RunnerStates.INITIAL;
	}
	
	/**
	 * Adds a new {@link AlgorithmCompletionListener} to this runner.
	 * @param listener listener to be added
	 */
	public void addAlgorithmCompletionListener( final AlgorithmCompletionListener listener ) {
		completionListeners.add( listener );
	}
	
	/**
	 * Removes an {@link AlgorithmCompletionListener} from this runner.
	 * @param listener listener to be removed
	 */
	public void removeAlgorithmCompletionListener( final AlgorithmCompletionListener listener ) {
		completionListeners.remove( listener );
	}
	
	/**
	 * Starts the execution of the algorithm.
	 */
	public synchronized void startAlgorithm() {
		if ( runnerState != RunnerStates.INITIAL )
			throw new IllegalStateException();
		
		start();
		
		runnerState = RunnerStates.RUNNING;
	}
	
	/**
	 * Suspends the execution of the algorithm.
	 */
	public synchronized void suspendAlgorithm() {
		if ( runnerState != RunnerStates.RUNNING )
			throw new IllegalStateException();
		
		algorithm.suspend();
		
		runnerState = RunnerStates.SUSPENDED;
	}
	
	/**
	 * Resumes a suspended algorithm.
	 */
	public synchronized void resumeAlgorithm() {
		if ( runnerState != RunnerStates.SUSPENDED )
			throw new IllegalStateException();
		
		algorithm.resume();
		
		runnerState = RunnerStates.RUNNING;
	}
	
	/**
	 * Stops a suspended algorithm, ending the execution thread.
	 */
	public synchronized void stopAlgorithm() {
		if ( runnerState == RunnerStates.SUSPENDED )
			resumeAlgorithm();
		
		if ( runnerState == RunnerStates.RUNNING ) {
			runnerState = RunnerStates.STOPPED;
			algorithm.stop();
		}
	}
	
	@Override
	public void run() {
		if ( n != null )
			factor = algorithm.getFactor( n );
		else
			factor = new BigInteger( "-1" );
		
		if ( runnerState != RunnerStates.STOPPED )
			for ( final AlgorithmCompletionListener listener : completionListeners )
				listener.algorithmCompleted( factor );
		
		runnerState = RunnerStates.FINISHED;
	}
	
	/**
	 * Returns the state of the algorithm.
	 * @return the state of the algorithm
	 */
	public AlgorithmState getAlgorithmState() {
		return algorithm.getState();
	}
	
}
