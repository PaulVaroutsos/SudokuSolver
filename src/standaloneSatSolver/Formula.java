/**
 * This class represents a boolean formula and supplements the dpSolver file.
 * Boolean formulas are stored in CNF form.
 *
 * A formula is empty if it has all of its clauses satisfied.
 *
 * A formula has an empty clause if a clause cannot be satisfied
 * with the current assignment and has no unassigned variables.
 *
 * A clause is a unit clause if it has only one unassigned variable.
 *
 * A variable is a unit variable if it is the only variable in a
 * clause without an assignment.
 *
 * Variable assignment order is determined by the Jeroslow Wang Heuristic.
 * This heuristic favors variables that do not occur often and/or occur in
 * small clauses.  These types of variables will be given a greater weight
 * in the calculations.
 *
 * March-May 2009
 * @author Tom De Vito, Paul Varoutsos
 *
 */
package standaloneSatSolver;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.LinkedList;
import java.util.Scanner;
import java.util.Stack;
import java.util.TreeSet;

public class Formula {

	// Holds the value of each variable: -1 unassigned, 0 false, 1 true
	// Size: total variables
	public int[] currentAssignment;

	// Holds the temporary formula throughout this program. The current
	// formula is always the currentFormula.peek()
	public Stack<LinkedList<Integer>> currentFormula = new Stack<LinkedList<Integer>>();

	// Formula that never changes throughout this program
        // Each array represents a clause
	private int[][] formula;

	// Holds the ranks of each variable and its negation.
        // Ranks are computed using the Jeroslow-Wang heuristic.
	// Size of array: 2 * total variables + 1
	// The rank for variable x is located at index location 2x-1
	// The rank for variable -x is located at index 2x
	// Index 0 is not used in this array as variables start at 1
	private double[] variableRanks;

	// Holds an index to the highest ranking variable in currentAssignments
	private int highestRank;

	// Holds the highest variable rank value, which is the addition of
	// both x and -x values. This is used for computeRanks.
	// When we find the highestRank we store the index in highestRank but the
	// rank value of it here
	private double highestRankValue;

	// True if there is an empty clause in the current formula
	// False if there is not
	private boolean hasEmptyClause;

	// Look up table for powers of two.  Reduces computation.
	private static final double[] POWERS_OF_TWO = { 0, .5, .25, .125, .625,
			.03125, .015625, .0078125, 0.00390625, 0.001953125, 0.0009765625 };

	// Holds the variables that been affected by unit propagation
	private Stack<LinkedList<Integer>> variablesAffected;

	// Holds the unit variables.  May contain a branch variable if
	// no unit variables exist
	private TreeSet<Integer> unitVariables = new TreeSet<Integer>();

	//Variable assignments
	private static final int TRUE = 1;
	private static final int FALSE = 0;
	private static final int UNASSIGNED = -1;

