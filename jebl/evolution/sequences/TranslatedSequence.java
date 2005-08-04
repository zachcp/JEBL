package jebl.evolution.sequences;

/**
 * @author rambaut
 *         Date: Jul 27, 2005
 *         Time: 12:48:31 AM
 */
public class TranslatedSequence extends FilteredSequence {

	public TranslatedSequence(Sequence source, GeneticCode geneticCode) {
		super(source);

		this.geneticCode = geneticCode;
	}

	protected State[] filterSequence(Sequence source) {
		return Utils.translate(source.getStates(), geneticCode);
	}

	private final GeneticCode geneticCode;

}
