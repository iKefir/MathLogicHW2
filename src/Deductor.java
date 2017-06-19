import expressions.Any;
import expressions.Exists;
import expressions.Expression;
import expressions.Implication;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Scanner;

class Deductor {

    private Parser parser = new Parser();
    private Correctness correctness = new Correctness();

    ArrayList<String> deduct(String alpha, ArrayList<Expression> assumptions, ArrayList<String> proof, Expression[] axioms, Expression[] axiomsFA) throws IOException {

        ArrayList<Expression> listOfExpressions = new ArrayList<>();

        ArrayList<String> result = new ArrayList<>();
        
        for (String curr : proof) {


            Expression currExp = parser.parse(curr);
            Expression alphaExp = parser.parse(alpha);
            Expression tmp = parser.parse(curr);

            if (correctness.checkEqual(currExp, alphaExp)) {
                listOfExpressions.add(tmp);
                String g = "(" + alpha + ")->(" + alpha + ")->(" + alpha + ")";
                result.add(g);
                result.add("(" + g + ")->((" + alpha + ")->(((" + alpha + ")->(" +
                        alpha + "))->(" + alpha + ")))->((" + alpha + ")->(" + alpha + "))");
                result.add("((" + alpha + ")->(((" + alpha + ")->(" + alpha + "))->(" +
                        alpha + ")))->((" + alpha + ")->(" + alpha + "))");
                result.add("((" + alpha + ")->(((" + alpha + ")->(" +
                        alpha + "))->(" + alpha + ")))");
                result.add("(" + alpha + ")->(" + alpha + ")");
                continue;
            }
            boolean f = false;

            for (Expression assumption : assumptions)
                if (assumptions.get(0) != null && correctness.checkEqual(assumption, currExp)) {
                    result.add(curr);
                    listOfExpressions.add(assumption);
                    String str2 = "(" + curr + ")->(" + alpha + ")->(" + curr + ")";
                    result.add(str2);
                    result.add("(" + alpha + ")->(" + curr + ")");
                    f = true;
                    break;
                }
            if (f) {
                continue;
            }

            int i1 = correctness.checkAxioms(currExp, axioms);
            int i2 = correctness.checkAxiomsFA(currExp, axiomsFA);
            if (i1 > 0 || i2 > 0 || correctness.checkLastAxiom(currExp)) {
                listOfExpressions.add(tmp);
                result.add(curr);
                String str2 = "(" + curr + ")->(" + alpha + ")->(" + curr + ")";
                result.add(str2);
                result.add("(" + alpha + ")->(" + curr + ")");
                continue;
            }

            if (correctness.checkingAny(currExp, listOfExpressions, assumptions) >= 0) {
                ArrayList<String> ar = new ArrayList<>();
                ar.add(alpha);
                ar.add(((Implication) currExp).firstExpression.toString());
                ar.add(((Any) ((Implication) currExp).secondExpression).helpExpression.toString());
                ar.add(((Any) ((Implication) currExp).secondExpression).mainExpression.toString());
                ArrayList<String> proofs = replaceFromRules(
                        Paths.get("deductions/anyDeduction.txt"), ar);
                listOfExpressions.add(tmp);
                result.addAll(new ArrayList<>(proofs));
                continue;
            }

            currExp = tmp.getCopy();

            if (correctness.checkingExists(currExp, listOfExpressions, assumptions) >= 0) {
                ArrayList<String> ar = new ArrayList<>();
                ar.add(alpha);
                ar.add(((Exists) ((Implication) currExp).firstExpression).helpExpression.toString());
                ar.add((((Implication) currExp).secondExpression).toString());
                ar.add(((Exists) ((Implication) currExp).firstExpression).mainExpression.toString());
                ArrayList<String> proofs = replaceFromRules(Paths.
                        get("deductions/existsDeduction.txt"), ar);
                listOfExpressions.add(tmp);
                result.addAll(new ArrayList<>(proofs));
                continue;
            }

            currExp = tmp.getCopy();

            int[] MP = MathLogicHW2.checkMP(currExp, listOfExpressions);
            if (MP[0] > -1) {
                listOfExpressions.add(tmp);
                result.add("((" + alpha + ")->(" + proof.get(MP[0]) + "))->(((" + alpha + ")->((" +
                        proof.get(MP[0]) + ")->(" + curr + ")))->((" + alpha + ")->(" + curr + ")))");
                result.add("(((" + alpha + ")->((" + proof.get(MP[0]) + ")->(" + curr + ")))->((" +
                        alpha + ")->(" + curr + ")))");
                result.add("(" + alpha + ")->(" + curr + ")");
                continue;
            }
            listOfExpressions.add(tmp);
            result.add(curr);

        }

        return result;
    }

    private ArrayList<String> replaceFromRules(Path path, ArrayList<String> names) {
        ArrayList<String> arrayList = new ArrayList<>();
        try (Scanner in = new Scanner(new File(path.toString()))) {
            while (in.hasNext()) {
                String str = in.next();
                str = str.replace("B", "#_B");
                str = str.replace("C", "#_C");
                str = str.replace("x", "#_x");
                str = str.replace("A", "(" + (names.get(0)) + ")");
                if (names.size() > 1) {
                    str = str.replace("#_B", "(" + names.get(1) + ")");
                }
                if (names.size() > 2) {
                    str = str.replace("#_C", "(" + names.get(2) + ")");
                }
                if (names.size() > 3) {
                    str = str.replace("#_x", names.get(3));
                }
                arrayList.add(str);
            }

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return arrayList;
    }
}
