package expressions;

import java.util.HashMap;

public abstract class AbstractUnaryOperator implements Expression {
    public Expression mainExpression;
    public Expression helpExpression;
    private String op;

    public AbstractUnaryOperator(Expression mainExpression, String op) {
        this.mainExpression = mainExpression;
        this.helpExpression = null;
        this.op = op;
    }

    public AbstractUnaryOperator(Expression mainExpression, Expression helpExpression, String op) {
        this.mainExpression = mainExpression;
        this.helpExpression = helpExpression;
        this.op = op;
    }

    public String toString() {
        String result;
        if (helpExpression != null) {
            result = op + mainExpression.toString() + "(" + helpExpression.toString() + ")";
        } else {
            result = op + "(" + mainExpression.toString() + ")";
        }
        return result;
    }
}
