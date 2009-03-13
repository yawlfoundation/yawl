/*
 * Created on 10/10/2003
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

package org.yawlfoundation.yawl.editor.swing;

import org.yawlfoundation.yawl.editor.net.NetGraph;
import org.yawlfoundation.yawl.editor.specification.SpecificationModel;
import org.yawlfoundation.yawl.editor.specification.SpecificationSelectionListener;
import org.yawlfoundation.yawl.editor.swing.net.YAWLEditorNetPanel;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;

public class YAWLEditorDesktop extends JTabbedPane implements ChangeListener {

  /**
   *
   */
  private static final long serialVersionUID = 1L;
  private static final SpecificationModel model = SpecificationModel.getInstance();
  private static YAWLEditorDesktop INSTANCE = null;


   private YAWLEditorDesktop() {
     super();
     addChangeListener(this);  
   }

    public static YAWLEditorDesktop getInstance( ) {
       if (INSTANCE == null)
           INSTANCE = new YAWLEditorDesktop();
       return INSTANCE;
   }


  public YAWLEditorNetPanel newNet() {
    YAWLEditorNetPanel frame = new YAWLEditorNetPanel(getBounds());
    bindFrame(frame);
    return frame;
  }


  public void openNet(NetGraph graph) {
      YAWLEditorNetPanel frame = new YAWLEditorNetPanel(getBounds(), graph);
      bindFrame(frame);
      SpecificationSelectionListener.getInstance().publishSubscriptions(
              graph.getSelectionModel(), null);
  }


  private void bindFrame(final YAWLEditorNetPanel frame) {
      addTab(frame.getTitle(), frame.getFrameIcon(), frame);
      updateState();
  }


  public void removeActiveNet() {
      YAWLEditorNetPanel frame = (YAWLEditorNetPanel) getSelectedComponent();
      if ((frame != null) && (! frame.getNet().getNetModel().isStartingNet())) {
        frame.removeFromSpecification();
        remove(frame);
      }
  }


    public void closeAllNets() {
        Component[] frames = getComponents();

        for(int i = 0; i < frames.length; i++) {
            ((YAWLEditorNetPanel) frames[i]).resetFrame();
            remove(frames[i]);
        }
    }


  private void updateState() {
    YAWLEditorNetPanel frame = (YAWLEditorNetPanel) this.getSelectedComponent();
    if ((frame == null) || (frame.getNet() == null)) {
      model.nothingSelected();
      return;
    }
    model.somethingSelected();
    try {
      getSelectedGraph().getSelectionListener().forceActionUpdate();
      getSelectedGraph().getCancellationSetModel().refresh();
    }
    catch (Exception e) {}
  }

    
  public NetGraph getSelectedGraph() {
      YAWLEditorNetPanel frame = (YAWLEditorNetPanel) this.getSelectedComponent();
    if (frame != null) {
      return frame.getNet();
    }
    return null;
  }

  public void stateChanged(ChangeEvent e) {
      updateState();
  }
}