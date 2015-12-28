package hu.belicza.andras.factorization.view;

import hu.belicza.andras.factorization.algorithm.Algorithm;
import hu.belicza.andras.factorization.algorithm.AlgorithmState;
import hu.belicza.andras.factorization.control.AlgorithmCompletionListener;
import hu.belicza.andras.factorization.control.AlgorithmRunner;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.math.BigInteger;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTextArea;
import javax.swing.SpinnerNumberModel;
import javax.swing.UIManager;

/**
 * The main frame of the factorization application, a GUI to view and control the algorithm runner.
 * 
 * @author Andras Belicza
 */
public class FactorizationFrame extends JFrame {
	
	private static final long serialVersionUID = 1L;
	
	/** Background color of inactive text areas. */
	private static final Color INACTIVE_TEXT_AREA_BACKGROUND = new Color( 230, 230, 230 );
	/** Fonts to be used in text areas.          */
	private static final Font  TEXT_AREA_FONT                = new Font( null, Font.PLAIN, 12 );
	
	/**
	 * The entry point of the program.<br>
	 * Creates the application frame.
	 * 
	 * @param arguments used to take arguments from the running environment - not used here
	 */
	public static void main( final String[] arguments ) {
		try {
			UIManager.setLookAndFeel( UIManager.getSystemLookAndFeelClassName() );
		} catch ( final Exception e ) {
		}
		
		new FactorizationFrame().setVisible( true );
	}
	
	private static class AlgorithmSpecifier {
		public final Class< ? extends Algorithm > algorithmClass;
		public AlgorithmSpecifier( Class< ? extends Algorithm > algorithmClass ) {
			this.algorithmClass = algorithmClass;
		}
		@Override
		public String toString(){
			return algorithmClass.getSimpleName();
		}
	}
	
	private static final AlgorithmSpecifier[] availableAlgorithms = new AlgorithmSpecifier[] {
		new AlgorithmSpecifier( hu.belicza.andras.factorization.algorithm.SquareNumberFactorization.class ),
		new AlgorithmSpecifier( hu.belicza.andras.factorization.algorithm.BasicFactorization.class ),
		new AlgorithmSpecifier( hu.belicza.andras.factorization.algorithm.TryingPrimesFactorization.class ),
	};
	
	private static final int COMPLETION_PROGRESS_BAR_MAX = 1000;
	
	/** Option to start a new algorithm.                   */
	private final JRadioButton startNewAlgorithmRadioButton        = new JRadioButton( "Start a new algorithm" );
	/** Option to resume an algorithm from a state.        */
	private final JRadioButton resumeAlgorithmRadioButton          = new JRadioButton( "Resume algorithm from a state" );
	/** Suspend algorithm execution button.                */
	private final JButton      suspendAlgorithmButton              = new JButton( "Suspend execution" );
	/** Text area to get and set the algorithm state.      */
	private final JTextArea    algorithmStateTextArea              = new JTextArea( 5, 35 );
	/** Label to display the execution time.               */
	private final JLabel       executionTimeLabel                  = new JLabel();
	/** Progress bar to indicate algorithm completion.     */
	private final JProgressBar completionProgressBar               = new JProgressBar( 0, COMPLETION_PROGRESS_BAR_MAX );
	/** Text area to display the algorithm result.         */
	private final JTextArea    resultTextArea                      = new JTextArea( 3, 35 );
	/** Combo box to select from the available algorithms. */
	private final JComboBox    algorithmComboBox                   = new JComboBox( availableAlgorithms );
	/** Option to factorize a random number.               */
	private final JRadioButton factorizeRandomNumberRadioButton    = new JRadioButton( "Factorize random number:" );
	/** Option to factorize a specified number.            */
	private final JRadioButton factorizeSpecifiedNumberRadioButton = new JRadioButton( "Factorize specified number:" );
	/** Bitlength of factor 1 spinner.                     */
	private final JSpinner     factor1BitlengthSpinner             = new JSpinner( new SpinnerNumberModel( 30, 2, 10000, 1 ) );
	/** Bitlength of factor 2 spinner.                     */
	private final JSpinner     factor2BitlengthSpinner             = new JSpinner( new SpinnerNumberModel( 30, 2, 10000, 1 ) );
	/** Input number to be factorized text area.           */
	private final JTextArea    inputNumberTextArea                 = new JTextArea( 5, 35 );
	
