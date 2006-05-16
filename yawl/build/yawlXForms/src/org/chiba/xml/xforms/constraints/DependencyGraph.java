/*
 *
 *    Artistic License
 *
 *    Preamble
 *
 *    The intent of this document is to state the conditions under which a Package may be copied, such that
 *    the Copyright Holder maintains some semblance of artistic control over the development of the
 *    package, while giving the users of the package the right to use and distribute the Package in a
 *    more-or-less customary fashion, plus the right to make reasonable modifications.
 *
 *    Definitions:
 *
 *    "Package" refers to the collection of files distributed by the Copyright Holder, and derivatives
 *    of that collection of files created through textual modification.
 *
 *    "Standard Version" refers to such a Package if it has not been modified, or has been modified
 *    in accordance with the wishes of the Copyright Holder.
 *
 *    "Copyright Holder" is whoever is named in the copyright or copyrights for the package.
 *
 *    "You" is you, if you're thinking about copying or distributing this Package.
 *
 *    "Reasonable copying fee" is whatever you can justify on the basis of media cost, duplication
 *    charges, time of people involved, and so on. (You will not be required to justify it to the
 *    Copyright Holder, but only to the computing community at large as a market that must bear the
 *    fee.)
 *
 *    "Freely Available" means that no fee is charged for the item itself, though there may be fees
 *    involved in handling the item. It also means that recipients of the item may redistribute it under
 *    the same conditions they received it.
 *
 *    1. You may make and give away verbatim copies of the source form of the Standard Version of this
 *    Package without restriction, provided that you duplicate all of the original copyright notices and
 *    associated disclaimers.
 *
 *    2. You may apply bug fixes, portability fixes and other modifications derived from the Public Domain
 *    or from the Copyright Holder. A Package modified in such a way shall still be considered the
 *    Standard Version.
 *
 *    3. You may otherwise modify your copy of this Package in any way, provided that you insert a
 *    prominent notice in each changed file stating how and when you changed that file, and provided that
 *    you do at least ONE of the following:
 *
 *        a) place your modifications in the Public Domain or otherwise make them Freely
 *        Available, such as by posting said modifications to Usenet or an equivalent medium, or
 *        placing the modifications on a major archive site such as ftp.uu.net, or by allowing the
 *        Copyright Holder to include your modifications in the Standard Version of the Package.
 *
 *        b) use the modified Package only within your corporation or organization.
 *
 *        c) rename any non-standard executables so the names do not conflict with standard
 *        executables, which must also be provided, and provide a separate manual page for each
 *        non-standard executable that clearly documents how it differs from the Standard
 *        Version.
 *
 *        d) make other distribution arrangements with the Copyright Holder.
 *
 *    4. You may distribute the programs of this Package in object code or executable form, provided that
 *    you do at least ONE of the following:
 *
 *        a) distribute a Standard Version of the executables and library files, together with
 *        instructions (in the manual page or equivalent) on where to get the Standard Version.
 *
 *        b) accompany the distribution with the machine-readable source of the Package with
 *        your modifications.
 *
 *        c) accompany any non-standard executables with their corresponding Standard Version
 *        executables, giving the non-standard executables non-standard names, and clearly
 *        documenting the differences in manual pages (or equivalent), together with instructions
 *        on where to get the Standard Version.
 *
 *        d) make other distribution arrangements with the Copyright Holder.
 *
 *    5. You may charge a reasonable copying fee for any distribution of this Package. You may charge
 *    any fee you choose for support of this Package. You may not charge a fee for this Package itself.
 *    However, you may distribute this Package in aggregate with other (possibly commercial) programs as
 *    part of a larger (possibly commercial) software distribution provided that you do not advertise this
 *    Package as a product of your own.
 *
 *    6. The scripts and library files supplied as input to or produced as output from the programs of this
 *    Package do not automatically fall under the copyright of this Package, but belong to whomever
 *    generated them, and may be sold commercially, and may be aggregated with this Package.
 *
 *    7. C or perl subroutines supplied by you and linked into this Package shall not be considered part of
 *    this Package.
 *
 *    8. The name of the Copyright Holder may not be used to endorse or promote products derived from
 *    this software without specific prior written permission.
 *
 *    9. THIS PACKAGE IS PROVIDED "AS IS" AND WITHOUT ANY EXPRESS OR IMPLIED
 *    WARRANTIES, INCLUDING, WITHOUT LIMITATION, THE IMPLIED WARRANTIES OF
 *    MERCHANTIBILITY AND FITNESS FOR A PARTICULAR PURPOSE.
 *
 */
