package engine;


import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.StringBuilder;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Scanner;

import org.telegram.telegrambots.ApiContextInitializer;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.json.simple.*;
import org.json.simple.parser.*;

// Made according to https://github.com/rubenlagus/TelegramBots/blob/master/TelegramBots.wiki/Getting-Started.md

public class ChatBot extends TelegramLongPollingBot {
	public void onUpdateReceived(Update update) {
		// We check if the update has a message and the message has text
		if (update.hasMessage() && update.getMessage().hasText()) {

			String kokoteksti = update.getMessage().getText();
			String sanotaanTakaisin = "";
			if (kokoteksti.contains(" ")) {
				// String[] solut = kokoteksti.split(" ");
				// String viestinAlku = solut[0];
				// if (solut.length == 2) {
				// System.out.println("JUKKA " + solut[1]);
				// }
				try {
					/*
					 * Krissepaid:
					 */
					String kenelta = "";
					if (kokoteksti.contains("/krissepaid")) {

						String message = update.getMessage().getText();
						User usr = update.getMessage().getFrom();
						System.out.println("DEBUG:\n" + "user=" + usr + "\nmessage: " + message);
						kenelta = usr.getFirstName();
						sanotaanTakaisin = sanoTakaisinKrissepaid(kokoteksti, kenelta);
					} else {
						sanotaanTakaisin = sanoTakaisin(kokoteksti);
					}
				} catch (IOException ioe) {
					sanotaanTakaisin = "Virhe avattaessa tiedostoa";
					System.out.println("Error: " + ioe.toString());
					ioe.printStackTrace();
				} catch (Exception e) {
					sanotaanTakaisin = "Tuntematon virhe";
					System.out.println("Error: " + e.toString());
				}
			} else {
				try {
					sanotaanTakaisin = sanoTakaisin(kokoteksti);
				} catch (IOException ioe) {
					sanotaanTakaisin = "Virhe avattaessa tiedostoa";
					System.out.println("Error: " + ioe.toString());
					ioe.printStackTrace();
				} catch (Exception e) {
					sanotaanTakaisin = "Tuntematon virhe";
					System.out.println("Error: " + e.toString());
				}
			}
			SendMessage message = new SendMessage() // Create a SendMessage
													// object with mandatory
													// fields
					.setChatId(update.getMessage().getChatId()).setText(sanotaanTakaisin);
			try {
				execute(message); // Call method to send the message
			} catch (TelegramApiException e) {
				e.printStackTrace();
			}
		}
	}
	// ====================================================================================================

	public String sanoTakaisinKrissepaid(String botilleSanottua, String kenelta) throws Exception {
		System.out.println("KRISSEPAID KUTSUTTU");
		String sanotaanTakaisin = "";
		// String komento = botilleSanottua.split(" ", 3)[0];
		String krissepaidKomento = botilleSanottua.split(" ", 3)[1];
		String krissepaidViesti = botilleSanottua.split(" ", 3)[2];
		System.out.println("krissepaidKomento = " + krissepaidKomento + " -- viesti = " + krissepaidViesti);

		if (krissepaidKomento.equalsIgnoreCase("list")) {
			sanotaanTakaisin = ListaaMaksut();
		} else if (krissepaidKomento.equalsIgnoreCase("today")) {
			sanotaanTakaisin = LisaaMaksuTanaan(krissepaidViesti, kenelta);
		} else if (krissepaidKomento.equalsIgnoreCase("date")) {
			sanotaanTakaisin = LisaaMaksu(krissepaidViesti, kenelta);
		} else if (krissepaidKomento.equalsIgnoreCase("clean")) {
			TyhjennaTiedosto();
			sanotaanTakaisin = "Tiedosto tyhjennetty";
		} else if (krissepaidKomento.equalsIgnoreCase("delete")) {
			sanotaanTakaisin = PoistaMaksu(krissepaidViesti);

		} else if (krissepaidKomento.equalsIgnoreCase("sum")) {
			sanotaanTakaisin = YhteensaMaksettu();

		} else if (krissepaidKomento.equalsIgnoreCase("find")) {
			sanotaanTakaisin = EtsiTietynAjanMaksut(krissepaidViesti);
		} else if (krissepaidKomento.equalsIgnoreCase("count")) {
			sanotaanTakaisin = KuukaudenKaikkiMaksutYhteensa(krissepaidViesti);
		} else if (krissepaidKomento.equalsIgnoreCase("search")) {
			sanotaanTakaisin = KukaMaksoi(krissepaidViesti);
		} else if (krissepaidKomento.equalsIgnoreCase("help")) {
			sanotaanTakaisin = help();
		}

		return sanotaanTakaisin;
	}

