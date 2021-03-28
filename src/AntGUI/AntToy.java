 /*
   Copyright (C) 2021 Thomas DiModica <ricinwich@yahoo.com>

   This program is free software: you can redistribute it and/or modify
   it under the terms of the GNU General Public License as published by
   the Free Software Foundation, either version 3 of the License, or
   (at your option) any later version.

   This program is distributed in the hope that it will be useful,
   but WITHOUT ANY WARRANTY; without even the implied warranty of
   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
   GNU General Public License for more details.

   You should have received a copy of the GNU General Public License
   along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package AntGUI;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.io.File;
import java.util.concurrent.atomic.AtomicLong;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;
import javax.swing.text.DefaultCaret;

import AntWorld.World;
import StateEngine.StdLib.EnterDebugger;
import esl2.parser.ParserLogger;

public final class AntToy
{

    public static void main(String[] args)
    {
        try
        {
            SwingUtilities.invokeAndWait(new Runnable() {
                @Override
                public void run()
                {
                    new AntToy();
                }
            });
        }
        catch (Exception e)
        {
            System.err.println("Could not create GUI: " + e.getLocalizedMessage());
        }
    }

    private JFrame frame;
    private WorldViewer viewer;
    private JTextArea debugConsole;
    private AtomicLong GoMifune;
    private long turns;

    public final class Logger extends ParserLogger
    {
        @Override
        public synchronized void message(String msg)
        {
            debugConsole.append(msg + "\n");
        }
    }

    private Logger logger;

    public AntToy()
    {
        frame = new JFrame("Ant Toy");
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.setLocation(10, 10);
        frame.setSize(1000, 1000);

        JMenuBar menuBar = new JMenuBar();
        JMenu menu = new JMenu("File");
        menuBar.add(menu);
        JMenuItem load = new JMenuItem("Load");
        menu.add(load);
        load.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent arg0)
            {
                JFileChooser filePicker = new JFileChooser();
                if (JFileChooser.APPROVE_OPTION == filePicker.showOpenDialog(frame))
                {
                    File selectedFile = filePicker.getSelectedFile();
                    if (null != selectedFile)
                    {
                        if (false == viewer.loadFile(selectedFile.getAbsolutePath(), logger))
                        {
                            JOptionPane.showMessageDialog(frame, "Load Failed!");
                        }
                        else
                        {
                            frame.setTitle("Ant Toy : Running...");
                            frame.setResizable(false);
                        }
                    }
                }
            }
        });

        JMenuItem settings = new JMenuItem("Settings");
        menu.add(settings);
        final JFrame setWin = buildSettingsWindow();
        settings.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent arg0)
            {
                loadFieldValues();
                setWin.setVisible(true);
            }
        });

        JMenuItem debug = new JMenuItem("Debug");
        menu.add(debug);
        final JFrame console = buildDebugConsole();
        debug.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent arg0)
            {
                console.setVisible(true);
            }
        });

        JMenuItem exit = new JMenuItem("Exit");
        menu.add(exit);
        exit.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent arg0)
            {
                frame.dispatchEvent(new WindowEvent(frame, WindowEvent.WINDOW_CLOSING));
            }
        });

        GoMifune = new AtomicLong(100);
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run()
            {
                while (true)
                {
                    try
                    {
                        long time = GoMifune.getOpaque();
                        if (0 != time)
                        {
                            Thread.sleep(time);
                        }
                    }
                    catch (InterruptedException e)
                    {
                        // I DONT CARE!
                    }
                    World.RESULT result = viewer.update();
                    if (World.RESULT.WIN == result)
                    {
                        turns = viewer.getTurns();
                        SwingUtilities.invokeLater(new Runnable() {
                            @Override
                            public void run()
                            {
                                JOptionPane.showMessageDialog(frame, "You've Won!");
                                // Use logger to synchronize writes to debugConsole.
                                logger.message("You've Won!\nIt took " + turns + " updates.\n");
                                frame.setTitle("Ant Toy : You've Won!");
                            }
                        });
                    }
                    else if (World.RESULT.LOSE == result)
                    {
                        turns = viewer.getTurns();
                        SwingUtilities.invokeLater(new Runnable() {
                            @Override
                            public void run()
                            {
                                JOptionPane.showMessageDialog(frame, "You've lost.");
                                // Use logger to synchronize writes to debugConsole.
                                logger.message("You've lost.\nIt took " + turns + " updates.\n");
                                frame.setTitle("Ant Toy : You've lost.");
                            }
                        });
                    }
                    else if (World.RESULT.BROKEN == result)
                    {
                        SwingUtilities.invokeLater(new Runnable() {
                            @Override
                            public void run()
                            {
                                JOptionPane.showMessageDialog(frame, "You script crashed. See debug screen for details.");
                                frame.setTitle("Ant Toy : Your script crashed. See debug screen for details.");
                            }
                        });
                    }
                    else
                    {
                        SwingUtilities.invokeLater(new Runnable() {
                            @Override
                            public void run()
                            {
                                viewer.repaint();
                            }
                        });
                    }
                }
            }
        });

        JMenu speed = new JMenu("Speed");
        menuBar.add(speed);
        JMenuItem speedItem = new JMenuItem("Fastest");
        speed.add(speedItem);
        speedItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent arg0)
            {
                GoMifune.setOpaque(0);
                thread.interrupt();
            }
        });
        speedItem = new JMenuItem("Fast");
        speed.add(speedItem);
        speedItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent arg0)
            {
                GoMifune.setOpaque(10);
                thread.interrupt();
            }
        });
        speedItem = new JMenuItem("Medium");
        speed.add(speedItem);
        speedItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent arg0)
            {
                GoMifune.setOpaque(100);
                thread.interrupt();
            }
        });
        speedItem = new JMenuItem("Slow");
        speed.add(speedItem);
        speedItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent arg0)
            {
                GoMifune.setOpaque(1000);
                thread.interrupt();
            }
        });
        speedItem = new JMenuItem("Almost Paused");
        speed.add(speedItem);
        speedItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent arg0)
            {
                GoMifune.setOpaque(2592000000L); // One Month
                thread.interrupt();
            }
        });

        final JPanel OverPanel = new JPanel();
        frame.add(OverPanel);
        OverPanel.setLayout(new BorderLayout());

        viewer = new WorldViewer();
        OverPanel.add(viewer, BorderLayout.CENTER);

        frame.setJMenuBar(menuBar);
        frame.validate();
        frame.setVisible(true);

        thread.setDaemon(true);
        thread.start();
    }

    private JFrame buildDebugConsole()
    {
        JFrame console = new JFrame();
        console.setDefaultCloseOperation(WindowConstants.HIDE_ON_CLOSE);
        console.setLocation(100, 100);
        console.setSize(640, 480);
        JPanel cpanel = new JPanel();
        cpanel.setLayout(new BorderLayout());
        debugConsole = new JTextArea(10, 10);
        debugConsole.setEditable(false);
        DefaultCaret caret = (DefaultCaret)debugConsole.getCaret();
        caret.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);
        logger = new Logger();
        cpanel.add(new JScrollPane(debugConsole), BorderLayout.CENTER);
        JPanel ipanel = new JPanel();
        ipanel.setLayout(new BorderLayout());
        JTextField input = new JTextField(100);
        ipanel.add(input, BorderLayout.CENTER);
        JButton send = new JButton("Send Command");
        ActionListener listen = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                try
                {
                    EnterDebugger.queue.put(input.getText());
                    logger.message(input.getText());
                    input.setText("");
                }
                catch (InterruptedException e1)
                {
                    logger.message("ERROR : FAILED TO SEND COMMAND TO DEBUGGER!!!!");
                }
            }
        };
        send.addActionListener(listen);
        input.addActionListener(listen);
        ipanel.add(send, BorderLayout.EAST);
        cpanel.add(ipanel, BorderLayout.SOUTH);
        console.add(cpanel);
        console.validate();
        return console;
    }

    private JTextField seedField;
    private JTextField heightField;
    private JTextField widthField;
    private JTextField moveField;
    private JTextField viewField;
    private JTextField denseField;
    private JTextField scaleField;
    private JFrame buildSettingsWindow()
    {
        JFrame sFrame = new JFrame();
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new GridLayout(0, 2));
        sFrame.add(mainPanel);
        mainPanel.add(new JLabel("Random Seed"));
        seedField = new JTextField();
        mainPanel.add(seedField);
        mainPanel.add(new JLabel("Universe Height"));
        heightField = new JTextField();
        mainPanel.add(heightField);
        mainPanel.add(new JLabel("Universe Width"));
        widthField = new JTextField();
        mainPanel.add(widthField);
        mainPanel.add(new JLabel("BLUE move count"));
        moveField = new JTextField();
        mainPanel.add(moveField);
        mainPanel.add(new JLabel("Visible Distance"));
        viewField = new JTextField();
        mainPanel.add(viewField);
        mainPanel.add(new JLabel("Resource Probability"));
        denseField = new JTextField();
        mainPanel.add(denseField);
        mainPanel.add(new JLabel("Cell scaling"));
        scaleField = new JTextField();
        mainPanel.add(scaleField);
        JButton apply = new JButton("Apply");
        apply.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                if (false == frame.isResizable())
                {
                    JOptionPane.showMessageDialog(frame, "You can't change these values while running.");
                    return;
                }
                int seed;
                int height;
                int width;
                int move;
                int look;
                double dense;
                int scale;
                try
                {
                    seed = Integer.parseInt(seedField.getText());
                    height = Integer.parseInt(heightField.getText());
                    width = Integer.parseInt(widthField.getText());
                    move = Integer.parseInt(moveField.getText());
                    look = Integer.parseInt(viewField.getText());
                    dense = Double.parseDouble(denseField.getText());
                    scale = Integer.parseInt(scaleField.getText());
                }
                catch(NumberFormatException ex)
                {
                    JOptionPane.showMessageDialog(frame, "Failed to parse number: " + ex.getLocalizedMessage());
                    return;
                }
                viewer.seed = seed;
                int ypad = frame.getHeight() - viewer.getHeight();
                int xpad = frame.getWidth() - viewer.getWidth();
                frame.setSize((width + 4) * scale + xpad, (height + 4) * scale + ypad);
                viewer.energy = move;
                viewer.look = look;
                viewer.density = dense;
                viewer.scale = scale;
                viewer.reset();
            }
        });
        mainPanel.add(apply);
        JButton reset = new JButton("Reset");
        reset.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                loadFieldValues();
            }
        });
        mainPanel.add(reset);
        sFrame.pack();
        sFrame.validate();
        return sFrame;
    }

    private void loadFieldValues()
    {
        seedField.setText(Integer.toString(viewer.seed));
        heightField.setText(Integer.toString(viewer.getHeight() / viewer.scale - 4));
        widthField.setText(Integer.toString(viewer.getWidth() / viewer.scale - 4));
        moveField.setText(Integer.toString(viewer.energy));
        viewField.setText(Integer.toString(viewer.look));
        denseField.setText(Double.toString(viewer.density));
        scaleField.setText(Integer.toString(viewer.scale));
    }

}
