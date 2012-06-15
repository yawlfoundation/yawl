package org.yawlfoundation.yawl.editor.core.resourcing;

import org.jdom2.Element;
import org.jdom2.Namespace;
import org.yawlfoundation.yawl.resourcing.constraints.AbstractConstraint;
import org.yawlfoundation.yawl.resourcing.constraints.ConstraintFactory;
import org.yawlfoundation.yawl.resourcing.filters.AbstractFilter;
import org.yawlfoundation.yawl.resourcing.filters.FilterFactory;
import org.yawlfoundation.yawl.resourcing.interactions.AbstractInteraction;
import org.yawlfoundation.yawl.resourcing.interactions.ResourceParseException;
import org.yawlfoundation.yawl.resourcing.resource.Participant;
import org.yawlfoundation.yawl.resourcing.resource.Role;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * A reduced version of yawl's OfferInteraction class - we just need the storage parts,
 * no runtime required
 *
 * @author Michael Adams
 * @date 14/06/12
 */
public class BaseOfferInteraction extends AbstractInteraction {

    // distribution set
    private HashSet<Participant> _participants = new HashSet<Participant>();
    private HashSet<Role> _roles  = new HashSet<Role>();
    private HashSet<DynParam> _dynParams  = new HashSet<DynParam>();

    private HashSet<AbstractFilter> _filters  = new HashSet<AbstractFilter>();
    private HashSet<AbstractConstraint> _constraints  = new HashSet<AbstractConstraint>();

    private String _familiarParticipantTask ;


    protected BaseOfferInteraction(String taskID) { super(taskID); }

    protected BaseOfferInteraction(int initiator) { super(initiator); }


    protected void addParticipant(String id) {
        _participants.add(new Participant(id));
    }


    protected void addParticipantsByID(String idList) {
        String[] ids = idList.split(",") ;
        for (String id : ids) addParticipant(id.trim());
    }


    protected void addRole(String rid) {
        Role r =  new Role();
        r.setID(rid);
        _roles.add(r);
    }

    protected void addRoles(String roleList) {
        String[] roles = roleList.split(",") ;
        for (String role : roles) addRole(role.trim());
    }


    protected void addDynParam(String name, DynParam.Refers refers) {
        DynParam p = new DynParam(name, refers);
        _dynParams.add(p);
    }

    protected void addDynParam(DynParam param) {
        _dynParams.add(param);
    }


    protected void addFilter(AbstractFilter f) {
        _filters.add(f);
    }

    protected void addConstraint(AbstractConstraint c) {
        _constraints.add(c);
    }


    protected void setFamiliarParticipantTask(String taskid) {
       _familiarParticipantTask = taskid ;
    }

    protected String getFamiliarParticipantTask() {
        return _familiarParticipantTask;
    }

    protected void clearFamiliarParticipantTask() {
        _familiarParticipantTask = null;
     }

    protected Set<Participant> getParticipants() { return _participants; }

    protected Set<Role> getRoles() { return _roles; }

    protected Set<DynParam> getDynParams() { return _dynParams; }

    protected Set<AbstractFilter> getFilters() { return _filters; }

    protected Set<AbstractConstraint> getConstraints() { return _constraints; }

    protected void removeParticipant(String id) {
        Participant match = null;
        for (Participant p : _participants) {
            if (p.getID().equals(id)) {
                match = p;
                break;
            }
        }
        if (match != null) _participants.remove(match);
    }

    protected void clearParticipants() { _participants.clear(); }

    protected void removeRole(String id) {
        Role match = null;
        for (Role r : _roles) {
            if (r.getID().equals(id)) {
                match = r;
                break;
            }
        }
        if (match != null) _roles.remove(match);
    }

    protected void clearRoles() { _roles.clear(); }


    protected void removeDynParam(String name) {
        DynParam match = null;
        for (DynParam p : _dynParams) {
            if (p.getName().equals(name)) {
                match = p;
                break;
            }
        }
        if (match != null) _dynParams.remove(match);
    }

    protected void clearDynParams() { _dynParams.clear(); }

    protected void removeFilter(String name) {
        AbstractFilter match = null;
        for (AbstractFilter f : _filters) {
            if (f.getName().equals(name)) {
                match = f;
                break;
            }
        }
        if (match != null) _filters.remove(match);
    }

    protected void clearFilters() { _filters.clear(); }

    protected void removeConstraint(String name) {
        AbstractConstraint match = null;
        for (AbstractConstraint c : _constraints) {
            if (c.getName().equals(name)) {
                match = c;
                break;
            }
        }
        if (match != null) _constraints.remove(match);
    }

    protected void clearConstraints() { _constraints.clear(); }


    protected Set<String> getDynParamNames() {
         Set<String> names = new HashSet<String>();
         for (DynParam param : _dynParams) {
             names.add(param.getName() + "[" + param.getRefersString() + "]");
         }
         return names;
     }


    protected void parse(Element e, Namespace nsYawl) throws ResourceParseException {

        parseInitiator(e, nsYawl);

        // if offer is not system-initiated, there's no more to do
        if (! isSystemInitiated()) return ;

        parseDistributionSet(e, nsYawl) ;
        parseFamiliarTask(e, nsYawl) ;
    }


    private void parseDistributionSet(Element e, Namespace nsYawl)
                                                       throws ResourceParseException {
        Element eDistSet = e.getChild("distributionSet", nsYawl);
        if (eDistSet != null) {
            parseInitialSet(eDistSet, nsYawl) ;
            parseFilters(eDistSet, nsYawl) ;
            parseConstraints(eDistSet, nsYawl) ;
        }
        else
            throw new ResourceParseException(
                    "Missing required element in Offer block: distributionSet") ;
    }


