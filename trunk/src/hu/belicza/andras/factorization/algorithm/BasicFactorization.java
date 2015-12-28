package hu.belicza.andras.factorization.algorithm;

import hu.belicza.andras.factorization.utils.BigIntegerMath;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.MathContext;

/**
 * Basic factorization algorithm.<br>
 * <br>
 * This algorithm first checks the remainder divided by 2,
 * and checks the remainder divided by numbers starting from 3 and incremented by 2
 * up to the square root of <code>n</code>.<br>
 * This algorithm is only fast if <code>n</code> has a small factor.
 * 
 * @author Andras Belicza
 */
public class BasicFactorization extends Algorithm {
	
	@Override
	protected BigInteger getFactorImpl( final BigInteger n ) {
		if ( n.compareTo( BigIntegerMath.TWO ) <= 0 )
			return n;
		
		if ( n.remainder( BigIntegerMath.TWO ).equals( BigInteger.ZERO ) )
			return BigIntegerMath.TWO;
		
		final BigInteger squareRoot = BigIntegerMath.sqrt( n );
		
		// For status report (completion calculation):
		final BigDecimal halfSquareRoot = new BigDecimal( squareRoot.shiftRight( 1 ) );
		
		for ( BigInteger i = new BigInteger( "3" ); i.compareTo( squareRoot ) <= 0 && !requestingStop; i= i.add( BigIntegerMath.TWO ) ) {
			
			if ( requestingSuspension )
				suspended();
			
			if ( requestingStatus ) {
				final StringBuilder stateBuilder = createStateBuilder();
				stateBuilder.append( "i=" ).append( i ).append( '\n' );
				createStateSnapshot( new BigDecimal( i.shiftRight( 1 ) ).divide( halfSquareRoot, MathContext.DECIMAL32 ).floatValue(), stateBuilder );
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
