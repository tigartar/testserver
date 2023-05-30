package com.wurmonline.server.tools;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.TextArea;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JTextField;
import javax.swing.JToggleButton;

public class ItemModeller extends JFrame implements KeyListener, WindowListener {
   private static final long serialVersionUID = 1008389608509176516L;
   private JPanel southHackPanel = new JPanel();
   private JLabel ipAdressLabel = new JLabel();
   private JProgressBar hackProgressBar = new JProgressBar();
   private FlowLayout flowLayout1 = new FlowLayout();
   private JPanel textAreaPanel = new JPanel();
   private JTextField ipAdressTextField = new JTextField();
   private BorderLayout borderLayout1 = new BorderLayout();
   private JComboBox<?> portComboBox = new JComboBox();
   private JToggleButton hackButton = new JToggleButton();
   private JComboBox<?> hackComboBox = new JComboBox();
   private JTextField inputTextField = new JTextField();
   private JPanel ipAdressPanel = new JPanel();
   private JButton pingButton = new JButton();
   private JButton scanButton = new JButton();
   private TextArea messageTextArea = new TextArea();
   private TextArea codeTextArea = new TextArea();

   public ItemModeller() {
      super("Wurm Item Modeller");
      this.addMessage("Welcome to wurm item modeller.");

      try {
         this.jbInit();
         this.setBounds(0, 0, 1000, 700);
         this.setVisible(true);
      } catch (Exception var2) {
         var2.printStackTrace();
      }

      this.addWindowListener(this);
   }

   private void jbInit() throws Exception {
      this.ipAdressLabel.setText("Create new:");
      this.ipAdressLabel.setVerticalAlignment(1);
      this.ipAdressLabel.setVerticalTextPosition(1);
      this.southHackPanel.setLayout(this.flowLayout1);
      this.ipAdressTextField.setMinimumSize(new Dimension(70, 21));
      this.ipAdressTextField.setPreferredSize(new Dimension(170, 21));
      this.ipAdressTextField.setText("");
      this.ipAdressTextField.addActionListener(new ActionListener() {
         @Override
         public void actionPerformed(ActionEvent e) {
            ItemModeller.this.ipAdressTextField_actionPerformed(e);
         }
      });
      this.textAreaPanel.setLayout(this.borderLayout1);
      this.hackButton.setText("Load");
      this.hackButton.addActionListener(new ActionListener() {
         @Override
         public void actionPerformed(ActionEvent e) {
            ItemModeller.this.hackButton_actionPerformed(e);
         }
      });
      this.hackProgressBar.setMaximum(100);
      this.hackProgressBar.setMinimum(0);
      this.textAreaPanel.setMinimumSize(new Dimension(200, 500));
      this.textAreaPanel.setPreferredSize(new Dimension(250, 500));
      this.textAreaPanel.setToolTipText("");
      this.inputTextField.setText("");
      this.inputTextField.setHorizontalAlignment(0);
      this.inputTextField.addActionListener(new ActionListener() {
         @Override
         public void actionPerformed(ActionEvent e) {
            ItemModeller.this.inputTextField_actionPerformed(e);
         }
      });
      this.pingButton.setText("Save");
      this.pingButton.addActionListener(new ActionListener() {
         @Override
         public void actionPerformed(ActionEvent e) {
            ItemModeller.this.pingButton_actionPerformed(e);
         }
      });
      this.scanButton.setToolTipText("");
      this.scanButton.setText("Load");
      this.scanButton.addActionListener(new ActionListener() {
         @Override
         public void actionPerformed(ActionEvent e) {
            ItemModeller.this.scanButton_actionPerformed(e);
         }
      });
      this.hackComboBox.addActionListener(new ActionListener() {
         @Override
         public void actionPerformed(ActionEvent e) {
            ItemModeller.this.hackComboBox_actionPerformed(e);
         }
      });
      this.portComboBox.addActionListener(new ActionListener() {
         @Override
         public void actionPerformed(ActionEvent e) {
            ItemModeller.this.portComboBox_actionPerformed(e);
         }
      });
      this.textAreaPanel.add(this.inputTextField, "South");
      this.textAreaPanel.add(this.messageTextArea, "Center");
      this.getContentPane().add(this.ipAdressPanel, "North");
      this.ipAdressPanel.add(this.ipAdressLabel, null);
      this.ipAdressPanel.add(this.ipAdressTextField, null);
      this.ipAdressPanel.add(this.pingButton, null);
      this.ipAdressPanel.add(this.scanButton, null);
      this.getContentPane().add(this.southHackPanel, "South");
      String[] data = new String[]{""};
      this.portComboBox = new JComboBox<>(data);
      this.southHackPanel.add(this.hackComboBox, null);
      this.southHackPanel.add(this.portComboBox, null);
      this.southHackPanel.add(this.hackButton, null);
      this.southHackPanel.add(this.hackProgressBar, null);
      this.getContentPane().add(this.codeTextArea, "Center");
      this.getContentPane().add(this.textAreaPanel, "East");
      this.addMessage("Read all about it here.");
   }

   void hackButton_actionPerformed(ActionEvent e) {
   }

   void inputTextField_actionPerformed(ActionEvent e) {
      this.inputTextField.setText("");
   }

   void ipAdressTextField_actionPerformed(ActionEvent e) {
   }

   void pingButton_actionPerformed(ActionEvent e) {
      this.ipAdressTextField.setBackground(Color.white);
   }

   void scanButton_actionPerformed(ActionEvent e) {
   }

   void remoteFileSystem_actionPerformed(ActionEvent e) {
   }

   void localFileSystem_actionPerformed(ActionEvent e) {
      this.addMessage("Doing something with the local window.");
   }

   void hackComboBox_actionPerformed(ActionEvent e) {
   }

   void portComboBox_actionPerformed(ActionEvent e) {
   }

   @Override
   public void windowDeactivated(WindowEvent e) {
   }

   @Override
   public void windowActivated(WindowEvent e) {
   }

   @Override
   public void windowDeiconified(WindowEvent e) {
   }

   @Override
   public void windowIconified(WindowEvent e) {
   }

   @Override
   public void windowClosed(WindowEvent e) {
   }

   @Override
   public void windowClosing(WindowEvent e) {
      this.shutDown();
   }

   @Override
   public void windowOpened(WindowEvent e) {
   }

   public void addMessage(String message) {
      if (message.endsWith("\n")) {
         this.messageTextArea.append(message);
      } else {
         this.messageTextArea.append(message + "\n");
      }
   }

   private void shutDown() {
      System.exit(0);
   }

   @Override
   public void keyReleased(KeyEvent e) {
   }

   @Override
   public void keyTyped(KeyEvent e) {
   }

   @Override
   public synchronized void keyPressed(KeyEvent e) {
      if (e.getKeyCode() == 27) {
         this.shutDown();
      }
   }

   public static void main(String[] args) {
   }
}
