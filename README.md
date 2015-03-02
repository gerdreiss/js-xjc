# js-xjc

Various JAXB2 XJC plugins to generate classes compliant with Sonar rules

#### Implement security requirements when generating classes: clone Date objects when using them as constructor params
<arg>-Xsecure-value-constructor</arg>

#### Change members visibility to private
<arg>-Xprivate-members</arg>

#### Use constants for string values
<arg>-Xstring-to-constant</arg>

#### Implement security requirements when generating classes: clone Date objects when setting and/or returning the respective property in getter/setter methods
<arg>-Xsecure-getter-setter</arg>

For example usage see example.txt
