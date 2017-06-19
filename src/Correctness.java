import expressions.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.stream.Collectors;

class Correctness {
    boolean any;
    boolean anyAssumptions;
    boolean exs;
    boolean exsAssumptions;
    boolean notFreeForSubst;

    boolean checkEqualStructure(Expression firstExp, Expression secondExp, HashMap<String, Expression> mapOfExpr) {

        if (firstExp == null || secondExp == null) return false;

        if (firstExp.getClass() == secondExp.getClass()) {
            if (firstExp instanceof AbstractBinaryOperator) {
                return checkEqualStructure(((AbstractBinaryOperator) firstExp).firstExpression,
                        ((AbstractBinaryOperator) secondExp).firstExpression, mapOfExpr) &&
                        checkEqualStructure(((AbstractBinaryOperator) firstExp).secondExpression,
                                ((AbstractBinaryOperator) secondExp).secondExpression, mapOfExpr);
            }

            if (firstExp instanceof Predicate) {
                if (((Predicate) firstExp).terms.size()
                        != ((Predicate) secondExp).terms.size() ||
                        !(((Predicate) firstExp).name.
                                equals(((Predicate) secondExp).name))) return false;
                boolean flag = true;
                for (int i = 0; i < ((Predicate) firstExp).terms.size(); i++) {
                    flag = checkEqualStructure(((Predicate) firstExp).terms.get(i),
                            ((Predicate) secondExp).terms.get(i), mapOfExpr);
                    if (!flag) break;
                }
                return flag;
            }

            if (firstExp instanceof Function) {
                if (((Function) firstExp).terms.size()
                        != ((Function) secondExp).terms.size() ||
                        !(((Function) firstExp).name.
                                equals(((Function) secondExp).name))) return false;
                boolean flag2 = true;
                for (int i = 0; i < ((Function) firstExp).terms.size(); i++) {
                    flag2 = checkEqualStructure(((Function) firstExp).terms.get(i),
                            ((Function) secondExp).terms.get(i), mapOfExpr);
                    if (!flag2) break;
                }
                return flag2;
            }

            if (firstExp instanceof AbstractUnaryOperator) {
                return checkEqualStructure(((AbstractUnaryOperator) firstExp).mainExpression,
                        ((AbstractUnaryOperator) secondExp).mainExpression, mapOfExpr)
                        && (((AbstractUnaryOperator) firstExp).helpExpression == null || checkEqualStructure(((AbstractUnaryOperator) firstExp).helpExpression,
                        ((AbstractUnaryOperator) secondExp).helpExpression, mapOfExpr));
            }

            if (firstExp instanceof Variable) {
                if (Character.isDigit(((Variable) firstExp).value.charAt(0))
                        && Character.isDigit(((Variable) secondExp).value.charAt(0))) {
                    return ((Variable) firstExp).value.equals(((Variable) secondExp).value);
                }
                if (((Variable) firstExp).value.equals(((Variable) secondExp).value)) {
                    if (!mapOfExpr.containsKey(((Variable) secondExp).value))
                        mapOfExpr.put(((Variable) firstExp).value, secondExp);
                    else return mapOfExpr.
                            get(((Variable) secondExp).value).toString().
                            equals(((Variable) firstExp).value);
                    return true;
                }

                String tmpName = ((Variable) secondExp).value;
                if (mapOfExpr.containsKey(tmpName)) {
                    return checkEqual(firstExp, mapOfExpr.get(tmpName));
                }
                mapOfExpr.put(tmpName, firstExp);
                return true;
            }

        }
        if (secondExp instanceof Variable) {
            String tmpName = ((Variable) secondExp).value;
            if (mapOfExpr.containsKey(tmpName)) {
                return checkEqual(firstExp, mapOfExpr.get(tmpName));
            }
            mapOfExpr.put(tmpName, firstExp);
            return true;
        }
        return false;
    }

