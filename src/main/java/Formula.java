import java.util.Objects;

public abstract class Formula {
    private static int formulaHash(String formula) {
        return formula == null ? 0 : formula.hashCode();
    }


    private final int hash;

    boolean isSame(String formulaStr) {

        return formulaHash(formulaStr) == hash && Objects.equals(formulaStr, stringFormula());
    }


    public Formula() {
        hash = formulaHash(stringFormula());
    }


    public abstract Number calculate(Number... args);


    public abstract String stringFormula();


}
