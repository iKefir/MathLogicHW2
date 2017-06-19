package expressions;

public class Equatation extends AbstractBinaryOperator{
    public Equatation(Expression firstExpression, Expression secondExpression) {super(firstExpression, secondExpression, "=");}

    @Override
    public Expression getCopy() {
        return new Equatation(firstExpression.getCopy(), secondExpression.getCopy());
    }
}
