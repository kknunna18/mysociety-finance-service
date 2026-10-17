package com.mysociety.finance;

import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

@AnalyzeClasses(packages = "com.mysociety.finance")
class ArchitectureTest {
    @ArchTest
    static final ArchRule controllersDoNotUseRepositories = noClasses().that().resideInAPackage("..api..").should().dependOnClassesThat().resideInAPackage("..repository..");
}
