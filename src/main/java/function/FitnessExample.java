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
                    WikiPage suiteSetup = PageCrawlerImpl.getInheritedPage(SuiteResponder.SUITE_SETUP_NAME, wikiPage);
                    if (suiteSetup != null) {
                        String mode = "setup";
                        WikiPagePath pagePath = wikiPage.getPageCrawler().getFullPath(suiteSetup);
                        String pagePathName = PathParser.render(pagePath);
                        buffer.append("!include -" + mode + " .").append(pagePathName).append("\n");
                    }
                }
                WikiPage setup = PageCrawlerImpl.getInheritedPage("SetUp", wikiPage);
                if (setup != null) {
                    String mode = "setup";
                    WikiPagePath setupPath = wikiPage.getPageCrawler().getFullPath(setup);
                    String setupPathName = PathParser.render(setupPath);
                    buffer.append("!include -" + mode + " .").append(setupPathName).append("\n");
                }
            }

            buffer.append(pageData.getContent());
            if (pageData.hasAttribute("Test")) {
                WikiPage teardown = PageCrawlerImpl.getInheritedPage("TearDown", wikiPage);
                if (teardown != null) {
                    String mode = "teardown";
                    WikiPagePath tearDownPath = wikiPage.getPageCrawler().getFullPath(teardown);
                    String tearDownPathName = PathParser.render(tearDownPath);
                    buffer.append("!include -" + mode + " .").append(tearDownPathName).append("\n");
                }
                if (includeSuiteSetup) {
                    WikiPage suiteTeardown = PageCrawlerImpl.getInheritedPage(SuiteResponder.SUITE_TEARDOWN_NAME, wikiPage);
                    if (suiteTeardown != null) {
                        String mode = "teardown";
                        WikiPagePath pagePath = wikiPage.getPageCrawler().getFullPath(suiteTeardown);
                        String pagePathName = PathParser.render(pagePath);
                        buffer.append("!include -" + mode + " .").append(pagePathName).append("\n");
                    }
                }
            }

            pageData.setContent(buffer.toString());
            return pageData.getHtml();
        }
    }
}
