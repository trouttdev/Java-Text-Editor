/*
 * Main.java
 * 
 * This is a fairly barebones text editor with an HTML export option.
 * This allows you to format text like you would in any other text
 * editor, but then export it and use it in a web page fairly easily.
 * All styling is done inline or inernally, this does not make a stylesheet.
 * 
 * Copyright 2010-2011 Michael Troutt
 */

package editor;

import javax.swing.UIManager;

/**
 * Main
 * @author Michael Troutt
 */
public class Main {

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception evt) {}
        new HTMLEditor();
    }

}

/*
 * HTMLEditor.java
 * 
 * This is a fairly barebones text editor with an HTML export option.
 * This allows you to format text like you would in any other text
 * editor, but then export it and use it in a web page fairly easily.
 * All styling is done inline or inernally, this does not make a stylesheet.
 * 
 * Copyright 2010-2011 Michael Troutt
 */

package editor;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.GraphicsEnvironment;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JColorChooser;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.JToggleButton;
import javax.swing.JToolBar;
import javax.swing.KeyStroke;
import javax.swing.text.DefaultEditorKit;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledEditorKit;

/**
 * HTMLEditor
 * @author Michael Troutt
 */

public class HTMLEditor extends JFrame implements ActionListener, KeyListener, MouseListener {

    private String fileName = "Untitled"; //Default File Name

    private JFrame frame = new JFrame();

    private StyledEditorKit eK = new StyledEditorKit();

    private JTextPane pane = new JTextPane();

    private JMenuBar menu = new JMenuBar();
    private JToolBar tool = new JToolBar(); //Toolbar holding style attributes

    private JScrollPane scroller = new JScrollPane(pane,
                                                    JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                                                    JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);

    //current working directory
    private JFileChooser dialog = new JFileChooser(System.getProperty("user.dir"));

    private SimpleAttributeSet attr = new SimpleAttributeSet();

    private boolean isChanged = false; //Has the doc been modified?


    //Head options
    private boolean includeDoctype = true;
    private boolean includeTitle = true;
    private boolean includeMetaDescription = false;
    private boolean includeMetaKeywords = false;
    private boolean includeStyleSheet = false;
    private boolean cssInline = true;

    private String doctype = "<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Strict//EN\" \"http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd\">";
    private String title = this.fileName;
    private String metaDescription = "Your description.";
    private String metaKeywords = "your,keywords";
    private String stylesheet = "yourStylesheet.css";
    //End head options

    //Style buttons: declared here so they're easily editable later.
    private JToggleButton boldButton = new JToggleButton("Bold");
    private JToggleButton italButton = new JToggleButton("Italicize");
    private JToggleButton underlineButton = new JToggleButton("Underline");
    private JToggleButton leftAlignButton = new JToggleButton(
                new StyledEditorKit.AlignmentAction("Left Align",StyleConstants.ALIGN_LEFT));
    private JToggleButton centerAlignButton = new JToggleButton(
                new StyledEditorKit.AlignmentAction("Center Align",StyleConstants.ALIGN_CENTER));
    private JToggleButton rightAlignButton = new JToggleButton(
                new StyledEditorKit.AlignmentAction ("Right Align",StyleConstants.ALIGN_RIGHT));
    //end style buttons

    //Font combo boxes
    GraphicsEnvironment env = GraphicsEnvironment.getLocalGraphicsEnvironment();
    String[] fontFamilies = env.getAvailableFontFamilyNames();
    JComboBox fonts = new JComboBox(fontFamilies);
    Integer[] fontSizes = {8,9,10,11,12,14,16,18,20,22,24,26,28,36,48,72};
    JComboBox sizes = new JComboBox(fontSizes);
    //end combo boxes

    /*
     * This constructor method creates the entire editor within it.
     */
    public HTMLEditor ()
    {
        //Make the frame...
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(700,500);
        frame.setTitle(fileName);
        frame.setLayout(new BorderLayout());

        this.pane.setEditorKit(eK);
        this.pane.setContentType("text/html"); //allows for HTML exporting

        StyleConstants.setFontSize(attr, 14); //default font size set to 14
        StyleConstants.setFontFamily(attr, "Times New Roman");
        pane.setCharacterAttributes(attr, false);

        //Add a listener to know when the file has been modified
        this.pane.addKeyListener(this);

        this.pane.addMouseListener(this);

        this.tool.setFloatable(false); //so you can't drag the toolbar

        //Menu bar like you'd find in most apps...
	this.setJMenuBar(menu);

        //Begin adding the menu's to the menu bar

        buildFileMenu();

        buildEditMenu();

        buildHelpMenu();
        
        //end menu

        //Begin adding styles to the toobar

        addStyles();

        //end styles

        tool.addSeparator();

        //Begin add alignment

        addAlignment();
        leftAlignButton.setSelected(true);

        //end alignment

        tool.addSeparator();

        //Begin add color button

        addColorButton();

        //end color button

        tool.addSeparator();

        //Begin add font family combobox

        addFonts();

        //end add font family

        tool.addSeparator();

        //Begin add font sizes
        
        addFontSize();
        
        //end add font size

        //Add everything to the frame then set it visible...
        frame.add(scroller,BorderLayout.CENTER);
        frame.setJMenuBar(menu);
        frame.add(tool,BorderLayout.NORTH);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }

