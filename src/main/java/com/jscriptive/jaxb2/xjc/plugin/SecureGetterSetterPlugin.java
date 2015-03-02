package com.jscriptive.jaxb2.xjc.plugin;

import com.sun.codemodel.*;
import com.sun.tools.xjc.Options;
import com.sun.tools.xjc.Plugin;
import com.sun.tools.xjc.outline.ClassOutline;
import com.sun.tools.xjc.outline.Outline;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;

import java.util.Date;
import java.util.Map;

public class SecureGetterSetterPlugin extends Plugin {

    private static final String PARAM_TYPE_VOID = "void";
    private static final String PARAM_NAME_VALUE = "value";
    private static final String METHOD_NAME_CLONE = "clone";
    private static final String METHOD_NAME_PREFIX_SET = "set";
    private static final String METHOD_NAME_PREFIX_GET = "get";


    @Override
    public String getOptionName() {
        return "Xsecure-getter-setter";
    }

    @Override
    public String getUsage() {
        return "  -Xsecure-getter-setter    : implement security requirements when generating classes: clone Date objects when setting and/or returning the respective property in getter/setter methods";
    }

    @Override
    public boolean run(Outline outline, Options opt, ErrorHandler errorHandler) throws SAXException {
        for (ClassOutline co : outline.getClasses()) {
            Map<String, JFieldVar> fields = co.implClass.fields();
            for (JFieldVar field : fields.values()) {
                String fieldTypeName = field.type().name();
                if (Date.class.getSimpleName().equals(fieldTypeName)) {
                    replaceGetterMethod(co, field);
                    replaceSetterMethod(outline, co, field);
                }
            }
        }
        return true;
    }

    private void replaceGetterMethod(ClassOutline co, JFieldVar field) {

        //Find and remove Old Getter!
        String getter = METHOD_NAME_PREFIX_GET + capitalize(field.name());
        JMethod oldGetter = co.implClass.getMethod(getter, new JType[0]);

        if (co.implClass.methods().remove(oldGetter)) {

            //Create New Getter
            JMethod getterMethod = co.implClass.method(JMod.PUBLIC, field.type(), getter);
            getterMethod.javadoc().add("Gets the value of the " + field.name() + " property.");

            // Create Getter Body -> {if (f = null) return null; return f.clone();}
            getterMethod.body()._if(JExpr.refthis(field.name()).eq(JExpr._null()))._then()._return(JExpr._null());
            getterMethod.body()._return(JExpr.cast(field.type(), JExpr.refthis(field.name()).invoke(METHOD_NAME_CLONE)));
        }
    }

    private void replaceSetterMethod(Outline outline, ClassOutline co, JFieldVar field) {

        // Find and remove Old Setter!
        String setter = METHOD_NAME_PREFIX_SET + capitalize(field.name());
        JMethod oldSetter = co.implClass.getMethod(setter, new JType[]{field.type()});

        if (co.implClass.methods().remove(oldSetter)) {

            // Create New Setter
            JMethod setterMethod = co.implClass.method(JMod.PUBLIC, JType.parse(outline.getCodeModel(), PARAM_TYPE_VOID), setter);
            setterMethod.javadoc().add("Sets the value of the " + field.name() + " property.");
            setterMethod.param(field.type(), PARAM_NAME_VALUE);

            // Create Setter Body -> {if (p = null) f = null; else f = p.clone();}
            JConditional condition = setterMethod.body()._if(JExpr.ref(PARAM_NAME_VALUE).eq(JExpr._null()));
            condition._then().assign(JExpr.refthis(field.name()), JExpr._null());
            condition._else().assign(JExpr.refthis(field.name()), JExpr.cast(field.type(), JExpr.ref(PARAM_NAME_VALUE).invoke(METHOD_NAME_CLONE)));
        }
    }

    private String capitalize(String name) {
        String firstChar = String.valueOf(name.charAt(0));
        String upperFirstChar = firstChar.toUpperCase();
        return name.replaceFirst(firstChar, upperFirstChar);
    }
}
