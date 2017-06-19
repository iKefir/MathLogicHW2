package expressions;

import java.util.HashMap;

public class Variable implements Expression {
    public String value;

    public Variable(String value) {this.value = value;}

    public String toString() {
        return value;
    }

    @Override
    public Expression getCopy() {
        return new Variable(value);
    }
}
