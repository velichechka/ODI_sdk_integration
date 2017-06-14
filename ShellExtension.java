package utilities;

import java.io.BufferedReader;
import java.io.InputStreamReader;


public class ShellExtension {

    public static String executeCommand(String pCommand) {

        StringBuffer output = new StringBuffer();
        Process p;

        try {
            p = Runtime.getRuntime().exec(pCommand);
            p.waitFor();
            BufferedReader reader = 
                    new BufferedReader(new InputStreamReader(p.getInputStream()));

            String line = "";            
            while ((line = reader.readLine())!= null) {
                output.append(line + "\n");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return output.toString();
    }
}