package org.chiba.xml.xforms.constraints;

import org.apache.commons.jxpath.JXPathContext;
import org.apache.commons.jxpath.Pointer;
import org.apache.commons.jxpath.ri.Parser;
import org.apache.log4j.Category;
import org.apache.xerces.dom.NodeImpl;
import org.chiba.xml.xforms.Instance;
import org.chiba.xml.xforms.LocalValue;
import org.chiba.xml.xforms.xpath.PathUtil;

import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

/**
 * Superclass for Dependency Checking.
 *
 * @author This code is based on the ideas of Mikko Honkala from the X-Smiles project.
 *         Although it has been heavily refactored and rewritten to meet our needs.
 * @version $Id: DependencyGraph.java,v 1.19 2004/12/21 15:35:11 unl Exp $
 */
public class DependencyGraph {
    /**
     * Logger
     */
    protected static Category LOGGER = Category.getInstance(DependencyGraph.class);

    /**
     * holds all vertices for a model
     */
    protected Vector vertices;

    /**
     * Creates a new DependencyGraph object.
     */
    public DependencyGraph() {
        this.vertices = new Vector();
    }

    /**
     * returns a Vertex of given type that is attached to a given instanceNode
     *
     * @param instanceNode the instance data node to check for Vertex
     * @param property     the wanted Model Item Property
     * @return returns the matching Vertex object or null if not found
     */
    public Vertex getVertex(NodeImpl instanceNode, short property) {
        Enumeration enumeration = vertices.elements();

        while (enumeration.hasMoreElements()) {
            Vertex v = (Vertex) enumeration.nextElement();
            boolean equalNodes = v.instanceNode == instanceNode;
            boolean equalTypes = v.getVertexType() == property;

            if (equalNodes && equalTypes) {
                return v;
            }
        }

        return null;
    }

    /**
     * determines which nodes are referenced by given xpath expression and returns them as nodes.
     *
     * @param xpath - the xpath expression under examination
     * @return a list with nodes referenced in given xpath
     */
    public Vector getXPathRefNodes(JXPathContext relativeContext, String xpath) {
        ReferenceFinder referenceFinder = new ReferenceFinder();
        Parser.parseExpression(xpath, referenceFinder);

        List pathes = referenceFinder.getLocationPathes();
        Vector refNodes = new Vector();

        for (int i = 0; i < pathes.size(); i++) {
            String refPath = pathes.get(i).toString();
            Instance instance = (Instance) relativeContext.getParentContext().getContextBean();
            JXPathContext context = relativeContext;

            if (PathUtil.hasInstanceFunction(refPath)) {
                String instanceId = PathUtil.getInstanceId(instance.getModel(), refPath);

                // use container for instance lookup to allow cross-model references
                instance = (Instance) instance.getModel().getContainer().lookup(instanceId);
                context = instance.getInstanceContext();
            }

// iterate all referenced nodes
            Iterator iterator = context.iteratePointers(refPath);

            while (iterator.hasNext()) {
                Pointer localPointer = (Pointer) iterator.next();
//                Object node = localPointer.getNode();
//                if (node instanceof Pointer) {
//                    localPointer = (Pointer) node;
//                }

                String realPath = localPointer.asPath();
                LocalValue localValue = instance.getModelItem(realPath);

                if (localValue != null) {
// add *existing* reference node
                    refNodes.add(localValue.getNode());
                }
            }
        }

        return refNodes;
    }