    /**
     * Constructor that instantiates a formula object.
     *
     * @param file  - The file that holds the location of the file
     *                containing the formula to solve.
     * @throws FileNotFoundException - If the file is not found
     */
    public Formula(String file) throws FileNotFoundException {
        LinkedList<Integer> originalFormula = new LinkedList<Integer>();
        variablesAffected = new Stack<LinkedList<Integer>>();
        unitVariables = new TreeSet<Integer>();

        // Temporary string that is used for reading each line of the file
        String temp = "";

        // Current clause
        int clauseCount = 0;
        String comment = null;
        String formulaType = "";
        int varTotal;
        int clauseTotal = 0;

        // Create a file reader and scanner to read the file
        FileReader cnfFile = new FileReader(file);
        Scanner sc = new Scanner(cnfFile);

        // Go through each line
        while (sc.hasNextLine()) {
            temp = sc.nextLine();

            // Its a comment
            if (temp.startsWith("c")) {
                comment += (temp + "\n");

               // This is the line that tells us the formula information
            } else if (temp.startsWith("p")) {

                // Split it into an array of strings and then we can get the
                // Number of clauses and variables
                String[] parse;

                // Parse the string apart by all whitespaces
                parse = temp.trim().split("\\s+");
                formulaType = parse[1];

                // number of variables
                varTotal = Integer.parseInt(parse[2]);

                // initialize variableRank array
                variableRanks = new double[2 * varTotal + 1];

                // initialize currentAssignment need to loop through and make
                // default value -1
                currentAssignment = new int[varTotal + 1];

                // Initialize all variables as unassigned
                for (int i = 1; i < currentAssignment.length; i++) {
                    currentAssignment[i] = UNASSIGNED;
                }

                // number of clauses
                clauseTotal = Integer.parseInt(parse[3]);

                // Initialize the formula
                formula = new int[clauseTotal][];

            }
            // Put the value into the formula
            else {
                if (clauseCount != clauseTotal) {

                    String[] parse;
                    parse = temp.trim().split("\\s+");

                    int varInClause = parse.length - 1;

                    // each clause we have add it to the currentFormula
                    originalFormula.add(clauseCount);

                    // if the clause has only one variable add to unit clause
                    if (varInClause == 1) {
                        unitVariables.add(new Integer(parse[0]));
                    }

                    // set the number of literals for this clause
                    // in the formula array
                    formula[clauseCount] = new int[varInClause];

                    // now fill the clause with the correct values
                    for (int i = 0; i < varInClause; i++) {
                        formula[clauseCount][i] = Integer.parseInt(parse[i]);
                    }
                    clauseCount++;
                }
            }// end of if statement
        }// end of while statement

        currentFormula.push(originalFormula);
        variablesAffected.add(new LinkedList<Integer>());

        hasEmptyClause = false;

        unitVariables.clear();
    }

