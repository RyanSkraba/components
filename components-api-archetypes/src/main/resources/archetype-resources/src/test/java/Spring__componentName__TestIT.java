package ${package};

import org.junit.runner.RunWith;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.talend.components.service.spring.SpringTestApp;


@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = SpringTestApp.class)
public class Spring${componentName}TestIT extends ${componentName}TestBase {
    //all test are to be found the parent classes    
}