    /*
     * The first of the following three methods checks to see if a key
     * has been typed. If it has, it sets a bool to true, which allows
     * the program to know when a document has been modified. Once
     * modified, you will be asked if you want to save before you close
     * it (assuming it's not already saved).
     * The other two are mandatory to implement, but remain empty
     * because they do us no good.
     * @param e   the key pressed and action
    */
    public void keyTyped(KeyEvent e) {
        //Make sure ctrl/alt modified keys aren't included (shortcuts)
        //and the arrow keys weren't what triggered the event
        if(e.getModifiers() < 2 && !(e.getKeyCode() >= 37 && e.getKeyCode() <= 40)){
            this.isChanged = true;
        }
    }
    public void keyPressed(KeyEvent e) {
    }
    public void keyReleased(KeyEvent e) {
        //if the key was an arrow key, and the caret is not in the begining
        //position, go back one position, then set the style.
        if(e.getKeyCode() >= 37 && e.getKeyCode() <= 40 && e.getModifiers() >= 2 && pane.getCaretPosition() != 0){
            pane.setCaretPosition(pane.getCaretPosition() - 1);
            setStyle();
            pane.setCaretPosition(pane.getCaretPosition() + 1);
         //if the caret is in the 0 position, just set the style
        }else if(pane.getCaretPosition() == 0)
            setStyle();
    }
    public void mousePressed(MouseEvent e) {
    }

    public void mouseReleased(MouseEvent e) {
    }

    public void mouseEntered(MouseEvent e) {
    }

    public void mouseExited(MouseEvent e) {
    }

    public void mouseClicked(MouseEvent e) {
        //if the caret is not in the zero position, get the style
        if(pane.getCaretPosition() != 0){
            pane.setCaretPosition(pane.getCaretPosition() - 1);
            setStyle();
            pane.setCaretPosition(pane.getCaretPosition() + 1);
        }
        else if(isChanged)
            setStyle();
    }

    /*
     * This function sets the style of all buttons and combo boxes to the
     * correct values at the selected caret position.
     */
    private void setStyle(){
        boolean b, i, u;
        b = StyleConstants.isBold(pane.getCharacterAttributes());
        i = StyleConstants.isItalic(pane.getCharacterAttributes());
        u = StyleConstants.isUnderline(pane.getCharacterAttributes());
        boldButton.setSelected(b);
        italButton.setSelected(i);
        underlineButton.setSelected(u);
        fonts.setSelectedItem(StyleConstants.getFontFamily(pane.getCharacterAttributes()));
        sizes.setSelectedItem(StyleConstants.getFontSize(pane.getCharacterAttributes()));
        StyleConstants.setFontSize(attr, StyleConstants.getFontSize(pane.getCharacterAttributes()));
        StyleConstants.setFontFamily(attr, StyleConstants.getFontFamily(pane.getCharacterAttributes()));
        StyleConstants.setForeground(attr, StyleConstants.getForeground(pane.getCharacterAttributes()));
        StyleConstants.setAlignment(attr, StyleConstants.getAlignment(pane.getCharacterAttributes()));
        int align = StyleConstants.getAlignment(pane.getCharacterAttributes());
        if(align == 0)
            leftAlignButton.setSelected(true);
        else if(align == 1)
            centerAlignButton.setSelected(true);
        else if(align == 2)
            rightAlignButton.setSelected(true);
        pane.setCharacterAttributes(attr, false);
    }

