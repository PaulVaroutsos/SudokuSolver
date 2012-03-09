package display;

import java.awt.*;
import javax.swing.JComponent;

/**
 * This is a component that can be added to a JFrame to display a sudoku puzzle.
 * 
 * @author Paul Varoutsos
 */
public class SudokuDisplay extends JComponent {

    private static Font serifFont = new Font("Serif", Font.BOLD, 24);
    
    //Initialized to all 0s
    private int[][] sudokuArray = new int[9][9];

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);

        // draw entire component white
        g.setColor(Color.white);
        g.fillRect(0, 0, getWidth(), getHeight());

        g.setColor(Color.black);
        FontMetrics fm = g.getFontMetrics();
        g.setFont(serifFont);
        fm = g.getFontMetrics();

        //Vertical lines
        for (int i = 0; i <= 400- 40; i = i + 40) {
            if(i%3 == 0){//make every third line thicker
                g.drawLine(i-2, 0, i-2, 360);
                g.drawLine(i-1, 0, i-1, 360);
                g.drawLine(i, 0, i, 360);
                g.drawLine(i+1, 0, i+1, 360);
                g.drawLine(i+2, 0, i+2, 360);
            }else{
                g.drawLine(i, 0, i, 360);
            }
        }

        //Horizontal lines
        for (int i = 0; i <= 400-40; i = i + 40) {
            if(i%3 == 0){//make ever third line thicker
                g.drawLine(0, i-2, 360, i-2);
                g.drawLine(0, i-1, 360, i-1);
                g.drawLine(0, i, 360, i);
                g.drawLine(0, i+1, 360, i+1);
                g.drawLine(0, i+2, 360, i+2);
            }else{
                g.drawLine(0, i, 360, i);
            }
        }
       
        drawNumbers(g);
    }
    
    public void updateArray(int[][] newArray){
        sudokuArray = newArray;
        this.repaint();
    }
    
    /**
     * Draws whatever numbers are found in the sudokuArray.  Does not display
     * 0s (For example, before a puzzle is loaded).
     * @param g 
     */
    private void drawNumbers(Graphics g){
        for (int x = 0, i = 0; x < 400 && i<9; x += 40) {
            for (int y = 40, j=0; y < 400 && j < 9; y += 40) {
                if(sudokuArray[j][i]!= 0){
                    g.drawString(sudokuArray[j][i]+"", x + 15, y - 12);
                }
                j++;
            }
            i++;
        }
    }

    @Override
    public Dimension getPreferredSize() {
        return new Dimension(360, 360);
    }
}