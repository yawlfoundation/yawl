/*
 * Created on 23/10/2003
 * YAWLEditor v1.0 
 *
 * @author Lindsay Bradford
 * 
 * 
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 *
 */

package org.yawlfoundation.yawl.editor.ui.elements.model;

import org.yawlfoundation.yawl.elements.YDecomposition;
import org.yawlfoundation.yawl.elements.YTask;
import org.yawlfoundation.yawl.elements.YTimerParameters;

import java.awt.geom.Point2D;

public class AtomicTask extends YAWLTask implements YAWLAtomicTask {

    /**
     * This constructor is to be invoked whenever we are creating a new
     * atomic task from scratch. It also creates the correct ports needed for
     * the task as an intended side-effect.
     */
    public AtomicTask(Point2D startPoint) {
        super(startPoint);
    }

    public AtomicTask(Point2D startPoint, YTask yTask) {
        super(startPoint);
        setShadowTask(yTask);
    }


    /**
     * This constructor is to be invoked whenever we are creating a new
     * atomic task from scratch with an icon. It also creates the correct
     * ports needed for the task as an intended side-effect.
     */
    public AtomicTask(Point2D startPoint, String iconPath) {
        super(startPoint, iconPath);
    }

    public String getType() {
        return "Atomic Task";
    }

    public void setDecomposition(YDecomposition decomposition) {
        super.setDecomposition(decomposition);
    }

    public YDecomposition getDecomposition() {
        return super.getDecomposition();
    }

    public void setTimerParameters(YTimerParameters timerParameters) {
        getShadowTask().setTimerParameters(timerParameters);
    }

    public YTimerParameters getTimerParameters() {
        return getShadowTask().getTimerParameters();
    }


    public boolean hasTimerEnabled() {
        return getTimerParameters() != null;
    }

}