    /**
     * This is where clauses are removed from the formula when a variable
     * assignment is made.  When a branch variable is selected, the
     * variable is put into the unitVariables set.  It will then
     * assign a value to that selected variable and begin to
     * remove clauses from the formula if they are satisfied, as
     * well as shorten any clauses that contain the negation of that
     * variable.  If any more unit variables are discovered, then they are
     * added to the unitVariables set.  If a contradiction is found, then
     * the assignment and unit propagation attempt is undone.
     */
    private void unitProp() {

        // start off with the current formula
        LinkedList<Integer> newFormula = currentFormula.peek();

        // keeps track of the variables assigned by unit propagation
        LinkedList<Integer> variablesSetbyUP = new LinkedList<Integer>();

        // The formula that will become the newFormula after one
        // round of unitPropagation is done
        LinkedList<Integer> curFormula = new LinkedList<Integer>();

        while (unitVariables.size() > 0) {

            // get the next unit variable and remove from list
            int unitVar = unitVariables.first();
            unitVariables.remove(unitVariables.first());

            // make sure we know unit propagation messed around
            // with this variable
            variablesSetbyUP.add(unitVar);

            // make sure variable is not assigned already to create
            // contradiction
            if (currentAssignment[abs(unitVar)] != UNASSIGNED) {
                hasEmptyClause = true;
                currentFormula.push(newFormula);
                variablesAffected.push(variablesSetbyUP);
                return;
            }

            // check if propagation made the formula empty out
            if (newFormula.size() == 0) {
                currentFormula.push(newFormula);
                variablesAffected.push(variablesSetbyUP);
                return;
            }

            //  assign the variable now
            if (unitVar < 0) {
                currentAssignment[-unitVar] = FALSE;
            } else {
                currentAssignment[unitVar] = TRUE;
            }

            for (Integer curClause : newFormula) {

                //Assume clause was not removed
                boolean clauseRemoved = false;

                // If all variables are assigned and it hasn't been removed
                // then the clause is empty.
                boolean allVariablesAssigned = true;

                // The size of the clause, not including false variables.
                int trueClauseSize = 0;

                // Check each variable in the clause
                for (int j = 0; j < formula[curClause].length; j++) {

                    int variableToCheck = formula[curClause][j];

                    // if the clause is satisfied
                    if ((variableToCheck > 0 && currentAssignment[variableToCheck] == TRUE)
                            || (variableToCheck < 0 && currentAssignment[-variableToCheck] == FALSE)) {

                        //remove the clause
                        clauseRemoved = true;
                        break;
                    } // Variable not satisfiable.
                    else if ((variableToCheck > 0 && currentAssignment[variableToCheck] == UNASSIGNED)
                            || (variableToCheck < 0 && currentAssignment[-variableToCheck] == UNASSIGNED)) {
                        trueClauseSize++;
                        allVariablesAssigned = false;
                    }
                }

                // empty clause?
                if (!clauseRemoved && allVariablesAssigned) {
                    hasEmptyClause = true;
                    currentFormula.push(newFormula);
                    variablesAffected.push(variablesSetbyUP);
                    return;
                }

                // Add to the new current formula
                if (!clauseRemoved) {
                    curFormula.add(curClause);

                    // check if clause has a unit variable and add to unitVars set
                    if (trueClauseSize == 1) {
                        for (int i = 0; i < formula[curClause].length; i++) {

                            if (currentAssignment[abs(formula[curClause][i])] == UNASSIGNED) {

                                // check if a contradiction appears in the set already
                                // i.e (1 is in the set and we are trying to add -1)
                                // if it does, we can tell right away that this path
                                // will not lead to a solution, end early
                                if (unitVariables.contains(-formula[curClause][i])) {
                                    hasEmptyClause = true;
                                    currentFormula.push(newFormula);
                                    variablesAffected.push(variablesSetbyUP);
                                    return;
                                } else {
                                    unitVariables.add(formula[curClause][i]);
                                    break;
                                }
                            }
                        }//end for
                    }
                }
            }// end of for loop
            newFormula = curFormula;
            curFormula = new LinkedList<Integer>();
        }// end of while loop

        // Propagation went well, save results
        hasEmptyClause = false;
        currentFormula.push(newFormula);
        variablesAffected.push(variablesSetbyUP);
    }

    /**
     * This function computes the ranks for the clauses using the
     * Jeroslow-Wang heuristic.
     * How do we know which variable to set an assignment for next?  Choose
     * the one with the highest rank depending on number of clauses the
     * variable appears in and how large those clauses are.
     */
    private void computeRanks() {
        resetRanks();
        double tempNewHighestRankValue = 0;

        int trueClauseSize;

        for (Integer clause : currentFormula.peek()) {

            trueClauseSize = 0;

            for (int i = 0; i < formula[clause].length; i++) {
                int varToCheck = formula[clause][i];
                if (currentAssignment[abs(varToCheck)] == UNASSIGNED) {
                    trueClauseSize++;
                }
            }

            // loop through the clause and update each rank for the variables
            for (int i = 0; i < formula[clause].length; i++) {

                // get the current variable that is in the clause at this index
                int tempVar = formula[clause][i];

                // test to see if the variable is negated or not
                if (tempVar > 0 && (currentAssignment[tempVar] == UNASSIGNED)) {

                    // add the new value to what exist
                    if (trueClauseSize < 10) {
                        variableRanks[2 * tempVar - 1] += POWERS_OF_TWO[trueClauseSize];
                    } else {
                        variableRanks[2 * tempVar - 1] += Math.pow(2, -trueClauseSize);
                    }
                } // negated variable, do the same as we did above
                else if (tempVar < 0 && currentAssignment[tempVar * -1] == UNASSIGNED) {

                    // add the new value to what exist
                    if (trueClauseSize < 10) {
                        variableRanks[2 * -tempVar] += POWERS_OF_TWO[trueClauseSize];
                    } else {
                        variableRanks[2 * -tempVar] += Math.pow(2, -trueClauseSize);
                    }
                }

                // get the new temp value for the variable's positive and
                // negated value
                if (tempVar > 0) {
                    tempNewHighestRankValue = variableRanks[2 * tempVar - 1]
                            + variableRanks[2 * tempVar];
                } else {
                    tempVar = -tempVar;
                    tempNewHighestRankValue = variableRanks[2 * (tempVar) - 1]
                            + variableRanks[2 * (tempVar)];
                }

                // test whether the new rank is the largest rank so far,
                // if so change the index and the value
                if (tempNewHighestRankValue > highestRankValue) {

                    // Now we need to check whether or not it is the
                    // x1 or -x1 variable that is the highest rank
                    if (variableRanks[2 * tempVar - 1] > variableRanks[2 * tempVar]) {
                        highestRank = tempVar;
                        highestRankValue = tempNewHighestRankValue;
                    } else {
                        highestRank = -tempVar;
                        highestRankValue = tempNewHighestRankValue;
                    }
                }
            }
        }
    }

