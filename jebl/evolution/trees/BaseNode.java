package jebl.evolution.trees;

import jebl.util.AttributableHelper;
import jebl.evolution.graphs.Node;

import java.util.Set;
import java.util.Collections;
import java.util.Map;

/**
 * Common implementation of Attributable interface used by Nodes.
 *
 * Created by IntelliJ IDEA.
 * User: joseph
 * Date: 7/01/2006
 * Time: 07:26:35
 *
 * @author joseph
 * @version $Id$
 *
 */

abstract class BaseNode extends Node {
    // Attributable IMPLEMENTATION

    public void setAttribute(String name, Object value) {
        if (helper == null) {
            helper = new AttributableHelper();
        }
        helper.setAttribute(name, value);
    }

    public Object getAttribute(String name) {
        if (helper == null) {
            return null;
        }
        return helper.getAttribute(name);
    }

    public Set<String> getAttributeNames() {
        if (helper == null) {
            return Collections.emptySet();
        }
        return helper.getAttributeNames();
    }

    public Map<String, Object> getAttributeMap() {
        if (helper == null) {
            return Collections.emptyMap();
        }
        return helper.getAttributeMap();
    }

    // PRIVATE members

    private AttributableHelper helper = null;
}
