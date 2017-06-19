import expressions.*;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class MathLogicHW2 {

    private static Correctness correctness;

    private static final ArrayList<Expression> listOfExpressions = new ArrayList<>();
    private static final ArrayList<String> strings = new ArrayList<>();
    private static final ArrayList<Expression> assumptions = new ArrayList<>();

    private static HashMap<String, Expression> mapOfExpression = new HashMap<>();

    private static final Expression[] axioms = new Expression[11];
    private static final Expression[] axiomsFA = new Expression[9];

    private static String alpha;
    private static String deductedHeader;

    private static Parser parser;

    private static int position = 1;

    private static void putAxioms() {
        axioms[1] = parser.parse("a->b->a");
        axioms[2] = parser.parse("(a->b)->(a->b->c)->(a->c)");
        axioms[3] = parser.parse("a->b->a&b");
        axioms[4] = parser.parse("a&b->a");
        axioms[5] = parser.parse("a&b->b");
        axioms[6] = parser.parse("a->a|b");
        axioms[7] = parser.parse("b->a|b");
        axioms[8] = parser.parse("(a->c)->(b->c)->(a|b->c)");
        axioms[9] = parser.parse("(a->b)->(a->!b)->!a");
        axioms[10] = parser.parse("!!a->a");

        axiomsFA[1] = parser.parse("a=b->a'=b'");
        axiomsFA[2] = parser.parse("(a=b)->(a=c)->(b=c)");
        axiomsFA[3] = parser.parse("a'=b'->a=b");
        axiomsFA[4] = parser.parse("!a'=0");
        axiomsFA[5] = parser.parse("a+b'=(a+b)'");
        axiomsFA[6] = parser.parse("a+0=a");
        axiomsFA[7] = parser.parse("a*0=0");
        axiomsFA[8] = parser.parse("a*b'=a*b+a");
    }

    private static void initialSetup() {
        position = 1;
        listOfExpressions.clear();
        strings.clear();
        assumptions.clear();
        mapOfExpression.clear();
        correctness = new Correctness();
        alpha = "";

        parser = new Parser();

        putAxioms();
    }

    private static String getErrorMessage(Expression e) {

        if (correctness.any && correctness.anyAssumptions) {
            return (": используется правило с квантором по переменной " +
                    ((Variable) ((Any) ((Implication) e).secondExpression).mainExpression).value + " входящей свободно в допущение " +
                    assumptions.get(assumptions.size() - 1));
        } else if (correctness.any) {
            return (": переменная " +
                    ((Variable) ((Any) ((Implication) e).secondExpression).mainExpression).value +
                    " входит свободно в формулу " +
                    (((Implication) e).firstExpression));
        }

        if (correctness.exs && correctness.exsAssumptions) {
            return (": используется правило с квантором по переменной " +
                    ((Variable) ((Exists) ((Implication) e).firstExpression).mainExpression).value + " входящей свободно в допущение " +
                    assumptions.get(assumptions.size() - 1));
        } else if (correctness.exs) {
            return (": переменная " +
                    ((Variable) ((Exists) ((Implication) e).firstExpression).mainExpression).value +
                    " входит свободно в формулу "
                    + (((Implication) e).secondExpression));
        }

        if (correctness.notFreeForSubst) {
            Expression e1 = null, e2 = null;
            if (e instanceof Implication && ((Implication) e).firstExpression instanceof Any) {
                correctness.checkEqualStructure(((Implication) e).secondExpression, ((Any) ((Implication) e).firstExpression).
                        helpExpression, mapOfExpression);
                e2 = ((Any) ((Implication) e).firstExpression).mainExpression;
                e1 = mapOfExpression.
                        get(((Variable) e2).value);
                e = ((Any) ((Implication) e).firstExpression).helpExpression;
            } else if (e instanceof Implication &&
                    ((Implication) e).firstExpression instanceof Exists) {
                correctness.checkEqualStructure(((Implication) e).firstExpression,
                        ((Exists) ((Implication) e).secondExpression).helpExpression,
                        mapOfExpression);
                e1 = mapOfExpression.
                        get(((Variable) ((Exists) ((Implication) e).secondExpression).mainExpression).value);
                e2 = ((Exists) ((Implication) e).secondExpression).mainExpression;
                e = ((Exists) ((Implication) e).secondExpression).helpExpression;
            }

            return (": терм " + e1 +
                    " не свободен для подстановки в формулу " + e.toString() +
                    " вместо переменной " + e2);
        }
        return "";
    }

    static int[] checkMP(Expression e, ArrayList<Expression> listOfExpressions) {
        int first = -1, second = -1;
        for (int i = listOfExpressions.size() - 1; i >= 0; i--) {
            Expression e1 = listOfExpressions.get(i);
            if (e1 instanceof Implication && correctness.checkEqual(e, ((Implication) e1).secondExpression)) {
                for (int j = listOfExpressions.size() - 1; j >= 0; j--) {
                    if (correctness.checkEqual(((Implication) e1).firstExpression, listOfExpressions.get(j))) {
                        second = i;
                        first = j;
                        break;
                    }
                }
                if (first > -1) break;
            }
        }
        return new int[]{first, second};
    }

    private static HashMap<String, Integer> getAssumptions(String header) {
        HashMap<String, Integer> result = new HashMap<>();
        StringBuilder builder = new StringBuilder();
        String[] twoParts = header.split("\\|-");
        twoParts[0] = twoParts[0].replace(" ", "");
        if (twoParts.length > 0) {
            List<String> data = new ArrayList<>();
            int balance = 0;
            int firstInd = 0;
            for (int i = 0; i < twoParts[0].length(); ++i) {
                if (twoParts[0].charAt(i) == '(') {
                    ++balance;
                }
                if (twoParts[0].charAt(i) == ')') {
                    --balance;
                    if (balance < 0) {
                        balance = 0;
                    }
                }
                if (twoParts[0].charAt(i) == ',' && balance == 0) {
                    data.add(twoParts[0].substring(firstInd, i));
                    if (data.size() != 1) {
                        builder.append(",");
                    }
                    builder.append(data.get(data.size() - 1));
                    firstInd = i + 1;
                }
            }
            String toAdd = twoParts[0].substring(firstInd, twoParts[0].length());
            if (!toAdd.equals("")) {
                data.add(toAdd);
            }
            for (int i = 0; i < data.size(); i++) {
                assumptions.add(parser.parse(data.get(i)));
                result.put(assumptions.get(assumptions.size() - 1).toString(), i + 1);
            }
            if (data.size() != 0) {
                alpha = data.get(data.size() - 1);
                builder.append("|-(").append(alpha).append(")->").append(twoParts[1]);
                deductedHeader = builder.toString();
            } else  {
                deductedHeader = header;
            }
        }
        return result;
    }

    public static void main(String[] args) {

        Path inputFile;
        Path outputFile;
        if (args.length == 2) {
            inputFile = Paths.get(args[0]);
            outputFile = Paths.get(args[1]);
        } else {
            inputFile = Paths.get("test.in");
            outputFile = Paths.get("test.out");
        }

        initialSetup();
        long time = System.currentTimeMillis();

        try (BufferedReader reader = Files.newBufferedReader(inputFile);
             BufferedWriter writer = Files.newBufferedWriter(outputFile)) {
            List<String> result = new ArrayList<>();

            String header = reader.readLine();
            result.add(header);
            header = header.replaceAll("\\s", "");

            HashMap<String, Integer> allAssumptions = getAssumptions(header);

            String currStr = reader.readLine();
            while (currStr != null) {
                currStr = currStr.replaceAll("\\s", "");
                result.add(currStr);
                strings.add(currStr);

                if (allAssumptions.containsKey(currStr)) {
                    int tmpAssumption = allAssumptions.get(currStr);
                    position++;
                    listOfExpressions.add(assumptions.get(tmpAssumption - 1));
                    currStr = reader.readLine();
                    continue;
                }
                Expression e;
                e = parser.parse(currStr);
                correctness.notFreeForSubst = false;
                boolean tmpFlag = false;
                for (Expression assumption : assumptions)
                    if (assumptions.get(0) != null &&
                            correctness.checkEqual(assumption, e)) {
                        position++;
                        listOfExpressions.add(assumption);
                        tmpFlag = true;
                        break;
                    }
                if (tmpFlag) {
                    currStr = reader.readLine();
                    continue;
                }

                Expression tmpExpr = parser.parse(currStr);

                int i1 = correctness.checkAxioms(e, axioms);
                int i2 = correctness.checkAxiomsFA(e, axiomsFA);
                if (i1 > 0 || i2 > 0) {
                    position++;
                    listOfExpressions.add(tmpExpr);
                    currStr = reader.readLine();
                    continue;
                }

                if (correctness.checkLastAxiom(e)) {
                    position++;
                    listOfExpressions.add(tmpExpr);
                    currStr = reader.readLine();
                    continue;
                }

                correctness.any = false;
                correctness.anyAssumptions = false;
                int j1 = correctness.checkingAny(e, listOfExpressions, assumptions);

                e = tmpExpr.getCopy();

                correctness.exs = false;
                correctness.exsAssumptions = false;
                int j2 = correctness.checkingExists(e, listOfExpressions, assumptions);
                if (j1 > -1 || j2 > -1) {
                    position++;
                    listOfExpressions.add(e);
                    currStr = reader.readLine();
                    continue;
                }

                e = tmpExpr.getCopy();

                int[] MP = checkMP(e, listOfExpressions);
                if (MP[0] > -1) {
                    position++;
                    listOfExpressions.add(e);
                    currStr = reader.readLine();
                    continue;
                }
                result.clear();
                result.add(header);
                String errorString = "(" + position + ") " + currStr +
                        " Вывод некорректен, начиная с формулы №" + position++;
                errorString += getErrorMessage(e);
                result.add(errorString);

                System.out.println("----TIME : " + (System.currentTimeMillis() - time) + "ms");

                for (String line : result) {
                    writer.write(line + "\n");
                }
                return;

            }
            if (!alpha.equals("")) {
                result.clear();
                result.add(deductedHeader);
                Deductor deductor = new Deductor();
                result.addAll(deductor.deduct(alpha, assumptions, strings, axioms, axiomsFA));
            }
            for (String line : result) {
                writer.write(line + "\n");
            }
            System.out.println("----TIME : " + (System.currentTimeMillis() - time) + "ms");

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
