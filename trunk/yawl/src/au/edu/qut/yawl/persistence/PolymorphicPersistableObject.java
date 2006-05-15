/*
 * This file is made available under the terms of the LGPL licence.
 * This licence can be retreived from http://www.gnu.org/copyleft/lesser.html.
 * The source remains the property of the YAWL Group.  The YAWL Group is a collaboration of 
 * individuals and organiations who are commited to improving workflow technology.
 *
 */


package au.edu.qut.yawl.persistence;

/**
 * 
 * @author Dean Mao
 * @created Oct 27, 2005
 */
public interface PolymorphicPersistableObject extends PersistableObject {
		/**
		 * This method must occur in all classes to be persisted.  This method is
		 * responsible for setting the typeID attribute if the class is persisted
		 * by means of discriminator.  It is also responsible for setting the id
		 * if there is a necessary unsaved-value.
		 *
		 */
}