    /*
     * This actionPerformed method is the catch-all for all of the
     * menus. Once triggered by clicking on a menu item, or using a
     * shortcut, the source is found and turned into a string version,
     * which is then used to do the various tasks assigned.
     * @param e   the action performed
     */
    public void actionPerformed(ActionEvent e) {
        JMenuItem source = (JMenuItem)(e.getSource());
        String sourceText = source.getText();
        if(sourceText.equals("Exit"))
        {
            System.exit(0);
        }else if(sourceText.equals("New")){
            if(isChanged)
            {
                String[] options = {"Yes","No","Cancel"};
                int option = JOptionPane.showOptionDialog(null,
                                     "Would you like to save this document before starting a new one?",
                                     "Save?", 0, JOptionPane.QUESTION_MESSAGE, null, options,options[0]);
                if(option == 0)
                {
                    if(this.fileName.equals("Untitled"))
                    {
                        if(dialog.showSaveDialog(null)==JFileChooser.APPROVE_OPTION){
                            save(dialog.getSelectedFile().getAbsolutePath(),dialog.getSelectedFile().getName());
                            newFile();
                         }
                    }else{
                        save(this.fileName,this.fileName);
                        newFile();
                    }
                }else if(option == 1){
                    newFile();
                }
            }else{
                newFile();
            }
        }else if (sourceText.equals("Save")) {

            if(this.fileName.equals("Untitled"))
            {
                if(dialog.showSaveDialog(null)==JFileChooser.APPROVE_OPTION)
                    save(dialog.getSelectedFile().getAbsolutePath(),dialog.getSelectedFile().getName());
            }else{
                save(this.fileName,this.fileName);
            }

        }else if(sourceText.equals("Save As")){
            this.isChanged = true;
            if(dialog.showSaveDialog(null)==JFileChooser.APPROVE_OPTION)
                    save(dialog.getSelectedFile().getAbsolutePath(),dialog.getSelectedFile().getName());

        }else if(sourceText.equals("Open")){
            open();
        }else if(sourceText.equals("Get Help")){
            JOptionPane.showMessageDialog(this,"I can't help you.");
        }else if(sourceText.equals("About")){
            JOptionPane.showMessageDialog(this,"Made by Michael Troutt");
        }else if(sourceText.equals("Export HTML")){
            exportHTML();
        }else if(sourceText.equals("Editor Options")){
            editorOptions();
        }
    }

    /*
     * This method, even though called "editor options" actually allows
     * you to edit the options asscociated with the <head> portion
     * of the export HTML feature. Through these options, you can
     * turn off/on the options of showing the title, doctype,
     * meta description, meta keywords, and a stylesheet.
     */
    private void editorOptions(){
        final JFrame optionFrame = new JFrame();
        optionFrame.setTitle("Editor Options");
        optionFrame.setSize(500, 375);
        optionFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        optionFrame.setLayout(new BorderLayout());

        JPanel optionPanel = new JPanel();
        optionPanel.setLayout(new BorderLayout());

        JPanel optionPanelLeft = new JPanel();
        optionPanelLeft.setLayout(new GridLayout(8, 1, 5, 20));

        JPanel optionPanelRight = new JPanel();
        optionPanelRight.setLayout(new GridLayout(8, 1, 0, 20));

        JPanel buttons = new JPanel();
        buttons.setLayout(new GridLayout(1, 2, 10, 0));

        JLabel boolEnable = new JLabel ("Enable?");
        JLabel values = new JLabel("Value");

        final JCheckBox checkDocType = new JCheckBox("Doctype",this.includeDoctype);
        final JCheckBox checkTitle = new JCheckBox("Title",this.includeTitle);
        final JCheckBox checkMetaDescription = new JCheckBox("Meta Description",this.includeMetaDescription);
        final JCheckBox checkMetaKeywords = new JCheckBox("Meta Keywords",this.includeMetaKeywords);
        final JCheckBox checkStylesheet = new JCheckBox("Stylesheet",this.includeStyleSheet);

        final JTextField fieldDocType = new JTextField(this.doctype);
        final JTextField fieldTitle = new JTextField(this.title);
        final JTextField fieldMetaDescription = new JTextField(this.metaDescription);
        final JTextField fieldMetaKeywords = new JTextField(this.metaKeywords);
        final JTextField fieldStylesheet = new JTextField(this.stylesheet);

        //css options
        final JLabel cssLabel = new JLabel("CSS options");
        final JRadioButton inline = new JRadioButton("Inline");
        final JRadioButton internal = new JRadioButton("Internal");

        ButtonGroup cssGroup = new ButtonGroup();

        cssGroup.add(inline);
        cssGroup.add(internal);

        if(this.cssInline)
            inline.setSelected(true);
        else
            internal.setSelected(true);

        JButton saveButton = new JButton("Save");
        JButton cancelButton = new JButton("Cancel");

        saveButton.addActionListener(new ActionListener( ) {
            public void actionPerformed(ActionEvent e) {
                includeDoctype = checkDocType.isSelected();
                includeTitle = checkTitle.isSelected();
                includeMetaDescription = checkMetaDescription.isSelected();
                includeMetaKeywords = checkMetaKeywords.isSelected();
                includeStyleSheet = checkStylesheet.isSelected();
                cssInline = inline.isSelected();

                doctype = fieldDocType.getText();
                title = fieldTitle.getText();
                metaDescription = fieldMetaDescription.getText();
                metaKeywords = fieldMetaKeywords.getText();
                stylesheet = fieldStylesheet.getText();

                optionFrame.setVisible(false);
            }
        });

        cancelButton.addActionListener(new ActionListener( ) {
            public void actionPerformed(ActionEvent e) {
                checkDocType.setSelected(includeDoctype);
                checkTitle.setSelected(includeTitle);
                checkMetaDescription.setSelected(includeMetaDescription);
                checkMetaKeywords.setSelected(includeMetaKeywords);
                checkStylesheet.setSelected(includeStyleSheet);
                inline.setSelected(cssInline);

                fieldDocType.setText(doctype);
                fieldTitle.setText(title);
                fieldMetaDescription.setText(metaDescription);
                fieldMetaKeywords.setText(metaKeywords);
                fieldStylesheet.setText(stylesheet);

                optionFrame.setVisible(false);
            }
        });

        //top labels
        optionPanelLeft.add(boolEnable);
        optionPanelRight.add(values);

        //doctype
        optionPanelLeft.add(checkDocType);
        optionPanelRight.add(fieldDocType);

        //title
        optionPanelLeft.add(checkTitle);
        optionPanelRight.add(fieldTitle);

        //meta description
        optionPanelLeft.add(checkMetaDescription);
        optionPanelRight.add(fieldMetaDescription);

        //meta keywords
        optionPanelLeft.add(checkMetaKeywords);
        optionPanelRight.add(fieldMetaKeywords);

        //stylesheet
        optionPanelLeft.add(checkStylesheet);
        optionPanelRight.add(fieldStylesheet);

        //css options
        optionPanelLeft.add(cssLabel);
        optionPanelRight.add(inline);
        optionPanelRight.add(internal);

        //add the check boxes and descriptors to the left side, and the
        //text fields to the right
        optionPanel.add(optionPanelLeft, BorderLayout.WEST);
        optionPanel.add(optionPanelRight, BorderLayout.CENTER);

        //buttons
        buttons.add(saveButton);
        buttons.add(cancelButton);


        //put it in the middle of the parent frame
        optionFrame.setLocationRelativeTo(this);
        optionFrame.add(optionPanel, BorderLayout.CENTER);
        optionFrame.add(buttons, BorderLayout.SOUTH);

        optionFrame.setVisible(true);
    }

