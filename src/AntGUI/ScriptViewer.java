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
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.util.Map;

import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultStyledDocument;
import javax.swing.text.DocumentFilter;

import AntUtil.JSONIO;
import AntUtil.PreEnvironment;
import AntUtil.PreState;
import StateEngine.Environment;
import esl2.input.Lexeme;
import esl2.input.Lexer;
import esl2.input.StringInput;
import esl2.types.FatalException;

public class ScriptViewer extends JFrame
{

    private String thisFile;
    // This has to manipulate the JSON representation-ish,
    // because it will handle the function blocks as text.
    private PreEnvironment env;
    private AntToy parent;
    boolean dirty;

    private static final String GLOBALS = "(Global Functions)";

    public ScriptViewer(AntToy par)
    {
        thisFile = null;
        parent = par;
        env = new PreEnvironment(); // Never allow this pointer to be null, it simplified things.
        dirty = false;

        par.frame.setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        par.frame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e)
            {
                if (false == callSaveBeforeActing(par.frame, "Would you like to save your changes to the script before exiting?"))
                {
                    return;
                }

                par.frame.dispose();
                System.exit(0); // How barbaric! In my testing, it's necessary to ensure the program exits.
            }
        });

        JMenuBar menuBar = new JMenuBar();
        JMenu menu = new JMenu("File");
        menuBar.add(menu);
        JMenuItem newm = new JMenuItem("New");
        menu.add(newm);
        newm.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent arg0)
            {
                if (false == callSaveBeforeActing(ScriptViewer.this, "Would you like to save your changes to the script before creating a new one?"))
                {
                    return;
                }

                env = new PreEnvironment();
                dirty = false;
                onLoad();
            }
        });

        JMenuItem open = new JMenuItem("Open...");
        menu.add(open);
        open.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent arg0)
            {
                if (false == callSaveBeforeActing(ScriptViewer.this, "Would you like to save your changes to the script before loading a new one?"))
                {
                    return;
                }

                JFileChooser filePicker = new JFileChooser();
                if (null != par.lastDirectory)
                {
                    filePicker.setCurrentDirectory(par.lastDirectory);
                }
                if (JFileChooser.APPROVE_OPTION == filePicker.showOpenDialog(ScriptViewer.this))
                {
                    File selectedFile = filePicker.getSelectedFile();
                    if (null != selectedFile)
                    {
                        par.lastDirectory = filePicker.getCurrentDirectory();
                        thisFile = selectedFile.getAbsolutePath();
                        try
                        {
                            env = JSONIO.initialize(JSONIO.readFile(thisFile, parent.logger), parent.logger);
                            dirty = false;
                            onLoad();
                        }
                        catch (FatalException e)
                        {
                            parent.logger.message(e.getLocalizedMessage());
                            JOptionPane.showMessageDialog(ScriptViewer.this, "Load Failed!");
                        }
                    }
                }
            }
        });

        JMenuItem save = new JMenuItem("Save");
        menu.add(save);
        save.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent arg0)
            {
                doSave();
            }
        });

        JMenuItem saveas = new JMenuItem("Save As...");
        menu.add(saveas);
        saveas.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent arg0)
            {
                doSaveAs();
            }
        });

        JMenuItem run = new JMenuItem("Run");
        menu.add(run);
        run.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent arg0)
            {
                try
                {
                    Environment environ = JSONIO.transform(env, parent.logger);
                    if (false == parent.viewer.loadData(environ, parent.logger))
                    {
                        JOptionPane.showMessageDialog(ScriptViewer.this, "Cannot run from this script!");
                    }
                    else
                    {
                        parent.frame.setTitle("Ant Toy : Running...");
                        parent.frame.setResizable(false);
                    }
                }
                catch (FatalException e)
                {
                    parent.logger.message(e.getLocalizedMessage());
                    JOptionPane.showMessageDialog(ScriptViewer.this, "Cannot run from this script!");
                }
            }
        });

        setJMenuBar(menuBar);

        buildWindow();

        pack();
        validate();
    }

    private boolean doSaveAs()
    {
        boolean success = false;
        JFileChooser filePicker = new JFileChooser();
        if (null != parent.lastDirectory)
        {
            filePicker.setCurrentDirectory(parent.lastDirectory);
        }
        if (JFileChooser.APPROVE_OPTION == filePicker.showSaveDialog(this))
        {
            File selectedFile = filePicker.getSelectedFile();
            if (null != selectedFile)
            {
                parent.lastDirectory = filePicker.getCurrentDirectory();
                thisFile = selectedFile.getAbsolutePath();
                if (false == JSONIO.writeFile(env, thisFile))
                {
                    JOptionPane.showMessageDialog(ScriptViewer.this, "Save Failed!");
                }
                else
                {
                    dirty = false;
                    success = true;
                }
            }
        }
        return success;
    }

    private boolean doSave()
    {
        boolean success = false;
        if (null == thisFile)
        {
            success = doSaveAs();
        }
        else
        {
            if (false == JSONIO.writeFile(env, thisFile))
            {
                JOptionPane.showMessageDialog(this, "Save Failed!");
            }
            else
            {
                dirty = false;
                success = true;
            }
        }
        return success;
    }

    private DefaultListModel<String> statesList;
    private JTextArea functions;
    private JCheckBox isStart;
    private DefaultListModel<String> dataList;
    private JButton dataRemove;
    private JButton dataAdd;
    private void buildWindow()
    {
        JPanel overpanel = new JPanel();
        overpanel.setLayout(new BorderLayout());
        add(overpanel);

        JPanel left = new JPanel();
        left.setLayout(new BorderLayout());
        overpanel.add(left, BorderLayout.WEST);
        JList<String> states = new JList<String>();
        states.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        states.setEnabled(true);
        states.setPrototypeCellValue("thisisaverylongstatename");
        statesList = new DefaultListModel<String>();
        statesList.addElement(GLOBALS);
        states.setModel(statesList);
        JScrollPane statesScroll = new JScrollPane(states);
        left.add(statesScroll, BorderLayout.CENTER);
        JPanel leftbottom = new JPanel();
        leftbottom.setLayout(new BorderLayout());
        left.add(leftbottom, BorderLayout.SOUTH);
        JButton stateRemove = new JButton("Remove");
        leftbottom.add(stateRemove, BorderLayout.WEST);
        JButton stateAdd = new JButton("Add");
        leftbottom.add(stateAdd, BorderLayout.EAST);
        JTextField stateName = new JTextField("State name to Add");
        leftbottom.add(stateName, BorderLayout.SOUTH);

        functions = new JTextArea();
        functions.setEnabled(false);
        JScrollPane functionsScroll = new JScrollPane(functions);
        functionsScroll.setPreferredSize(new Dimension(600, 600));
        overpanel.add(functionsScroll, BorderLayout.CENTER);

        JPanel right = new JPanel();
        right.setLayout(new BorderLayout());
        overpanel.add(right, BorderLayout.EAST);
        JList<String> data = new JList<String>();
        isStart = new JCheckBox("Initial State");
        isStart.setEnabled(false);
        right.add(isStart, BorderLayout.NORTH);
        data.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        data.setEnabled(true);
        data.setPrototypeCellValue("thisisaverylongvariablename");
        dataList = new DefaultListModel<String>();
        data.setModel(dataList);
        JScrollPane dataScroll = new JScrollPane(data);
        right.add(dataScroll, BorderLayout.CENTER);
        JPanel rightbottom = new JPanel();
        rightbottom.setLayout(new BorderLayout());
        right.add(rightbottom, BorderLayout.SOUTH);
        dataRemove = new JButton("Remove");
        dataRemove.setEnabled(false);
        rightbottom.add(dataRemove, BorderLayout.WEST);
        dataAdd = new JButton("Add");
        dataAdd.setEnabled(false);
        rightbottom.add(dataAdd, BorderLayout.EAST);
        JTextField dataName = new JTextField("Data name to Add");
        rightbottom.add(dataName, BorderLayout.SOUTH);


        states.addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e)
            {
                if (true == e.getValueIsAdjusting())
                {
                    return;
                }
                if (null != states.getSelectedValue())
                {
                    updateData(states.getSelectedValue());
                }
                else
                {
                    clearData();
                }
            }
        });
        stateRemove.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                if (null != states.getSelectedValue())
                {
                    String toRemove = states.getSelectedValue();
                    if (GLOBALS.equals(toRemove))
                    {
                        // Ignore this request.
                        return;
                    }
                    if (JOptionPane.YES_OPTION ==
                        JOptionPane.showOptionDialog(ScriptViewer.this, "Are you sure you want to remove state >" + toRemove + "<.", "Are you sure?",
                            JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE, null, null, null))
                    {
                        dirty = true;
                        statesList.removeElement(toRemove);
                        env.states.remove(toRemove);
                    }
                }
            }
        });
        stateAdd.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                String toAdd = stateName.getText();
                if (true == "".equals(toAdd))
                {
                    // Assume that they didn't try to add an empty name.
                    return;
                }
                if (true == statesList.contains(toAdd))
                {
                    JOptionPane.showMessageDialog(ScriptViewer.this, "There is already a state called >" + toAdd + "<.");
                    return;
                }
                dirty = true;
                statesList.addElement(toAdd);
                env.states.put(toAdd, new PreState());
            }
        });

        DefaultStyledDocument doc = new DefaultStyledDocument();
        doc.setDocumentFilter(new DocumentFilter() {
            @Override
            public void insertString(FilterBypass fb, int offset, String string, AttributeSet attr) throws BadLocationException
            {
                String newString = string.replace("\"", "'");
                super.insertString(fb, offset, newString, attr);
                updateFunctions(states.getSelectedValue(), fb.getDocument().getText(0, fb.getDocument().getLength()));
            }
            @Override
            public void replace(FilterBypass fb, int offset, int length, String text, AttributeSet attr) throws BadLocationException
            {
                if (null != text)
                {
                    String newText = text.replace("\"", "'");
                    super.replace(fb, offset, length, newText, attr);
                }
                else
                {
                    super.replace(fb, offset, length, text, attr);
                }
                updateFunctions(states.getSelectedValue(), fb.getDocument().getText(0, fb.getDocument().getLength()));
            }
            @Override
            public void remove(FilterBypass fb, int offset, int length) throws BadLocationException
            {
                super.remove(fb, offset, length);
                updateFunctions(states.getSelectedValue(), fb.getDocument().getText(0, fb.getDocument().getLength()));
            }
        });
        functions.setDocument(doc);

        isStart.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                if (null != states.getSelectedValue())
                {
                    dirty = true;
                    PreState myState = env.states.get(states.getSelectedValue());
                    if (true == isStart.isSelected())
                    {
                        for (Map.Entry<String, PreState> state : env.states.entrySet())
                        {
                            state.getValue().isInitialState = false;
                        }
                        myState.isInitialState = true;
                    }
                    else
                    {
                        myState.isInitialState = false;
                    }
                }
            }
        });
        dataRemove.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                if ((null != states.getSelectedValue()) && (null != data.getSelectedValue()))
                {
                    String from = states.getSelectedValue();
                    String toRemove = data.getSelectedValue();
                    dirty = true;
                    dataList.removeElement(toRemove);
                    env.states.get(from).data.remove(toRemove);
                }
            }
        });
        dataAdd.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                if (null != states.getSelectedValue())
                {
                    String toAdd = dataName.getText();
                    if (true == "".equals(toAdd))
                    {
                        // Assume that they didn't try to add an empty name.
                        return;
                    }
                    if (true == dataList.contains(toAdd))
                    {
                        JOptionPane.showMessageDialog(ScriptViewer.this, "There is already a data variable called " + toAdd + ".");
                        return;
                    }
                    {
                        try
                        {
                            StringInput varName = new StringInput(toAdd);
                            Lexer lexer = new Lexer(varName, "Input Field", 1, 1);
                            if ((Lexeme.IDENTIFIER != lexer.getNextToken().tokenType) ||
                                (Lexeme.END_OF_FILE != lexer.peekNextToken().tokenType))
                            {
                                JOptionPane.showMessageDialog(ScriptViewer.this, "The name >" + toAdd + "< is not valid for a data variable.");
                                return;
                            }
                        }
                        catch (FatalException ex)
                        {
                            JOptionPane.showMessageDialog(ScriptViewer.this, "Unexpected exception: " + ex.getLocalizedMessage());
                            return;
                        }
                    }
                    dirty = true;
                    dataList.addElement(toAdd);
                    env.states.get(states.getSelectedValue()).data.add(toAdd);
                }
            }
        });
    }

    /**
     * The function to call before doing any activity which drastically alters the data at hand.
     * @param parentFrame The component to associate the message box to as its parent.
     * @param question What to ask the user.
     * @return If it is safe to continue the action. If this function returns false, abort what you are doing.
     */
    private boolean callSaveBeforeActing(JFrame parentFrame, String question)
    {
        boolean continueAction = false;
        if (true == dirty)
        {
            switch (JOptionPane.showOptionDialog(parentFrame, question, "Save?",
                JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE, null, null, null))
            {
            case JOptionPane.YES_OPTION:
                // If we fail to save, treat it as cancel.
                if (true == doSave())
                {
                    continueAction = true;
                }
                break;
            case JOptionPane.NO_OPTION:
                continueAction = true;
                break;
            case JOptionPane.CANCEL_OPTION:
            default: // As far as I know, default is an error condition.
                break;
            }
        }
        else
        {
            continueAction = true;
        }
        return continueAction;
    }

    private void onLoad()
    {
        resetStatesList();
        for (String state : env.states.keySet())
        {
            statesList.addElement(state);
        }
    }

    private void resetStatesList()
    {
        statesList.clear();
        statesList.addElement(GLOBALS);
    }

    private void updateData(String stateName)
    {
        functions.setEnabled(true);
        if (true == GLOBALS.equals(stateName))
        {
            functions.setText(env.functions.getValue());
            isStart.setEnabled(false);
            isStart.setSelected(false);
            dataList.clear();
            dataRemove.setEnabled(false);
            dataAdd.setEnabled(false);
        }
        else
        {
            functions.setText(env.states.get(stateName).functions.getValue());
            isStart.setEnabled(true);
            isStart.setSelected(env.states.get(stateName).isInitialState);
            dataList.clear();
            for (String datum : env.states.get(stateName).data)
            {
                dataList.addElement(datum);
            }
            dataRemove.setEnabled(true);
            dataAdd.setEnabled(true);
        }
    }

    private void clearData()
    {
        functions.setEnabled(false);
        functions.setText("");
        isStart.setEnabled(false);
        isStart.setSelected(false);
        dataList.clear();
        dataRemove.setEnabled(false);
        dataAdd.setEnabled(false);
    }

    private void updateFunctions(String stateName, String newText)
    {
        if (true == GLOBALS.equals(stateName))
        {
            // We need to check for this because we recycle the text box.
            // Sometimes we change its contents not because we've changed its contents,
            // but because we're displaying new contents.
            if (false == env.functions.getValue().equals(newText))
            {
                dirty = true;
                env.functions.setValue(newText);
            }
        }
        else if (null != stateName)
        {
            if (false == env.states.get(stateName).functions.getValue().equals(newText))
            {
                dirty = true;
                env.states.get(stateName).functions.setValue(newText);
            }
        }
    }

}
