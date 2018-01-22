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
        private final StringBuffer buffer;

        public TestableHtmlMaker(PageData pageData, boolean includeSuiteSetup) {
            this.pageData = pageData;
            this.includeSuiteSetup = includeSuiteSetup;
            buffer = new StringBuffer();
        }

        public String invoke() throws Exception {
            wikiPage = pageData.getWikiPage();

            if (pageData.hasAttribute("Test")) {
                if (includeSuiteSetup) {
                    String pageName = SuiteResponder.SUITE_SETUP_NAME;
                    WikiPage suiteSetup = PageCrawlerImpl.getInheritedPage(pageName, wikiPage);
                    if (suiteSetup != null) {
                        String mode = "setup";
                        includePage(suiteSetup, mode);
                    }
                }
                String setUp = "SetUp";
                WikiPage setup = PageCrawlerImpl.getInheritedPage(setUp, wikiPage);
                if (setup != null) {
                    String mode = "setup";
                    includePage(setup, mode);
                }
            }

            buffer.append(pageData.getContent());
            if (pageData.hasAttribute("Test")) {
                String tearDown = "TearDown";
                WikiPage teardown = PageCrawlerImpl.getInheritedPage(tearDown, wikiPage);
                if (teardown != null) {
                    String mode = "teardown";
                    includePage(teardown, mode);
                }
                if (includeSuiteSetup) {
                    String suiteTeardownName = SuiteResponder.SUITE_TEARDOWN_NAME;
                    WikiPage suiteTeardown = PageCrawlerImpl.getInheritedPage(suiteTeardownName, wikiPage);
                    if (suiteTeardown != null) {
                        String mode = "teardown";
                        includePage(suiteTeardown, mode);
                    }
                }
            }

            pageData.setContent(buffer.toString());
            return pageData.getHtml();
        }

        private void includePage(WikiPage suiteSetup, String mode) throws Exception {
            WikiPagePath pagePath = wikiPage.getPageCrawler().getFullPath(suiteSetup);
            String pagePathName = PathParser.render(pagePath);
            buffer.append("!include -" + mode + " .").append(pagePathName).append("\n");
        }
    }
}
