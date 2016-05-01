package jebl.evolution.taxa;

/**
 * @author Andrew Rambaut
 */
public class MissingTaxonException extends Throwable {
	public MissingTaxonException(Taxon taxon) {
		super("Taxon, " + taxon.getName() + ", is missing.");
	}
}
