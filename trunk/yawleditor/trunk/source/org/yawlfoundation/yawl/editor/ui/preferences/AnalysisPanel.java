/*
 * Created on 09/10/2003
 * YAWLEditor v1.0 
 *
 * @author Lindsay Bradford
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

package org.yawlfoundation.yawl.editor.ui.preferences;

import org.yawlfoundation.yawl.editor.ui.engine.AnalysisResultsParser;
import org.yawlfoundation.yawl.editor.ui.util.UserSettings;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;

public class AnalysisPanel extends JPanel implements PreferencePanel {

    private JCheckBox wofYawlAnalysisCheckBox;
    private JCheckBox relaxedSoundnessCheckBox;
    private JCheckBox transitionInvariantCheckBox;
    private JCheckBox extendedCoverabilityCheckBox;

    private JCheckBox resetNetAnalysisCheckBox;
    private JCheckBox weakSoundnessCheckBox;
    private JCheckBox soundnessCheckBox;
    private JCheckBox cancellationCheckBox;
    private JCheckBox orjoinCheckBox;
    private JCheckBox orjoinCycleCheckBox;
    private JCheckBox showObservationsCheckBox;
    private JCheckBox useYAWLReductionRulesCheckBox;
    private JCheckBox useResetReductionRulesCheckBox;
    private JCheckBox keepOpenCheckBox;

    public AnalysisPanel(ActionListener listener) {
        super();
        add(getContentPanel(listener));
        if (! AnalysisResultsParser.wofYawlAvailable()) {
            wofYawlAnalysisCheckBox.setEnabled(false);
            enableWofYAWLCheckBoxes(false);
        }
    }


    public void applyChanges() {
        UserSettings.setResetNetAnalysis(resetNetAnalysisCheckBox.isSelected());
        UserSettings.setWeakSoundnessAnalysis(weakSoundnessCheckBox.isSelected());
        UserSettings.setSoundnessAnalysis(soundnessCheckBox.isSelected());
        UserSettings.setCancellationAnalysis(cancellationCheckBox.isSelected());
        UserSettings.setOrJoinAnalysis(orjoinCheckBox.isSelected());
        UserSettings.setOrJoinCycleAnalysis(orjoinCycleCheckBox.isSelected());
        UserSettings.setShowObservations(showObservationsCheckBox.isSelected());
        UserSettings.setUseYawlReductionRules(useYAWLReductionRulesCheckBox.isSelected());
        UserSettings.setUseResetReductionRules(useResetReductionRulesCheckBox.isSelected());
        UserSettings.setKeepAnalysisDialogOpen(keepOpenCheckBox.isSelected());

        UserSettings.setWofyawlAnalysis(wofYawlAnalysisCheckBox.isSelected());
        UserSettings.setStructuralAnalysis(relaxedSoundnessCheckBox.isSelected());
        UserSettings.setBehaviouralAnalysis(transitionInvariantCheckBox.isSelected());
        UserSettings.setExtendedCoverability(extendedCoverabilityCheckBox.isSelected());
    }


    private JPanel getContentPanel(ActionListener listener) {
        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));

        content.add(getResetNetAnalysisCheckBox(listener));
        content.add(getUseYAWLReductionRulesCheckBox(listener));
        content.add(getUseResetReductionRulesCheckBox(listener));
        content.add(getWeakSoundnessCheckBox(listener));
        content.add(getCancellationCheckBox(listener));
        content.add(getOrjoinCheckBox(listener));
        content.add(getOrjoinCycleCheckBox(listener));
        content.add(getSoundnessCheckBox(listener));
        content.add(getShowObservationsCheckBox(listener));
        content.add(getKeepOpenCheckBox(listener));

        content.add(getWofYawlAnalysisCheckBox(listener));
        content.add(getRelaxedSoundnessCheckBox(listener));
        content.add(getTransitionInvariantCheckBox(listener));
        content.add(getExtendedCoverabilityCheckBox(listener));

        return content;
    }

    private JCheckBox getResetNetAnalysisCheckBox(ActionListener listener) {
        resetNetAnalysisCheckBox = makeCheckBox("Use the reset net analysis algorithm",
                KeyEvent.VK_U, UserSettings.getResetNetAnalysis(), false, listener);
        resetNetAnalysisCheckBox.addActionListener(
                new ActionListener() {
                    public void actionPerformed(ActionEvent event) {
                        enableResetNetCheckBoxes(resetNetAnalysisCheckBox.isSelected());
                    }
                }
        );
        return resetNetAnalysisCheckBox;
    }

    private void enableResetNetCheckBoxes(boolean enable) {
            weakSoundnessCheckBox.setEnabled(enable);
            soundnessCheckBox.setEnabled(enable);
            cancellationCheckBox.setEnabled(enable);
            orjoinCheckBox.setEnabled(enable);
            orjoinCycleCheckBox.setEnabled(enable);
            showObservationsCheckBox.setEnabled(enable);
            useYAWLReductionRulesCheckBox.setEnabled(enable);
            useResetReductionRulesCheckBox.setEnabled(enable);
    }

    private JCheckBox getWeakSoundnessCheckBox(ActionListener listener) {
        weakSoundnessCheckBox = makeCheckBox(
                "Check for weak soundness property using coverability",
                KeyEvent.VK_W, UserSettings.getWeakSoundnessAnalysis(), true, listener);
        return weakSoundnessCheckBox;
    }

    private JCheckBox getSoundnessCheckBox(ActionListener listener) {
        soundnessCheckBox = makeCheckBox(
                "Check for soundness property using reachability results from bounded nets",
                KeyEvent.VK_S, UserSettings.getSoundnessAnalysis(), true, listener);
        return soundnessCheckBox;
    }

    private JCheckBox getCancellationCheckBox(ActionListener listener) {
        cancellationCheckBox = makeCheckBox("Check for unnecessary cancellation regions",
                KeyEvent.VK_C, UserSettings.getCancellationAnalysis(), true, listener);
        return cancellationCheckBox;
    }

    private JCheckBox getOrjoinCheckBox(ActionListener listener) {
        orjoinCheckBox = makeCheckBox("Check for unnecessary or-joins",
                KeyEvent.VK_O, UserSettings.getOrJoinAnalysis(), true, listener);
        return orjoinCheckBox;
    }

    private JCheckBox getOrjoinCycleCheckBox(ActionListener listener) {
        orjoinCycleCheckBox = makeCheckBox("Check for or-joins in a cycle",
                KeyEvent.VK_V, UserSettings.getOrJoinCycleAnalysis(), true, listener);
        return orjoinCycleCheckBox;
    }

    private JCheckBox getShowObservationsCheckBox(ActionListener listener) {
        showObservationsCheckBox = makeCheckBox("Show observations in analysis results",
                KeyEvent.VK_S, UserSettings.getShowObservations(), true, listener);
        return showObservationsCheckBox;
    }

    private JCheckBox getUseYAWLReductionRulesCheckBox(ActionListener listener) {
        useYAWLReductionRulesCheckBox = makeCheckBox(
                "Use YAWL reduction rules before analysis for optimisation",
                KeyEvent.VK_Y, UserSettings.getUseYawlReductionRules(), true, listener);
        return useYAWLReductionRulesCheckBox;
    }

    private JCheckBox makeCheckBox(String caption, int mnemonic, boolean selected,
                                   boolean indented, ActionListener listener) {
        int indent = indented ? 10 : 0;
        JCheckBox checkBox = new JCheckBox(caption);
        checkBox.setBorder(new EmptyBorder(5, indent, 5, 0));
        checkBox.setMnemonic(mnemonic);
        checkBox.setSelected(selected);
        checkBox.setAlignmentX(LEFT_ALIGNMENT);
        checkBox.addActionListener(listener);
        return checkBox;
    }

    private JCheckBox getUseResetReductionRulesCheckBox(ActionListener listener) {
        useResetReductionRulesCheckBox = makeCheckBox(
                "Use Reset reduction rules before analysis for optimisation",
                KeyEvent.VK_R, UserSettings.getUseResetReductionRules(), true, listener);
        return useResetReductionRulesCheckBox;
    }

    private JCheckBox getKeepOpenCheckBox(ActionListener listener) {
        keepOpenCheckBox = makeCheckBox(
                "Keep Analysis progress dialog open when analysis completes",
                KeyEvent.VK_K, UserSettings.getKeepAnalysisDialogOpen(), true, listener);
        return keepOpenCheckBox;
    }


    private JCheckBox getWofYawlAnalysisCheckBox(ActionListener listener) {
        wofYawlAnalysisCheckBox = makeCheckBox("Use the WofYAWL analysis algorithm",
                KeyEvent.VK_U, UserSettings.getWofyawlAnalysis(), false, listener);
        wofYawlAnalysisCheckBox.setBorder(new EmptyBorder(25, 0, 5, 0));
        wofYawlAnalysisCheckBox.addActionListener(
                new ActionListener() {
                    public void actionPerformed(ActionEvent event) {
                        enableWofYAWLCheckBoxes(wofYawlAnalysisCheckBox.isSelected());
                    }
                }
        );
        return wofYawlAnalysisCheckBox;
    }

    private JCheckBox getRelaxedSoundnessCheckBox(ActionListener listener) {
        relaxedSoundnessCheckBox = makeCheckBox(
                "Structural check for relaxed soundness in a bounded analysis net",
                KeyEvent.VK_R, UserSettings.getStructuralAnalysis(), true, listener);
        return relaxedSoundnessCheckBox;
    }

    private JCheckBox getTransitionInvariantCheckBox(ActionListener listener) {
        transitionInvariantCheckBox = makeCheckBox(
                "Behaviourial check for semi-positive transition invariants in a short-circuited net",
                KeyEvent.VK_S, UserSettings.getBehaviouralAnalysis(), true, listener);
        return transitionInvariantCheckBox;
    }

    private JCheckBox getExtendedCoverabilityCheckBox(ActionListener listener) {
        extendedCoverabilityCheckBox = makeCheckBox(
                "Extend coverability graph of an unbounded analysis net (slow)",
                KeyEvent.VK_E, UserSettings.getExtendedCoverability(), true, listener);
        return extendedCoverabilityCheckBox;
    }


    private void enableWofYAWLCheckBoxes(boolean enable) {
        relaxedSoundnessCheckBox.setEnabled(enable);
        transitionInvariantCheckBox.setEnabled(enable);
        extendedCoverabilityCheckBox.setEnabled(enable);
    }
}
