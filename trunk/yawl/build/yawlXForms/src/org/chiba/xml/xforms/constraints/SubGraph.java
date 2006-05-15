package org.chiba.xml.xforms.constraints;

import org.apache.commons.jxpath.JXPathContext;
import org.apache.xerces.dom.NodeImpl;

import java.util.Iterator;
import java.util.List;

/**
 * Builds the pertinent subdependency graph.
 *
 * @author This code is based on the ideas of Mikko Honkala from the X-Smiles project.
 *         Although it has been heavily refactored and rewritten to meet our needs.
 * @version $Id$
 */
public class SubGraph extends DependencyGraph {
    /**
     * Creates a new SubGraph object.
     */
    public SubGraph() {
        super();
    }

    /**
     * builds the pertinent subdependency graph
     *
     * @param clonedParent    - the other side of the edge
     * @param changedVertices - a list of changed vertices
     * @param mainGraph       - the Main Dependency Graph
     */
    public void constructSubDependencyGraph(Vertex clonedParent, List changedVertices,
                                            MainDependencyGraph mainGraph) {
        Iterator it = changedVertices.iterator();

        while (it.hasNext()) {
            Vertex original = (Vertex) it.next();

            if (!this.vertices.contains(original)) {
                // not visited yet ...
                Vertex clone = createSubVertex(original.relativeContext, original.instanceNode,
                        original.xpathExpression, original.getVertexType());
                this.vertices.add(clone);

                if (clonedParent != null) {
                    clonedParent.addDep(clone);
                }

                if (original.depList != null) {
                    constructSubDependencyGraph(clone, original.depList, mainGraph);
                }
            } else {
                // ... already visited
                if (clonedParent != null) {
                    clonedParent.addDep(original);
                }
            }
        }
    }

    /**
     * create a new Vertex of the specified type.
     *
     * @param relativeContext - the evaluation context for the vertex
     * @param instanceNode    - the instanceNode associated with this vertex
     * @param expression      - a xpath expression to evaluate with the context
     * @param property        - the type of vertex
     * @return a new vertex of specified type
     */
    private Vertex createSubVertex(JXPathContext relativeContext, NodeImpl instanceNode, String expression,
                                   short property) {
        Vertex v = null;

        switch (property) {
            case Vertex.CALCULATE_VERTEX:
                v = new CalculateVertex(relativeContext, instanceNode, expression);

                break;

            case Vertex.RELEVANT_VERTEX:
                v = new RelevantVertex(relativeContext, instanceNode, expression);

                break;

            case Vertex.READONLY_VERTEX:
                v = new ReadonlyVertex(relativeContext, instanceNode, expression);

                break;

            case Vertex.CONSTRAINT_VERTEX:
                v = new ConstraintVertex(relativeContext, instanceNode, expression);

                break;

            case Vertex.REQUIRED_VERTEX:
                v = new RequiredVertex(relativeContext, instanceNode, expression);

                break;
        }

        return v;
    }
}

//end of class