    boolean checkEqual(Expression firstExp, Expression secondExp) {
        if (firstExp == null || secondExp == null) return false;
        if (firstExp.getClass() == secondExp.getClass()) {
            if (firstExp instanceof Variable)
                return ((Variable) firstExp).value.equals(((Variable) secondExp).value);
            if (firstExp instanceof AbstractBinaryOperator) {
                return checkEqual(((AbstractBinaryOperator) firstExp).firstExpression,
                        ((AbstractBinaryOperator) secondExp).firstExpression)
                        && checkEqual(((AbstractBinaryOperator) firstExp).secondExpression,
                        ((AbstractBinaryOperator) secondExp).secondExpression);
            }
            if (firstExp instanceof AbstractUnaryOperator) {
                if (firstExp instanceof Exists)
                    return checkEqual(((Exists) firstExp).mainExpression,
                            ((Exists) secondExp).mainExpression) &&
                            checkEqual(((Exists) firstExp).helpExpression,
                                    ((Exists) secondExp).helpExpression);
                if (firstExp instanceof Any)
                    return checkEqual(((Any) firstExp).mainExpression,
                            ((Any) secondExp).mainExpression) &&
                            checkEqual(((Any) firstExp).helpExpression,
                                    ((Any) secondExp).helpExpression);
                return checkEqual(((AbstractUnaryOperator) firstExp).mainExpression,
                        ((AbstractUnaryOperator) secondExp).mainExpression);
            }
            if (firstExp instanceof Predicate) {
                return ((Predicate) firstExp).name.
                        equals(((Predicate) secondExp).name) &&
                        isEqualsLists(((Predicate) firstExp).terms,
                                ((Predicate) secondExp).terms);
            }
            if (firstExp instanceof Function) {
                return ((Function) firstExp).name.
                        equals(((Function) secondExp).name) &&
                        isEqualsLists(((Function) firstExp).terms,
                                ((Function) secondExp).terms);
            }
        }
        return false;
    }

    private boolean isEqualsLists(ArrayList<Expression> firstList, ArrayList<Expression> secondList) {

        if (firstList.size() != secondList.size()) {
            return false;
        }

        for (int i = 0; i < firstList.size(); i++) {
            if (!(firstList.get(i).toString().
                    equals(secondList.get(i).toString()))) {
                return false;
            }
        }

        return true;
    }

    int checkAxioms(Expression e, Expression[] expressions) {
        HashMap<String, Expression> mapForAxioms;

        //first 10 axioms
        for (int i = 1; i < expressions.length; i++) {
            mapForAxioms = new HashMap<>();
            if (checkEqualStructure(e, expressions[i], mapForAxioms)) {
                return i;
            }
        }

        //11 axiom
        mapForAxioms = new HashMap<>();
        if (e instanceof Implication && ((Implication) e).firstExpression instanceof Any
                && checkEqualStructure(((Implication) e).secondExpression,
                ((Any) ((Implication) e).firstExpression).helpExpression, mapForAxioms)) {
            if (isFreeForSubst(((Implication) e).firstExpression,
                    ((Variable) ((Any) ((Implication) e).firstExpression).mainExpression),
                    mapForAxioms.get(((Variable) ((Any) ((Implication) e).firstExpression).mainExpression).value))) {
                Expression tmpExpr = e.getCopy();
                Expression tmp = substitution(((Implication) e).firstExpression,
                        ((Variable) ((Any) ((Implication) e).firstExpression).mainExpression),
                        mapForAxioms.get(((Variable) ((Any) ((Implication) e).firstExpression).mainExpression).value));
                e = tmpExpr;
                if (checkEqual((((Implication) e).secondExpression), ((Any) tmp).helpExpression)) {
                    Expression exp = mapForAxioms.get(((Variable) ((Any) ((Implication) e).firstExpression).mainExpression).value);
                    if (checkEqual(exp, ((Any) ((Implication) e).firstExpression).mainExpression) ||
                            checkQuant(((Any) ((Implication) e).firstExpression).helpExpression,
                                    ((Variable) ((Any) ((Implication) e).firstExpression).mainExpression),
                                    false, false, exp)) {
                        return 11;
                    } else {
                        notFreeForSubst = true;
                        checkEqualStructure(((Implication) e).secondExpression,
                                ((Any) ((Implication) e).firstExpression).helpExpression, mapForAxioms);
                    }
                }
            } else {
                notFreeForSubst = true;
                checkEqualStructure(((Implication) e).secondExpression,
                        ((Any) ((Implication) e).firstExpression).helpExpression, mapForAxioms);
            }
        }

        //12 axiom
        mapForAxioms = new HashMap<>();
        if (e instanceof Implication && ((Implication) e).secondExpression instanceof Exists
                && checkEqualStructure(((Implication) e).firstExpression,
                ((Exists) ((Implication) e).secondExpression).helpExpression, mapForAxioms)) {
            if (isFreeForSubst(((Implication) e).secondExpression,
                    ((Variable) ((Exists) ((Implication) e).secondExpression).mainExpression),
                    mapForAxioms.get(((Variable) ((Exists) ((Implication) e).secondExpression).mainExpression).value))) {
                Expression tmpExpr = e.getCopy();
                Expression tmp = substitution(((Implication) e).secondExpression,
                        ((Variable) ((Exists) ((Implication) e).secondExpression).mainExpression),
                        mapForAxioms.get(((Variable) ((Exists) ((Implication) e).secondExpression).mainExpression).value));
                e = tmpExpr;
                if (checkEqual((((Implication) e).firstExpression), ((Exists) tmp).helpExpression)) {
                    Expression exp = mapForAxioms.get(((Variable) ((Exists) ((Implication) e).secondExpression).mainExpression).value);
                    if (checkEqual(exp, ((Exists) ((Implication) e).secondExpression).mainExpression) ||
                            checkQuant(((Exists) ((Implication) e).secondExpression).helpExpression,
                                    ((Variable) ((Exists) ((Implication) e).secondExpression).mainExpression),
                                    false, false, exp)) {
                        return 12;
                    } else {
                        notFreeForSubst = true;
                    }
                }
            } else {
                notFreeForSubst = true;
            }
        }

        return -1;
    }

