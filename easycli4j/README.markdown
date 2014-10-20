Chicory CLI
===========

Chicory CLI is a command line parsing library.

Chicory CLI is part of Chicory.

Licence
-------

Chicory CLI is provided by means of BSD-3 Clause Licence.

Usage
-----

### Maven ###

    <dependencies>

        <!-- ... -->

        <dependency>
            <groupId>com.github.sviperll</groupId>
            <artifactId>easycli4j</artifactId>
            <version>0.17</version>
        </dependency>

        <!-- ... -->

    </dependencies>

### Gradle ###

    dependencies {

        // ...

        compile(group: 'com.github.sviperll', name: 'easycli4j', version: '0.17')

        // ...

    }

Example
-------

Here is a full Hello world example

    class Hello {

        public static void main(String[] args) {
            Hello hello = new Hello();
            CLISpecification cli = new CLISpecification(System.out);
            cli.add('n', "name", "Your name", CLIHandlers.string(hello.name));
            cli.add("prefix", "Prefix for your name, i. e. Mr, Mrs, Dr, Sir", CLIHandlers.string(hello.prefix));
            try {
                cli.run(args);
                application.run();
            } catch (CLIException ex) {
                System.out.println(ex.getMessage());
                System.out.println("usage: hello [OPTIONS]");
                try {
                    cli.usage();
                } catch (IOException ex1) {
                    ex1.printStackTrace(System.out);
                }
            } catch (IOException ex) {
                ex.printStackTrace(System.out);
            }
        }

        private Property<String> name = new Property<String>(null);
        private Property<String> prefix = new Property<String>("Mr.");

        private void run() {
            System.out.println("Hello " + prefix.get() + " " + name.get());
        }

    }

When you compile it and run like

    java -jar hello.jar -n John --prefix=Sir.

You'll get the following

    Hello Sir. John

You can get usage-message like this:

    java -jar hello.jar -h

You'll get the following answer

    Unknown option -h
    usage: hello [OPTIONS]
                     -n OPTION      Your name
                    --name=OPTION   Your name
                    --prefix=OPTION Prefix for your name, i. e. Mr, Mrs, Dr, Sir (default Mr.)
