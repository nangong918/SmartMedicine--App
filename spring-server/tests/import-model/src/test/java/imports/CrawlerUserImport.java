package imports;

import com.czy.imports.ImportsApplication;
import com.czy.imports.service.ImportAuthorService;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

/**
 * @author 13225
 * @date 2025/5/30 14:27
 */


@Slf4j
@SpringBootTest(classes = ImportsApplication.class)
@TestPropertySource("classpath:application.yml")
public class CrawlerUserImport {

    @Test
    public void helloWorld() {
        log.info("helloWorld");
    }

    @Autowired
    private ImportAuthorService importAuthorService;

    @Test
    public void importAll(){
        importAuthorService.importAllData();
    }

}