	/** Reference to the current algorithm runner if there's any. */
	private AlgorithmRunner algorithmRunner;
	
	/** Timer to time state refreshes. */
	private Timer stateRefresherTimer;
	
	/**
	 * Creates a new AlgorithmRunner.
	 */
	public FactorizationFrame() {
		super( "Factorization Algorithm Runner" );
		
		setDefaultCloseOperation( EXIT_ON_CLOSE );
		setResizable( false );
		
		buildGUI();
		
		pack();
		
		setLocation( 100, 100 );
		setVisible( true );
	}
	
	/**
	 * Builds the graphical user interface.
	 */
	private void buildGUI() {
		final Box contentBox = Box.createVerticalBox();
		JPanel panel;
		
		final Box taskBox = Box.createVerticalBox();
		taskBox.setBorder( BorderFactory.createTitledBorder( "Task" ) );
		final Box taskBox2 = Box.createVerticalBox();
		taskBox2.add( startNewAlgorithmRadioButton );
		taskBox2.add( resumeAlgorithmRadioButton );
		panel = new JPanel();
		panel.add( taskBox2 );
		taskBox.add( panel );
		final ButtonGroup taskButtonGroup = new ButtonGroup();
		taskButtonGroup.add( startNewAlgorithmRadioButton );
		taskButtonGroup.add( resumeAlgorithmRadioButton );
		contentBox.add( taskBox );
		
		final Box controlBox = Box.createVerticalBox();
		controlBox.setBorder( BorderFactory.createTitledBorder( "Control" ) );
		panel = new JPanel();
		final JButton startOrResumeAlgorithmButton = new JButton( "Start/Resume" );
		startOrResumeAlgorithmButton.setMnemonic( startOrResumeAlgorithmButton.getText().charAt( 0 ) );
		panel.add( startOrResumeAlgorithmButton );
		suspendAlgorithmButton.setEnabled( false );
		suspendAlgorithmButton.setMnemonic( suspendAlgorithmButton.getText().charAt( 1 ) );
		panel.add( suspendAlgorithmButton );
		final JButton dropStartNewAlgorithmButton = new JButton( "Drop algorithm, start new" );
		dropStartNewAlgorithmButton.setMnemonic( dropStartNewAlgorithmButton.getText().charAt( 0 ) );
		panel.add( dropStartNewAlgorithmButton );
		controlBox.add( panel );
		contentBox.add( controlBox );
		
		final Box algorithmStateBox = Box.createVerticalBox();
		algorithmStateBox.setBorder( BorderFactory.createTitledBorder( "Algorithm state" ) );
		algorithmStateTextArea.setFont( TEXT_AREA_FONT );
		algorithmStateTextArea.setForeground( Color.BLACK );
		algorithmStateBox.add( new JScrollPane( algorithmStateTextArea ) );
		panel = new JPanel();
		panel.add( new JLabel( "Execution time:" ) );
		panel.add( executionTimeLabel );
		executionTimeLabel.setText( formatExecutionTime( 0l ) );
		algorithmStateBox.add( panel );
		panel = new JPanel();
		panel.add( new JLabel( "Algorithm completion:" ) );
		panel.add( completionProgressBar );
		completionProgressBar.setStringPainted( true );
		algorithmStateBox.add( panel );
		resultTextArea.setFont( TEXT_AREA_FONT );
		resultTextArea.setEditable( false );
		resultTextArea.setBackground( INACTIVE_TEXT_AREA_BACKGROUND );
		final JScrollPane resultScrollPane = new JScrollPane( resultTextArea );
		resultScrollPane.setBorder( BorderFactory.createTitledBorder( "Result:" ) );
		algorithmStateBox.add( resultScrollPane );
		contentBox.add( algorithmStateBox );
		
		final Box algorithmParamsBox = Box.createVerticalBox();
		algorithmParamsBox.setBorder( BorderFactory.createTitledBorder( "Algorithm parameters" ) );
		panel = new JPanel();
		panel.add( new JLabel( "Algorithm:" ) );
		panel.add( algorithmComboBox );
		algorithmParamsBox.add( panel );
		contentBox.add( algorithmParamsBox );
		final Box inputNumberBox = Box.createVerticalBox();
		inputNumberBox.setAlignmentX( 0.5f );
		final Box randomNumberBox = Box.createHorizontalBox();
		randomNumberBox.add( factorizeRandomNumberRadioButton );
		factorizeRandomNumberRadioButton.setAlignmentY( 1.0f );
		final Box randomNumberFactorsBox = Box.createVerticalBox();
		randomNumberFactorsBox.setBorder( BorderFactory.createTitledBorder( "Factors" ) );
		panel = new JPanel();
		panel.add( new JLabel( "Factor #1 bitlength:" ) );
		panel.add( factor1BitlengthSpinner );
		randomNumberFactorsBox.add( panel );
		panel = new JPanel();
		panel.add( new JLabel( "Factor #2 bitlength:" ) );
		panel.add( factor2BitlengthSpinner );
		randomNumberFactorsBox.add( panel );
		randomNumberBox.add( randomNumberFactorsBox );
		randomNumberBox.add( new JPanel( new BorderLayout() ) );
		inputNumberBox.add( randomNumberBox );
		panel = new JPanel( new BorderLayout() );
		panel.add( factorizeSpecifiedNumberRadioButton );
		inputNumberBox.add( panel );
		final ButtonGroup inputNumberButtonGroup = new ButtonGroup();
		inputNumberButtonGroup.add( factorizeRandomNumberRadioButton );
		inputNumberButtonGroup.add( factorizeSpecifiedNumberRadioButton );
		final JPanel specifiedNumberPanel = new JPanel();
		specifiedNumberPanel.add( new JLabel( "Number to be factorized:" ) );
		inputNumberTextArea.setFont( TEXT_AREA_FONT );
		inputNumberTextArea.setLineWrap( true );
		inputNumberTextArea.setForeground( Color.BLACK );
		specifiedNumberPanel.add( new JScrollPane( inputNumberTextArea ) );
		inputNumberBox.add( specifiedNumberPanel );
		algorithmParamsBox.add( inputNumberBox );
		
		getContentPane().add( contentBox, BorderLayout.CENTER );
		
		// Register listeners
		startNewAlgorithmRadioButton.addActionListener( new ActionListener() {
			@Override
			public void actionPerformed( final ActionEvent event ) {
				algorithmStateTextArea.setEditable( false );
				algorithmStateTextArea.setBackground( INACTIVE_TEXT_AREA_BACKGROUND );
				algorithmComboBox.setEnabled( true );
				factorizeRandomNumberRadioButton.setEnabled( true );
				factorizeSpecifiedNumberRadioButton.setEnabled( true );
				if ( factorizeRandomNumberRadioButton.isSelected() )
					factorizeRandomNumberRadioButton.doClick();
				if ( factorizeSpecifiedNumberRadioButton.isSelected() )
					factorizeSpecifiedNumberRadioButton.doClick();
				algorithmComboBox.requestFocusInWindow();
			}
		} );
		resumeAlgorithmRadioButton.addActionListener( new ActionListener() {
			@Override
			public void actionPerformed( final ActionEvent event ) {
				algorithmStateTextArea.setEditable( true );
				algorithmStateTextArea.setBackground( Color.WHITE );
				disableContainer( algorithmParamsBox );
				algorithmStateTextArea.requestFocusInWindow();
			}
		} );
		factorizeRandomNumberRadioButton.addActionListener( new ActionListener() {
			@Override
			public void actionPerformed( final ActionEvent event ) {
				disableContainer( specifiedNumberPanel );
				factor1BitlengthSpinner.setEnabled( true );
				factor2BitlengthSpinner.setEnabled( true );
			}
		} );
		factorizeSpecifiedNumberRadioButton.addActionListener( new ActionListener() {
			@Override
			public void actionPerformed( final ActionEvent event ) {
				inputNumberTextArea.setEditable( true );
				inputNumberTextArea.setBackground( Color.WHITE );
				disableContainer( randomNumberFactorsBox );
			}
		} );
		startOrResumeAlgorithmButton.addActionListener( new ActionListener() {
			@Override
			public void actionPerformed( final ActionEvent event ) {
				disableContainer( taskBox            );
				disableContainer( algorithmStateBox  );
				disableContainer( algorithmParamsBox );
				startOrResumeAlgorithmButton.setEnabled( false );
				suspendAlgorithmButton.setEnabled( true );
				suspendAlgorithmButton.requestFocusInWindow();
				startOrResumeAlgorithm();
			}
		} );
		suspendAlgorithmButton.addActionListener( new ActionListener() {
			@Override
			public void actionPerformed( final ActionEvent event ) {
				suspendAlgorithm();
				startOrResumeAlgorithmButton.setEnabled( true );
				suspendAlgorithmButton.setEnabled( false );
				startOrResumeAlgorithmButton.requestFocusInWindow();
			}
		} );
		dropStartNewAlgorithmButton.addActionListener( new ActionListener() {
			@Override
			public void actionPerformed( final ActionEvent event ) {
				dropAndStartNewAlgorithm();
				
				startNewAlgorithmRadioButton.setEnabled( true );
				resumeAlgorithmRadioButton.setEnabled( true );
				if ( startNewAlgorithmRadioButton.isSelected() )
					startNewAlgorithmRadioButton.doClick();
				else if ( resumeAlgorithmRadioButton.isSelected() )
					resumeAlgorithmRadioButton.doClick();
				
				startOrResumeAlgorithmButton.setEnabled( true );
				suspendAlgorithmButton.setEnabled( false );
			}
		} );
		
		// Set init params
		factorizeRandomNumberRadioButton.doClick();
		startNewAlgorithmRadioButton.doClick();
	}
	
