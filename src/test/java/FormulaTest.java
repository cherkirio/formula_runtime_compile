import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class FormulaTest {

    private Main main;

    @Before
    public void init() {
        main = new Main();
    }

    @Test
    public void test() throws Exception {

        String formulStr = "1 + 2 + 3 - arg0";


        Assert.assertEquals(0, main.calculate(formulStr, 6).intValue());
        Assert.assertEquals(2, main.calculate(formulStr, 4).intValue());


        Number val = Math.cos(100) * 10000 + 90;
        formulStr = "Math.cos(arg0) * arg100 - arg2000 ";

        Assert.assertEquals(val.intValue(), main.calculate(formulStr, 100, 10000, -90).intValue());

        formulStr = " arg0 / 0 ";

        Assert.assertTrue(Double.isInfinite(main.calculate(formulStr, 1000).doubleValue()));
        formulStr = " arg0/arg1 *   0 ";

        Assert.assertTrue(Double.isNaN(main.calculate(formulStr, 1000, 0).doubleValue()));
        formulStr = " arg0 - arg1 - arg1*2 ";

        Assert.assertEquals(25, main.calculate(formulStr, 10, -5).intValue());
    }

}
