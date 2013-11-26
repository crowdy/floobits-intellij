package floobits;

import java.io.File;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;
import java.io.FileOutputStream;

import floobits.Shared;


class Utils {
    
    public static String pathJoin(String... paths)
    {
        File file = new File(paths[0]);

        for (int i = 1; i < paths.length ; i++) {
            file = new File(file, paths[i]);
        }

        return file.getPath();
    }

    public static String defaultBaseDir() {
        return Utils.pathJoin(System.getProperty("user.home"), "floobits");
    }

    public static String readFile (String fileName) throws IOException {
        BufferedReader br = new BufferedReader(new FileReader(fileName));
        try {
            StringBuilder sb = new StringBuilder();
            String line = br.readLine();

            while (line != null) {
                sb.append(line);
                sb.append("\n");
                line = br.readLine();
            }
            return sb.toString();
        } finally {
            br.close();
        }
    }

    public static void writeFile (String fileName, String text) throws IOException {
        PrintStream out;
        out = null;
        try {
            out = new PrintStream(new FileOutputStream(fileName));
            out.print(text);
        }
        finally {
            if (out != null) {
                out.close();
            }
        }
    }
}