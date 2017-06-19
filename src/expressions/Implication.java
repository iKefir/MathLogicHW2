package expressions;

import expressions.AbstractBinaryOperator;
import expressions.Expression;

public class Implication extends AbstractBinaryOperator {
    public Implication(Expression first, Expression second) {super(first, second, "->");}

    @Override
    public Expression getCopy() {
        return new Implication(firstExpression.getCopy(), secondExpression.getCopy());
    }
}