	// ============================================================================

	public String sanoTakaisin(String botilleSanottua) throws Exception {
		System.out.println(botilleSanottua);
		String sanotaanTakaisin = "";
		if (botilleSanottua.equals("/i love you")) {
			sanotaanTakaisin = "I love you too.";
		} else if (botilleSanottua.equals("/who will fetch pauline today?")) {
			sanotaanTakaisin = "I can tell you that when that feature is implemented.";
		} else if (botilleSanottua.equals("/when did jukka pay?")) {
			sanotaanTakaisin = "I can tell you that when that feature is implemented.";
		} else if (botilleSanottua.equals("/weather")) {
			sanotaanTakaisin = etsiSaa();
		} else if (botilleSanottua.equals("/foodmenu")) {
			sanotaanTakaisin = etsiRuokalista();
		} else if (botilleSanottua.equals("/jukkapaid")) {
			sanotaanTakaisin = etsiJukanMaksut();

		}
		return sanotaanTakaisin;
	}

	// ==========================================================================================================

	public String help() {

		String palautettava = "Kaikki komennot:\nAloita komento /krissepaid\nLisää maksu: today + maksu tai date + pvm + maksu\n"
				+ "Poista maksu: delete + monesko rivi\nListaa kaikki maksut: list all\nKaikki maksut yhteensä : sum all\n"
				+ "Summaa kuukauden maksut: count + 2 2020\nEtsi kuukauden tai vuoden maksut : esim find + 2 2020 tai find 2020\n"
				+ "Etsi tietyn henkilön maksut: search + Kristiina tai search + 2020 Kristiina tai search 2 + 2020 + Kristiina \nAloita alusta: clean all";

		return palautettava;

	}

	public String ListaaMaksut() throws Exception {

		File krissenMaksut = new File("krissepaid.txt");
		Scanner tiedostonlukija = new Scanner(krissenMaksut);

		StringBuilder builder = new StringBuilder();

		while (tiedostonlukija.hasNext()) {
			builder.append(tiedostonlukija.nextLine());
			builder.append("\n");
		}
		tiedostonlukija.close();

		String tulos = builder.toString();

		return "Kaikki maksut:\n" + tulos;

	}

	public void KirjoitaTiedostoon(String teksti) throws IOException {
		File krissenMaksut = new File("krissepaid.txt");
		PrintWriter kirjoita = new PrintWriter(new FileWriter(krissenMaksut, true));
		kirjoita.println(teksti);
		kirjoita.close();

	}

	public String LisaaMaksuTanaan(String lisattava, String kuka) throws Exception {

		String[] lista = lisattava.split(" ");

		StringBuilder builder = new StringBuilder();

		for (int i = 0; i < lista.length; i++) {
			builder.append(" ");
			builder.append(lista[i]);
		}
		builder.append(" ");
		builder.append(kuka);
		String lisays = builder.toString();
		LocalDate tanaan = LocalDate.now();
		DateTimeFormatter formaatti = DateTimeFormatter.ofPattern("d.M.yyy");
		String pvm = tanaan.format(formaatti);

		KirjoitaTiedostoon(pvm + lisays);

		return "Maksu lisätty, " + kuka;
	}

	public String LisaaMaksu(String maksu, String kuka) throws IOException {

		String[] lista = maksu.split(" ");
		StringBuilder builder = new StringBuilder();

		for (int i = 0; i < lista.length; i++) {

			builder.append(lista[i]);
			builder.append(" ");

		}
		builder.append(" ");
		builder.append(kuka);
		String kirjoita = builder.toString();

		KirjoitaTiedostoon(kirjoita);

		return "Maksu lisätty " + kuka;
	}

	public void TyhjennaTiedosto() throws FileNotFoundException {

		PrintWriter writer = new PrintWriter("krissepaid.txt");
		writer.close();
	}

