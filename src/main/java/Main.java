import com.google.common.base.Strings;
import com.google.common.io.ByteStreams;
import com.google.common.primitives.Doubles;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Main {


    public static void main(String... args) throws Exception {
        if (args.length == 0) {
            // test
            args = new String[]{"cos(arg0) + pow(arg1,2) + arg0 * 2", "10", "-5"};
        }
        String formula = args[0];
        Number[] params = new Number[args.length - 1];
        for (int i = 1; i < args.length; ++i) {
            Double param = Doubles.tryParse(Strings.nullToEmpty(args[i]).trim());
            if (param == null) {
                LOG.error("Cant convert to double {}", args[i]);
                return;
            }
            params[i - 1] = param;
        }

        Main main = new Main();
        LOG.info("Result: " + main.calculate(formula, params));
    }


    private static Logger LOG = LoggerFactory.getLogger(Main.class);
    private static String ROOT_DIR = "java_gen";
    private static String FORMULA_IMPL_CLASS_NAME = "FormulaImpl";


    private volatile Formula formula;
    private final File root;

    public Main() {

        root = new File(ROOT_DIR);
        root.mkdirs();
        LOG.debug("Root dir is {}", root.getAbsolutePath());

        try {
            if (Files.exists(Paths.get(root.getAbsolutePath(), FORMULA_IMPL_CLASS_NAME + ".class"))) {
                LOG.debug("Restore previous formula from file {}", Paths.get(root.getAbsolutePath(), FORMULA_IMPL_CLASS_NAME + ".class"));
                formula = createFormulaInstance();
                LOG.debug("Previous formula loaded.\nFORMULA: {}\n", formula.stringFormula());
            }
        } catch (Exception ignore) {
            LOG.warn("Failed to restore previous formula: {}", ignore.getMessage());
        }
    }


    public Number calculate(String formulaStr, Number... args) throws Exception {


        if (formula == null || !formula.isSame(formulaStr)) { //double check lock
            synchronized (this) {
                if (formula == null || !formula.isSame(formulaStr)) {

                    if (formula != null) {
                        LOG.info("Formula changed ->\nFrom:\n{}\nTo:\n{}", formula.stringFormula(), formulaStr);
                    }

                    formula = createFormula(formulaStr);
                }
            }

        }

        return formula.calculate(args);

    }


    private Formula createFormula(String formulaStr) throws Exception {

        LOG.debug("Create new formula: {}", formulaStr);

        String formulaTemplateData = getFormulaTemplateData();
        formulaTemplateData = formulaTemplateData.replaceAll("%FORMULA%", formulaStr);
        formulaTemplateData = formulaTemplateData.replace("%ARGS_INIT%", defineVariables(formulaStr));
        File file = new File(root, FORMULA_IMPL_CLASS_NAME + ".java");
        try (FileWriter writer = new FileWriter(file)) {
            writer.write(formulaTemplateData);
        }
        if (Util.compile(file) != 0) {
            LOG.error("Cant compile formula file:\n{}", formulaTemplateData);

        }
        return createFormulaInstance();

    }


    private Formula createFormulaInstance() throws Exception {
        try {
            URLClassLoader classLoader = URLClassLoader.newInstance(new URL[]{root.toURI().toURL()});
            Class<?> cls = Class.forName(FORMULA_IMPL_CLASS_NAME, true, classLoader);
            return (Formula) cls.newInstance();
        } catch (Exception ex) {
            throw new Exception("Cant create formula instance", ex);
        }
    }

    private String getFormulaTemplateData() throws IOException {
        try (InputStream resource = Main.class.getResourceAsStream(FORMULA_IMPL_CLASS_NAME + ".template")) {

            ByteArrayOutputStream buffer = new ByteArrayOutputStream();
            ByteStreams.copy(resource, buffer);
            return buffer.toString();

        }
    }


    private String defineVariables(String formula) {

        // using variables from formula
        // construct variable initialise block that using function parameters
        // arg0 - arg20 + arg10 ->
        // double arg0 = args[0].doubleValue();
        // double arg10 = args[1].doubleValue();
        // double arg20 = args[2].doubleValue();
        Pattern pattern = Pattern.compile("arg(\\d+)");


        TreeSet<Integer> argsIndexes = new TreeSet<>();
        Matcher matcher = pattern.matcher(formula);
        while (matcher.find()) {
            argsIndexes.add(Integer.parseInt(matcher.group(1)));
        }

        StringBuilder sb = new StringBuilder();
        int i = 0;
        sb.append("\n");
        for (Integer index : argsIndexes) {
            sb.append("\t\tdouble arg").append(index).append(" = args[").append(i++).append("].doubleValue();\n");

        }
        return sb.toString();

    }


}
