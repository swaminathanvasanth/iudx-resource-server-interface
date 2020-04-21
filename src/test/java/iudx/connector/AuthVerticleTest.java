package iudx.connector;

import io.vertx.core.Vertx;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import io.vertx.core.json.JsonObject;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.eventbus.Message;
import io.vertx.core.eventbus.DeliveryOptions;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import java.util.concurrent.CountDownLatch;

@RunWith(VertxUnitRunner.class)
public class AuthVerticleTest {

    private static final Logger logger = LoggerFactory.getLogger(AuthVerticle.class.getName());
    private Vertx vertx;


    @Before
    public void setUp(TestContext tc) {
        vertx = Vertx.vertx();

        JsonObject conf = new JsonObject()
            .put("authserver.jksfile", "auth/examplekeystore.jks")
            .put("authserver.jkspasswd", "examplepassword")
            .put("authserver.url", "example.com.example");

        vertx.deployVerticle(AuthVerticle.class.getName(), new DeploymentOptions().setConfig(conf),
                tc.asyncAssertSuccess());
    }

    @After
    public void tearDown(TestContext tc) {
        vertx.close(tc.asyncAssertSuccess());
    }


    @Test
    public void testValidateToken(TestContext tc) {

        String tokenString = "exampleauthtoken";
        String itemId = "exampleid";
        logger.info("Starting test");
        CountDownLatch latch = new CountDownLatch(1);
	    DeliveryOptions	options = new DeliveryOptions();
        options.addHeader("action", "token-introspect");
        JsonObject requestedData = new JsonObject()
                                    .put("token", tokenString)
                                    .put("id", itemId);
                                
		vertx.eventBus().request("authqueue", requestedData, options, replyHandler -> {
			if (replyHandler.succeeded())
			{
                latch.countDown();
                logger.info("Succeded test");
			} else {
                latch.countDown();
                logger.info("Failed");
			}
		});
        try {
            latch.await();
        } catch (Exception e) {
            logger.info("Failed");
        }
    }
}
