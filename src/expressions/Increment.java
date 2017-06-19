package expressions;

public class Increment extends AbstractUnaryOperator {
    public Increment(Expression expression) {super(expression, "'");}

    public String toString() {
        return mainExpression.toString() + "'";
    }

    @Override
    public Expression getCopy() {
        return new Increment(mainExpression.getCopy());
    }
}
