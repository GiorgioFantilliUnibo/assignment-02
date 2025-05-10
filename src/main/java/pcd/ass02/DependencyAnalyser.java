package pcd.ass02;

public class DependencyAnalyser {
    public static void main(String[] args) {
        DependencyParser parser = new DependencyParser();
        AnalysisModel model = new AnalysisModel(parser);
        AnalysisView view = new AnalysisView();
        new AnalysisController(model, view);
    }
}
