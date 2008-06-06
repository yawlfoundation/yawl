/*
 * IzPack - Copyright 2001-2006 Julien Ponge, All Rights Reserved.
 * 
 * http://www.izforge.com/izpack/
 * http://developer.berlios.de/projects/izpack/
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 *     
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.izforge.izpack.panels;

import com.izforge.izpack.installer.InstallData;
import com.izforge.izpack.installer.InstallerFrame;
import java.util.Iterator;
import com.izforge.izpack.Pack;

/**
 * This panel adds some conditional behavior to the standard UserInputPanel. <br/> <b>Usage:</b><br/>
 * In the "panels" list, just use ConditionalUserInputPanel like the normal UserInputPanel. The
 * specification goes also into userInputSpec.xml and userInputLang.xml_XXX. To specify a condition
 * for a certain ConditionalUserInputPanel, you have to specify the condition in the
 * "variables"-section by defining the following variables:<br/>
 * <li><i>compareToVariable."panel-order"</i>: The variable name containing the value to compare
 * with
 * <li><i>compareToOperator."panel-order"</i>: The compare operator to use, currently only "=" and
 * "!=" are allowed
 * <li><i>compareToValue."panel-order"</i>: The value to compare with<br/> If the compare fails,
 * the panel will be skipped.
 * 
 * @see UserInputPanel
 * 
 * @author $author$
 * @version $Revision: 1421 $
 */
public class PackageDependentUserInputPanel extends UserInputPanel
{     

    /**

     */
    public PackageDependentUserInputPanel(InstallerFrame parent, InstallData installData)
    {
        super(parent, installData);
    }

    /**
     * Panel is only activated, if the configured condition is true
     */
    public void panelActivate()
    {
        // get configured condition for this panel
        String compareToValue = idata.getVariable("PackageDependency." + instanceNumber);
	  
        boolean found = false;
        if (null!=compareToValue)
        {

            java.util.Iterator iter = idata.selectedPacks.iterator();
            while (iter.hasNext())
            {
                Pack p = (Pack) iter.next();
                if (p.name.equals(compareToValue)) {
                     found = true;
                }
		}

            // compare using equal
            if (!found)
            {
                super.panelActivate();
            }
            else
            {
                parent.skipPanel();
            }
        }
        else
        {
            // wrong operator!
            emitError("Error in specification: maybe package does not exist","");
            parent.skipPanel();
        }
    }
}