	public String PoistaMaksu(String kasky) throws IOException {

		int moneskoRivi = Integer.parseInt(kasky);
		int indeksi = moneskoRivi - 1;

		File krissenMaksut = new File("krissepaid.txt");
		Scanner tiedostonlukija = new Scanner(krissenMaksut);

		ArrayList<String> rivit = new ArrayList<>();

		while (tiedostonlukija.hasNext()) {
			rivit.add(tiedostonlukija.nextLine());
		}
		tiedostonlukija.close();

		rivit.remove(indeksi);
		TyhjennaTiedosto();

		for (String kohta : rivit) {
			KirjoitaTiedostoon(kohta);
		}
		return "Rivi " + moneskoRivi + " poistettu.";
	}

	public String YhteensaMaksettu() throws FileNotFoundException {

		double summa = 0;

		File krissenMaksut = new File("krissepaid.txt");
		Scanner tiedostonlukija = new Scanner(krissenMaksut);

		while (tiedostonlukija.hasNext()) {
			String rivi = tiedostonlukija.nextLine();
			String[] pilkottu = rivi.split(" ");
			summa += Double.parseDouble(pilkottu[1]);
		}

		tiedostonlukija.close();

		return "Kaikki maksut yhteensä " + summa + " euroa.";
	}

	public String KuukaudenKaikkiMaksutYhteensa(String kasky) throws FileNotFoundException {
		String[] pvm = kasky.split(" ");
		int kk = Integer.parseInt(pvm[0]);
		int vuosi = Integer.parseInt(pvm[1]);
		ArrayList<String> lista = PalautaKuukausiListaus(kk, vuosi);
		double summa = 0;

		for (String rivi : lista) {
			String[] pilkottu = rivi.split(" ");
			summa += Double.parseDouble(pilkottu[1]);
		}
		return "Ajankohdan " + kk + "-" + vuosi + " kaikki maksut yhteensä: " + summa + " euroa";

	}

	public String EtsiTietynAjanMaksut(String ajankohta) throws FileNotFoundException {
		String[] pilkottu = ajankohta.split(" ");
		String tulos = "";

		if (pilkottu.length == 1) {
			int vuosi = Integer.parseInt(pilkottu[0]);
			tulos = "Haun tulos:\n" + ListaaVuodenMaksut(vuosi);
		} else if (pilkottu.length == 2) {
			int vuosi = Integer.parseInt(pilkottu[1]);
			int kk = Integer.parseInt(pilkottu[0]);
			tulos = "Haun tulos: \n" + ListaaKuukaudenMaksut(kk, vuosi);
		} else {
			tulos = "Ajankohtaa ei löytynyt.";
		}

		return tulos;
	}

	public ArrayList<String> PalautaVuosiListaus(int vuosi) throws FileNotFoundException {
		ArrayList<String> lista = new ArrayList<>();

		File krissenmaksut = new File("krissepaid.txt");
		Scanner lukija = new Scanner(krissenmaksut);

		while (lukija.hasNext()) {
			String rivi = lukija.nextLine();
			String[] riviPalasina = rivi.split(" ");
			String pvm = riviPalasina[0];

			String[] pilkottupvm = pvm.split("[.]");
			int verrattavaVuosi = Integer.parseInt(pilkottupvm[2]);
			if (vuosi == verrattavaVuosi) {
				lista.add(rivi);
			}

		}
		lukija.close();
		return lista;
	}

	public ArrayList<String> PalautaKuukausiListaus(int kk, int vuosi) throws FileNotFoundException {
		ArrayList<String> vuodenMaksut = PalautaVuosiListaus(vuosi);
		ArrayList<String> kuukaudenMaksut = new ArrayList<>();

		for (String rivi : vuodenMaksut) {
			String[] date = rivi.split(" ");
			String[] pilkottupvm = date[0].split("[.]");
			int verrattavakk = Integer.parseInt(pilkottupvm[1]);
			if (verrattavakk == kk) {
				kuukaudenMaksut.add(rivi);
			}
		}
		return kuukaudenMaksut;
	}

