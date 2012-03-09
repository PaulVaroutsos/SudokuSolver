/**
 * This class takes the output of the solver and creates a solution to the
 * puzzle based on that output.  If the solver could not find an answer to
 * solve the puzzle it will output that an answer could not be found.
 */
package sudokusolver;

import java.io.FileNotFoundException;

public class UnMapper {

    //this variable is where we will store our answered sudoku puzzle if a
    //solution is found
    private static int[][] sudokuPuzzle = new int[9][9];

    public static int[][] unMap(int[] solution) throws FileNotFoundException {

        if (solution == null) {
            System.out.println("There is no solution for this puzzle.");
            return sudokuPuzzle;
        }

        // 345 = row 3, column 4, box contains a 5
        //variables used for determining row, column, and value
        int quotient, remainder, row, column, value;

        //Go through all of the variables.  Skip variavles < 111 because they do
        //not represent valid sudoku variables
        for (int i = 111; i < solution.length; i++) {

            //make sure that the variable has only three digits and that the
            //variable is true.  This corresonds to a square on the puzzle:
            //ex. 345 = row 3, column 4, box contains a 5
            if (solution[i] > 0 && i < 1000) {

                //345 / 100 = 3 (row ) remainder 45
                quotient = i / 100;
                remainder = i % 100;
                row = quotient;

                //45 / 10 = 4 (column) remaider 5 (value)
                quotient = remainder / 10;
                column = quotient;
                value = remainder % 10;

                //need to take into account variables like
                //110 210 310 .... 201 301 401
                //these values can not be used since 0 is not valid sudoku entry
                if (value != 0 && column != 0) {
                    //place the value into the sudoku puzzle
                    sudokuPuzzle[translate(row)][translate(column)] = value;
                }
            }
        }//end for

        //once we are done parsing the variables we then need to print the
        //soduku board
        String output = "";
        for (int i = 1; i <= 9; i++) {
            for (int j = 1; j <= 9; j++) {
                output += sudokuPuzzle[translate(i)][translate(j)] + " ";
            }
            output += "\n";
        }

        System.out.println(output);
        return sudokuPuzzle;
    }

    /**
     * Creates the array index equivalent of the number
     * @param x The number you want to translate to an index
     * @return x-1
     */
    private static int translate(int x) {
        return x - 1;
    }
}