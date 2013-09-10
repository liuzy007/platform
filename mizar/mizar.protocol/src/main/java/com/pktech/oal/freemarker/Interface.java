package com.pktech.oal.freemarker;

import java.util.ArrayList;
import java.util.List;

public class Interface {
    private String interfaceName;
    private List<Method> methods;

    public String getInterfaceName() {
        return interfaceName;
    }

    public void setInterfaceName(String interfaceName) {
        this.interfaceName = interfaceName;
    }

    public List<Method> getMethods() {
        return methods;
    }

    public void setMethods(List<Method> methods) {
        this.methods = methods;
    }

    public static void main(String[] args) {
        Interface test = new Interface();
        test.setInterfaceName("InterfaceA");
        
        
        List<Method> methods = new ArrayList<Method>();
        test.setMethods(methods);
        
        Method method1 = new Method();
        method1.setName("say");
        method1.setReturnType("String");
        
        List<Parameter> parameters = new ArrayList<Parameter>();
        method1.setParameters(parameters);
        
        parameters.add(new Parameter("String","value1"));
        parameters.add(new Parameter("String","value2"));
        
        methods.add(method1);
        
        String outClassFile = FreeMarkerUtil.processPath(Interface.class, "generateInterfaceTemplate.ftl", test);

        System.out.println(outClassFile);
        
        
    }

}
