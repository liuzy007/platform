package com.pktech.oal.freemarker;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.apache.commons.lang.StringUtils;
import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;

import freemarker.cache.ClassTemplateLoader;
import freemarker.cache.MultiTemplateLoader;
import freemarker.cache.TemplateLoader;
import freemarker.template.Configuration;
import freemarker.template.ObjectWrapper;
import freemarker.template.Template;

/**
 * @see also org.springframework.ui.freemarker.FreeMarkerTemplateUtils
 * 
 * @author liwei
 * 
 */
public abstract class FreeMarkerUtil {

    public static String processPath(String templatePath, Object object) {
        return FreeMarkerSupport.getInstance().processPath(templatePath, object);
    }

    public static String processPath(Class<?> classPackageAsResourcePath, String fileName, Object object) {
        String templatePath = ClassUtils.classPackageAsResourcePath(classPackageAsResourcePath) + '/' + fileName;
        return processPath(templatePath, object);
    }

    public static String processString(String templateString, Object object) {
        return FreeMarkerSupport.getInstance().processString(templateString, object);
    }
}

class FreeMarkerSupport {

    private static final String FREEMARKER_PROPERTIES = "freemarker.properties";

    private static Configuration configuration;

    private FreeMarkerSupport() {
        configuration = new Configuration();

        ClassTemplateLoader classTemplateLoader = new ClassTemplateLoader(ClassTemplateLoader.class, "/");
        // FileTemplateLoader fileTemplateLoader = new FileTemplateLoader(new
        // File("fileDirectory"));
        // TemplateLoader[] loaders = new TemplateLoader[] {fileTemplateLoader,
        // classTemplateLoader};
        TemplateLoader[] loaders = new TemplateLoader[] { classTemplateLoader };
        configuration.setTemplateLoader(new MultiTemplateLoader(loaders));

        configuration.setObjectWrapper(ObjectWrapper.BEANS_WRAPPER);
        this.loadSettings(configuration);
    }

    private void loadSettings(freemarker.template.Configuration configuration) {
        URL fileUrl = null;
        InputStream in = null;
        try {
            fileUrl = FreeMarkerSupport.class.getClassLoader().getResource(FREEMARKER_PROPERTIES);

            in = fileUrl.openStream();

            if (null != in) {
                Properties p = new Properties();
                p.load(in);
                configuration.setSettings(p);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            fileUrl = null;
            in = null;
        }
    }

    private static class SingletonHelper {
        static FreeMarkerSupport instance = new FreeMarkerSupport();
    }

    public static FreeMarkerSupport getInstance() {
        return SingletonHelper.instance;
    }

    public String processTemplate(Template template, Object object) {
        StringWriter writer = new StringWriter();
        try {
            Assert.notNull(template, "Template not found.");
            template.process(object, writer);
        } catch (Exception e) {
            e.printStackTrace(new PrintWriter(writer));
        }
        return writer.toString();
    }

    public String processPath(String templatePath, Object object) {
        Template template = null;
        try {
            template = configuration.getTemplate(templatePath);
        } catch (IOException ioe) {
            throw new RuntimeException("failed to get template by (templatePath=" + templatePath + ")", ioe);
        }
        return this.processTemplate(template, object);
    }

    public String processString(String templateString, Object object) {
        Template template = null;
        Reader reader = null;
        try {
            reader = new StringReader(templateString);
            template = new Template(null, reader, FreeMarkerSupport.getConfiguration());
        } catch (IOException ioe) {
            throw new RuntimeException("failed to get template by (templateString=" + templateString + ")", ioe);
        } finally {
            reader = null;
        }
        return this.processTemplate(template, object);
    }

    public String removeBlankLines(String src) {
        if (null != src && !src.isEmpty()) {
            BufferedReader bufferedReader = new BufferedReader(new StringReader(src));
            StringBuilder sb = new StringBuilder();
            String inputLine;
            String lineSepartor = System.getProperty("line.separator");
            try {
                while ((inputLine = bufferedReader.readLine()) != null) {
                    if (inputLine.length() == 0 || StringUtils.isWhitespace(inputLine))
                        continue;
                    sb.append(inputLine).append(lineSepartor);
                }
            } catch (IOException ioe) {
                throw new RuntimeException("failed to read source string (src=" + src + ")", ioe);
            } finally {
                bufferedReader = null;
            }
            return sb.toString();
        }
        return src;
    }

    /**
     * @return the configuration
     */
    protected static Configuration getConfiguration() {
        return configuration;
    }

    public static void main(String[] args) throws Exception {
        Configuration configuration = new Configuration();
        configuration.setDirectoryForTemplateLoading(new File("templateDirectory"));// file
        // .
        // close
        // (
        // )
        // ;
        configuration.setObjectWrapper(ObjectWrapper.DEFAULT_WRAPPER);
        Template template = configuration.getTemplate("HelloWord.ftl");
        Map<String, Object> data = new HashMap<String, Object>();
        data.put("name", "my name");
        Writer out = new OutputStreamWriter(System.out);
        template.process(data, out);
        out.flush();
        out.close();
        out = null;
    }

}