    /*
     * The open method is used to open an existing file.
     * If the file we are currently working in has been modified,
     * then we are prompted to save the current file before opening.
     */
    private void open() {
        String file;
        try {
            if(!this.isChanged)
            {   //if the doc is not changed and if a new file was chosen to open
                if(dialog.showOpenDialog(null)==JFileChooser.APPROVE_OPTION)
                {
                    String text = "";
                    file = dialog.getSelectedFile().getName();
                    FileReader open = new FileReader(file);
                    int ch = open.read();
                    while(ch != -1)
                    {
                        text = text + (char) ch;
                        ch = open.read();
                    }
                    open.close(); //teehee
                    this.doctype = getDocType(text);
                    this.title = getHTMLTitle(text);
                    this.metaKeywords = getMKeys(text);
                    this.metaDescription = getMDesc(text);
                    this.stylesheet = getCSS(text);
                    this.fileName = file;
                    this.pane.setText(stripText(text));
                    frame.setTitle(this.fileName);
                    this.isChanged = false;
                }
            }else{ //if you want to save the doc before opening a new one....
                int option = JOptionPane.showConfirmDialog(null,"Would you like to save this document before opening a different one?",
                                                "Save?", JOptionPane.YES_NO_OPTION);
                if(option == JOptionPane.YES_OPTION)
                {
                    this.isChanged = true;
                    if(this.fileName.equals("Untitled"))
                    {
                        if(dialog.showSaveDialog(null)==JFileChooser.APPROVE_OPTION)
                            save(dialog.getSelectedFile().getAbsolutePath(),dialog.getSelectedFile().getName());
                    }else{
                        save(this.fileName,this.fileName);
                    }
                }
                isChanged = false;
                //re-call open(), which then goes to the original if statement
                open();
            }
        }
        catch(IOException e) {
                JOptionPane.showMessageDialog(this,"Unable to open file: " + e);
        }
    }

    /*
     * Save the given file. Can be used in a "Save as" fasion
     * with a dialog box to choose a file name, or in a standard
     * save-over fasion. Dialogs must be called before the method.
     * @param file  the absolute path to the file
     * @param name  the file name
     */
    private void save(String file, String name) {
        try {
                if(this.isChanged)
                {
                    System.out.println("Saving");
                    String writeText = formatHead(pane.getText());
                    FileWriter writer = new FileWriter(file);
                    writer.write(writeText);
                    writer.close();
                    this.fileName = name;
                    frame.setTitle(this.fileName);
                    this.isChanged = false;
                }
        }
        catch(IOException e) {
            JOptionPane.showMessageDialog(this,"Save failed for "+file);
        }
    }

