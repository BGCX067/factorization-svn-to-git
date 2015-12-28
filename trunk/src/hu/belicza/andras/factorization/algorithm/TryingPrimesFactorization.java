package hu.belicza.andras.factorization.algorithm;

import hu.belicza.andras.factorization.utils.BigIntegerMath;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.MathContext;

/**
 * This factorization algorithm starts from 2, and tries all prime numbers less than the square root of n,
 * obtained by {@link BigInteger#nextProbablePrime()} method.<br>
 * <br>
 * This algorithm is here only for demonstration. The {@link BigInteger#nextProbablePrime()} method
 * doesn't seem to be effective at all.
 * 
 * @author Andras Belicza
 */
public class TryingPrimesFactorization extends Algorithm {
	
	@Override
	protected BigInteger getFactorImpl( final BigInteger n ) {
		if ( n.compareTo( BigIntegerMath.TWO ) <= 0 )
			return n;
		
		final BigInteger squareRoot = BigIntegerMath.sqrt( n );
		
		for ( BigInteger i = BigInteger.valueOf( 2l ); i.compareTo( squareRoot ) <= 0 && !requestingStop; i = i.nextProbablePrime() ) {
			
			if ( requestingSuspension )
				suspended();
			
			if ( requestingStatus ) {
				final StringBuilder stateBuilder = createStateBuilder();
				stateBuilder.append( "i=" ).append( i ).append( '\n' );
				
				// This is an inaccurate estimation of completion rate!
				// (But I didn't want to implement the log function: pi(x)=x/log(x); a better approximation: pi(x) ~ x/log(x-1) ).
				createStateSnapshot( new BigDecimal( i ).divide( new BigDecimal( squareRoot ), MathContext.DECIMAL32 ).floatValue(), stateBuilder );
			}
			
			if ( n.remainder( i ).equals( BigInteger.ZERO ) )
				return i;
		}
		
		if ( requestingStop ) {
			stopping();
			return null;
		}
		
		return n;
	}
	
}
