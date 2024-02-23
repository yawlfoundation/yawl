/*
 * Copyright (c) 2004-2020 The YAWL Foundation. All rights reserved.
 * The YAWL Foundation is a collaboration of individuals and
 * organisations who are committed to improving workflow technology.
 *
 * This file is part of YAWL. YAWL is free software: you can
 * redistribute it and/or modify it under the terms of the GNU Lesser
 * General Public License as published by the Free Software Foundation.
 *
 * YAWL is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General
 * Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with YAWL. If not, see <http://www.gnu.org/licenses/>.
 */

package org.yawlfoundation.yawl.procletService.persistence;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Michael Adams
 * @date 3/02/12
 */
public class StoredDecisions {

    private long pkey;
    private String decision0;
    private String decision1;
    private String decision2;
    private String decision3;
    private String decision4;
    private String decision5;
    private int decision6;

    public StoredDecisions() { }

    public StoredDecisions(List decisions) {
        this.decision0 = (String) decisions.get(0);
        this.decision1 = (String) decisions.get(1);
        this.decision2 = (String) decisions.get(2);
        this.decision3 = (String) decisions.get(3);
        this.decision4 = (String) decisions.get(4);
        this.decision5 = (String) decisions.get(5);
        this.decision6 = (Integer) decisions.get(6);
    }
    
    
    public List getDecisionsAsList() {
        List decisions = new ArrayList<String>();
        decisions.add(decision0);
        decisions.add(decision1);
        decisions.add(decision2);
        decisions.add(decision3);
        decisions.add(decision4);
        decisions.add(decision5);
        decisions.add(decision6);
        return decisions;
    }


    public long getPkey() {
        return pkey;
    }

    public void setPkey(long pkey) {
        this.pkey = pkey;
    }

    public String getDecision0() {
        return decision0;
    }

    public void setDecision0(String decision0) {
        this.decision0 = decision0;
    }

    public String getDecision1() {
        return decision1;
    }

    public void setDecision1(String decision1) {
        this.decision1 = decision1;
    }

    public String getDecision2() {
        return decision2;
    }

    public void setDecision2(String decision2) {
        this.decision2 = decision2;
    }

    public String getDecision3() {
        return decision3;
    }

    public void setDecision3(String decision3) {
        this.decision3 = decision3;
    }

    public String getDecision4() {
        return decision4;
    }

    public void setDecision4(String decision4) {
        this.decision4 = decision4;
    }

    public String getDecision5() {
        return decision5;
    }

    public void setDecision5(String decision5) {
        this.decision5 = decision5;
    }

    public int getDecision6() {
        return decision6;
    }

    public void setDecision6(int decision6) {
        this.decision6 = decision6;
    }
}
