package expressions;

import java.util.HashMap;

public class Not extends AbstractUnaryOperator {
    public Not(Expression expression) {super(expression, "!");}

    @Override
    public Expression getCopy() {
        return new Not(mainExpression.getCopy());
    }
}
