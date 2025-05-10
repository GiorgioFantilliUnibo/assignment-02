package pcd.ass02;

public class DependencyAnalyser {
    public static void main(String[] args) {
        AnalysisModel model = new AnalysisModel();
        AnalysisView view = new AnalysisView();
        DependencyParser parser = new DependencyParser();
        new AnalysisController(model, view, parser);
    }
}
