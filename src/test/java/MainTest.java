import org.junit.jupiter.api.*;

public class MainTest {
    @BeforeAll
    public static void init() {
        System.out.println("This runs before whole test suite");
        // do not forget `maven-surefire-plugin` in pom.xml
    }

    @BeforeEach
    public void doEach() {
        System.out.println("This runs before each test");
    }

    @Test
    public void testMethod() {
        System.out.println("Put your tests in test methods annotated with @Test");
        // JUnit5 quick guide - https://reflectoring.io/junit5/
    }

    @AfterEach
    public void doAfterEach() {
        System.out.println("This runs after each test");
    }

    @AfterAll
    public static void finish() {
        System.out.println("This runs after whole test suite");
    }
}
