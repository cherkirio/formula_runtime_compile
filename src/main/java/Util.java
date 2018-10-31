import javax.tools.JavaCompiler;
import javax.tools.ToolProvider;
import java.io.File;

/**
 * Created by kirio on 31.10.2018.
 */
public interface Util {

    // use javax.tools.JavaCompiler for compiling formulaImpl class


    static int compile(File javaFile) {

        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        return compiler.run(null, null, null, javaFile.getPath());


    }
}
