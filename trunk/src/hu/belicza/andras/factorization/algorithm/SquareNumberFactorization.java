package hu.belicza.andras.factorization.algorithm;

import hu.belicza.andras.factorization.utils.BigIntegerMath;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.MathContext;

/**
 * Square number factorization.<br>
 * <br>
 * The basis of this algorithm is to write the <code>n</code> number in the form of 
 * <code>(d+c)*(d-c)</code> in which case
 * <code>n=d<sup>2</sup>-c<sup>2</sup></code>.<br>
 * This is equivalient with this form:
 * <code>n<sup>2</sup>+c<sup>2</sup>=d<sup>2</sup></code><br>
 * So we go on a quest to find a square number that added to <code>n</code> gives another square number.<br>
 * <br>
 * To avoid calculating squares and square roots this algorithm uses my idea that square numbers can easily
 * be recognized: they go on a pattern where if <code>n<sup>2</sup>=(n-1)<sup>2</sup>+a</code> then
 * <code>(n+1)<sup>2</sup>=n<sup>2</sup>+a+2</code>.<br>
 * <br>
 * Using this we just have to perform additions and substractions to test if we found a square number.<br>
 * <br>
 * Back to our formula: <code>n<sup>2</sup>+c<sup>2</sup>=d<sup>2</sup></code> where <code>n=a*b</code>.<br>
 * If we have a <code>c</code> and a <code>d</code> that satisfy the equation,
 * the factors of <code>n</code> are in the form of:<br>
 * <code>a=c-sqrt(c-n)</code><br>
 * <code>b=c+sqrt(c-n)</code><br>
 * We start from <code>n</code> incrementing with the proper value to get square numbers, and we quit 
 * if we found a factor or reached a limit which tells us that <code>n</code> is a prime.<br>
 * <br>
 * This limit can easily be determined: it is the square of the middle of the 2 factors.
 * This middle's highest value is when one of the factor is the smallest (and therefore it results
 * in the highest factor pair). This limit is: <code>((n/2 + 2)/2)<sup>2</sup></code>
 * (this limit will be fine tuned based on the current implementation)<br>
 * <br>
 * This algorithm is most effective when the 2 factors are close to each other.
 * 
 * @author Andras Belicza
 */
public class SquareNumberFactorization extends Algorithm {
	
	@Override
	protected BigInteger getFactorImpl( final BigInteger n ) {
		if ( n.compareTo( BigIntegerMath.TWO ) <= 0 )
			return n;
		
		// Even numbers are not handled by the rest of the algorithm:
		if ( n.remainder( BigIntegerMath.TWO ).equals( BigInteger.ZERO ) )
			return BigIntegerMath.TWO;
		
		// Square numbers are not handled by the rest of the algorithm:
		final BigInteger sqrtn = BigIntegerMath.sqrt( n );
		if ( sqrtn.pow( 2 ).equals ( n ) )
			return sqrtn;
		
		BigInteger origSquare = n.add( BigInteger.ONE );
		BigInteger origInc    = BigInteger.ONE;
		
		final BigInteger squareRootPlusOne = BigIntegerMath.sqrt( n ).add( BigInteger.ONE );
		BigInteger rootSquare = squareRootPlusOne.pow( 2 );
		BigInteger rootInc    = rootSquare.subtract( squareRootPlusOne.subtract( BigInteger.ONE ).pow( 2 ) );
		
		// Limit is the square of the number which is the square
		// of the greatest possible middle of the 2 factors of n: ((a+b)/2+1)^2
		// Since we already checked the division by 2, this cannot be larger than ((n/3+1 + 3 )/2+1)^2 = (n/6+3)^2.
		final BigInteger limit = n.divide( BigInteger.valueOf( 6l ) ).add( BigInteger.valueOf( 3l ) ).pow( 2 );
		// For status report (completion calculation):
		final BigDecimal limitMinusN = new BigDecimal( limit.subtract( n ) );
		
		while ( !origSquare.equals( rootSquare ) && origSquare.compareTo( limit ) < 0 && !requestingStop ) {
			
			if ( requestingSuspension )
				suspended();
			
			if ( requestingStatus ) {
				final StringBuilder stateBuilder = createStateBuilder();
				stateBuilder.append( "origSquare=" ).append( origSquare ).append( '\n' );
				stateBuilder.append( "rootSquare=" ).append( rootSquare ).append( '\n' );
				final float completionRate = (float) Math.sqrt( new BigDecimal( origSquare.subtract( n ) ).divide( limitMinusN, MathContext.DECIMAL32 ).doubleValue() );
				createStateSnapshot( completionRate, stateBuilder );
			}
			
			while ( origSquare.compareTo( rootSquare ) < 0 )
				origSquare = origSquare.add( origInc = origInc.add( BigIntegerMath.TWO ) );
			while ( rootSquare.compareTo( origSquare ) < 0 )
				rootSquare = rootSquare.add( rootInc = rootInc.add( BigIntegerMath.TWO ) );
		}
		
		if ( requestingStop ) {
			stopping();
			return null;
		}
		
		if ( origSquare.compareTo( limit ) >= 0 )
			return BigInteger.ONE;
		
		return BigIntegerMath.sqrt( origSquare ).subtract( BigIntegerMath.sqrt( origSquare.subtract( n ) ) );
	}
	
}
