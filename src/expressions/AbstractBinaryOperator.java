package expressions;

public abstract class AbstractBinaryOperator implements Expression {

    public Expression firstExpression;
    public Expression secondExpression;
    private  String op;

    public AbstractBinaryOperator(Expression firstExpression, Expression secondExpression, String op) {
        this.firstExpression = firstExpression;
        this.secondExpression = secondExpression;
        this.op = op;
    }

    public String toString() {
        return (firstExpression.toString() + op + secondExpression.toString());
    }

}