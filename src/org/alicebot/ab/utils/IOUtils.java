package org.alicebot.ab.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;


public class IOUtils {


	public static String readInputTextLine() {
        BufferedReader lineOfText = new BufferedReader(new InputStreamReader(System.in));
		String textLine = null;
		try {
			textLine = lineOfText.readLine();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return textLine;
	}
}

