package org.gooru.nucleus.handlers.resources.bootstrap;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

import org.gooru.nucleus.handlers.resources.bootstrap.scenarios.*;
import org.junit.*;
import org.junit.runner.RunWith;

import io.vertx.core.DeploymentOptions;
import io.vertx.core.Vertx;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.json.DecodeException;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;

/**
 * @author ashish on 28/9/16.
 */
@RunWith(VertxUnitRunner.class)
public class ResourceVerticleTest {

    private static final String CONFIG = "src/main/resources/nucleus-resource.json";
    private static Vertx vertx;
    private static EventBus eventBus;

    private final NonExistingDeleteScenario nonExistingDeleteScenario = new NonExistingDeleteScenario();
    private final ResourceCreateScenario resourceCreateScenario = new ResourceCreateScenario();
    private final ResourceDeleteScenario resourceDeleteScenario = new ResourceDeleteScenario();
    private final ResourceUpdateScenario resourceUpdateScenario = new ResourceUpdateScenario();
    private final ResourceFetchScenario resourceFetchScenario = new ResourceFetchScenario();
    private final ResourceNegativeScenario resourceNegativeScenario = new ResourceNegativeScenario();

    @BeforeClass
    public static void setUp(TestContext context) throws Exception {
        System.out.println("Setting up test");
        vertx = Vertx.vertx();
        deployVerticle(context);
        eventBus = vertx.eventBus();
    }

    private static void deployVerticle(TestContext context) {
        DeploymentOptions options = new DeploymentOptions().setConfig(readConfig(context));
        vertx.deployVerticle(ResourceVerticle.class.getName(), options, context.asyncAssertSuccess());
    }

    private static JsonObject readConfig(TestContext context) {
        JsonObject conf;
        String sconf = null;
        try (Scanner scanner = new Scanner(new File(CONFIG)).useDelimiter("\\A")) {
            sconf = scanner.next();
            conf = new JsonObject(sconf);
            return conf;
        } catch (DecodeException e) {
            System.out.println("Configuration file " + sconf + " does not contain a valid JSON object");
            throw e;
        } catch (FileNotFoundException fnfe) {
            System.out.println("File " + CONFIG + " Not found. ");
            throw new RuntimeException(fnfe);
        }
    }

    @AfterClass
    public static void tearDown(TestContext context) throws Exception {
        System.out.println("Tearing down test");
        vertx.close(context.asyncAssertSuccess());
    }

    @Before
    public void before(TestContext context) {
        System.out.println("Executing @Before");
    }

    @After
    public void after(TestContext context) {
        System.out.println("Executing @After");
    }

    @Test
    public void deleteNonExistingResourceRespondsWith404(TestContext context) throws Exception {
        nonExistingDeleteScenario.playScenario(context, eventBus);
    }

    @Test
    public void createResourceScenario(TestContext context) throws Exception {
        resourceCreateScenario.playScenario(context, eventBus);
    }

    @Test
    public void updateResourceScenario(TestContext context) throws Exception {
        resourceUpdateScenario.playScenario(context, eventBus);
    }

    @Test
    public void fetchResourceScenario(TestContext context) throws Exception {
        resourceFetchScenario.playScenario(context, eventBus);
    }

    @Test
    public void deleteResourceScenario(TestContext context) throws Exception {
        resourceDeleteScenario.playScenario(context, eventBus);
    }

    @Test
    public void negativeResourceScenarios(TestContext context) throws Exception {
        resourceNegativeScenario.playScenario(context, eventBus);
    }
}