	public String ListaaVuodenMaksut(int vuosi) throws FileNotFoundException {

		ArrayList<String> lista = PalautaVuosiListaus(vuosi);

		StringBuilder builder = new StringBuilder();
		for (String line : lista) {
			builder.append(line);
			builder.append("\n");
		}

		String palautettava = builder.toString();

		return "Vuoden " + vuosi + " maksut:\n" + palautettava;

	}

	public String ListaaKuukaudenMaksut(int kk, int vuosi) throws FileNotFoundException {

		ArrayList<String> lista = PalautaKuukausiListaus(kk, vuosi);

		StringBuilder builder = new StringBuilder();
		for (String line : lista) {
			builder.append(line);
			builder.append("\n");
		}

		String palautettava = builder.toString();

		return "Ajan: " + kk + "-" + vuosi + " maksut:\n" + palautettava;

	}

	public String KukaMaksoi(String viesti) throws FileNotFoundException {
		ArrayList<String> maksut;
		ArrayList<String> palautettava = new ArrayList<>();

		File krissenMaksut = new File("krissepaid.txt");
		Scanner tiedostonlukija = new Scanner(krissenMaksut);

		String[] pilkottu = viesti.split(" ");
		String kenenMaksut = "";
		
		Double summa = 0.00;

		if (pilkottu.length == 1) {
			while (tiedostonlukija.hasNext()) {

				String rivi = tiedostonlukija.nextLine();
				String[] pilkotturivi = rivi.split(" ");
				String verrattavakuka = pilkotturivi[pilkotturivi.length - 1];
				// String verrattavapvm = pilkotturivi[0];

				kenenMaksut = pilkottu[0];
				if (verrattavakuka.equalsIgnoreCase(kenenMaksut)) {
					palautettava.add(rivi);
					summa += Double.parseDouble(pilkotturivi[1]);

				}

			}
			tiedostonlukija.close();

		} else if (pilkottu.length == 2) {

			int mikaVuosi = Integer.parseInt(pilkottu[0]);
			kenenMaksut = pilkottu[1];
			

			maksut = PalautaVuosiListaus(mikaVuosi);

			for (String vuosirivi : maksut) {
				String[] vuosiriviPalasina = vuosirivi.split(" ");
				String hinta = vuosiriviPalasina[1];
				String verrattavaKuka = vuosiriviPalasina[vuosiriviPalasina.length - 1];
				if (verrattavaKuka.equalsIgnoreCase(kenenMaksut)) {
					palautettava.add(vuosirivi);
					summa += Double.parseDouble(hinta);

				}

			}

		} else if(pilkottu.length == 3) {
			int mikaKK = Integer.parseInt(pilkottu[0]);
			int mikaVuosi = Integer.parseInt(pilkottu[1]);
			kenenMaksut = pilkottu[2];
			
			maksut = PalautaKuukausiListaus(mikaKK, mikaVuosi);
			
			for(String kuukausirivi : maksut) {
				String[] kuukausiriviPalasina = kuukausirivi.split(" ");
				String hinta = kuukausiriviPalasina[1];
				String verrattavaKuka = kuukausiriviPalasina[kuukausiriviPalasina.length - 1];
				if (verrattavaKuka.equalsIgnoreCase(kenenMaksut)) {
					palautettava.add(kuukausirivi);
					summa += Double.parseDouble(hinta);

				}
				
			}
			
			
		}

		StringBuilder builder = new StringBuilder();
		for (String rivi : palautettava) {
			builder.append(rivi);
			builder.append("\n");
		}

		String vastaus = builder.toString();
		return kenenMaksut + "n maksut:\n" + vastaus + "\nYhteensä " + summa + " euroa.";

	}

	// ==============================================================================================

	public String etsiJukanMaksut() {
		String sanotaanTakaisin = "Jukka has paid a lot";
		return sanotaanTakaisin;
	}

