import javax.swing.AbstractButton;
import javax.swing.JButton;
import java.awt.Dimension;
import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;
import javax.swing.JFrame;
import javax.swing.*;
import java.util.*;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

public class SimpleCalc3 implements ActionListener, DocumentListener {
    private JButton[] numPad; // Number pad
    private JButton[] opPad;  // Operation keys
    private JFrame frame;
    private JTextField input; // input box
    private JLabel output; // output box

    // The operations that we can handle
    String operators = "+-/*";

    public SimpleCalc3() {
        frame = new JFrame();

        GridBagLayout layout = new GridBagLayout();

        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.setTitle("SimpleCalc3");
        frame.getContentPane().setLayout(layout);
        GridBagConstraints c = new GridBagConstraints();

        // set some defaults for our gridbag constraint
        c.fill = GridBagConstraints.BOTH;
        c.weightx = 1;
        c.weighty = 1;

        int numX = 0;
        int numY = 2;

        // Setup numpad
        numPad = new JButton[10];
        for(int i=9; i > -1; i--) {
            // create new button
            numPad[i] = new JButton(Integer.toString(i));
            numPad[i].setActionCommand(Integer.toString(i));
            numPad[i].addActionListener(this);
            if(numX == 3) {
                // our number keys are in a 4x3 grid
                // if X hits 3, move to next line
                numX = 0;
                numY++;
            }
            // put button at (numX, numY) on grid
            c.gridx = numX;
            c.gridy = numY;
            numX++;
            frame.add(numPad[i], c);
        }

        // Add operator buttons
        opPad = new JButton[7];
        numY = 2;
        c.gridwidth = 1;
        for(int i=0; i < 4; i++) {
            opPad[i] = new JButton(Character.toString(operators.charAt(i)));
            opPad[i].setActionCommand(Character.toString(operators.charAt(i)));
            opPad[i].addActionListener(this);
            // these buttons go at x=3
            // alongside the numbers (from 1 to 4) on the y axis
            c.gridx = 3;
            c.gridy = numY;
            numY++;
            frame.add(opPad[i], c);
        }

        // add decimal button in special position
        opPad[4] = new JButton(".");
        opPad[4].setActionCommand(".");
        opPad[4].addActionListener(this);
        c.gridy = 5;
        c.gridx = 1;
        frame.add(opPad[4], c);

        // add calculate key
        opPad[6] = new JButton("=");
        opPad[6].setActionCommand("=");
        opPad[6].addActionListener(this);
        c.gridy = 5;
        c.gridx = 2;
        frame.add(opPad[6], c);

        // add inputbox;
        input = new JTextField();
        // catch the enter keypress; if caught, do calculate action
        InputMap im = input.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
        ActionMap am = input.getActionMap();
        im.put(KeyStroke.getKeyStroke("ENTER"), "CALCULATE_ACTION");
        am.put("CALCULATE_ACTION", new CalcAction());
        // place at 0,0 on grid
        c.gridx = 0;
        c.gridy = 0;
        c.gridwidth = 3; // set width to 3 grid spaces
        input.getDocument().addDocumentListener(this);
        frame.add(input, c);

        // add outputbox;
        output = new JLabel();
        // place at 0,1 on grid
        c.gridx = 0;
        c.gridy = 1;
        frame.add(output, c);

        // add clear key
        opPad[5] = new JButton("CLR");
        opPad[5].setActionCommand("CLR");
        opPad[5].addActionListener(this);
        c.gridy = 0;
        c.gridx = 3;
        c.gridwidth = 1;
        frame.add(opPad[5], c);

        frame.setPreferredSize(new Dimension(300, 350)); // set default frame size to 300x350
        frame.pack();
        frame.setVisible(true);
    }

    public void actionPerformed(ActionEvent e) {
        if ("CLR".equals(e.getActionCommand())) {
            // if the CLEAR key was pressed; empty input box
            input.setText("");
            output.setText("");
        } else if ("=".equals(e.getActionCommand())) {
            // if the '=' key was pressed; do Calculate()
            String text = input.getText();
            String newText = Calculate(text);
            if(newText != "defaultError") {
                output.setText("= " + newText);
            }
        } else {
            // if one of the number keys has been pressed,
            // add that character to our input box
            String inputText = input.getText();
            input.setText(inputText + e.getActionCommand());
        }
    }

    public void insertUpdate(DocumentEvent ev) {
        // on update; do Calculate()
        String text = input.getText();
        String newText = Calculate(text);
        if(newText != "defaultError") {
            output.setText("= " + newText);
        }
    }

