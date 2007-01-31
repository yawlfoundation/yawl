/*
 * This file is made available under the terms of the LGPL licence.
 * This licence can be retreived from http://www.gnu.org/copyleft/lesser.html.
 * The source remains the property of the YAWL Foundation.  The YAWL Foundation is a collaboration of
 * individuals and organisations who are commited to improving workflow technology.
 *
 */


package au.edu.qut.yawl.elements.state;

import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.MapKey;
import javax.persistence.OneToMany;
import javax.persistence.Transient;

import org.hibernate.annotations.CollectionOfElements;

import au.edu.qut.yawl.elements.YConditionInterface;
import au.edu.qut.yawl.elements.YNet;
import au.edu.qut.yawl.exceptions.YPersistenceException;

/**
 * 
 * @author Lachlan Aldred
 * 
 */
@Entity
public class YIdentifierBag implements Serializable {
	/**
	 * One should only change the serialVersionUID when the class method signatures have changed.  The
	 * UID should stay the same so that future revisions of the class can still be backwards compatible
	 * with older revisions if method signatures have not changed. 
	 * Serial version format: year (4 digit) - month (2 digit) - yawl release version (4 digit)
	 */
	private static final long serialVersionUID = 2006030080l;
	private Map<YIdentifier, Integer> _idToQtyMap = new HashMap<YIdentifier, Integer>();
    public YConditionInterface _condition;

	
    public YIdentifierBag(YConditionInterface condition) {
        _condition = condition;
    }
    
    protected YIdentifierBag() {}
    
	private Long id;
	
	@Id
    @GeneratedValue(strategy=GenerationType.SEQUENCE)
    @Column(name="bag_id")
	public Long getId() {
		return id;
	}
	
	public void setId( Long id ) {
		this.id = id;
	}

    private void setIdentifierToQuantityMap(Map<YIdentifier, Integer> map) {
    	_idToQtyMap = map;
    }
    

//	@MapKey
//	@OneToMany
//	@JoinTable(
//			name = "IdentifierBagCountMap",
//			joinColumns = {@JoinColumn(name = "identifier_id")},
//			inverseJoinColumns = @JoinColumn(name = "count_id"))

    @CollectionOfElements (fetch=FetchType.EAGER)
    @JoinTable(name="counted_identifiers", joinColumns = @JoinColumn(name="bag_id"))
    @org.hibernate.annotations.MapKey (columns=@Column(name="identifier_id"))
    @Column(name="value", nullable=true)     
    private Map<YIdentifier, Integer> getIdentifierToQuantityMap() {
    	return _idToQtyMap;
    }
    
    public void addIdentifier(YIdentifier identifier) throws YPersistenceException {
        if (_idToQtyMap.containsKey(identifier)) {
        	_idToQtyMap.put(identifier,_idToQtyMap.get(identifier)+1);
        } else {
        	_idToQtyMap.put(identifier, Integer.valueOf(1));
        }
    }


    public int getAmount(YIdentifier identifier) {
        if (_idToQtyMap.containsKey(identifier)) {
            return _idToQtyMap.get(identifier).intValue();
        } else
            return 0;
    }

    public boolean contains(YIdentifier identifier) {
        return _idToQtyMap.containsKey(identifier);
    }


    @Transient
    public List getIdentifiers() {
        List idList = new Vector();
        Set keys = _idToQtyMap.keySet();
        Iterator iter = keys.iterator();
        while (iter.hasNext()) {
            YIdentifier identifier = (YIdentifier) iter.next();
            int amnt = _idToQtyMap.get(identifier).intValue();
            for (int i = 0; i < amnt; i++) {
                idList.add(identifier);
            }
        }
        return idList;
    }


    public void remove(YIdentifier identifier, YNet net, int amountToRemove) throws YPersistenceException {
        if (_idToQtyMap.containsKey(identifier)) {
            int amountExisting = _idToQtyMap.get(identifier).intValue();
            if (amountToRemove <= 0) {
                throw new RuntimeException("You cannot remove " + amountToRemove
                        + " from YIdentifierBag:" + _condition + " " + identifier.toString());
            } else if (amountExisting > amountToRemove) {
            	_idToQtyMap.put(identifier,_idToQtyMap.get(identifier) - amountToRemove);
            } else if (amountToRemove == amountExisting) {
                _idToQtyMap.remove(identifier);
            } else {
                throw new RuntimeException("You cannot remove " + amountToRemove
                        + " tokens from YIdentifierBag:" + _condition
                        + " - this bag only contains " + amountExisting
                        + " identifiers of type " + identifier.toString());
            }
        } else {
            throw new RuntimeException("You cannot remove " + amountToRemove
                    + " tokens from YIdentifierBag:" + _condition
                    + " - this bag contains no"
                    + " identifiers of type " + identifier.toString()
                    + ".  It does have " + this.getIdentifiers()
                    + " (locations of " + identifier + ":" + identifier.getLocationsForNet(net) + " )"
            );
        }
    }

    public Set<YIdentifier> removeAll() {
    	Set<YIdentifier> removed = new HashSet<YIdentifier>( _idToQtyMap.keySet() );
        Iterator<YIdentifier> keys = removed.iterator();
        while (keys.hasNext()) {
            YIdentifier identifier = keys.next();
            _idToQtyMap.remove(identifier);
        }
        return removed;
    }
}