	public String etsiRuokalista() {
		LocalDate tanaan = LocalDate.now();
		int vuosi = tanaan.getYear();
		int kuukausi = tanaan.getMonthValue();
		int paiva = tanaan.getDayOfMonth();
		DayOfWeek viikonpaivaOlio = tanaan.getDayOfWeek();
		String viikonpaiva = viikonpaivaOlio.toString();
		String paivaTanaan = paiva + "." + kuukausi + "." + vuosi;
		String inline = "";
		String ruokainfo = viikonpaiva + " " + paivaTanaan + " ";
		try {
			URL url = new URL("https://hhapp.info/api/amica/pasila/fi");
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
				// System.out.println(inline);
				JSONParser parse = new JSONParser();

				JSONObject result = (JSONObject) parse.parse(inline);
				JSONArray menus = (JSONArray) result.get("LunchMenus");
				Iterator i = menus.iterator();

				System.out.println("TAPANI: ");

				while (i.hasNext()) {
					JSONObject weekinfo = (JSONObject) i.next();
					String dayOfWeek = (String) weekinfo.get("DayOfWeek");
					System.out.println(dayOfWeek);
					String date = (String) weekinfo.get("Date");
					System.out.println(date);
					JSONArray linjastot = (JSONArray) weekinfo.get("SetMenus");
					Iterator j = linjastot.iterator();
					while (j.hasNext()) {
						JSONObject mealInfo = (JSONObject) j.next();
						JSONArray setmenus = (JSONArray) mealInfo.get("SetMenus");

						JSONArray meals = (JSONArray) mealInfo.get("Meals");
						Iterator k = meals.iterator();
						while (k.hasNext()) {
							JSONObject dayInfo = (JSONObject) k.next();
							String name = (String) dayInfo.get("Name");
							System.out.println(name);

							if (date.equals(paivaTanaan)) {
								ruokainfo = ruokainfo + " " + name;
							}
						}
					}
				}

			}

		} catch (Exception e) {
			e.printStackTrace();
		}

		return ruokainfo;
	}

	public String etsiSaa() {
		String inline = "";
		try {
			URL url = new URL(
					"http://api.openweathermap.org/data/2.5/weather?q=Helsinki&APPID=a8720cf3a65bd981b2fecc6381cd729e&units=metric");
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
				JSONObject main = (JSONObject) result.get("main");
				double temp = (Double) main.get("temp");
				conn.disconnect();
				url = new URL(
						"http://api.openweathermap.org/data/2.5/weather?q=Nurmijarvi&APPID=a8720cf3a65bd981b2fecc6381cd729e&units=metric");
				conn = (HttpURLConnection) url.openConnection();
				conn.setRequestMethod("GET");
				conn.connect();

				sc = new Scanner(url.openStream());
				inline = "";
				while (sc.hasNext()) {
					inline += sc.nextLine();
				}
				sc.close();
				System.out.println("\nJSON data in string format");
				System.out.println(inline);
				parse = new JSONParser();

				result = (JSONObject) parse.parse(inline);
				main = (JSONObject) result.get("main");

				JSONObject wind = (JSONObject) result.get("wind");
				double speed = (Double) wind.get("speed");
				System.out.println("wind speed");
				conn.disconnect();

				LocalDate tanaan = LocalDate.now();
				int vuosi = tanaan.getYear();
				int kuukausi = tanaan.getMonthValue();
				int paiva = tanaan.getDayOfMonth();
				DayOfWeek viikonpaivaOlio = tanaan.getDayOfWeek();
				String viikonpaiva = viikonpaivaOlio.toString();

				DateTimeFormatter dtf = DateTimeFormatter.ofPattern("HH:mm:ss");
				LocalDateTime now = LocalDateTime.now();
				String aika = dtf.format(now);

				System.out.println("JUKKA " + temp);
				inline = "Outside at Helsinki it is now " + temp + "C. Wind speed is: " + speed + "m/s. Today is "
						+ viikonpaiva + " " + paiva + "." + kuukausi + "." + vuosi + " at " + aika + ".";
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
		return inline;
	}

	public String getBotUsername() {
		//return "kipa_bot";
		 return "inarabot";

	}

	@Override
	public String getBotToken() {
		return "944529343:AAEYPDWipfk6YnUQcIzw90r-U4RIuup0Gio";
		//return "944529343:AAEYPDWipfk6YnUQcIzw90r-U4RIuup0Gio";
	}

	public static void main(String[] args) {

		ApiContextInitializer.init();

		TelegramBotsApi botsApi = new TelegramBotsApi();

		try {
			botsApi.registerBot(new ChatBot());
		} catch (TelegramApiException e) {
			e.printStackTrace();
		}
	}
}