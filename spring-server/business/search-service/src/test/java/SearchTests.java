import com.czy.api.constant.es.FieldAnalyzer;
import com.czy.api.domain.Do.test.TestSearchDo;
import com.czy.api.domain.Do.test.TestSearchEsDo;
import com.czy.search.SearchApplication;
import com.czy.search.mapper.es.TestSearchEsMapper;
import com.czy.search.mapper.TestSearchMapper;
import com.czy.search.mapper.TestSearchMongoMapper;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.client.indices.AnalyzeRequest;
import org.elasticsearch.client.indices.AnalyzeResponse;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.elasticsearch.core.ElasticsearchRestTemplate;
import org.springframework.test.context.TestPropertySource;

import java.io.IOException;
import java.util.List;

/**
 * @author 13225
 * @date 2025/4/24 17:26
 */

@Slf4j
@SpringBootTest(classes = SearchApplication.class)
@TestPropertySource("classpath:application.yml")
public class SearchTests {

    // 零级搜索：通过like模糊匹配
    public static final String[] testContent = {
            "新冠病毒如何治疗",
            "新冠病毒最佳治疗方法",
            "普通感冒治疗",
            "新冠病毒致死率是多少",
            "冠状病毒感染如何治疗",
            "季节性流感应该如何治疗？"
    };
    @Test
    public void test() {
        log.info("test");
    }


    /**
     * 零级匹配
     * 关键词级别匹配，使用左右相等去匹配
     */
    @Autowired
    private TestSearchMapper testSearchMapper;
    @Autowired TestSearchMongoMapper testSearchMongoMapper;
    @Test
    public void testMysqlInsert(){
        for (String s : testContent) {
            TestSearchDo testSearchDo = new TestSearchDo();
            testSearchDo.setSearchName(s);
            testSearchMapper.insert(testSearchDo);
        }
    }

    @Test
    public void testMongoInsert(){
        for (int i = 0; i < testContent.length; i++) {
            TestSearchDo testSearchDo = new TestSearchDo();
            testSearchDo.setSearchName(testContent[i]);
            testSearchDo.setId(i + 1L);
            testSearchMongoMapper.saveSearchTestDo(testSearchDo);
        }
    }

    @Test
    public void deleteMongoAll(){
        testSearchMongoMapper.deleteSearchTestDoAll();
    }

    @Test
    public void testMysqlLikeSearch(){
        List<TestSearchDo> testSearchDo = testSearchMapper.selectByLikeName("新冠病毒");
        testSearchDo.forEach(item -> System.out.println(item.toJsonString()));
    }

    @Test
    public void testMongoLikeSearch(){
        List<TestSearchDo> testSearchDo = testSearchMongoMapper.findSearchTestDoByLikeName("新冠病毒");
        testSearchDo.forEach(item -> System.out.println(item.toJsonString()));
    }

    /**
     * 一级匹配
     * ElasticSearch倒排索引匹配
     * <p>
     * 关于一级匹配：需要添加专业词语到分词器。
     * 我有个问题，怎么给分词器添加我指定的词典，
     * 比如“新冠病毒”这种是属于专业词语，它不能直接进行分词，而且要识别出来作为索引。
     * 而现状是不因分为了：新 + 冠 + 病毒；还没有把新冠病毒作为索引
     * <p>
     * 上述已完成
     */
    @Autowired
    private TestSearchEsMapper testSearchEsMapper;
    @Test
    public void testEsInsert(){
        for (int i = 0; i < testContent.length; i++) {
            TestSearchEsDo testSearchDo = new TestSearchEsDo();
            testSearchDo.setSearchName(testContent[i]);
            testSearchDo.setId(i + 1L);
            testSearchEsMapper.save(testSearchDo);
        }
    }

    @Test
    public void testEsDelete(){
        testSearchEsMapper.deleteAll();
    }

    @Test
    public void testEsPrintAll(){
        Iterable<TestSearchEsDo> testSearchDo = testSearchEsMapper.findAll();
        testSearchDo.forEach(item -> System.out.println(item.toJsonString()));
    }

    @Autowired
    ElasticsearchRestTemplate elasticsearchRestTemplate;

    @Test
    public void testEsLikeSearch(){
        List<TestSearchEsDo> testSearchDo = testSearchEsMapper.findBySearchNameLike("如何");
        List<TestSearchEsDo> testSearchDo1 = testSearchEsMapper.findBySearchNameLike("病毒");
        List<TestSearchEsDo> testSearchDo2 = testSearchEsMapper.findBySearchNameLike("冠");

        System.out.println("=========如何=========");
        testSearchDo.forEach(item -> System.out.println(item.toJsonString()));
        System.out.println("=========病毒=========");
        testSearchDo1.forEach(item -> System.out.println(item.toJsonString()));
        System.out.println("=========冠=========");
        testSearchDo2.forEach(item -> System.out.println(item.toJsonString()));
    }

    @Test
    public void testEsLikeMain(){
        testEsDelete();
        testEsInsert();
//        testEsPrintAll();
        System.out.println("=========testEsLikeSearch=========");
        testEsLikeSearch();
    }

    @Autowired
    private RestHighLevelClient client;
    @Test
    public void testIk() throws IOException {


        for (String message : testContent){
            AnalyzeRequest request = AnalyzeRequest.withGlobalAnalyzer(
                    FieldAnalyzer.IK_MAX_WORD,
                    message
            );

            AnalyzeResponse response = client.indices().analyze(request, RequestOptions.DEFAULT);
            List<AnalyzeResponse.AnalyzeToken> token1 = response.getTokens();
            System.out.println("IK_MAX_WORD token");
            token1.forEach(token -> {
                System.out.println("term: " + token.getTerm());
                System.out.println("start: " + token.getStartOffset());
                System.out.println("end: " + token.getEndOffset());
                System.out.println("type: " + token.getType());
            });
        }

    }

    /**
     * 关于二级模糊匹配：
     * 1. 句子分词；将句子进行分词；
     * 2. 由于限制句子长度为15个，所以关键词的长的数量一定小于7；
     * 3. 用elasticSearch匹配这15个词语，只要找到一个内容包含2个以上的关键词就返回。
     */
    // 分词测试
    @Test
    public void testIkSegmentWord() throws IOException {

    }

    // 1. 句子分词
    private void segmentWord(String message) throws IOException {
        AnalyzeRequest request = AnalyzeRequest.withGlobalAnalyzer(
                FieldAnalyzer.IK_MAX_WORD,
                message
        );

        AnalyzeResponse response = client.indices().analyze(request, RequestOptions.DEFAULT);
        List<AnalyzeResponse.AnalyzeToken> token1 = response.getTokens();
        System.out.println("IK_MAX_WORD token");
        token1.forEach(token -> {
            System.out.println("term: " + token.getTerm());
            System.out.println("start: " + token.getStartOffset());
            System.out.println("end: " + token.getEndOffset());
            System.out.println("type: " + token.getType());
        });
    }

    // 逐级匹配：2，3，4，5，6

    /**
     * 三级搜索（类推荐）:
     * 输入是句子，将句子分词但是二级搜索也找不到，查询知识图谱，
     * 知识图谱的增删改查
     * 知识图谱查询到其实体最相近的物品，将物品返回到搜索结果（新冠病毒怎么治疗–>感冒怎么治疗）
     */

    /**
     * 四级搜索（nlp）:输入句子，Bert意图分类，知识图谱匹配，返回规则集组合
     */
}
