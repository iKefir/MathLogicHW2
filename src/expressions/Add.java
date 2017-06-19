package expressions;

public class Add extends AbstractBinaryOperator{
    public Add(Expression firstExpression, Expression secondExpression) {super(firstExpression, secondExpression, "+");}

    @Override
    public Expression getCopy() {
        return new Add(firstExpression.getCopy(), secondExpression.getCopy());
    }
}