    /**
     * constructs edges for dependency graph.
     *
     * @param from connecting Vertex
     * @param to   connected Vertex
     */
    public void addEdge(Vertex from, Vertex to) {
        from.addDep(to);

        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("added edge from " + from + " to " + to);
        }
    }

    /**
     * adds a new vertex to the graph.
     * If the vertex v already exists in graph:
     * - if v.bind == null, then update v.bind = bind
     * This is called by MainDependencyGraph.addBind()
     */
    public Vertex addVertex(JXPathContext relativeContext, NodeImpl instanceNode, String xpathExpression,
                            short property) {
        Vertex v = this.getVertex(instanceNode, property);

        if (v != null) {
            v.wasAlreadyInGraph = true;

            // set value of pre-built vertex. vertices are pre-built when
            // they are referenced before their bind is processed
            if (v.relativeContext == null) {
                v.relativeContext = relativeContext;
                v.xpathExpression = xpathExpression;
            }

            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("found vertex " + v);
            }

            return v;
        }

        switch (property) {
            case Vertex.CALCULATE_VERTEX:
                v = new CalculateVertex(relativeContext, instanceNode, xpathExpression);

                break;

            case Vertex.RELEVANT_VERTEX:
                v = new RelevantVertex(relativeContext, instanceNode, xpathExpression);

                break;

            case Vertex.READONLY_VERTEX:
                v = new ReadonlyVertex(relativeContext, instanceNode, xpathExpression);

                break;

            case Vertex.CONSTRAINT_VERTEX:
                v = new ConstraintVertex(relativeContext, instanceNode, xpathExpression);

                break;

            case Vertex.REQUIRED_VERTEX:
                v = new RequiredVertex(relativeContext, instanceNode, xpathExpression);

                break;
        }

        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("built vertex " + v);
        }

        vertices.addElement(v);

        return v;
    }

    /**
     * adds a Vertex to the pool hold by this object. Will add no duplicates to the collection.
     *
     * @param v the Vertex to add
     */
    private void addVertex(Vertex v) {
        if (!vertices.contains(v)) {
            vertices.addElement(v);
        }
    }

    /**
     * print the list of vertices
     */
    public void printGraph() {
        Enumeration enumeration = vertices.elements();

        while (enumeration.hasMoreElements()) {
            Vertex v = (Vertex) enumeration.nextElement();

            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("Next vertex:");
            }

            v.print(0);
        }
    }

    /**
     * recalculates this graph.
     */
    public void recalculate() {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("starting recalculation ...");
        }

        // remove non zero vertices
        this.removeNonZeroVertices();

        while (this.vertices.size() > 0) {
            // remove a vertex v from Z
            Vertex v = (Vertex) this.vertices.firstElement();
            this.removeVertex(v);
            v.compute();

            Enumeration enumeration = v.depList.elements();

            while (enumeration.hasMoreElements()) {
                Vertex w = (Vertex) enumeration.nextElement();
                w.inDegree--;

                if (w.inDegree == 0) {
                    this.addVertex(w);
                }
            }
        }

        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("... recalculation finished");
        }
    }

    private void removeNonZeroVertices() {
        Vector nonzeros = new Vector();
        Enumeration enumeration = vertices.elements();

        while (enumeration.hasMoreElements()) {
            Vertex v = (Vertex) enumeration.nextElement();

            if (v.inDegree > 0) {
                nonzeros.addElement(v);
            }
        }

        enumeration = nonzeros.elements();

        while (enumeration.hasMoreElements()) {
            Vertex v = (Vertex) enumeration.nextElement();
            this.removeVertex(v);
        }
    }

    /**
     * removes a Vertex from the collection.
     *
     * @param v the Vertex to remove
     */
    protected void removeVertex(Vertex v) {
        this.vertices.removeElement(v);
    }
}

//end of class

