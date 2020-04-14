package engine;

import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Iterator;
import java.util.Scanner;

import org.json.simple.*;
import org.json.simple.parser.*;

public class PandemyFetcher {
	public static void main(String[] args) {
		String inline = "";
		try {
			URL url = new URL(
					"https://corona.lmao.ninja/countries/finland");
			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
			conn.setRequestMethod("GET");
			conn.connect();
			int responsecode = conn.getResponseCode();
			if (responsecode != 200) {
				throw new RuntimeException("HttpResponseCode: " + responsecode);
			} else {
				Scanner sc = new Scanner(url.openStream());
				while (sc.hasNext()) {
					inline += sc.nextLine();
				}
				sc.close();
				System.out.println("\nJSON data in string format");
				System.out.println(inline);
				JSONParser parse = new JSONParser();

				JSONObject result = (JSONObject) parse.parse(inline);
				JSONObject main = (JSONObject) result.get("country");
				int cases = (Integer) main.get("cases");
				System.out.println(cases);
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
	}	
}