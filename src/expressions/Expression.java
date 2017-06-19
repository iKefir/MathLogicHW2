package expressions;

import java.util.HashMap;

public interface Expression {
    String toString();

    Expression getCopy();
}