    public void removeUpdate(DocumentEvent ev) {
        // on update; do Calculate()
        String text = input.getText();
        String newText = Calculate(text);
        if(newText != "defaultError") {
            output.setText("= " + newText);
        }
    }

    public void changedUpdate(DocumentEvent ev) {
    }

    class CalcAction extends AbstractAction {
        public void actionPerformed(ActionEvent ev) {
            // this only happens when enter has been pressed while
            // focused on the input box;  it calls Calculate()
            String text = input.getText();
            String newText = "= " + Calculate(text);
            if(newText != "defaultError") {
                output.setText(newText);
            }
        }
    }

    private String Calculate(String text) {
        // our main calculation logic
        String output = "defaultError";
        String numArray[] = new String[256];
        char op[] = new char[128];
        int offset[] = new int[128];
        int opNum = 0;
        int prevOp = -1;
        float val = 0;
        //String operators = "+-*/";

        /* Iterate through our input string;  if we find an operator,
         * take note of its position in the string. */
        for(int i=0; i<text.length(); i++) {
            for(int u=0; u<operators.length(); u++) {

                if(text.charAt(i) == operators.charAt(u)) {
                    // grab operator
                    int needBreak = 0;

                    if((i != 0) && (i != prevOp + 1)) {
                        op[opNum] = text.charAt(i);
                        offset[opNum] = i;
                        opNum++;
                        needBreak = 1;
                    }
                    prevOp = i;
                    if(needBreak == 1) break;
                }
            }
        }

        // if we have found more than 0 operators, populate our number array
        if((offset[0] != 0)) {
            numArray[0] = "";
            for(int i=0; i<offset[0]; i++) {
                numArray[0] = numArray[0]+text.charAt(i);
            }

            for(int i=1; i < opNum; i++) {
                numArray[i] = "";
                for(int x=offset[i - 1] + 1; x<offset[i]; x++) {
                    numArray[i] = numArray[i]+text.charAt(x);
                }
            }
            numArray[opNum] = "";
            for(int i=offset[opNum - 1] + 1; i<text.length(); i++) {
                numArray[opNum] = numArray[opNum]+text.charAt(i);
            }
        }

        // debugging
        /*for(int i=0; i<=opNum; i++) {
            System.out.println("numArray[" + i + "]: " + numArray[i]);
            System.out.println("op[" + i + "]: " + op[i]);
        }
        System.out.println("-------------");
        // System.out.println("do" + i + "-" + opNum + ": " + val + op[i] + numArray[i+1]);
        */

        if((opNum != 0) && (numArray[1] != "")) {
            try {
                /* This code does the mathematical operations left to right,
                 * one at a time.  It completely ignores the order of operations
                 */

                String tmpOp;
                // do initial operation
                tmpOp = doCalcOp(op[0], Float.parseFloat(numArray[0]), Float.parseFloat(numArray[1]));
                if(tmpOp != "err") {

                    /* do sequential operations in a loop, one at a time
                     * storing the result of each operation in a variable `val`
                     *  and then doing 'val [op] nextNumber'
                     * it works fine for addition and multiplication, but fails
                     * horribly for some combinations (due to ignoring pemdas)
                     */
                    val = Float.parseFloat(tmpOp);
                    for(int i=1; i<opNum; i++) {
                        tmpOp = "";
                        tmpOp = doCalcOp(op[i], val, Float.parseFloat(numArray[i+1]));
                        if(tmpOp == "err") output = "error";
                        val = Float.parseFloat(tmpOp);
                    }
                } else {
                    output = "error";
                }

                if(output != "error") output = Float.toString(val);
            } catch (Exception e) {
                /* errors caught here will most likely be due to invalid characters
                 * in the input box
                 */
                output = "Caught Error!";
            }

        }

        return output;
    }

    private String doCalcOp(char op, float num1, float num2) {
        /* do one math operation with two numbers
         * base 10. only supports these operations (so far):
         * multiplication, division
         * addition, subtraction.
         * returns "err" if it was handed an invalid operation
         */

        String result = "err";
        float val = 0;
        switch(op) {
            case '+': // addition
                val = num1 + num2;
                break;

            case '-': // subtraction
                val = num1 - num2;
                break;

            case '*': // multiplication
                val = num1 * num2;
                break;

            case '/': // division
                val = num1 / num2;
                break;
            default:
                result = Character.toString(op);
                break;
        }
        if(result != "ERROR") result = Float.toString(val);
        return result;
    }

    public static void main(String args[]) {
    SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                new SimpleCalc3();
            }
        });
    }
}
