package expressions;

public class Multiply extends AbstractBinaryOperator{
    public Multiply(Expression firstExpression, Expression secondExpression) {super(firstExpression, secondExpression, "*");}

    @Override
    public Expression getCopy() {
        return new Multiply(firstExpression.getCopy(), secondExpression.getCopy());
    }
}
