package com.pktech.oal.compile;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.List;

import javax.tools.Diagnostic;
import javax.tools.DiagnosticCollector;
import javax.tools.JavaCompiler;
import javax.tools.JavaFileObject;
import javax.tools.ToolProvider;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.pktech.oal.util.CommonUtil;
import com.sun.tools.javac.Main;

//http://www.oschina.net/code/snippet_220184_8607
public class DynamicEngine {

    private static final transient Log log = LogFactory.getLog(DynamicEngine.class);

    private static DynamicEngine ourInstance = new DynamicEngine();

    public static DynamicEngine getInstance() {
        return ourInstance;
    }

    private URLClassLoader parentClassLoader;
    private String classpath;

    private DynamicEngine() {
        this.parentClassLoader = (URLClassLoader) this.getClass().getClassLoader();
        this.buildClassPath();
    }

    private void buildClassPath() {
        this.classpath = null;
        StringBuilder sb = new StringBuilder();
        for (URL url : this.parentClassLoader.getURLs()) {
            String p = url.getFile();
            sb.append(p).append(File.pathSeparator);
        }
        this.classpath = sb.toString();
    }

    public Object javaCodeToObject(String fullClassName, String javaCode) throws IllegalAccessException, InstantiationException {

        Object instance = null;
        Class<?> clazz = javaCodeToClass(fullClassName, javaCode);
        if (null != clazz) {
            instance = clazz.newInstance();
        }
        return instance;
    }

    public Class<?> javaCodeToClass(String fullClassName, String javaCode) {
        long start = System.currentTimeMillis();
        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        DiagnosticCollector<JavaFileObject> diagnostics = new DiagnosticCollector<JavaFileObject>();
        ClassFileManager fileManager = new ClassFileManager(compiler.getStandardFileManager(diagnostics, null, null));
        List<JavaFileObject> jfiles = new ArrayList<JavaFileObject>();
        jfiles.add(new CharSequenceJavaFileObject(fullClassName, javaCode));
        List<String> options = new ArrayList<String>();
        options.add("-encoding");
        options.add("UTF-8");
        options.add("-classpath");
        options.add(this.classpath);
        options.add("-d");
        options.add(System.getProperty("user.dir"));

        JavaCompiler.CompilationTask task = compiler.getTask(null, fileManager, diagnostics, options, null, jfiles);
        boolean success = task.call();
        if (success) {
            JavaClassObject jco = fileManager.getJavaClassObject();
            DynamicUrlClassLoader dynamicClassLoader = new DynamicUrlClassLoader(this.parentClassLoader);
            Class<?> clazz = dynamicClassLoader.loadClass(fullClassName, jco);
            return clazz;
        } else {
            String error = "";
            for (Diagnostic<?> diagnostic : diagnostics.getDiagnostics()) {
                error = error + compilePrint(diagnostic);
            }
            log.error(error);
        }
        long end = System.currentTimeMillis();
        log.info("javaCodeToObject use:" + (end - start) + "ms");
        return null;

    }

    private String compilePrint(Diagnostic<?> diagnostic) {
        StringBuffer res = new StringBuffer();
        res.append("Code:[" + diagnostic.getCode() + "]\n");
        res.append("Kind:[" + diagnostic.getKind() + "]\n");
        res.append("Position:[" + diagnostic.getPosition() + "]\n");
        res.append("Start Position:[" + diagnostic.getStartPosition() + "]\n");
        res.append("End Position:[" + diagnostic.getEndPosition() + "]\n");
        res.append("Source:[" + diagnostic.getSource() + "]\n");
        res.append("Message:[" + diagnostic.getMessage(null) + "]\n");
        res.append("LineNumber:[" + diagnostic.getLineNumber() + "]\n");
        res.append("ColumnNumber:[" + diagnostic.getColumnNumber() + "]\n");
        return res.toString();
    }

    public Class<?> compile(String className, String filePath, String fileContent) {
        Class<?> clazz = null;
        String simpleClassName = CommonUtil.getSimpleClassName(className);
        String fullFilePath = null;
        if (null == CommonUtil.noSimpleClassName(className)) {
            fullFilePath = CommonUtil.classPathParser(filePath);
        } else {
            fullFilePath = CommonUtil.classPathParser(filePath) + CommonUtil.classNameParser(CommonUtil.noSimpleClassName(className)) + "/";
        }

        File dirfile = new File(fullFilePath);
        if (!dirfile.isDirectory()) {
            dirfile.mkdirs();
        }
        File f = new File(fullFilePath + simpleClassName + ".java");
        PrintWriter pw = null;
        try {
            pw = new PrintWriter(f);
            pw.write(fileContent);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        pw.close();

        String[] commadline = { "-d", CommonUtil.classPathParser(filePath), "-cp", CommonUtil.classPathParser(filePath), "-encoding", "utf-8", f.getAbsolutePath() };
        int status = Main.compile(commadline);
        if (status != 0) {
            log.error("compile  fileContent failed!!!!!");
            return null;
        }
        try {
            clazz = Class.forName(className);
        } catch (ClassNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return clazz;
    }

    public static void main(String[] args) throws Exception {

        String srcString = "package test;\n public interface DynaClass {\n" + " String kkkk(String s);\n" + "}";
        DynamicEngine.getInstance().compile("test.DynaClass", "D:\\tool\\repository\\git\\platform\\mizar\\mizar.protocol\\target\\classes", srcString);

    }

}
