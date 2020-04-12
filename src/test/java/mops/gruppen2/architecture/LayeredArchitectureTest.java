package mops.gruppen2.architecture;

import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchIgnore;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;
import com.tngtech.archunit.library.Architectures;

@AnalyzeClasses(packages = "mops.gruppen2", importOptions = ImportOption.DoNotIncludeTests.class)
class LayeredArchitectureTest {

    private static final Architectures.LayeredArchitecture layeredArchitecture = Architectures
            .layeredArchitecture()
            .layer("Domain").definedBy("..domain..")
            .layer("Service").definedBy("..service..")
            .layer("Controller").definedBy("..web..")
            .layer("Repository").definedBy("..persistance..");

    @ArchIgnore
    @ArchTest
    public static final ArchRule domainLayerShouldOnlyBeAccessedByServiceAndControllerLayer = layeredArchitecture
            .whereLayer("Domain")
            .mayOnlyBeAccessedByLayers("Service", "Controller");

    @ArchIgnore
    @ArchTest
    public static final ArchRule serviceLayerShouldOnlyBeAccessedByControllerLayer = layeredArchitecture
            .whereLayer("Service")
            .mayOnlyBeAccessedByLayers("Controller");

    @ArchIgnore
    @ArchTest
    public static final ArchRule repositoryLayerShouldOnlyBeAccessedByServiceLayer = layeredArchitecture
            .whereLayer("Repository")
            .mayOnlyBeAccessedByLayers("Service");

    @ArchIgnore
    @ArchTest
    public static final ArchRule controllerLayerShouldNotBeAccessedByAnyLayer = layeredArchitecture
            .whereLayer("Controller")
            .mayNotBeAccessedByAnyLayer();

}
