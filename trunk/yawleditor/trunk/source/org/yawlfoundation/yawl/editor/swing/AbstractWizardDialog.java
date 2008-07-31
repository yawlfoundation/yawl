package org.yawlfoundation.yawl.editor.swing;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.EtchedBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.util.LinkedList;

public abstract class AbstractWizardDialog extends JDialog {
  
  private JLabel titleLabel = buildTitleLabel();

  private JButton backButton = buildBackButton();
  private JButton nextButton = buildNextButton();
  private JButton finishButton = buildFinishButton();

  private int currentStep = 0;
  
  private JPanel housedContentPanel;
  
  protected AbstractWizardPanel[] panels;
  
  public AbstractWizardDialog() {
    super();
    initialise();
    buildInterface();
    makeLastAdjustments(); 
  }
  
  private void buildInterface() {
    getContentPane().add(
        bindContentAndButtons(panels[currentStep]), 
        BorderLayout.CENTER
    );
  }
  
  private JPanel bindContentAndButtons(JPanel contentPanel) {
    JPanel panel = new JPanel(new BorderLayout());
  
    panel.add(buildTitlePanel(), BorderLayout.NORTH);
    panel.add(buildHousedContentPanel(), BorderLayout.CENTER);
    panel.add(buildButtonPanel(), BorderLayout.SOUTH);
  
    return panel;
  }
  
  private JPanel buildHousedContentPanel() {
    housedContentPanel = new JPanel(new BorderLayout());

    housedContentPanel.setBorder(
        new CompoundBorder(
            new EmptyBorder(0,10,5,10),
            new EtchedBorder()
        )
    );
    
    housedContentPanel.add(panels[currentStep], BorderLayout.CENTER);
    updateButtonState();
    
    return housedContentPanel;
  }
  
  private JPanel buildTitlePanel() {
    JPanel titlePanel = new JPanel(new BorderLayout());
    titlePanel.setBorder(new EmptyBorder(10,10,10,10));
    
    JPanel innerPanel = new JPanel(new BorderLayout());
    innerPanel.setBackground(Color.GRAY.darker());
    
    innerPanel.add(titleLabel, BorderLayout.WEST);
    innerPanel.add(Box.createHorizontalGlue(), BorderLayout.CENTER);
    
    titlePanel.add(innerPanel, BorderLayout.CENTER);
    
    return titlePanel;
  }
  
  private JLabel buildTitleLabel() {
    JLabel label = new JLabel("A funky Title");
    label.setHorizontalAlignment(
      SwingConstants.LEFT    
    );
    label.setBackground(Color.GRAY.darker());

    label.setBorder(new EmptyBorder(10,10,10,10));

    Font headerFont = new Font(
      getFont().getName(),
      getFont().getStyle(),
      (int) (getFont().getSize() * 1.5)
    );
    
    label.setFont(headerFont);
    
    label.setForeground(Color.WHITE);
    
    return label;
  }
  
  protected void setPanelTitle(String title) {
    titleLabel.setText("Step " + (currentStep+1) + " : " + title);
  }
  
  private JPanel buildButtonPanel() {
    JPanel panel = new JPanel();
    panel.setBorder(new EmptyBorder(17,12,11,11));
    panel.setLayout(new BoxLayout(panel, BoxLayout.LINE_AXIS));
  
    panel.add(Box.createHorizontalGlue());
    panel.add(backButton); 

    panel.add(Box.createHorizontalStrut(10));
    panel.add(nextButton); 
    
    panel.add(Box.createHorizontalStrut(30));
    panel.add(finishButton); 
    
    LinkedList<JButton> buttonList = new LinkedList<JButton>();

    buttonList.add(backButton);
    buttonList.add(nextButton);
    buttonList.add(finishButton);
      
    JUtilities.equalizeComponentSizes(buttonList);

    panel.add(Box.createHorizontalStrut(10));

    return panel;
  }
  
  private JButton buildBackButton() {
    JButton button = new JButton("< Back");
    button.setMnemonic(KeyEvent.VK_B);
    button.setMargin(new Insets(2,11,3,12));
    button.addActionListener(new ActionListener(){
        public void actionPerformed(ActionEvent e) {
          doBack();
        }
      }
    );
    return button; 
  }

  private JButton buildNextButton() {
    JButton button = new JButton("> Next");
    button.setMnemonic(KeyEvent.VK_N);
    button.setMargin(new Insets(2,11,3,12));
    button.addActionListener(new ActionListener(){
        public void actionPerformed(ActionEvent e) {
          doNext();
        }
      }
    );
    return button; 
  }

  private JButton buildFinishButton() {
    JButton button = new JButton("Finish");
    button.setMnemonic(KeyEvent.VK_F);
    button.setMargin(new Insets(2,11,3,12));
    final JDialog dialog = this;
    button.addActionListener(new ActionListener(){
        public void actionPerformed(ActionEvent e) {
          doFinish();
          dialog.setVisible(false);
        }
      }
    );
    return button; 
  }
  
  public void doNext() {
    if (getCurrentPanel().doNext()) {
      currentStep++;
      while(currentStep < (panels.length - 1) && !shouldDoCurrentStep()) {
        currentStep++;
      }
      moveToCurrentStep();
    }    
  }
  
  
  public void doBack() {
    getCurrentPanel().doBack();
    currentStep--;
    while(currentStep > 0 && !shouldDoCurrentStep()) {
      currentStep--;
    }
    moveToCurrentStep();
  }

  public void doFirst() {
    currentStep = 0;
    moveToCurrentStep();
  }
  
  public void doStep(int step) {
    currentStep = step;
    moveToCurrentStep();
  }
  
  public void doLast() {
    currentStep = panels.length - 1;
    moveToCurrentStep();
  }

  public void setCurrentPanel(AbstractWizardPanel panel) {
    housedContentPanel.removeAll();
    housedContentPanel.add(panels[currentStep]);
    repaint();
  }
  
  public AbstractWizardPanel getCurrentPanel() {
    return panels[currentStep];
  }
  
  public boolean shouldDoCurrentStep() {
    return getCurrentPanel().shouldDoThisStep();
  }
  
  private void moveToCurrentStep() {
    setPanelTitle(panels[currentStep].getWizardStepTitle());
    setCurrentPanel(panels[currentStep]);
    updateButtonState();
  }
  
  private void updateButtonState() {
    backButton.setEnabled(true);
    nextButton.setEnabled(true);
    
    if (currentStep <= 0) {
      backButton.setEnabled(false);
    } else {
      backButton.setEnabled(true);
    } 
    if (currentStep >= panels.length - 1) {
      nextButton.setEnabled(false);
    } else {
      nextButton.setEnabled(true);
    }  
  }
  
  public JButton getBackButton() {
    return this.backButton;
  }
  
  public JButton getNextButton() {
    return this.nextButton;
  }

  public JButton getFinishButton() {
    return this.finishButton;
  }
  
  protected void setPanels(AbstractWizardPanel[] panels) {
    this.panels = panels;
    this.setPanelTitle(panels[0].getWizardStepTitle());
  }
  
  protected AbstractWizardPanel[] getPanels() {
    return panels;
  }
  
  protected abstract void initialise();
  protected abstract void makeLastAdjustments();
  public abstract void doFinish();
}