    /*
     * This method sets most attributes of the file back to default,
     * giving the impression that it's an entirely new document.
     */
    private void newFile(){
        this.isChanged = false;
        this.fileName = "Untitled";
        centerAlignButton.setSelected(false);
        rightAlignButton.setSelected(false);
        leftAlignButton.setSelected(true);
        boldButton.setSelected(false);
        underlineButton.setSelected(false);
        italButton.setSelected(false);
        StyleConstants.setBold(attr, false);
        StyleConstants.setUnderline(attr, false);
        StyleConstants.setItalic(attr, false);
        StyleConstants.setFontFamily(attr, "Times New Roman");
        StyleConstants.setFontSize(attr, 14);
        StyleConstants.setForeground(attr, Color.black);
        fonts.setSelectedItem("Times New Roman");
        sizes.setSelectedItem(14);
        pane.setCharacterAttributes(attr, false);
        StyleConstants.setAlignment(attr, StyleConstants.ALIGN_LEFT);
        pane.setParagraphAttributes(attr, false);
        frame.setTitle(this.fileName);
        this.pane.setText("");
    }
    
    /*
     * This is where the HTML export happens. It brings up a new dialog
     * that holds all of the HTML, and does some parsing to clean it
     * up a bit. You then have the option to save the displayed text, or
     * just close the box.
     */
    private void exportHTML(){
        final JFrame HTMLFrame = new JFrame();
        HTMLFrame.setTitle("Editor Options");
        HTMLFrame.setSize(500, 375);
        HTMLFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        HTMLFrame.setLayout(new BorderLayout());

        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new GridLayout(1,2,5,5));

        JButton button;

        String currText = pane.getText();

        //Replace empty paragraphs with breaks
        currText = currText.replaceAll("<p([^>]*)>\\s*</p>","<br/>");

        //Replace <b> tags with <strong> tags, more standards compliant
        currText = currText.replaceAll("<b>","<strong>");
        currText = currText.replaceAll("</b>","</strong>");

        //Replace <i> tags with <em> tags, more standards compliant
        currText = currText.replaceAll("<i>","<em>");
        currText = currText.replaceAll("</i>","</em>");

        currText = formatHead(currText);

        if(!cssInline)//if CSS is not inline, assume internal
            currText = buildCSS(currText);

        final JTextPane textPane = new JTextPane();
        textPane.setText(currText);

        JScrollPane scrollPane = new JScrollPane(textPane);

        HTMLFrame.add(scrollPane, BorderLayout.CENTER);

        button = new JButton("Save");

        button.addActionListener(new ActionListener( ) {
            public void actionPerformed(ActionEvent e) {
                if(dialog.showSaveDialog(null)==JFileChooser.APPROVE_OPTION){
                    try{
                        FileWriter writer = new FileWriter(dialog.getSelectedFile().getAbsolutePath());
                        writer.write(textPane.getText());
                        writer.close();
                    }
                    catch(IOException z) {
                        JOptionPane.showMessageDialog(HTMLFrame,"Save failed for " + dialog.getSelectedFile().getName());
                    }
                }
            }
        });

        buttonPanel.add(button);

        button = new JButton("Close");

        button.addActionListener(new ActionListener( ) {
            public void actionPerformed(ActionEvent e) {
                HTMLFrame.dispose();
            }
        });
        
        buttonPanel.add(button);

