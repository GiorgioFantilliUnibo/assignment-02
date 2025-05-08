
package pcd.ass02;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;

import pcd.ass02.ClassDepsReport.DependencyEntry;

import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.VariableDeclarator;
import com.github.javaparser.ast.expr.ObjectCreationExpr;
import com.github.javaparser.ast.type.TypeParameter;
import com.github.javaparser.ast.PackageDeclaration;
import com.github.javaparser.ast.ImportDeclaration;


import java.util.ArrayList;
import java.util.List;

public class Visitor<T> extends VoidVisitorAdapter<T> {
    private List<DependencyEntry> dependencies = new ArrayList<>();
    private String className = "Unknown";
    private String packageName = "";

    public ClassDepsReport visitAST(CompilationUnit cu, T arg) {
        cu.getPackageDeclaration().ifPresent(pd -> packageName = pd.getNameAsString());
        cu.getPrimaryTypeName().ifPresent(name -> className = name);
        visit(cu, arg);
        return new ClassDepsReport(className, packageName, new ArrayList<>(dependencies));
    }

    @Override
    public void visit(ClassOrInterfaceDeclaration n, T arg) {
        super.visit(n, arg);
        dependencies.add(new DependencyEntry(n.getNameAsString(), "class/int decl"));
    }

    @Override
    public void visit(PackageDeclaration n, T arg) {
        super.visit(n, arg);
        packageName = n.getNameAsString();
    }

    @Override
    public void visit(FieldDeclaration n, T arg) {
        super.visit(n, arg);
        n.getVariables().forEach(v -> dependencies.add(new DependencyEntry(v.getTypeAsString(), "field decl")));
    }

    @Override
    public void visit(MethodDeclaration n, T arg) {
        super.visit(n, arg);
        n.getParameters().forEach(p -> dependencies.add(new DependencyEntry(p.getTypeAsString(), "method decl, param type")));
        dependencies.add(new DependencyEntry(n.getTypeAsString(), "method decl, return type"));
    }

    @Override
    public void visit(ObjectCreationExpr n, T arg) {
        super.visit(n, arg);
        dependencies.add(new DependencyEntry(n.getTypeAsString(), "obj creation decl"));
    }

    @Override
    public void visit(VariableDeclarator n, T arg) {
        super.visit(n, arg);
        dependencies.add(new DependencyEntry(n.getTypeAsString(), "var decl"));
    }

    @Override
    public void visit(TypeParameter n, T arg) {
        super.visit(n, arg);
        dependencies.add(new DependencyEntry(n.asString(), "type decl"));
    }

    @Override
    public void visit(ImportDeclaration n, T arg) {
        super.visit(n, arg);
        if (!n.isAsterisk()) {
            var nameNode = n.getChildNodes().get(0); 
            var typeName = nameNode.toString();
            var packageNode = nameNode.getChildNodes().get(0); 
            String packageName = packageNode.toString();
            String context = "import package: " + packageName;
            dependencies.add(new DependencyEntry(typeName, context));
        } else {
            var packageNode = n.getChildNodes().get(0); 
            String packageName = packageNode.toString();
            dependencies.add(new DependencyEntry(packageName, "import"));
        }
    }
}
