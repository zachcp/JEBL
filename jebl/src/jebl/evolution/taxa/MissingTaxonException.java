package jebl.evolution.taxa;

/**
 * @author Andrew Rambaut
 * @version $Id$
 */
public class MissingTaxonException extends Throwable {
	public MissingTaxonException(Taxon taxon) {
		super("Taxon, " + taxon.getName() + ", is missing.");
	}
}
