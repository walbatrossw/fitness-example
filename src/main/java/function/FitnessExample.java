package function;

import fitnesse.responders.run.SuiteResponder;
import fitnesse.wiki.*;

public class FitnessExample {
    public String testableHtml(PageData pageData, boolean includeSuiteSetup) throws Exception {
        return new TestableHtmlMaker(pageData, includeSuiteSetup).invoke();
    }

    private class TestableHtmlMaker {
        private PageData pageData;
        private boolean includeSuiteSetup;
        private WikiPage wikiPage;
        private String content;

        public TestableHtmlMaker(PageData pageData, boolean includeSuiteSetup) {
            this.pageData = pageData;
            this.includeSuiteSetup = includeSuiteSetup;
            wikiPage = pageData.getWikiPage();
            content = new String();
        }

        public String invoke() throws Exception {
            if (isTestPage()) {
                content = includeSetups();
                content += pageData.getContent();
                content += includeTearDowns();
                pageData.setContent(content);
            }
            return pageData.getHtml();
        }

        private boolean isTestPage() throws Exception {
            return pageData.hasAttribute("Test");
        }

        private String includeTearDowns() throws Exception {
            String tearDowns = includeInherited("TearDown", "teardown");
            if (includeSuiteSetup)
                tearDowns += includeInherited(SuiteResponder.SUITE_TEARDOWN_NAME, "teardown");
            return tearDowns;
        }

        private String includeSetups() throws Exception {
            String setUps = "";
            if (includeSuiteSetup)
                includeInherited(SuiteResponder.SUITE_SETUP_NAME, "setup");
            setUps += includeInherited("SetUp", "setup");
            return setUps;
        }

        private String includeInherited(String pageName, String mode) throws Exception {
            WikiPage suiteSetup = PageCrawlerImpl.getInheritedPage(pageName, wikiPage);
            if (suiteSetup != null) {
                return includePage(suiteSetup, mode);
            }
            return "";
        }

        private String includePage(WikiPage suiteSetup, String mode) throws Exception {
            WikiPagePath pagePath = wikiPage.getPageCrawler().getFullPath(suiteSetup);
            String pagePathName = PathParser.render(pagePath);
            return String.format("!include -%s .%s\n", mode, pagePathName);
        }
    }
}
