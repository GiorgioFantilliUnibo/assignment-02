
package pcd.ass02;

import java.io.File;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Stream;

public class RecursiveFileOpenIterator implements Iterator<File> {

    private Iterator<File> directories;
    private Iterator<File> files;

    public RecursiveFileOpenIterator(File workplace) {
        if (workplace.isDirectory()) {
            files = Stream.concat(List.of(workplace).stream(), Arrays.stream(workplace.listFiles(File::isFile))).iterator();
            directories = Arrays.stream(workplace.listFiles(File::isDirectory)).iterator();
        } else {
            directories = Collections.emptyIterator();
            files = List.of(workplace).iterator();
        }
    }

    @Override
    public boolean hasNext() {
        if (files.hasNext())
            return files.hasNext();
        else {
            return directories.hasNext();
        }
    }

    @Override
    public File next() {
        if(files.hasNext()) {
            return files.next();
        } else {
            files = new RecursiveFileOpenIterator(directories.next());
            return files.next();
        }
    }

}
