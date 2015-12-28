package hu.belicza.andras.factorization.utils;

import java.math.BigInteger;

/**
 * Some math algorithms for {@link BigInteger}s.
 * @author Andras Belicza
 */
public class BigIntegerMath {
	
	/** BigInteger constant of the value 2. */
	public static final BigInteger TWO = BigInteger.valueOf( 2l );
	
	/**
	 * Returns the square root of <code>n</code>.
	 * @param n number whose suqre root to be returned
	 * @return the square root of <code>n</code>
	 */
	public static BigInteger sqrt( final BigInteger n ) {
		BigInteger min = BigInteger.ONE;
		BigInteger max = n;
		
		while ( true ) {
			final BigInteger middle = min.add( max ).shiftRight( 1 );
			if ( middle.equals( min ) )
				return min;
			
			if ( middle.pow( 2 ).compareTo( n ) > 0 )
				max = middle;
			else
				min = middle;
		}
	}
	
}