    int checkAxiomsFA(Expression e, Expression[] exprs) {
        for (int j = 1; j < exprs.length; j++) {
            if (checkEqual(exprs[j], e)) {
                return j;
            }
        }
        return -1;
    }

    boolean checkLastAxiom(Expression e) {
        if (e instanceof Implication && ((Implication) e).firstExpression instanceof And &&
                ((And) ((Implication) e).firstExpression).secondExpression instanceof Any) {
            HashMap<String, Expression> mapOfExp = new HashMap<>();
            if (checkEqualStructure(((And) ((Implication) e).firstExpression).firstExpression,
                    ((Implication) e).secondExpression, mapOfExp)) {
                Variable v = ((Variable) ((Any) ((And) ((Implication) e).firstExpression).secondExpression).mainExpression);
                if (mapOfExp.get(v.value) instanceof Variable && Character.isDigit(((Variable) mapOfExp.get(v.value)).value.charAt(0))) {
                    Expression tmpExpr = e.getCopy();
                    Expression subs = substitution(((Implication) tmpExpr).secondExpression,
                            v, mapOfExp.get(v.value));
                    if (checkEqual(subs, ((And) ((Implication) tmpExpr).firstExpression).firstExpression)) {
                        mapOfExp.clear();
                        Expression tmp = ((Any) ((And) ((Implication) tmpExpr).firstExpression).
                                secondExpression).helpExpression;
                        if (tmp instanceof Implication &&
                                checkEqualStructure(((Implication) tmp).secondExpression,
                                        ((Implication) tmp).firstExpression, mapOfExp)) {
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }

    private boolean isFreeForSubst(Expression forS, Variable var, Expression sub) {
        HashSet<String> freeVars = getFreeVars(sub, new HashSet<>());
        return isRecFree(((AbstractUnaryOperator) forS).helpExpression, var, freeVars, true);
    }

    private boolean isRecFree(Expression forS, Variable var, HashSet<String> freeVars, boolean noQuantOnFreeVar) {
        if (forS instanceof AbstractBinaryOperator) {
            return isRecFree(((AbstractBinaryOperator) forS).firstExpression, var, freeVars, noQuantOnFreeVar)
                    && isRecFree(((AbstractBinaryOperator) forS).secondExpression, var, freeVars, noQuantOnFreeVar);
        }
        if (forS instanceof AbstractUnaryOperator) {
            if (((AbstractUnaryOperator) forS).helpExpression != null) {
                Expression tmp = ((AbstractUnaryOperator) forS).mainExpression;
                String tmpString = tmp.toString();
                if (var.toString().equals(tmpString)) {
                    return noQuantOnFreeVar;
                }
                if (freeVars.contains(((Variable) ((AbstractUnaryOperator) forS).mainExpression).value)) {
                    return isRecFree(((AbstractUnaryOperator) forS).helpExpression, var, freeVars, false);
                }
            }
            return isRecFree(((AbstractUnaryOperator) forS).mainExpression, var, freeVars, noQuantOnFreeVar);
        }
        if (forS instanceof Predicate) {
            boolean result = true;
            for (Expression term : ((Predicate) forS).terms) {
                result &= isRecFree(term, var, freeVars, noQuantOnFreeVar);
            }
            return result;
        }
        if (forS instanceof Function) {
            boolean result = true;
            for (Expression term : ((Function) forS).terms) {
                result &= isRecFree(term, var, freeVars, noQuantOnFreeVar);
            }
            return result;
        }
        if (forS instanceof Variable) {
            if (((Variable) forS).value.equals(var.value)) {
                return noQuantOnFreeVar;
            }
        }
        return true;
    }

    private HashSet<String> getFreeVars(Expression exp, HashSet<String> quantors) {
        if (exp instanceof AbstractBinaryOperator) {
            HashSet<String> result = getFreeVars(((AbstractBinaryOperator) exp).firstExpression, new HashSet<>(quantors));
            result.addAll(getFreeVars(((AbstractBinaryOperator) exp).secondExpression, new HashSet<>(quantors)));
            return result;
        }
        if (exp instanceof AbstractUnaryOperator) {
            if (((AbstractUnaryOperator) exp).helpExpression != null) {
                quantors.add(((Variable) ((AbstractUnaryOperator) exp).mainExpression).value);
                return getFreeVars(((AbstractUnaryOperator) exp).helpExpression, quantors);
            }
            return getFreeVars(((AbstractUnaryOperator) exp).mainExpression, quantors);
        }
        if (exp instanceof Predicate) {
            HashSet<String> result = new HashSet<>();
            for (Expression term : ((Predicate) exp).terms) {
                result.addAll(getFreeVars(term, new HashSet<>(quantors)));
            }
            return result;
        }
        if (exp instanceof Function) {
            HashSet<String> result = new HashSet<>();
            for (Expression term : ((Function) exp).terms) {
                result.addAll(getFreeVars(term, new HashSet<>(quantors)));
            }
            return result;
        }
        if (exp instanceof Variable) {
            if (!quantors.contains(((Variable) exp).value)) {
                HashSet<String> result = new HashSet<>();
                result.add(((Variable) exp).value);
                return result;
            }
        }
        return new HashSet<>();
    }

    private boolean checkQuant(Expression e1, Variable v1, boolean bool1, boolean bool2, Expression e2) {
        if (e1 instanceof Variable && checkEqual(e1, v1)) {
            return bool1 || !bool2;
        }
        if (e1 instanceof AbstractBinaryOperator)
            return checkQuant(((AbstractBinaryOperator) e1).firstExpression, v1, bool1, bool2, e2)
                    && checkQuant(((AbstractBinaryOperator) e1).secondExpression,
                    v1, bool1, bool2, e2);
        if (e1 instanceof AbstractUnaryOperator) {
            if (((AbstractUnaryOperator) e1).helpExpression != null) {
                if (checkEqual(((AbstractUnaryOperator) e1).mainExpression, e2)) {
                    return checkQuant(((AbstractUnaryOperator) e1).helpExpression, v1, bool1, true, e2);
                }
                if (checkEqual(((AbstractUnaryOperator) e1).mainExpression, v1)) {
                    return true;
                }
            } else {
                return (checkQuant(((AbstractUnaryOperator) e1).mainExpression,
                        v1, bool1, bool2, e2));
            }
        }
        if (e1 instanceof Predicate)
            return (!((Predicate) e1).terms.contains(v1)) || bool1 || !bool2;
        return !(e1 instanceof Function) ||
                ((Function) e1).terms.contains(v1) && !bool1 && bool2;
    }

    private Expression substitution(Expression forS, Variable var, Expression sub) {
        if (forS instanceof AbstractBinaryOperator) {
            ((AbstractBinaryOperator) forS).firstExpression =
                    substitution(((AbstractBinaryOperator) forS).firstExpression, var, sub);
            ((AbstractBinaryOperator) forS).secondExpression =
                    substitution(((AbstractBinaryOperator) forS).secondExpression, var, sub);
            return forS;
        }
        if (forS instanceof AbstractUnaryOperator) {
            if (((AbstractUnaryOperator) forS).helpExpression != null) {
                checkEqual(((AbstractUnaryOperator) forS).mainExpression, sub);
                ((AbstractUnaryOperator) forS).helpExpression
                        = substitution(((AbstractUnaryOperator) forS).helpExpression, var, sub);
                return forS;
            }
            ((AbstractUnaryOperator) forS).mainExpression
                    = substitution(((AbstractUnaryOperator) forS).mainExpression, var, sub);
            return forS;
        }
        if (forS instanceof Predicate) {
            ((Predicate) forS).terms = ((Predicate) forS).terms.stream()
                    .map(e -> substitution(e, var, sub))
                    .collect(Collectors.toCollection(ArrayList::new));
        }
        if (forS instanceof Function) {
            ArrayList<Expression> new_vars = new ArrayList<>();
            for (int i = 0; i < ((Function) forS).terms.size(); i++) {
                new_vars.add(substitution(((Function) forS).terms.get(i), var, sub));
            }
            ((Function) forS).terms = new_vars;
        }
        if (forS instanceof Variable)
            if (checkEqual(forS, var))
                return sub;

        return forS;
    }

    int checkingAny(Expression e, ArrayList<Expression> arrayOfExpr, ArrayList<Expression> assumptions) {
        int flag = -1;
        if (!(e instanceof Implication)) return flag;
        for (int i = arrayOfExpr.size() - 1; i >= 0; i--) {
            Expression e1 = arrayOfExpr.get(i);
            if (e1 instanceof Implication && checkEqual(((Implication) e).firstExpression, ((Implication) e1).firstExpression)) {
                if (((Implication) e).secondExpression instanceof Any && checkEqual(((Implication) e1).secondExpression,
                        ((Any) ((Implication) e).secondExpression).helpExpression)) {
                    any = true;
                    if (checkForFreeInstances((Variable) (((Any) ((Implication) e).secondExpression).mainExpression), ((Implication) e).firstExpression, false)) {
                        if (!assumptions.isEmpty()) {
                            if (!checkForFreeInstances((Variable) (((Any) ((Implication) e).secondExpression).mainExpression),
                                    assumptions.get(assumptions.size() - 1), false)) {
                                anyAssumptions = true;
                                return -1;
                            }
                        }
                        flag = i;
                        break;
                    }
                }
            }
        }
        return flag;
    }

    int checkingExists(Expression e, ArrayList<Expression> arrayOfExpr, ArrayList<Expression> assumptions) {
        int flag = -1;
        if (!(e instanceof Implication)) return flag;
        for (int i = arrayOfExpr.size() - 1; i >= 0; i--) {
            Expression e1 = arrayOfExpr.get(i);
            if (e1 instanceof Implication &&
                    checkEqual(((Implication) e).secondExpression, ((Implication) e1).secondExpression)) {
                if (((Implication) e).firstExpression instanceof Exists &&
                        checkEqual(((Implication) e1).firstExpression,
                                ((Exists) ((Implication) e).firstExpression).helpExpression)) {
                    exs = true;
                    if (checkForFreeInstances((Variable) (((Exists) ((Implication) e).firstExpression).mainExpression), ((Implication) e).secondExpression, false)) {
                        if (!assumptions.isEmpty()) {
                            if (!checkForFreeInstances((Variable) (((Exists) ((Implication) e).firstExpression).mainExpression),
                                    assumptions.get(assumptions.size() - 1), false)) {
                                exsAssumptions = true;
                                return -1;
                            }
                        }
                        flag = i;
                        return flag;
                    }
                }
            }
        }
        return flag;
    }

    private boolean checkForFreeInstances(Variable x, Expression expression, boolean quant) {
        if (expression instanceof Variable && checkEqual(expression, x)) {
            return quant;
        }
        if (expression instanceof AbstractBinaryOperator)
            return checkForFreeInstances(x, ((AbstractBinaryOperator) expression).firstExpression, quant)
                    && checkForFreeInstances(x, ((AbstractBinaryOperator) expression).secondExpression, quant);
        if (expression instanceof AbstractUnaryOperator) {
            if (((AbstractUnaryOperator) expression).helpExpression != null) {
                return checkEqual(((AbstractUnaryOperator) expression).mainExpression, x) ||
                        checkForFreeInstances(x, ((AbstractUnaryOperator) expression).helpExpression, quant);
            }
            return (checkForFreeInstances(x, ((AbstractUnaryOperator) expression).mainExpression, quant));
        }
        if (expression instanceof Predicate) {
            boolean flag = false;
            for (int i = 0; i < ((Predicate) expression).terms.size(); i++) {
                if (!(checkForFreeInstances(x, ((Predicate) expression).terms.get(i), quant))) {
                    flag = true;
                    break;
                }
            }
            return !flag || quant;
        }
        if (expression instanceof Function) {
            boolean flag = false;
            for (int i = 0; i < ((Function) expression).terms.size(); i++) {
                if (!(checkForFreeInstances(x,
                        ((Function) expression).terms.get(i), quant))) {
                    flag = true;
                    break;
                }
            }
            return !flag || quant;
        }
        return true;
    }

}
