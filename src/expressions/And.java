package expressions;

import expressions.Expression;

public class And extends AbstractBinaryOperator {
    public And(Expression first, Expression second) {super(first, second, "&");}

    @Override
    public Expression getCopy() {
        return new And(firstExpression.getCopy(), secondExpression.getCopy());
    }
}
