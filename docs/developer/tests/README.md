Mutation testing
===============

To measure the quality of our tests, we are using the mutation testing
 framework [Pitest](http://pitest.org/).

Pitest runs your unit tests against automatically modified versions of your application code.
When the application code changes, it should produce different results and cause the unit
tests to fail. If a unit test does not fail in this situation, it may indicate an issue
with the test suite.

To run a mutation test, you have to add the Pitest plugin to build/plugins in your moduls pom.xml.

```
<plugin>
    <groupId>org.pitest</groupId>
    <artifactId>pitest-maven</artifactId>
    <version>LATEST</version>
 </plugin>
```

By default Pitest will mutate all code in your project/module. You can limit which code is mutated
and which tests are run using `targetClasses` and `targetTests`.

```
<plugin>
    <groupId>org.pitest</groupId>
    <artifactId>pitest-maven</artifactId>
    <version>LATEST</version>
    <configuration>
        <targetClasses>
            <param>com.your.package.root.want.to.mutate*</param>
        </targetClasses>
        <targetTests>
            <param>com.your.package.root*</param>
        </targetTests>
    </configuration>
</plugin>
```

The mutation test can be run directly from the commandline 

``mvn org.pitest:pitest-maven:mutationCoverage``

This will output an html report to **target/pit-reports/YYYYMMDDHHMI**.