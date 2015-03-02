Chicory: Core Libraries for Java
=====================================

This is an umbrella project that unifies several libraries.
All libraries provides some basic most generic functionality.

This package is used to share generic code between more specialized projects.

All provided libraries tend to have as simple implementation as possible,
basically some wrapping above Java SE.

Child projects
--------------

 * [chicory-core](https://github.com/sviperll/chicory/tree/master/chicory-core)

   Basic utilities to compensate shortcomings of Java SE.

 * [chicory-stream](https://github.com/sviperll/chicory/tree/master/chicory-stream)

   Stream processing like Java 8 streams, but works with Java 6 and have slightly different desing.

    - Chicory-stream provides sequential-only streams, no parallel support.
    - Chicory-stream allows easy forking and reuse of streams, unlike Java 8 where stream can be used only once.
    - Chicory-stream automatically and transparently opens and closes resources each time stream is used,
      unlike Java 8 where stream should be manually closed.

 * [chicory-time](https://github.com/sviperll/chicory/tree/master/chicory-time)

   Simple facade for Java SE date-time classes to provide functionality close to Joda-time.

 * [chicory-text](https://github.com/sviperll/chicory/tree/master/chicory-text)

   Rudimentary tools needed for text handling.

 * [metachicory](https://github.com/sviperll/chicory/tree/master/metachicory)

   Metaprogramming tools. Useful for annotation processors implementation.

 * [chicory-environment](https://github.com/sviperll/chicory/tree/master/metachicory)

   Not portable part of chicory. This package should be as small as possible.
   Classes used for talking to operating system.


License
-------

Chicory is under BSD 3-clause license.

Flattr
------

[![Flattr this git repo](http://api.flattr.com/button/flattr-badge-large.png)](https://flattr.com/submit/auto?user_id=sviperll&url=https%3A%2F%2Fgithub.com%2Fsviperll%2Fchicory&title=chicory&language=Java&tags=github&category=software)

Changelog
---------

Since 0.18

 * Transform text-formats package into chicory-text.
 * Rename packages to use more generic naming with less specific names.

Since 0.17

 * Introduce metachicory and text-formats packages
 * Move easycli4j, repository4j and multitasking4j to independent projects