	/**
	 * Recursively disables components in a container.
	 * 
	 * @param container container whose components to be disabled
	 */
	private void disableContainer( final Container container ) {
		for ( int i = container.getComponentCount() - 1; i >= 0; i-- ) {
			Component component = container.getComponent( i );
			if ( component instanceof JScrollPane )
				component = ( (JScrollPane) component ).getViewport().getView();
			
			if ( component instanceof Box || component instanceof JPanel )
				disableContainer( (Container) component );
			else if ( component instanceof JRadioButton || component instanceof JSpinner || component instanceof JComboBox )
				component.setEnabled( false );
			else if ( component instanceof JTextArea ) {
				( (JTextArea) component ).setEditable( false );
				component.setBackground( INACTIVE_TEXT_AREA_BACKGROUND );
			}
		}
	}
	
	/**
	 * Starts or resumes the algorithm.
	 */
	private void startOrResumeAlgorithm() {
		if ( algorithmRunner == null ) {
			final Class< ? extends Algorithm> algorithmClass = ( (AlgorithmSpecifier) algorithmComboBox.getSelectedItem() ).algorithmClass;
			
			if ( startNewAlgorithmRadioButton.isSelected() ) {
				BigInteger n = null;
				final Random random = new Random();
				if ( factorizeRandomNumberRadioButton.isSelected() ) {
					n = BigInteger.probablePrime( (Integer) factor1BitlengthSpinner.getValue(), random ).multiply( BigInteger.probablePrime( (Integer) factor2BitlengthSpinner.getValue(), random ) );
					inputNumberTextArea.setText( n.toString() );
				}
				else if ( factorizeSpecifiedNumberRadioButton.isSelected() )
					n = new BigInteger( inputNumberTextArea.getText() );
				algorithmRunner = new AlgorithmRunner( algorithmClass, n );
			}
			else if ( resumeAlgorithmRadioButton.isSelected() ) {
				algorithmRunner = new AlgorithmRunner( algorithmClass, algorithmStateTextArea.getText() );
			}
			
			algorithmRunner.addAlgorithmCompletionListener( new AlgorithmCompletionListener() {
				@Override
				public void algorithmCompleted( final BigInteger factor ) {
					suspendAlgorithmButton.setEnabled( false );
					resultTextArea.setText( factor.toString() );
					refreshDisplayedAlgorithmState();
					algorithmRunner = null;
				}
			} );
			algorithmRunner.startAlgorithm();
		}
		else {
			algorithmRunner.resumeAlgorithm();
		}
		resultTextArea.setText( "Calculating..." );
		
		if ( stateRefresherTimer != null ) {
			stateRefresherTimer.cancel();
			stateRefresherTimer.purge();
		}
		stateRefresherTimer = new Timer();
		stateRefresherTimer.schedule( new TimerTask() {
			@Override
			public void run() {
				refreshDisplayedAlgorithmState();
			}
		}, 0l, 2000l );
	}
	
