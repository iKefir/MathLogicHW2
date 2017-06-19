package expressions;

public class Or extends AbstractBinaryOperator {
    public Or(Expression first, Expression second) {super(first, second, "|");}

    @Override
    public Expression getCopy() {
        return new Or(firstExpression.getCopy(), secondExpression.getCopy());
    }
}