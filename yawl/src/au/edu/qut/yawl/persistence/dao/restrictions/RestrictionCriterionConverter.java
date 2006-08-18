/*
 * This file is made available under the terms of the LGPL licence.
 * This licence can be retreived from http://www.gnu.org/copyleft/lesser.html.
 * The source remains the property of the YAWL Foundation.  The YAWL Foundation is a collaboration of
 * individuals and organisations who are commited to improving workflow technology.
 *
 */
package au.edu.qut.yawl.persistence.dao.restrictions;

import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Restrictions;

/**
 * Converts a restriction into a Hibernate Criterion.
 * 
 * @author Nathan Rose
 */
public class RestrictionCriterionConverter {
	/**
	 * @return a {@link Criterion} object representing the given restriction.
	 */
	public static Criterion convertRestriction( Restriction r ) {
		if( r instanceof Unrestricted ) {
			return Restrictions.sqlRestriction( "true" );
		}
		else if( r instanceof LogicalRestriction ) {
			LogicalRestriction lr = (LogicalRestriction) r;
			switch( lr.getOperation() ) {
				case AND:
					return Restrictions.and(
							convertRestriction( lr.getRestriction1() ),
							convertRestriction( lr.getRestriction2() ) );
				case OR:
					return Restrictions.or(
							convertRestriction( lr.getRestriction1() ),
							convertRestriction( lr.getRestriction2() ) );
				case XOR:
					Criterion crit1 = convertRestriction( lr.getRestriction1() );
					Criterion crit2 = convertRestriction( lr.getRestriction2() );
					Criterion not1 = Restrictions.not( crit1 );
					Criterion not2 = Restrictions.not( crit2 );
					Criterion lhs = Restrictions.and( crit1, not2 );
					Criterion rhs = Restrictions.and( not1, crit2 );
					return Restrictions.or( lhs, rhs );
				case NOR:
					return Restrictions.not( Restrictions.or(
							convertRestriction( lr.getRestriction1() ),
							convertRestriction( lr.getRestriction2() ) ) );
				case NAND:
					return Restrictions.not( Restrictions.and(
							convertRestriction( lr.getRestriction1() ),
							convertRestriction( lr.getRestriction2() ) ) );
				default:
					throw new UnsupportedOperationException(
							"Unsupported logical operation: " + lr.getOperation() );
			}
		}
		else if( r instanceof NegatedRestriction ) {
			return Restrictions.not( convertRestriction( ((NegatedRestriction) r ).getRestriction() ) );
		}
		else if( r instanceof PropertyRestriction ) {
			PropertyRestriction restriction = (PropertyRestriction) r;
			switch( restriction.getComparison() ) {
				case EQUAL:
					return Restrictions.eq( restriction.getPropertyName(), restriction.getValue() );
				case LIKE:
					return Restrictions.like( restriction.getPropertyName(), restriction.getValue() );
				case NOT_EQUAL:
					return Restrictions.ne( restriction.getPropertyName(), restriction.getValue() );
				case LESS_THAN:
					return Restrictions.lt( restriction.getPropertyName(), restriction.getValue() );
				case LESS_THAN_OR_EQUAL:
					return Restrictions.le( restriction.getPropertyName(), restriction.getValue() );
				case GREATER_THAN:
					return Restrictions.gt( restriction.getPropertyName(), restriction.getValue() );
				case GREATER_THAN_OR_EQUAL:
					return Restrictions.ge( restriction.getPropertyName(), restriction.getValue() );
				default:
					throw new UnsupportedOperationException(
							"Unsupported comparison: " + restriction.getComparison() );
			}
		}
		else {
			throw new UnsupportedOperationException( "Restriction " + r.getClass().getName() +
					" is not supported!" );
		}
	}
}
