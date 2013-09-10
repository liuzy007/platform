package com.pktech.oal.compile;

import javax.tools.FileObject;
import javax.tools.ForwardingJavaFileManager;
import javax.tools.JavaFileObject;
import javax.tools.StandardJavaFileManager;

@SuppressWarnings("rawtypes")
public class ClassFileManager extends ForwardingJavaFileManager {

    private JavaClassObject jclassObject;

    public JavaClassObject getJavaClassObject() {
        return jclassObject;
    }

    @SuppressWarnings("unchecked")
    public ClassFileManager(StandardJavaFileManager standardManager) {
        super(standardManager);
    }

    public JavaFileObject getJavaFileForOutput(Location location, String className, JavaFileObject.Kind kind, FileObject sibling) {
        jclassObject = new JavaClassObject(className, kind);
        return jclassObject;
    }

}
