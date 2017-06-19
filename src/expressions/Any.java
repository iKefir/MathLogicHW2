package expressions;

public class Any extends AbstractUnaryOperator{
    public Any(Expression firstExpression, Expression helpExpression) {super(firstExpression, helpExpression, "@");}

    @Override
    public Expression getCopy() {
        return new Any(mainExpression.getCopy(), helpExpression.getCopy());
    }
}
