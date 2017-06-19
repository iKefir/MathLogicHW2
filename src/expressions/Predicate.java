package expressions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.function.BooleanSupplier;

public class Predicate implements Expression {
    public String name;
    public ArrayList<Expression> terms;
    public Predicate(String name, ArrayList<Expression> terms) {
        this.name = name;
        this.terms = terms;
    }

    public String toString() {
        String finalAns = name;
        if (!terms.isEmpty()) {
            finalAns += "(";
            for (int i = 0; i < terms.size() - 1; ++i) {
                finalAns += terms.get(i).toString() + ",";
            }
            finalAns += terms.get(terms.size() - 1) + ")";
        }
        return finalAns;
    }

    @Override
    public Expression getCopy() {
        ArrayList<Expression> newList = new ArrayList<>();
        for (Expression term : terms) {
            newList.add(term.getCopy());
        }
        return new Predicate(name, newList);
    }
}
