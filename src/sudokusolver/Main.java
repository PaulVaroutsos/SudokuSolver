/**
 * This is the main class that runs creats a SudokuReader object which reads
 * in the information from a sudoku file and and extracts the clauses and
 * outputs the clauses into an output file along with the information line in
 * the form of: "p cnf 999 'clausecount'"
 *
 * It then creats a Mapper object which also outputs a bunch of clauses to the
 * same file.
 *
 * The clauses that the Mapper makes are the same for all sudoku puzzles.  The
 * ones created from the SudokuReader are unique to the file give.
 *
 * @author Paul Varoutsos, Tom Devito
 * February 2009
 */
package sudokusolver;
import java.io.FileReader;
import java.io.IOException;
import javax.swing.JFileChooser;
import satsolver.dpSolver;

public class Main {

    /**
     * This method is basically a driver for creating a Mapper and a
     * SudokuReader object to create all the clauses required by the
     * solver to solve the mapped version of the sudoku puzzle.
     *
     * @param args - The file that represents the sudoku puzzle
     * @throws java.io.IOException
     */
    public static void main(String[] args) throws IOException{

        //The files that we use for input/ouput
        JFileChooser fc = new JFileChooser();
        int fileChosen = fc.showOpenDialog(null);
        String pathname = "";
        if(fileChosen == 0){
            pathname = fc.getSelectedFile().getPath();
        }else{
            pathname = "C:/test.sudoku";
        }
        FileReader in = new FileReader(pathname);
        StringBuffer out = new StringBuffer("");

        //Objects that create the clauses and outputs to a file
        SudokuReader sr = new SudokuReader(in,out);
        Mapper m = new Mapper(out);

        //The clauses built from buildSudokuClauses will always
        //come to a total of 11988 for all sudoku puzzles.
        //Add this with the number of puzzle specific clauses

        //Get puzzle specific clauses
        sr.extractClauses();

        //Get clauses that are true for all sudoku puzzles
        m.buildSudokuClauses();

        in.close();

        //Give the file to the solver and check the solution.
        dpSolver solver = new dpSolver();
        int[] solution = solver.solve(out);
        UnMapper.unMap(solution);
    }
}
