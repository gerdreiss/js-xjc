package com.jscriptive.jaxb2.xjc.plugin;

import com.sun.codemodel.JFieldVar;
import com.sun.codemodel.JMod;
import com.sun.tools.xjc.Options;
import com.sun.tools.xjc.Plugin;
import com.sun.tools.xjc.outline.ClassOutline;
import com.sun.tools.xjc.outline.Outline;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;

public class PrivateMembersPlugin extends Plugin {

    @Override
    public String getOptionName() {
        return "Xprivate-members";
    }

    @Override
    public String getUsage() {
        return "  -Xprivate-members    : Change members visibility to private";
    }

    @Override
    public boolean run(Outline model, Options opt, ErrorHandler errorHandler) throws SAXException {
        for (ClassOutline co : model.getClasses()) {
            for (JFieldVar field : co.implClass.fields().values()) {
                // never do anything with serialVersionUID if it exists.
                if (isSerialVersionUID(field)) {
                    continue;
                }
                // only try to change members that are not static and not private
                if (isNotStatic(field) && isNotPrivate(field)) {
                    field.mods().setPrivate();
                }
            }
        }
        return true;
    }

    private boolean isSerialVersionUID(JFieldVar field) {
        return "serialVersionUID".equalsIgnoreCase(field.name());
    }

    private boolean isNotStatic(JFieldVar field) {
        return (field.mods().getValue() & JMod.STATIC) == 0;
    }

    private boolean isNotPrivate(JFieldVar field) {
        return (field.mods().getValue() & JMod.PRIVATE) == 0;
    }

}