    private void parseInitialSet(Element e, Namespace nsYawl) throws ResourceParseException {

        Element eInitialSet = e.getChild("initialSet", nsYawl);
        if (eInitialSet != null) {
            parseParticipants(eInitialSet, nsYawl);
            parseRoles(eInitialSet, nsYawl);
            parseDynParams(eInitialSet, nsYawl);
        }
        else throw new ResourceParseException(
            "Missing required distributionSet child element in Offer block: initialSet") ;
    }


    private void parseParticipants(Element e, Namespace nsYawl) {

        // from the specified initial set, add all participants
        for (Element eParticipant : e.getChildren("participant", nsYawl)) {
            String participant = eParticipant.getText();
            if (participant.indexOf(',') > -1)
                addParticipantsByID(participant);
            else
                addParticipant(participant);
        }
    }


    private void parseRoles(Element e, Namespace nsYawl) {

        // ... and roles
        for (Element eRole : e.getChildren("role", nsYawl)) {
            String role = eRole.getText();
            if (role.indexOf(',') > -1)
                addRoles(role);
            else
                addRole(role);
        }
    }


    private void parseDynParams(Element e, Namespace nsYawl) {

        // ... and input parameters
        for (Element eParam : e.getChildren("param", nsYawl)) {
            String name = eParam.getChildText("name", nsYawl);
            String refers = eParam.getChildText("refers", nsYawl);
            DynParam.Refers pType = refers.equals("role") ?
                    DynParam.Refers.Role : DynParam.Refers.Participant;
            addDynParam(name, pType);
        }
    }


    private void parseFilters(Element e, Namespace nsYawl) throws ResourceParseException {

        // get the Filters
        Element eFilters = e.getChild("filters", nsYawl);
        if (eFilters != null) {
            List<Element> filters = eFilters.getChildren("filter", nsYawl);
            if (filters == null)
                throw new ResourceParseException(
                        "No filter elements found in filters element");

            for (Element eFilter : filters) {
                String filterClassName = eFilter.getChildText("name", nsYawl);
                if (filterClassName != null) {
                    AbstractFilter filter = FilterFactory.getInstance(filterClassName);
                    if (filter != null) {
                        filter.setParams(parseParams(eFilter, nsYawl));
                        _filters.add(filter);
                    }
                    else throw new ResourceParseException("Unknown filter name: " +
                                                                   filterClassName);
                }
                else throw new ResourceParseException("Missing filter element: name");
            }
        }
    }

    private void parseConstraints(Element e, Namespace nsYawl)
                                                        throws ResourceParseException {
        // get the Constraints
        Element eConstraints = e.getChild("constraints", nsYawl);
        if (eConstraints != null) {
            List<Element> constraints = eConstraints.getChildren("constraint", nsYawl);
            if (constraints == null)
                throw new ResourceParseException(
                        "No constraint elements found in constraints element");

            for (Element eConstraint : constraints) {
                String constraintClassName = eConstraint.getChildText("name", nsYawl);
                if (constraintClassName != null) {
                    AbstractConstraint constraint =
                            ConstraintFactory.getInstance(constraintClassName);
                    if (constraint != null) {
                        constraint.setParams(parseParams(eConstraint, nsYawl));
                        _constraints.add(constraint);
                    }
                    else throw new ResourceParseException("Unknown constraint name: " +
                                                                   constraintClassName);
                }
                else throw new ResourceParseException("Missing constraint element: name");
            }
        }
    }


    private void parseFamiliarTask(Element e, Namespace nsYawl) {

        // finally, get the familiar participant task
        Element eFamTask = e.getChild("familiarParticipant", nsYawl);
        if (eFamTask != null)
            _familiarParticipantTask = eFamTask.getAttributeValue("taskID");

    }

    /********************************************************************************/

    public String toXML() {
        StringBuilder xml = new StringBuilder("<offer ");

        xml.append("initiator=\"").append(getInitiatorString()).append("\">");

        // the rest of the xml is only needed if it's system initiated
        if (isSystemInitiated()) {
            xml.append("<distributionSet>") ;
            xml.append("<initialSet>");

            if (_participants != null) {
                for (Participant p : _participants) {
                    xml.append("<participant>").append(p.getID()).append("</participant>");
                }
            }
            if (_roles != null) {
                for (Role r : _roles) {
                    xml.append("<role>").append(r.getID()).append("</role>");
                }
            }
            if (_dynParams != null) {
                for (DynParam p : _dynParams) {
                    xml.append(p.toXML());
                }
            }

            xml.append("</initialSet>");

            if ((_filters != null) && (! _filters.isEmpty())) {
                xml.append("<filters>") ;
                for (AbstractFilter filter : _filters) {
                    xml.append(filter.toXML());
                }
                xml.append("</filters>") ;
            }

            if ((_constraints != null) && (! _constraints.isEmpty())) {
                xml.append("<constraints>") ;
                for (AbstractConstraint constraint : _constraints) {
                    xml.append(constraint.toXML());
                }
                xml.append("</constraints>") ;
            }

            xml.append("</distributionSet>") ;

            if (_familiarParticipantTask != null) {
                xml.append("<familiarParticipant taskID=\"");
                xml.append(_familiarParticipantTask).append("\"/>");
            }
        }

        xml.append("</offer>");
        return xml.toString();
    }


}
