package artifacts;

import cartago.Artifact;
import cartago.OPERATION;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.io.Writer;
import java.io.Reader;
import java.io.BufferedReader;
import java.io.InputStreamReader;

public class WindowManagerArtifact extends Artifact {	
	/* Runs a given process and returns the standard output. */
	private String runProcessAndGetStdOut(String commandLine) {
		try {
			Process p = Runtime.getRuntime().exec(commandLine);
			InputStream in = p.getInputStream();
			StreamToString sts = new StreamToString();
			p.waitFor();
			return sts.convertStreamToString(in);
		} catch(Exception ex) {
			ex.printStackTrace();
		}
		
		return "";
	}
	
	private String getPresentationWindowId() {
		String readerWindows = runProcessAndGetStdOut("wmctrl -l");
		String[] lines = readerWindows.split("\\r?\\n");
		String firstWindow = "";
		for (int i = 0; i < lines.length; i++) {
			if (lines[i].indexOf("Adobe Reader") >= 0) {
				firstWindow = lines[i];
				break;
			}
		}
		System.out.println("first window = " + firstWindow);
		String firstWindowId = firstWindow.split("\\s")[0];
		System.out.println("first window id = " + firstWindowId);
		return firstWindowId;
	}
	
	@OPERATION void sendRightKeyToPresentation() {
		try {
			String windowId = getPresentationWindowId();
			Runtime.getRuntime().exec("xvkbd -window " + windowId + " -text \"\\[Right]\"");
		} catch(Exception ex) {
			ex.printStackTrace();
		}
	}
	
	// Convert an InputStream to a string
	public static class StreamToString {
	    public String convertStreamToString(InputStream is)
	            throws IOException {
	        /*
	         * To convert the InputStream to String we use the
	         * Reader.read(char[] buffer) method. We iterate until the
	         * Reader return -1 which means there's no more data to
	         * read. We use the StringWriter class to produce the string.
	         */
	        if (is != null) {
	            Writer writer = new StringWriter();

	            char[] buffer = new char[1024];
	            try {
	                Reader reader = new BufferedReader(
	                        new InputStreamReader(is, "UTF-8"));
	                int n;
	                while ((n = reader.read(buffer)) != -1) {
	                    writer.write(buffer, 0, n);
	                }
	            } finally {
	                is.close();
	            }
	            return writer.toString();
	        } else {        
	            return "";
	        }
	    }
	}

}