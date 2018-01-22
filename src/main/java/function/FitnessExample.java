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
            wikiPage = pageData.getWikiPage();
            this.includeSuiteSetup = includeSuiteSetup;
            content = new String();
        }

        public String invoke() throws Exception {
            if (isTestPage())
                surroundPageWithSetupsAndTeardowns();
            return pageData.getHtml();
        }

        private void surroundPageWithSetupsAndTeardowns() throws Exception {
            content = includeSetups();
            content += pageData.getContent();
            content += includeTeardowns();
            pageData.setContent(content);
        }

        private boolean isTestPage() throws Exception {
            return pageData.hasAttribute("Test");
        }

        private String includeTeardowns() throws Exception {
            String teardown = includeInherited("TearDown", "teardown");
            if (includeSuiteSetup)
                teardown += includeInherited(SuiteResponder.SUITE_TEARDOWN_NAME, "teardown");
            return teardown;
        }

        private String includeSetups() throws Exception {
            String setups = "";
            if (includeSuiteSetup)
                includeInherited(SuiteResponder.SUITE_SETUP_NAME, "setup");
            setups += includeInherited("SetUp", "setup");
            return setups;
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
