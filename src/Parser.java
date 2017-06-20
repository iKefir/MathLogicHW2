import expressions.*;

import java.util.ArrayList;


class Parser {
    private String s;
    private int nom;

    Expression parse(String args) {
        s = args;
        nom = 0;
        return parseBinaryExpression(0);
    }

    private String parseToken() {
        while ((nom < s.length()) && ((s.charAt(nom) == ' ') || (s.charAt(nom) == '\t'))) ++nom;

        if (nom == s.length()) {
            return "";
        }
        if (Character.isLowerCase(s.charAt(nom))) {
            StringBuilder build = new StringBuilder();
            while (nom < s.length() && (Character.isLowerCase(s.charAt(nom)) || Character.isDigit(s.charAt(nom)))) {
                build.append(s.charAt(nom));
                nom++;
            }
            return build.toString();
        }
        if (Character.isUpperCase(s.charAt(nom))) {
            StringBuilder build = new StringBuilder();
            while (nom < s.length() && (Character.isUpperCase(s.charAt(nom)) || Character.isDigit(s.charAt(nom)))) {
                build.append(s.charAt(nom));
                nom++;
            }
            return build.toString();
        }
        if (s.charAt(nom) == '0') {
            ++nom;
            return s.substring(nom - 1, nom);
        }
        pair hpOps = new pair();
        hpOps.arr = new String[]{"->"};
        hpOps.l = new int[]{2};
        String[] ops = {"&", "|", "!", "(", ")", "+", "*", "'", "=", "@", "?", ","};
        String t;
        int tSdv = 0;
        if (nom + 2 <= s.length()) {
            t = s.substring(nom, nom + 2);
            for (int i = 0; i < hpOps.arr.length; i++) {
                if (t.equals(hpOps.arr[i])) tSdv = hpOps.l[i];
            }
        } else {
            t = s.substring(nom, nom + 1);
        }
        if (tSdv == 0) {
            t = t.substring(0, 1);
            for (String op : ops) {
                if (t.equals(op)) tSdv = 1;
            }
        }
        if (tSdv > 0) {
            t = s.substring(nom, nom + tSdv);
            nom += tSdv;
            return t;
        }

        return "";
    }

    private Expression parseSimpleExpression() {
        String token = parseToken();

        if (token.equals("(")) {
            Expression res = parseBinaryExpression(0);
            parseToken();
            String isIncrement = parseToken();
            while (isIncrement.equals("'")) {
                res = new Increment(res);
                isIncrement = parseToken();
            }
            if (!isIncrement.equals("'")) nom -= isIncrement.length();
            return res;
        }

        Expression arg;
        if (token.equals("!")) {
            return new Not(parseBinaryExpression(3));
        }

        if (token.equals("@") || token.equals("?")) {
            String str = parseToken();
            arg = new Variable(str);
            Expression nextArg = parseBinaryExpression(3);
            if (token.equals("@")) {
                return new Any(arg, nextArg);
            } else return new Exists(arg, nextArg);
        }

        if ((token.matches("^[A-Z0-9]+$") || token.matches("^[a-z0-9]+$")) && !token.equals("0")) {
            ArrayList<Expression> terms = new ArrayList<>();
            String someTok = parseToken();
            if (someTok.equals("(")) {
                Expression someExp = parseBinaryExpression(3);
                terms.add(someExp);
                while (parseToken().equals(",")) {
                    someExp = parseBinaryExpression(3);
                    terms.add(someExp);
                }
            } else {
                nom -= someTok.length();
            }
            if (token.matches("^[A-Z0-9]+$")) {
                return new Predicate(token, terms);
            } else {
                if (terms.size() != 0) {
                    return new Function(token, terms);
                }
            }
        }

        String isIncrement = parseToken();
        Expression incrementedMaximally = new Variable(token);
        while (isIncrement.equals("'")) {
            incrementedMaximally = new Increment(incrementedMaximally);
            isIncrement = parseToken();
        }
        if (!isIncrement.equals("'")) nom -= isIncrement.length();
        return incrementedMaximally;
    }

    private int expressionPower(String token) {
        int number = -1;
        if (token.equals("+")) {
            number = 5;
        }
        if (token.equals("*")) {
            number = 6;
        }
        if (token.equals("=")) {
            number = 4;
        }
        if (token.equals("&")) {
            number = 3;
        }
        if (token.equals("|")) {
            number = 2;
        }
        if (token.equals("->")) {
            number = 1;
        }
        return number;
    }

    private Expression expressionCreate(String token, Expression l, Expression r) {
        if (token.equals("+")) {
            return new Add(l, r);
        }
        if (token.equals("*")) {
            return new Multiply(l, r);
        }
        if (token.equals("=")) {
            return new Equatation(l, r);
        }
        if (token.equals("&")) {
            return new And(l, r);
        }
        if (token.equals("|")) {
            return new Or(l, r);
        }

        return new Implication(l, r);
    }

    private Expression parseBinaryExpression(int power) {
        Expression left = parseSimpleExpression();
        Expression right;
        while (true) {
            String token = parseToken();
            int tPower = expressionPower(token);
            if ((token.equals("->") && tPower < power) || (!token.equals("->") && tPower <= power)) {
                nom -= token.length();
                return left;
            }

            right = parseBinaryExpression(tPower);
            left = expressionCreate(token, left, right);
        }
    }

    private static class pair {
        String[] arr;
        int[] l;
    }
}
