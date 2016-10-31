package ${package};

import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.talend.components.service.spring.SpringTestApp;


@RunWith(SpringRunner.class)
@SpringBootTest(classes = SpringTestApp.class)
public class Spring${componentName}TestIT extends ${componentName}TestBase {
    //all test are to be found the parent classes    
}
