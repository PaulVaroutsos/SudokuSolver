/**
 * This class takes in a sudoku files and reads all the clauses that are
 * specific to that puzzle.  It then outputs the information line at the top
 * in the form of:  "p cnf 999 " + totalClauseCount"  It will then list all
 * of the clauses that are specific to the puzzle passed into the constructor.
 */
package sudokusolver;

import java.io.FileReader;
import java.io.IOException;
import java.util.Scanner;

public class SudokuReader {

    //This is the number of clauses that each sudoku puzzles all have in common
    private static int CLAUSE_COUNT = 11988;
    private FileReader in;
    private StringBuffer out;

    /**
     * Creates a SudokuReader object.
     * @param in - The input file to use that represents the sudoku puzzle
     * @param out - The output file to write to
     */
    public SudokuReader(FileReader in, StringBuffer out) {
        this.in = in;
        this.out = out;
    }

    /**
     * This method extracts the clauses from the sudoku file and writes them
     * to the file that will be used to solve the sudoku problem
     *
     * @throws java.io.IOException
     */
    public void extractClauses() throws IOException {

        String tmpLine = "";

        //We append the clauses to a string so we can append the variable
        //count and clause count line to the top of the file, THEN append
        //the clauses.  We do this because a file can only be scanned once,
        //so we have to get the clauses and the clause count on the same scan
        String clauseString = "";
        int clauseCount = 0;

        Scanner sc = new Scanner(in);

        // this just scans the given sudoku puzzle and creates the variables
        // and inserts it into the clause list
        while (sc.hasNextLine()) {
            tmpLine = sc.nextLine();
            if (tmpLine.startsWith("c") || tmpLine.equals("")) {
                //we do nothing
            } else {
                String[] parse = tmpLine.trim().split("\\s+");

                //A clause in a sudoku puzzle file will always have the first
                //three arguments as ROW COLUMN VALUE
                clauseString += (parse[0] + "" + parse[1] + "" + parse[2]
                        + " 0 \n");
                clauseCount++;
            }
        }

        int totalClauseCount = clauseCount + CLAUSE_COUNT;

        //Write the information line first, then the clauses
        out.append("p cnf 999 " + totalClauseCount + "\n");
        out.append(clauseString);
    }
}