    /**
     * Resets all of the variable ranks.
     */
    private void resetRanks() {

        // Index 0 is not used
        variableRanks[0] = -1;

        for (int i = 1; i < variableRanks.length; i++) {
            variableRanks[i] = 0;
        }

        // reset the highestRank, we will find the newest highestRank using
        // computeRank
        highestRank = -1;
        highestRankValue = -1;
    }

    /**
     * Gets the highest unassigned rank variable in the formula. This is used by
     * dpSolver to choose which variable to branch to.
     *
     * @return - The variable with the highest ranking variable.
     */
    public int getHighestRankVariable() {
        computeRanks();
        return highestRank;
    }

    /**
     * Checks to see if the current formula has an empty clause.
     *
     * @return - True if an empty clause exists False otherwise
     */
    public boolean hasEmptyClause() {

        return hasEmptyClause;
    }

    /**
     * This adds the variable that was chosen to unitVariables so unitProp can
     * propagate the formula.  This method essentially initiates setting a
     * value for a variable.
     *
     * @param Variable - the variable we want to propagate on,
     */
    public void setVariable(int var) {
        if (unitVariables.size() == 0) {
            unitVariables.add(var);
        }

        unitProp();
    }

    /**
     * This method undoes unit propagation by going back to the previous version of
     * the formula and unassigning all values to the variables that were set
     * during unit propagation
     *
     */
    public void undoProp() {
        if (currentFormula.size() > 1) {
            currentFormula.pop();
        }

        // unset propagation
        if (variablesAffected.size() > 1) {
            LinkedList<Integer> temp = variablesAffected.pop();
            for (Integer i : temp) {
                currentAssignment[abs(i)] = UNASSIGNED;
            }
        }

        // make sure you clear the set of unit variables when backtracking
        unitVariables.clear();
    }

    /**
     * If the current formula size is 0, all clauses have been satisfied
     * and removed.
     *
     * @return True if the formula is satisfied False otherwise
     */
    public boolean isEmpty() {
        return currentFormula.peek().size() == 0;
    }

    /**
     * Returns the absolute value of the parameter.
     *
     * @param num  - Number to get absolute value
     * @return Returns the absolute value of the parameter.
     */
    private int abs(int num) {
        if (num < 0) {
            return -num;
        }
        return num;
    }

    /**
     * Should only be used to get the final assigment when a solution is
     * found.
     * @return - The array containing the solution, null if no solution was
     *           found
     */
    public int[] getCurrentAssigment() {
        return currentAssignment;
    }

    /**
     * Prints the formula's solution.
     */
    @Override
    public String toString() {

        String temp = "";

        for (int i = 1; i < currentAssignment.length; i++) {
            temp += "Variable " + i + " Value " + currentAssignment[i] + "\n";
        }

        return temp;
    }
}