	/**
	 * Suspends the algorithm.
	 */
	private void suspendAlgorithm() {
		stateRefresherTimer.cancel();
		stateRefresherTimer.purge();
		algorithmRunner.suspendAlgorithm();
		resultTextArea.setText( "Suspended." );
	}
	
	/**
	 * Drops the current algorithm and enables the inteface to start a new one.
	 */
	private void dropAndStartNewAlgorithm() {
		if ( algorithmRunner != null ) {
			stateRefresherTimer.cancel();
			stateRefresherTimer.purge();
			algorithmRunner.stopAlgorithm();
			algorithmRunner = null;
			resultTextArea.setText( "Stopped." );
		}
		else
			resultTextArea.setText( "" );
	}
	
	/**
	 * Refreshes the displayed state of the algorithm.
	 */
	private void refreshDisplayedAlgorithmState() {
		if ( algorithmRunner != null ) {
			final AlgorithmState algorithmState = algorithmRunner.getAlgorithmState();
			algorithmStateTextArea.setText( algorithmState.internalState );
			
			executionTimeLabel.setText( formatExecutionTime( algorithmState.executionTimeNanos ) );
			
			completionProgressBar.setValue( (int) ( algorithmState.completionRate * COMPLETION_PROGRESS_BAR_MAX ) );
			completionProgressBar.setString( String.format( "%1.4f %%", algorithmState.completionRate * 100.0f ) );
		}
	}
	
	/**
	 * Formats the given nano time to human readable format.
	 * 
	 * @param nanos nano time to be formatted
	 * @return the human readable format of the given time
	 */
	private static String formatExecutionTime( final long nanos ) {
		final StringBuilder executionTimeBuilder = new StringBuilder();
		
		final String[] UNIT_NAMES       = new String[] {         " day", " hour", " min", " sec",  " ms", " \u00B5s", " ns" };
		final long  [] CONVERSION_UNITS = new long  [] { Long.MAX_VALUE,     24l,    60l,    60l,  1000l,      1000l, 1000l };
		
		long time = nanos;
		for ( int i = UNIT_NAMES.length - 1; i >=0; i-- ) {
			executionTimeBuilder.insert( 0, UNIT_NAMES[ i ] );
			executionTimeBuilder.insert( 0, time % CONVERSION_UNITS[ i ] );
			if ( i != 0 )
				executionTimeBuilder.insert( 0, ", " );
			time /= CONVERSION_UNITS[ i ];
		}
		
		return executionTimeBuilder.toString();
	}
	
}