        HTMLFrame.add(buttonPanel,BorderLayout.SOUTH);
        HTMLFrame.setLocationRelativeTo(this);
        HTMLFrame.setVisible(true);
    }

    /*
     * This method formats the head in a fairly stripped down version
     * of a <head> section you'd find in most web pages.
     *
     * @param text  the text in the pane
     * @return      the formatted text with the head included
     */
    private String formatHead(String text){

        String headReplace = "<head>\n";

        if(includeTitle)
        {
            headReplace = headReplace + "<title>" + title + "</title>\n";
        }

        if(includeMetaDescription || includeMetaKeywords)
        {
            headReplace = headReplace + "<meta http-equiv=\"Content-Type\" content=\"text/html; charset=utf-8\" />\n";
        }

        if(includeMetaDescription)
        {
            headReplace = headReplace + "<meta name=\"description\" content=\"" +  metaDescription + "\" />\n";
        }

        if(includeMetaKeywords)
        {
            headReplace = headReplace + "<meta name=\"keywords\" content=\"" + metaKeywords + "\" />\n";
        }

        if(includeStyleSheet)
        {
            headReplace = headReplace + "<link href=\"" + stylesheet + "\" rel=\"stylesheet\" type=\"text/css\" />\n";
        }

        if(includeDoctype)
        {
            text = text.replaceFirst("<html>",doctype + "\n<html>");
        }

        text = text.replaceFirst("<head>",headReplace);

        return text;
    }

    /*
     * This method formats the styling of the text into CSS format, rather than
     * inline formatting.
     * ex: <p style="margin: 0">
     * goes to
     * .style_0
     * {
     *  margin: 0;
     * }
     * and the <p > tag turns to <p class="style_0">
     *
     * @param   text from the pane with inline formatted styling.
     * @return  text with formatted CSS styling.
     */
    private String buildCSS(String text){

        ArrayList<String> css = new ArrayList<String>(); //holds style strings
                                                         //from each paragraph

        ArrayList<String> cssBuilder = new ArrayList<String>(); //holds somewhat formatted
                                                                //style strings
        String style = "";
        String findString = "<p ";
        String styledText = "";
        String styleText = "";

        int findFirst = text.indexOf(findString);
        int findLast = text.indexOf(">",findFirst);

        /*
         * This while loop finds each <p> tag with style elements in it,
         * then looks to see if the same style is already in our css
         * array list. If it's not, it's added.
         */
        while(findFirst != -1)
        {
            style = text.substring(findFirst + findString.length(),findLast);
            if(!css.contains(style))
            {
                css.add(style);
            }
            findFirst = text.indexOf(findString,findLast);
            findLast = text.indexOf(">",findFirst);
        }

        /*
         *The next two lines sort the css array list by the string length.
         * This allows for easier handling of the list later on.
         */
        Comparator comparator = new CSSComparator();

        Collections.sort(css,comparator);
        //end sort

        /*
         * The next ~35 lines is where the bulk of the css formatting is done.
         * The for loop goes through each css style found in the document,
         * sorts out the align and style properties, adds them to a string,
         * then adds that string to the cssbuilder array list, which holds the
         * CSS looking styles (rather than the in-line).
         */
        for(String s : css)
        {
            //if the css string has an align property:
            if(s.indexOf("align=\"") != -1)
            {
                String sAlign = "align=\"";
                int iAlign = s.indexOf("align=\"");
                //This takes out the "align" part of the string and replaces
                //it with somethin recognisable by CSS. It then adds the property to the string.
                styleText = styleText + "text-align: " + s.substring(iAlign + sAlign.length(),
                                                s.indexOf("\"",iAlign + sAlign.length()))
                                                + ";\n";
                //take out the align property from the css string
                s = s.replace("align=\"([^>]*)\"","");
            }
            String sStyle = "style=\"";
            //if there is a style property
            if(s.indexOf(sStyle) != -1)
            {
                int startPos = s.indexOf(sStyle);
                //if there is no semi colon in the string, that indicates there
                //is only one style property within this css string, so no need
                // to loop.
                if(s.indexOf("\"",startPos + sStyle.length()) < s.indexOf(";",startPos) || s.indexOf(";",startPos) == -1)
                    styleText = styleText + s.substring(startPos + sStyle.length(),s.indexOf(":",startPos) + 1)
                                        + s.substring(s.indexOf(":",startPos) + 1,s.indexOf("\"",startPos + sStyle.length())) + ";\n";
                else{
                    //but if there is a semi colon, go through each style until
                    //a semi colon is reached
                    while(startPos != -1)
                    {
                        if(s.indexOf(";",startPos) == -1){
                            styleText = styleText + s.substring(startPos,s.indexOf(":",startPos) + 1)
                                            + s.substring(s.indexOf(":",startPos) + 1,s.indexOf("\"",startPos + sStyle.length())) + ";\n";
                        }
                        else{
                            styleText = styleText + s.substring(startPos,s.indexOf(":",startPos) + 1)
                                            + s.substring(s.indexOf(":",startPos) + 1,s.indexOf(";",startPos)) + ";\n";
                            startPos = s.indexOf(";",startPos) + 1;
                        }
                    }
                }
            }
            //add the semi-formatted string to the cssBuilder arraylist
            cssBuilder.add(styleText);
            styleText = "";
        }

        //this replaces all the originally found inline styles with
        //more css friendly "classes". The classes are generically named
        //with "style_" and the number with which they were in the arraylist
        for(int i = 0;i<css.size();i++)
        {
            text = text.replaceAll(css.get(i),"class=\"style_" + i + "\"");
        }

        styledText = "<style>\n";

        int counter = 0;

        /*
         * This is where the real css "magic" happens. Each propery in the
         * cssBuilder arraylist is now added to a style class that coresponds
         * to the original css(arraylist) from which it came, so all text
         * still looks the same from inline formatting to the new styled format
         */
        for(String s : cssBuilder)
        {
            styledText = styledText + ".style_" + counter + "{\n"
                            + s + "}\n";
            counter++;
        }

        //now the </head> tag will be replaced with our new <style>*</style>
        //tags, as well as a replacement </head> tag to finish it off.

        styledText = styledText + "</style>\n</head>";

        text = text.replace("</head>",styledText);

        //and now return our pretty styling

        return text;
    }

    /*
     * Used for sorting in the buildCSS method.
     * Takes in 2 objects (in this case, strings), and then compares them
     * to see which is longer. The sort method then uses this information
     * to sort them within my arraylist.
     */
    public class CSSComparator implements Comparator{
            public int compare(Object o1, Object o2){

            String one = (String)o1;
            String two = (String)o2;

            if(one.length() > two.length())
                return -1;
            else if(one.length() < two.length())
                return 1;
            else
                return 0;
            }
    }

    /*
     * Adds the usual File menu components to the menu. Also adds an
     * "Export HTML" option.
     */
    private void buildFileMenu(){
        JMenu file = new JMenu("File");
	menu.add(file);

        //Adds the new menu item, as well as a ctrl + n shortcut
        JMenuItem menuNew = new JMenuItem("New");
            menuNew.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_N, ActionEvent.CTRL_MASK));
            menuNew.addActionListener(this);
            file.add(menuNew);

        //Adds the open menu item, as well as a ctrl + o shortcut
        JMenuItem menuOpen = new JMenuItem("Open");
            menuOpen.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O, ActionEvent.CTRL_MASK));
            menuOpen.addActionListener(this);
            file.add(menuOpen);

        //Adds the save menu item, as well as a ctrl + s shortcut
        JMenuItem menuSave = new JMenuItem("Save");
            menuSave.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, ActionEvent.CTRL_MASK));
            menuSave.addActionListener(this);
            file.add(menuSave);

        //Adds the save as menu item
        JMenuItem menuSaveAs = new JMenuItem("Save As");
            menuSaveAs.addActionListener(this);
            file.add(menuSaveAs);

        file.addSeparator();

        //Adds the export HTML menu item
        JMenuItem menuHTML = new JMenuItem("Export HTML");
            menuHTML.addActionListener(this);
            file.add(menuHTML);

        file.addSeparator();

        //Adds the exit menu item
        JMenuItem menuExit = new JMenuItem("Exit");
            menuExit.addActionListener(this);
            file.add(menuExit);
    }

    /*
     * Adds a pretty bare version of a normal Edit menu.
     */
    private void buildEditMenu(){
        JMenu edit = new JMenu("Edit");
        menu.add(edit);

        //Select All Item and ctrl + a shortcut
            edit.add(pane.getActionMap().get(DefaultEditorKit.selectAllAction));
            edit.getItem(0).setText("Select All");
            edit.getItem(0).setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_A, ActionEvent.CTRL_MASK));

        edit.addSeparator();

        //Cut Item and ctrl + x shortcut
            edit.add(pane.getActionMap().get(DefaultEditorKit.cutAction));
            edit.getItem(2).setText("Cut");
            edit.getItem(2).setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_X, ActionEvent.CTRL_MASK));

        //Copy Item and ctrl + c shortcut
            edit.add(pane.getActionMap().get(DefaultEditorKit.copyAction));
            edit.getItem(3).setText("Copy");
            edit.getItem(3).setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_C, ActionEvent.CTRL_MASK));

       //Paste Item and ctrl + v shortcut
            edit.add(pane.getActionMap().get(DefaultEditorKit.pasteAction));
            edit.getItem(4).setText("Paste");
            edit.getItem(4).setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_V, ActionEvent.CTRL_MASK));

      edit.addSeparator();

      JMenuItem editorOptions = new JMenuItem("Editor Options");
            editorOptions.addActionListener(this);
            edit.add(editorOptions);
    }

    /*
     * Adds a very bare Help menu. No actual help can be gotten from here.
     */
    private void buildHelpMenu(){
        JMenu help = new JMenu("Help");
        menu.add(help);

        JMenuItem menuGetHelp = new JMenuItem("Get Help");
            menuGetHelp.addActionListener(this);
            help.add(menuGetHelp);

        JMenuItem menuAbout = new JMenuItem("About");
            menuAbout.addActionListener(this);
            help.add(menuAbout);
    }

    /*
     * Adds the standard Bold/Ital/Underline buttons to the toolbar.
     */
    private void addStyles(){

        boldButton.addActionListener(new StyledEditorKit.BoldAction());
        boldButton.setFocusable(false);

        italButton.addActionListener(new StyledEditorKit.ItalicAction());
        italButton.setFocusable(false);

        underlineButton.addActionListener(new StyledEditorKit.UnderlineAction());
        underlineButton.setFocusable(false);

        tool.add(boldButton);
        tool.add(italButton);
        tool.add(underlineButton);
    }

    /*
     * Adds Left/Right/Center alignment to the toolbar.
     */
    private void addAlignment(){

        ButtonGroup group = new ButtonGroup();

        leftAlignButton.setFocusable(false);

        centerAlignButton.setFocusable(false);

        rightAlignButton.setFocusable(false);

        group.add(leftAlignButton);
        group.add(centerAlignButton);
        group.add(rightAlignButton);

        tool.add(leftAlignButton);
        tool.add(centerAlignButton);
        tool.add(rightAlignButton);
    }
    
    /*
     * The next series of 6 methods gets the doctype, title, meta tags,and css
     * from the incoming document, sets the necessary variables within the
     * program, then the stripText method takes only the text from within
     * the body tags and returns is. These methods are needed because if
     * you try to open a document with these tags implemented, it will fail.
     *
     * @param   text from the file being opened
     * @return  various aspects of the doc -- see each method
     */

    private String getDocType(String text){

        String beginDocType = "<!DOCTYPE";
        String endDocType = ">";
        if(text.indexOf(beginDocType) != -1)
            return text.substring(text.indexOf(beginDocType), text.indexOf(endDocType, text.indexOf(beginDocType)) + endDocType.length());
        //if doctype is found in the doc we're opening, return it, if not return the default
        return this.doctype;
    }

    private String getHTMLTitle(String text){
        String beginLookingFor = "<title>";
        String endLookingFor = "</title>";
        if(text.indexOf(beginLookingFor) != -1)
            return text.substring(text.indexOf(beginLookingFor) + beginLookingFor.length(),
                    text.indexOf(endLookingFor, text.indexOf(beginLookingFor)));
        //if title is found in the doc we're opening, return it
        //if not return the default
        return this.title;
    }

    private String getMKeys(String text){
        String beginLookingFor = "<meta name=\"keywords\" content=\"";
        String endLookingFor = "\" />";
        if(text.indexOf(beginLookingFor) != -1)
            return text.substring(text.indexOf(beginLookingFor) + beginLookingFor.length(),
                    text.indexOf(endLookingFor,
                    text.indexOf(beginLookingFor)));
        //if keyword is found in the doc we're opening, return it
        //if not return the default
        return this.metaKeywords;
    }

    private String getMDesc(String text){
        String beginLookingFor = "<meta name=\"description\" content=\"";
        String endLookingFor = "\" />";
        if(text.indexOf(beginLookingFor) != -1)
            return text.substring(text.indexOf(beginLookingFor) + beginLookingFor.length(),
                    text.indexOf(endLookingFor,
                    text.indexOf(beginLookingFor)));
        //if description is found in the doc we're opening, return it
        //if not return the default
        return this.metaDescription;
    }

    private String getCSS(String text){
        String beginLookingFor = "<link href=\"";
        String endLookingFor = "\" rel=\"stylesheet\" type=\"text/css\" />";
        if(text.indexOf(beginLookingFor) != -1)
            return text.substring(text.indexOf(beginLookingFor) + beginLookingFor.length(),
                    text.indexOf(endLookingFor,
                    text.indexOf(beginLookingFor)));
        //if stylesheet is found in the doc we're opening, return it
        //if not return the default
        return this.stylesheet;
    }

    private String stripText(String text){

        String beginLookingFor = "<body>";
        String endLookingFor = "</body>";
        if(text.indexOf(beginLookingFor) != -1)
            return text.substring(text.indexOf(beginLookingFor) + beginLookingFor.length(),
                    text.indexOf(endLookingFor,
                    text.indexOf(beginLookingFor)));
        return "Error/not found.";
    }

    //end formatting

    /*
     * Adds all system fonts to a JComboBox that can be selected.
     */
    private void addFonts(){
        fonts.setFocusable(false);
        fonts.setSelectedItem(StyleConstants.getFontFamily(attr));
        fonts.setEditable(false);
        fonts.addActionListener(new ActionListener( ) {
            public void actionPerformed(ActionEvent e) {
                JComboBox cb = (JComboBox)e.getSource();
                String fontFam = (String)cb.getSelectedItem();
                StyleConstants.setFontFamily(attr, fontFam);
                pane.setCharacterAttributes(attr, false);
            }
        });
        tool.add(fonts);
    }

    /*
     * Adds font sizes to a JComboBox, only a few standard sizes are
     * included, but it is editable, so any font size is accessible.
     */
    private void addFontSize(){
        sizes.setFocusable(false);
        sizes.setSelectedItem(StyleConstants.getFontSize(attr));
        sizes.setEditable(true);
        sizes.addActionListener(new ActionListener( ) {
            public void actionPerformed(ActionEvent e) {
                JComboBox cb = (JComboBox)e.getSource();
                Integer fontSize = (Integer)cb.getSelectedItem();
                StyleConstants.setFontSize(attr, fontSize);
                pane.setCharacterAttributes(attr, false);
            }
        });

        tool.add(sizes);
    }

    private void addColorButton(){
        JButton colorButton = new JButton("Set Color");
        colorButton.setFocusable(false);
        tool.add(colorButton);

        colorButton.addActionListener(new ActionListener( ) {
            public void actionPerformed(ActionEvent e) {
                Color c = JColorChooser.showDialog(frame,"Choose a color", getBackground());
                StyleConstants.setForeground(attr, c);
                pane.setCharacterAttributes(attr, false);
            }
        });
    }
}