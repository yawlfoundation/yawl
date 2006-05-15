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
import org.apache.xerces.dom.NodeImpl;

import java.util.Enumeration;
import java.util.Vector;

/**
 * A base class for vertices used in the recalculation sequence algorithm.
 *
 * @author This code is based on the ideas of Mikko Honkala from the X-Smiles project.
 *         Although it has been heavily refactored and rewritten to meet our needs.
 * @version $Id$
 */
public abstract class Vertex {
    /**
     * constant for calculate Vertex
     */
    public static final short CALCULATE_VERTEX = 1;

    /**
     * constant for relevant Vertex
     */
    public static final short RELEVANT_VERTEX = 2;

    /**
     * constant for readonly Vertex
     */
    public static final short READONLY_VERTEX = 3;

    /**
     * constant for required Vertex
     */
    public static final short REQUIRED_VERTEX = 4;

    /**
     * constant for constraint Vertex
     */
    public static final short CONSTRAINT_VERTEX = 5;

    //    protected Vertex index;

    /**
     * the parent JXPathContext used for evaluation of this Vertex
     */
    protected JXPathContext relativeContext = null;

    /**
     * the instance node this Vertex is attached to
     */
    protected NodeImpl instanceNode = null;

    /**
     * the value of the Model Item Property
     */
    protected String xpathExpression = null;

    /**
     * list of referencees
     */
    protected Vector depList; //should be array?

    /**
     * flag used to avoid duplicates
     */
    protected boolean wasAlreadyInGraph = false;

    /**
     * is 0 if this Vertex is not dependent on any other, otherwise the count of Vertices this Vertex depends on.
     */
    protected int inDegree;

    /**
     * Creates a new Vertex object.
     *
     * @param relativeContext __UNDOCUMENTED__
     * @param instanceNode    __UNDOCUMENTED__
     * @param xpathExpression __UNDOCUMENTED__
     */
    public Vertex(JXPathContext relativeContext, NodeImpl instanceNode, String xpathExpression) {
        this.relativeContext = relativeContext;
        this.instanceNode = instanceNode;
        this.xpathExpression = xpathExpression;
        this.depList = new Vector();

        //        this.visited = false;
        this.inDegree = 0;

        //        this.index = null;
    }

    /**
     * to be overwritten by subclass to signal the Vertextype.
     *
     * @return a constant defining Vertex type
     */
    public abstract short getVertexType();

    /**
     * to be overwritten by subclass to evaluate the xpath expression in context
     * of its parent context (relativeContext).
     */
    public abstract void compute();

    /**
     * returns xpath expression for this Vertex.
     *
     * @return xpath expression for this Vertex
     */
    public String getXPathExpression() {
        return this.xpathExpression;
    }

    /**
     * adds a dependent Vertex to the depList
     *
     * @param to the Vertex to add to the dependents
     */
    public void addDep(Vertex to) {
        // 1. A vertex will be added to a depList only once
        // 2. Vertex v is excluded from its own depList to allow self-references
        // to occur without causing a circular reference exception.
        if (this.depList.contains(to) || (this == to)) {
            return;
        }

        this.depList.addElement(to);
        to.inDegree++;
    }

    /**
     * returns true, if instanceNode and xpath expression are the same.
     *
     * @param object the Vertex object to compare
     * @return true, if instanceNode and xpath expression are the same.
     */
    public boolean equals(Object object) {
        if (object == null) {
            return false;
        }

        if (!(object instanceof Vertex)) {
            return false;
        }

        Vertex v = (Vertex) object;

        return this.instanceNode.equals(v.instanceNode) && this.xpathExpression.equals(v.xpathExpression) &&
                (getVertexType() == v.getVertexType());
    }

    /**
     * recursively prints some debug info.
     *
     * @param level the level of the graph to print
     */
    public void print(int level) {
        for (int i = 0; i < level; i++) {
            System.out.print("\t");
        }

        System.out.println(this.getClass().getName() + ": " + this.instanceNode.getNodeName() + " = '" +
                this.instanceNode.getTextContent() + "' inDegree:" + inDegree);

        Enumeration enumeration = depList.elements();

        while (enumeration.hasMoreElements()) {
            Vertex v = (Vertex) enumeration.nextElement();
            v.print(level + 1);
        }
    }
}

//end of class


