package mops.gruppen2.architecture;

import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;

@AnalyzeClasses(packages = "mops.gruppen2", importOptions = ImportOption.DoNotIncludeTests.class)
class RepositoryTest {

    @ArchTest
    public static final ArchRule repositoryClassesThatAreAnnotatedWithRepositoryShouldHaveRepositoryInName = classes()
            .that().areAnnotatedWith(Repository.class)
            .should().haveSimpleNameEndingWith("Repository");

    @ArchTest
    public static final ArchRule repositoryClassesShouldBeInRepositoryPackage = classes()
            .that().haveSimpleNameEndingWith("Repository")
            .should().resideInAPackage("..persistance");

    @ArchTest
    public static final ArchRule repositoryClassesShouldBeAnnotatedWithRepositoryAnnotation = classes()
            .that().haveSimpleNameEndingWith("Repository")
            .should().beAnnotatedWith(Repository.class);

    @ArchTest
    public static final ArchRule classesInRepositoryPackageShouldHaveRepositoryInName = classes()
            .that().resideInAPackage("..persistance")
            .should().haveSimpleNameEndingWith("Repository");

    @ArchTest
    public static final ArchRule classesThatAreAssignableToCrudRepositoryShouldHaveRepositoryInName = classes()
            .that().areAssignableTo(CrudRepository.class)
            .should().beAnnotatedWith(Repository.class);

    @ArchTest
    public static final ArchRule dtoClassesShouldBeInDtoPackage = classes()
            .that().haveSimpleNameEndingWith("DTO")
            .should().resideInAPackage("..persistance.dto..");

}
