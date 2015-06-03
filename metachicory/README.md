metachicory: Metaprogramming tools
=====================================

Metaprogramming tools. Useful for annotation processors implementation.

Part of [chicory](https://github.com/sviperll/chicory).

Installation
------------

Use maven dependency:

```xml
    <dependency>
        <groupId>com.github.sviperll</groupId>
        <artifactId>metachicory</artifactId>
        <version>0.26</version>
    </dependency>
```

You can use latest unstable version instead:

```xml
    <dependency>
        <groupId>com.github.sviperll</groupId>
        <artifactId>metachicory</artifactId>
        <version>0.27-rc2</version>
    </dependency>
```

Changelog
---------

 * Since 0.25

    - Full support for annotations as parameters to other annotations
      when converting from javax.lang.model to jcodemodel.

 * Since 0.22

    - Change `@Visitor` retention to compile-time

 * Since 0.21

    - Move JCodeModelJavaxLangModelAdapter-related classes to com.github.sviperll.meta.java.model package
    - Add support to jcodemodel's error-types to JCodeModelJavaxLangModelAdapter

Usage
-----

See [javadoc](http://sviperll.github.io/chicory/metachicory/apidocs/index.html)
