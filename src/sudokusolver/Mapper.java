/**
 * This class is the Mapper.  This class creates all 11988 clauses that are
 * required for all sudoku puzzles.  These are constraints such as:
 *   1.1.A  each number must occur at least once in a row
 *   1.1.B  each number must occur only once in the row
 *   2.1.A  each number must occur at least once in the column
 *   2.1.B  each number must occur only once in the column
 *   3.1.A  Each number must occur at least once in each box
 *   3.1.B  Each number must occur at most once in each box
 *   4.2    each box has at most 1 variable
 *   4.1    each box has at least 1 variable
 *
 *  This would create the 11988 variables which are required for all sudoku
 *  puzzles.
 *
 *  *Reference:  Teaching Problem Reduction: NP-Completeness via Sudoku
 *                      -Andrea F. Lobo
 *
 *               The SuDoku Puzzle as a Satisfiability Problem
 *               http://www.cs.qub.ac.uk/~I.Spence/SuDoku/SuDoku.html
 *                      -Ivor Spence
 *
 * @author Paul Varoutsos, Tom Devito
 * February 2009
 */
package sudokusolver;

import java.io.IOException;

public class Mapper {

    private final int NUM_VARS = 9;
    private final int SQUARE_SIZE = 9;
    private StringBuffer out;

    public Mapper(StringBuffer out) {
        this.out = out;
    }

    /**
     * This method creates all of the clauses and outputs them to the file
     * defined by the constructor when the object was created.
     *
     *
     * @throws java.io.IOException
     */
    public void buildSudokuClauses() throws IOException {

        //Create all the clauses based on the given constraints
        constraint_11A();
        constraint_21A();
        constraint_11B();
        constraint_21B();
        constraint_31A();
        constraint_31B();
        constraint_41();
        constraint_42();
    }

    // CONSTRAINT 1.1.A
    // Check that each number must occur at least once in the row
    // 111 V 121 V 131 V ... V 191
    // 211 V 221 V 231 V ... V 291
    private void constraint_11A() throws IOException {

        for (int k = 1; k <= NUM_VARS; k++) {
            for (int i = 1; i <= SQUARE_SIZE; i++) {
                // Inner loop checks each box in that row
                for (int j = 1; j <= SQUARE_SIZE; j++) {
                    out.append(i + "" + j + "" + k + " ");
                }
                out.append("0\n");
            }
        }
    }
    // CONSTRAINT 2.1.A
    // Checks that each number must occur at least once in the column
    // 111 V 211 V 311 V ... V 911
    // 121 V 221 V 321 V ... V 921

    private void constraint_21A() throws IOException {

        for (int k = 1; k <= NUM_VARS; k++) {
            for (int i = 1; i <= SQUARE_SIZE; i++) {
                for (int j = 1; j <= SQUARE_SIZE; j++) {
                    out.append(j + "" + i + "" + k + " ");
                }
                out.append("0\n");
            }
        }
    }
    // CONSTRAINT 1.1.B
    // Checks that each number must occur only once in the row
    // (-111 V -121) /\ (-111 V -131) /\ ... /\ (-111 V -191)
    // (-121 V -131) /\ (-121 V -141) /\ ... /\ (-121 V -191)

    private void constraint_11B() throws IOException {

        for (int k = 1; k <= NUM_VARS; k++) {
            for (int n = 1; n <= SQUARE_SIZE; n++) {
                for (int j = 1; j <= SQUARE_SIZE; j++) {
                    int ptr = 1;
                    for (int i = j; i < SQUARE_SIZE; i++) {
                        out.append("-" + n + "" + j + "" + k + " ");
                        out.append("-" + n + (j + ptr) + k + " 0\n");
                        ptr++;
                    }
                }
            }
        }
    }

    // CONSTRAINT 2.1.B
    // Checks that each number must occur only once in the column
    // (-111 V -211) /\ (-111 V -311) /\ ... /\ (-111 V -911)
    // (-211 V -311 /\ (-211 V -411) /\ ... /\ (-211 V -911)
    private void constraint_21B() throws IOException {

        for (int k = 1; k <= NUM_VARS; k++) {
            for (int n = 1; n <= SQUARE_SIZE; n++) {
                for (int j = 1; j <= SQUARE_SIZE; j++) {
                    int ptr = 1;
                    for (int i = j; i < SQUARE_SIZE; i++) {
                        out.append("-" + j + "" + n + "" + k + " ");
                        out.append("-" + (j + ptr) + "" + n + "" + k + " 0\n");
                        ptr++;
                    }
                }
            }
        }
    }

    // Sub boxes
    // Check that each number must occur at least once in each
    // box
    // 111 V 121 V 131 V 211 V 221 V 231 V 311 V 321 V 331
    private void constraint_31A() throws IOException {

        for (int k = 1; k <= NUM_VARS; k++) {
            for (int m = 1; m <= SQUARE_SIZE; m += 3) {
                for (int n = 1; n <= SQUARE_SIZE; n += 3) {

                    for (int i = 0; i < 3; i++) {
                        for (int j = 0; j < 3; j++) {
                            out.append((m + i) + "" + (n + j) + "" + k + " ");
                        }
                    }
                    out.append("0\n");
                }
            }
        }
    }

    // put all the literal contained in a box into an array
    // we already solved how to find all the combinations when
    // they are in a row, so make it easier and place the box in
    // order in an array
    // this is a slow way of doing it, may need to look for an
    // alternative method
    private void constraint_31B() throws IOException {


        for (int k = 1; k <= NUM_VARS; k++) {
            for (int m = 1; m <= SQUARE_SIZE; m += 3) {
                for (int n = 1; n <= SQUARE_SIZE; n += 3) {

                    int p = 0;
                    // place box row by row into a an array to create one big
                    // row
                    String[] temp = new String[9];

                    for (int i = 0; i < 3; i++) {
                        for (int j = 0; j < 3; j++) {
                            temp[p] = (m + i) + "" + (n + j) + "" + k;
                            p++;
                        }
                    }

                    // now the box is placed into an array of strings
                    // we are going to use the same algorithm from above
                    // to find the combination as if the box was a row
                    for (int j = 0; j <= temp.length; j++) {
                        int ptr = 1;
                        for (int i = j; i < temp.length - 1; i++) {
                            out.append("-" + temp[j] + " ");
                            out.append("-" + temp[j + ptr] + " 0\n");
                            ptr++;
                        }
                    }
                }
            }
        }
    }

    //We are at 8991 clauses.  Now we need to say that only one number
    //can be in any one box
    //CONSTRAINT 4
    //Constraint 4.1
    //Check that each box has at least 1 variable
    //V111 V V112 V V113 V ... V V119
    private void constraint_41() throws IOException {

        for (int i = 1; i <= SQUARE_SIZE; i++) {
            for (int j = 1; j <= SQUARE_SIZE; j++) {
                for (int k = 1; k <= SQUARE_SIZE; k++) {
                    out.append(i + "" + j + "" + k + " ");
                }
                out.append("0\n");
            }
        }
    }

    //Constraint 4.2
    //Check that each box has at most 1 variable
    //(-V111 V -V112) /\ (-V111 V -V112)
    private void constraint_42() throws IOException {
        for (int i = 1; i <= SQUARE_SIZE; i++) {
            for (int j = 1; j <= SQUARE_SIZE; j++) {
                for (int k = 1; k <= NUM_VARS; k++) {
                    for (int m = k + 1; m <= SQUARE_SIZE; m++) {
                        out.append("-" + i + "" + j + "" + k + " ");
                        out.append("-" + i + "" + j + "" + m + " 0\n");
                    }
                }
            }
        }
    }
}
