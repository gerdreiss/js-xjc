package com.jscriptive.jaxb2.xjc.plugin;

import com.sun.codemodel.*;
import com.sun.tools.xjc.Options;
import com.sun.tools.xjc.Plugin;
import com.sun.tools.xjc.outline.ClassOutline;
import com.sun.tools.xjc.outline.Outline;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;

import javax.xml.bind.annotation.XmlSchemaType;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

public class StringValueToConstantPlugin extends Plugin {

    private static final String ANNOTATION_MEMBER_NAME = "name";

    @Override
    public String getOptionName() {
        return "Xstring-to-constant";
    }

    @Override
    public String getUsage() {
        return "  -Xstring-to-constant    : Use constants for string values";
    }

    @Override
    public boolean run(Outline model, Options opt, ErrorHandler errorHandler) throws SAXException {
        for (ClassOutline co : model.getClasses()) {
            List<JFieldVar> fields = new ArrayList<JFieldVar>(co.implClass.fields().values());
            for (JFieldVar field : fields) {
                // never do anything with serialVersionUID if it exists.
                if (isSerialVersionUID(field)) {
                    continue;
                }
                for (JAnnotationUse annotation : field.annotations()) {
                    // if annotation is @XmlSchemaType, replace it's String value by a constant
                    if (isXmlSchemaTypeAnnotation(annotation)) {
                        JAnnotationValue xmlSchemaTypeNameValue = annotation.getAnnotationMembers().get(ANNOTATION_MEMBER_NAME);
                        if (xmlSchemaTypeNameValue == null) {
                            continue;
                        }
                        String xmlSchemaTypeName = introspectAnnotationValue(xmlSchemaTypeNameValue);
                        if (xmlSchemaTypeName == null) {
                            continue;
                        }
                        String constantFieldName = "XML_SCHEMA_TYPE_" + xmlSchemaTypeName.toUpperCase();
                        if (hasNoConstantWithName(co.implClass, constantFieldName)) {
                            co.implClass.field(getConstantMods(), String.class, constantFieldName, JExpr.lit(xmlSchemaTypeName));
                        }
                        annotation.param(ANNOTATION_MEMBER_NAME, JExpr.ref(constantFieldName));
                    }
                }
            }
        }
        return true;
    }

    private boolean isSerialVersionUID(JFieldVar field) {
        return "serialVersionUID".equalsIgnoreCase(field.name());
    }

    private boolean isXmlSchemaTypeAnnotation(JAnnotationUse annotation) {
        return XmlSchemaType.class.getSimpleName().equals(annotation.getAnnotationClass().name());
    }

    private String introspectAnnotationValue(JAnnotationValue annotationValue) {
        String value = null;
        Class<? extends JAnnotationValue> annotationValueClass = annotationValue.getClass();
        // Use name of the class as a String because the class is not public
        if ("JAnnotationStringValue".equals(annotationValueClass.getSimpleName())) {
            try {
                Field annotationValueClassField = annotationValueClass.getDeclaredField("value");
                annotationValueClassField.setAccessible(true);
                Object annotationValueClassFieldValue = annotationValueClassField.get(annotationValue);
                Class<?> annotationValueClassFieldValueClass = annotationValueClassFieldValue.getClass();
                if (JStringLiteral.class.equals(annotationValueClassFieldValueClass)) {
                    value = ((JStringLiteral) annotationValueClassFieldValue).str;
                }
            } catch (Exception ignored) {
            }
        }
        return value;
    }

    private boolean hasNoConstantWithName(JDefinedClass implClass, String name) {
        return implClass.fields().get(name) == null;
    }

    public int getConstantMods() {
        return JMod.PRIVATE | JMod.STATIC | JMod.FINAL;
    }
}
