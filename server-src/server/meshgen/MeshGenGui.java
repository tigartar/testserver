/*
 * Decompiled with CFR 0.152.
 */
package com.wurmonline.server.meshgen;

import com.wurmonline.mesh.MeshIO;
import com.wurmonline.server.meshgen.IslandAdder;
import com.wurmonline.server.meshgen.MeshGen;
import com.wurmonline.server.utils.StringUtil;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.awt.image.RenderedImage;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JToggleButton;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.filechooser.FileFilter;
import javax.swing.text.DefaultCaret;

public final class MeshGenGui
extends JFrame
implements ActionListener {
    private static final long serialVersionUID = -1462641916710560981L;
    private static final Logger logger = Logger.getLogger(MeshGenGui.class.getName());
    JLabel imageLabel;
    JButton generateGroundButton;
    JButton normaliseButton;
    JButton flowButton;
    JButton texturizeButton;
    JButton saveButton;
    JButton saveImageButton;
    JButton loadButton;
    JButton addIslandsButton;
    JToggleButton layerToggle;
    long seed = 0L;
    MeshGen meshGen;
    JPanel panel;
    MeshIO topLayerMeshIO;
    MeshIO rockLayerMeshIO;
    boolean loaded = false;
    final String baseDir = "worldmachine" + File.separator + "NewEle2015";
    final String baseFile = File.separator + "output.r32";
    private JProgressBar progressBar;
    private JTextArea taskOutput;
    private Task task;
    private JFrame frame;

    public MeshGenGui() {
        super("Wurm MeshGen GUI");
        if (logger.isLoggable(Level.FINE)) {
            logger.fine("Starting Wurm MeshGen GUI");
        }
        this.panel = new JPanel();
        this.panel.setLayout(new BorderLayout());
        this.imageLabel = new JLabel();
        this.progressBar = new JProgressBar(0, 100);
        this.progressBar.setValue(0);
        this.progressBar.setStringPainted(true);
        this.flowButton = new JButton("Load base map");
        this.flowButton.addActionListener(this);
        this.flowButton.setToolTipText("Load the output.r32 base map file");
        this.texturizeButton = new JButton("Texturize");
        this.texturizeButton.addActionListener(this);
        this.saveButton = new JButton("Save");
        this.saveButton.addActionListener(this);
        this.saveButton.setToolTipText("Save the top_layer.map and rock_layer.map");
        this.loadButton = new JButton("Load");
        this.loadButton.addActionListener(this);
        this.loadButton.setToolTipText("Load the top_layer.map and rock_layer.map");
        this.saveImageButton = new JButton("Save Image");
        this.saveImageButton.addActionListener(this);
        this.saveImageButton.setToolTipText("Save the coloured image of top_layer.map to map.png");
        this.layerToggle = new JToggleButton("Layer", false);
        this.layerToggle.addActionListener(this);
        this.layerToggle.setToolTipText("Selected shows the rock layer, unselected shows the surface layer");
        this.addIslandsButton = new JButton("Add Islands");
        this.addIslandsButton.addActionListener(this);
        this.addIslandsButton.setToolTipText("Add some islands to the top_layer.map and rock_layer.map");
        JPanel buttonPanel = new JPanel();
        buttonPanel.add(this.progressBar);
        buttonPanel.add(this.layerToggle);
        buttonPanel.add(this.flowButton);
        buttonPanel.add(this.texturizeButton);
        buttonPanel.add(this.addIslandsButton);
        buttonPanel.add(this.saveButton);
        buttonPanel.add(this.loadButton);
        buttonPanel.add(this.saveImageButton);
        this.panel.add((Component)new JScrollPane(this.imageLabel), "Center");
        this.panel.add((Component)buttonPanel, "South");
        this.setContentPane(this.panel);
        this.setSize(1200, 800);
        this.setDefaultCloseOperation(3);
        this.enableButtons(false);
        this.frame = new JFrame("Please wait...");
        this.frame.setDefaultCloseOperation(3);
        this.frame.setLayout(new BorderLayout());
        this.taskOutput = new JTextArea(20, 30);
        this.taskOutput.setMargin(new Insets(5, 5, 5, 5));
        this.taskOutput.setEditable(false);
        DefaultCaret caret = (DefaultCaret)this.taskOutput.getCaret();
        caret.setUpdatePolicy(2);
        JScrollPane scroll = new JScrollPane(this.taskOutput, 22, 31);
        JPanel mpanel = new JPanel();
        mpanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        mpanel.add((Component)scroll, "Center");
        this.frame.setContentPane(mpanel);
        this.frame.pack();
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        try {
            Thread.sleep(500L);
        }
        catch (InterruptedException e2) {
            e2.printStackTrace();
        }
        try {
            if (e.getSource() == this.flowButton) {
                if (!this.loaded) {
                    this.loaded = true;
                    try {
                        JFileChooser chooser = new JFileChooser();
                        chooser.setFileFilter(new FileFilter(){

                            @Override
                            public boolean accept(File f) {
                                return f.isFile();
                            }

                            @Override
                            public String getDescription() {
                                return "Files";
                            }
                        });
                        chooser.setCurrentDirectory(new File(this.baseDir));
                        int returnVal = chooser.showOpenDialog(this.panel);
                        File worldMachineOutput = returnVal == 0 ? chooser.getSelectedFile() : new File(this.baseDir + this.baseFile);
                        long baseFileSize = worldMachineOutput.length();
                        double mapDimension = Math.sqrt(baseFileSize) / 2.0;
                        logger.info("Math.sqrt(fis.getChannel().size())/2 " + mapDimension);
                        if (logger.isLoggable(Level.FINE)) {
                            logger.fine("Opening " + worldMachineOutput.getName() + ", length: " + baseFileSize + " Bytes");
                        }
                        this.task = new Task("Loading file."){

                            @Override
                            public void doWork() throws Exception {
                                try {
                                    MeshGenGui.this.imageLabel.setIcon(null);
                                    FileInputStream fis = new FileInputStream(worldMachineOutput);
                                    DataInputStream dis = new DataInputStream(fis);
                                    int size2 = (int)(Math.log(mapDimension) / Math.log(2.0));
                                    int mapPixels = (int)(mapDimension * mapDimension);
                                    byte[] d = new byte[mapPixels * 4];
                                    dis.readFully(d);
                                    dis.close();
                                    ByteBuffer bb = ByteBuffer.wrap(d);
                                    bb.order(ByteOrder.LITTLE_ENDIAN);
                                    FloatBuffer fb = bb.asFloatBuffer();
                                    float[] data = new float[mapPixels];
                                    fb.get(data);
                                    fb.position(0);
                                    fb.limit(data.length);
                                    fb = null;
                                    bb = null;
                                    d = null;
                                    dis = null;
                                    System.gc();
                                    MeshGenGui.this.meshGen = new MeshGen(size2, this);
                                    MeshGenGui.this.meshGen.setData(data, this);
                                    MeshGenGui.this.imageLabel.setIcon(new ImageIcon(MeshGenGui.this.meshGen.getImage(this)));
                                }
                                catch (FileNotFoundException fnfe) {
                                    logger.log(Level.WARNING, "Problem loading Base Map", fnfe);
                                }
                                catch (IOException ioe) {
                                    logger.log(Level.WARNING, "Problem loading Base Map", ioe);
                                }
                            }
                        };
                        logger.log(Level.INFO, "Created task. Now setting prio and starting.");
                        this.task.execute();
                    }
                    catch (Exception ex) {
                        logger.log(Level.WARNING, "Problem loading Base Map", ex);
                    }
                } else {
                    this.task = new Task("Showing Image."){

                        @Override
                        public void doWork() throws Exception {
                            MeshGenGui.this.imageLabel.setIcon(new ImageIcon(MeshGenGui.this.meshGen.getImage(this)));
                        }
                    };
                    this.task.execute();
                }
            } else if (e.getSource() == this.texturizeButton) {
                if (this.meshGen != null) {
                    this.task = new Task("Adding textures."){

                        @Override
                        public void doWork() throws Exception {
                            MeshGenGui.this.imageLabel.setIcon(null);
                            Random random = new Random();
                            MeshGenGui.this.meshGen.generateTextures(random, this);
                            MeshGenGui.this.imageLabel.setIcon(new ImageIcon(MeshGenGui.this.meshGen.getImage(this)));
                        }
                    };
                    this.task.execute();
                }
            } else if (e.getSource() == this.addIslandsButton) {
                if (this.meshGen != null) {
                    if (this.topLayerMeshIO != null) {
                        this.task = new Task("Adding islands."){

                            @Override
                            public void doWork() throws Exception {
                                MeshGenGui.this.imageLabel.setIcon(null);
                                IslandAdder islandAdder = new IslandAdder(MeshGenGui.this.topLayerMeshIO, MeshGenGui.this.rockLayerMeshIO);
                                islandAdder.addIslands(MeshGenGui.this.meshGen.getWidth());
                                MeshGenGui.this.meshGen = new MeshGen(islandAdder.getTopLayer().getSizeLevel(), this);
                                MeshGenGui.this.meshGen.setData(islandAdder.getTopLayer(), islandAdder.getRockLayer(), this);
                                BufferedImage image = MeshGenGui.this.meshGen.getImage(this);
                                MeshGenGui.this.imageLabel.setIcon(new ImageIcon(image));
                                MeshGenGui.this.topLayerMeshIO = islandAdder.getTopLayer();
                                MeshGenGui.this.rockLayerMeshIO = islandAdder.getRockLayer();
                            }
                        };
                        this.task.execute();
                    } else {
                        logger.info("Failed to add Islands. Save map first.");
                    }
                }
            } else if (e.getSource() == this.loadButton) {
                try {
                    String mapDirectory = this.selectMapDir();
                    logger.info("Opening Mesh " + mapDirectory + File.separatorChar + "top_layer.map");
                    MeshIO meshIO = MeshIO.open(mapDirectory + File.separatorChar + "top_layer.map");
                    logger.info("Opening Mesh " + mapDirectory + File.separatorChar + "rock_layer.map");
                    MeshIO meshIO2 = MeshIO.open(mapDirectory + File.separatorChar + "rock_layer.map");
                    if (meshIO.getSize() != meshIO2.getSize()) {
                        logger.warning("top layer and rock layer are not the same size");
                    }
                    this.task = new Task("Loading maps."){

                        @Override
                        public void doWork() throws Exception {
                            MeshGenGui.this.meshGen = new MeshGen(meshIO.getSizeLevel(), this);
                            MeshGenGui.this.meshGen.setData(meshIO, meshIO2, this);
                            BufferedImage image = MeshGenGui.this.meshGen.getImage(this);
                            MeshGenGui.this.imageLabel.setIcon(new ImageIcon(image));
                            MeshGenGui.this.topLayerMeshIO = meshIO;
                            MeshGenGui.this.rockLayerMeshIO = meshIO2;
                        }
                    };
                    this.task.execute();
                }
                catch (IOException ioe) {
                    logger.log(Level.WARNING, "Problem loading Map", ioe);
                }
            } else if (e.getSource() == this.generateGroundButton) {
                if (this.meshGen != null) {
                    this.task = new Task("Generating ground."){

                        @Override
                        public void doWork() throws Exception {
                            MeshGenGui.this.imageLabel.setIcon(null);
                            MeshGenGui.this.meshGen.generateGround(new Random(), this);
                            BufferedImage image = MeshGenGui.this.meshGen.getImage(this);
                            MeshGenGui.this.imageLabel.setIcon(new ImageIcon(image));
                        }
                    };
                    this.task.execute();
                }
            } else if (e.getSource() == this.saveButton) {
                this.task = new Task("Saving maps."){

                    @Override
                    public void doWork() throws Exception {
                        try {
                            MeshGenGui.this.imageLabel.setIcon(null);
                            MeshIO meshIO = MeshIO.createMap("top_layer_out.map", MeshGenGui.this.meshGen.getLevel(), MeshGenGui.this.meshGen.getData(this));
                            meshIO.close();
                            MeshGenGui.this.topLayerMeshIO = meshIO;
                            MeshGenGui.this.task.setNote(49, "Created top_layer_out.map");
                            logger.info("Created top_layer_out.map");
                            meshIO = MeshIO.createMap("rock_layer_out.map", MeshGenGui.this.meshGen.getLevel(), MeshGenGui.this.meshGen.getRockData(this));
                            meshIO.close();
                            MeshGenGui.this.rockLayerMeshIO = meshIO;
                            MeshGenGui.this.task.setNote(99, "Created rock_layer_out.map");
                            logger.info("Created rock_layer_out.map");
                        }
                        catch (IOException e1) {
                            logger.log(Level.WARNING, "problem saving Map", e1);
                        }
                    }
                };
                this.task.execute();
            } else if (e.getSource() == this.saveImageButton) {
                if (this.meshGen != null) {
                    this.task = new Task("Saving png."){

                        @Override
                        public void doWork() throws Exception {
                            try {
                                MeshGenGui.this.imageLabel.setIcon(null);
                                BufferedImage image = MeshGenGui.this.meshGen.getImage(this);
                                MeshGenGui.this.imageLabel.setIcon(new ImageIcon(image));
                                ImageIO.write((RenderedImage)image, "png", new File("map.png"));
                                logger.info("Created map.png");
                            }
                            catch (IOException e1) {
                                logger.log(Level.WARNING, "problem saving image", e1);
                            }
                        }
                    };
                    this.task.execute();
                }
            } else if (e.getSource() == this.layerToggle && this.meshGen != null) {
                this.task = new Task("Toggling layer."){

                    @Override
                    public void doWork() throws Exception {
                        MeshGenGui.this.imageLabel.setIcon(null);
                        MeshGenGui.this.meshGen.setImageLayer(MeshGenGui.this.layerToggle.isSelected() ? 1 : 0);
                        BufferedImage image = MeshGenGui.this.meshGen.getImage(this);
                        MeshGenGui.this.imageLabel.setIcon(new ImageIcon(image));
                    }
                };
                this.task.execute();
            }
        }
        catch (RuntimeException re) {
            logger.log(Level.SEVERE, "Error while handling ActionClass ", re);
            throw re;
        }
    }

    private void enableButtons(boolean running) {
        if (running) {
            this.progressBar.setVisible(true);
            this.layerToggle.setEnabled(false);
            this.flowButton.setEnabled(false);
            this.texturizeButton.setEnabled(false);
            this.addIslandsButton.setEnabled(false);
            this.saveButton.setEnabled(false);
            this.loadButton.setEnabled(false);
            this.saveImageButton.setEnabled(false);
        } else {
            boolean sf = this.meshGen != null;
            this.progressBar.setVisible(false);
            this.layerToggle.setEnabled(sf);
            this.flowButton.setEnabled(true);
            this.texturizeButton.setEnabled(sf);
            this.addIslandsButton.setEnabled(sf);
            this.saveButton.setEnabled(sf);
            this.loadButton.setEnabled(true);
            this.saveImageButton.setEnabled(sf);
        }
    }

    private String selectMapDir() {
        block3: {
            int option;
            File file;
            do {
                JFileChooser chooser = new JFileChooser();
                chooser.setFileFilter(new FileFilter(){

                    @Override
                    public boolean accept(File f) {
                        return f.isDirectory();
                    }

                    @Override
                    public String getDescription() {
                        return "Directories";
                    }
                });
                chooser.setCurrentDirectory(new File("."));
                chooser.setAcceptAllFileFilterUsed(false);
                chooser.setFileFilter(new FileFilter(){

                    @Override
                    public boolean accept(File f) {
                        return f.isFile();
                    }

                    @Override
                    public String getDescription() {
                        return "Files";
                    }
                });
                chooser.setCurrentDirectory(new File("."));
                chooser.setFileSelectionMode(1);
                chooser.setDialogTitle("Select the directory containing the map files");
                chooser.setApproveButtonText("Use this dir");
                chooser.setApproveButtonToolTipText("<html>The selected directory will be used by the Mesh Generator GUI<br> to load the top_layer.map and rock_layer.map files</html");
                int returnVal = chooser.showOpenDialog(this.panel);
                if (returnVal != 0) break block3;
                file = chooser.getSelectedFile();
                if (file.isFile()) {
                    if (logger.isLoggable(Level.FINE)) {
                        logger.fine("Using the directory containing the chosen file: " + file);
                    }
                    file = file.getParentFile();
                }
                if (file.exists()) continue;
                file.mkdir();
            } while (!(file.listFiles().length != 0 ? (option = JOptionPane.showConfirmDialog(this.panel, "<html>Use \"" + file.toString() + "\"?", "Confirm directory", 0)) == 0 : (option = JOptionPane.showConfirmDialog(this.panel, "<html>Use \"" + file.toString() + "\"?<br><br><b>Warning: The directory is empty.</b><br>This should contain the maps", "Confirm directory", 0)) == 0));
            return file.toString();
        }
        return null;
    }

    public static void main(String[] args) {
        new MeshGenGui().setVisible(true);
    }

    static /* synthetic */ Logger access$000() {
        return logger;
    }

    static /* synthetic */ Task access$100(MeshGenGui x0) {
        return x0.task;
    }

    static /* synthetic */ JTextArea access$200(MeshGenGui x0) {
        return x0.taskOutput;
    }

    static /* synthetic */ JFrame access$300(MeshGenGui x0) {
        return x0.frame;
    }

    static /* synthetic */ JProgressBar access$500(MeshGenGui x0) {
        return x0.progressBar;
    }

    static /* synthetic */ void access$600(MeshGenGui x0, boolean x1) {
        x0.enableButtons(x1);
    }

    abstract class Task
    extends SwingWorker<Void, Void> {
        int pmax = 100;

        Task(final String title) {
            new Thread("Wurm MeshGenGui"){

                @Override
                public void run() {
                    MeshGenGui.this.taskOutput.setText(title);
                    MeshGenGui.this.frame.setTitle(title + " Please wait...");
                    Task.this.setProgress(0);
                    MeshGenGui.this.progressBar.setValue(0);
                    MeshGenGui.this.frame.setVisible(true);
                    MeshGenGui.this.enableButtons(true);
                }
            }.start();
        }

        public void setMax(int max) {
            this.pmax = max;
        }

        public void setNote(String text) {
            System.out.println(text);
            if (MeshGenGui.this.taskOutput.getText().length() > 0) {
                MeshGenGui.this.taskOutput.append("\n");
            }
            MeshGenGui.this.taskOutput.append(text);
        }

        public void setNote(int progress, String text) {
            this.setNote(progress);
            this.setNote(text);
        }

        public void setNote(int progress) {
            int p = Math.max(Math.min(progress * 100 / this.pmax, 100), 0);
            MeshGenGui.this.progressBar.setValue(p);
        }

        @Override
        public final Void doInBackground() {
            try {
                this.doWork();
            }
            catch (OutOfMemoryError e) {
                logger.log(Level.SEVERE, "Out of memory (Java heap space)", e);
                SwingUtilities.invokeLater(new Runnable(){

                    @Override
                    public void run() {
                        String msg = StringUtil.format("Unexpected problem: %s", e.toString());
                        JOptionPane.showMessageDialog(MeshGenGui.this.panel, msg, "Unrecoverable error", 0);
                    }
                });
            }
            catch (Exception e) {
                logger.log(Level.SEVERE, "Error while performing Task.doWork ", e);
                SwingUtilities.invokeLater(new Runnable(){

                    @Override
                    public void run() {
                        String msg = StringUtil.format("Unexpected problem: %s", e.toString());
                        JOptionPane.showMessageDialog(MeshGenGui.this.panel, msg, "Unrecoverable error", 0);
                    }
                });
            }
            return null;
        }

        public abstract void doWork() throws Exception;

        @Override
        public void done() {
            this.setProgress(100);
            MeshGenGui.this.progressBar.setValue(100);
            this.setNote("Finished!");
            MeshGenGui.this.frame.setVisible(false);
            MeshGenGui.this.enableButtons(false);
        }
    }
}

