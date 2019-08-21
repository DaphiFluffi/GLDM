import ij.ImageJ;
import ij.ImagePlus;
import ij.gui.GenericDialog;
import ij.gui.NewImage;
import ij.plugin.PlugIn;
import ij.process.ImageProcessor;

//erste Uebung (elementare Bilderzeugung)

public class GLDM_U1 implements PlugIn {
	
	final static String[] choices = {
		"Schwarzes Bild",
		"Gelbes Bild",
		"Schwarz/Weiss Verlauf",
		"Horiz. Schwarz/Rot vert. Schwarz/Blau Verlauf",
		"Französische Fahne",
		"Tschechische Fahne",
		"Bangladeschische Fahne"
	};
	
	private String choice;
	
	public static void main(String args[]) {
		ImageJ ij = new ImageJ(); // neue ImageJ Instanz starten und anzeigen 
		ij.exitWhenQuitting(true);
		
		GLDM_U1 imageGeneration = new GLDM_U1();
		imageGeneration.run("");
	}
	
	public void run(String arg) {
		
		int width  = 566;  // Breite
		int height = 400;  // Hoehe
		
		// RGB-Bild erzeugen
		ImagePlus imagePlus = NewImage.createRGBImage("GLDM_U1", width, height, 1, NewImage.FILL_BLACK);
		ImageProcessor ip = imagePlus.getProcessor();
		
		// Arrays fuer den Zugriff auf die Pixelwerte
		int[] pixels = (int[])ip.getPixels();
		
		dialog();
		
		////////////////////////////////////////////////////////////////
		// Hier bitte Ihre Aenderungen / Erweiterungen
		
		if ( choice.equals("Schwarzes Bild") ) {
			generateBlackImage(width, height, pixels);
		}

		if ( choice.equals("Gelbes Bild") ) {
			generateYellowImage(width, height, pixels);
		}

		if ( choice.equals("Französische Fahne") ) {
			generateFrenchFlag(width, height, pixels);
		}

		if ( choice.equals("Schwarz/Weiss Verlauf") ) {
			generateSchwarzWeissVerlauf(width, height, pixels);
		}

		if ( choice.equals("Horiz. Schwarz/Rot vert. Schwarz/Blau Verlauf") ) {
			generateSchwarzRotSchwarzBlau(width, height, pixels);
		}

		if ( choice.equals("Horiz. Schwarz/Rot vert. Schwarz/Blau Verlauf") ) {
			generateSchwarzRotSchwarzBlau(width, height, pixels);
		}

		if ( choice.equals("Tschechische Fahne") ) {
			generateTschechischeFahne(width, height, pixels);
		}

		if ( choice.equals("Bangladeschische Fahne") ) {
			generateBangladeschischeFahne(width, height, pixels);
		}

		////////////////////////////////////////////////////////////////////
		
		// neues Bild anzeigen
		imagePlus.show();
		imagePlus.updateAndDraw();
	}

	private void generateBlackImage(int width, int height, int[] pixels) {
		// Schleife ueber die y-Werte
		double quarter = width / 4;
		for (int y=0; y<height; y++) {
			// Schleife ueber die x-Werte
			for (int x=0; x<width; x++) {

				int pos = y*width + x; // Arrayposition bestimmen
				
				/*int r = 0; //RGB auf 000 setzen fuer schwarz
				int g = 0;
				int b = 0;*/

				/*int b = (int)((x-(width/4))*(255.0/(width/2)));
				int r = 255 -b;
				int g = 0;*/

				int r = 255;
				int g = (int)(255 -((x-(width/2)) *(255.0/(width/2))));
				int b = (int) (255-((x-(width/4))*(255.0/(width/2))));
				/*int r = 255;
				int g = 0;
				int b = 0;
				if(x <= quarter) {
					g = 255;
					b = 255;
				}else if(x <= quarter * 2) {
					g = 255;
					b = (int)(-255 / quarter * (x - quarter) + 255);
				}else if(x < quarter * 3) {
					g = (int) (-255 / quarter * (x - 2 * quarter) + 255);
				}*/
				r = Math.min(Math.max(r,0),255);
				g = Math.min(Math.max(g,0),255);
				b = Math.min(Math.max(b,0),255);

				// Werte zurueckschreiben
				pixels[pos] = 0xFF000000 | (r << 16) | (g << 8) |  b;
			}
		}
	}

	private void generateSchwarzWeissVerlauf(int width, int height, int[] pixels) {

		//Konstante, die die "Schrittlaenge" bestimmt beim Umfaerben der Pixel
		//Die Breite des Bildes ist ja 566 Pixel, aber wir haben maximal 255 Farben
		double c1 = 255.0/(double)(width/2);

		//Deklaration von r,g,b
		int r;
		int g;
		int b;

		// Schleife ueber die y-Werte
		for (int y=0; y<height; y++) {
			// Schleife ueber die x-Werte
			for (int x=0; x<width; x++) {
				int pos = y*width + x; // Arrayposition bestimmen

				/*if (x <= width/2){ // Solange wir in der ersten Haelfte sind
					 r = (int)((255.0/(width/2))*x); //Sollen die r,g,b Werte auf ihre "normierte" Form gesetzt werden.
					 g = r;//Normiert werden sie durch die Konstante c1;
					 b = r;
				}else{ //In der zweiten Hälfte passiert genau das gleiche nur von weiss nach schwarz
					r = (int) (255-((255.0/(width/2)) *( x-(width/2)))); // Der erste x-Wert in der zweiten Haelfte wäre x= width/2.
					g = r;// Deshalb muss der x-Wert immer minus width/2 genommen werden, damit x bei 0 anfaengt.
					b = r;//Von 255 muss der berechnete Wert abgezogen werden, damit wir bei weiß(255,255,255) anfangen und bei schwarz(0,0,0) enden
				}*/

				if(x <= width/2){
					r = (int)(x * (255.0/(width/2)));
					g = r;
					b = r;
				}else{
					r = (int)(255-(255.0/(width/2)) * (x - (width/2)));
					g = r;
					b = r;
				}

				// Werte zurueckschreiben
				pixels[pos] = 0xFF000000 | (r << 16) | (g << 8) |  b;
			}
		}
	}

	private void generateFrenchFlag(int width, int height, int[] pixels) {
		// Schleife ueber die y-Werte
		for (int y=0; y<height; y++) {
			// Schleife ueber die x-Werte
			for (int x=0; x<width; x++) {
				int pos = y*width + x; // Arrayposition bestimmen

				int r, g, b;
				//Das erste Drittel der Flagge ist blau
				/*if(x <= width/3){
					r = 0;
					g = 0;
					b = 255;
				// Das zweite Drittel ist weiss
				}else if(x <= (width*2)/3){
					 r = 255;
					 g = 255;
					 b = 255;
				 //Das letzte Drittel ist rot
				}else{
					 r = 255;
					 g = 0;
					 b = 0;
				}*/

				b = (int)((x - (width/4))*(255.0/(width/2)));
				r = 255 -b;
				g = 255 - b;

				r = Math.min(Math.max(r, 0), 255);
				b = Math.min(Math.max(b, 0), 255);
				g = Math.min(Math.max(g, 0), 255);

				System.out.println((0xFF & 0x101D31)>>16);
				// Werte zurueckschreiben
				pixels[pos] = 0xFF000000 | (r << 16) | (g << 8) |  b;
			}
		}
	}

	private void generateYellowImage(int width, int height, int[] pixels) {
		// Schleife ueber die y-Werte
		for (int y=0; y<height; y++) {
			// Schleife ueber die x-Werte
			for (int x=0; x<width; x++) {
				int pos = y*width + x; // Arrayposition bestimmen

				//Die RGB Werte auf die Werte für Gelb
				int r = 255;
				int g = 255;
				int b = 0;

				// Werte zurueckschreiben
				pixels[pos] = 0xFF000000 | (r << 16) | (g << 8) |  b;
			}
		}
	}

	private void generateSchwarzRotSchwarzBlau(int width, int height, int[] pixels) {
		// Schleife ueber die y-Werte
		double c = 255.0/(double)(width); //Normierung von width
		double h = 255.0/(double)(height); //Normierung von Height

		int r; //Deklaration von r,g,b
		int g;
		int b;

		for (int y=0; y<height; y++) {
			// Schleife ueber die x-Werte
			for (int x=0; x<width; x++) {
				//int pos = y*width + x; // Arrayposition bestimmen

					/*r = (int)(255-(c *(width - x))); //wir wollen, dass das im rechten Teil des Bildes am inensivsten ist
					g = 0; //schwarz, blau und rot benötigen keinen gelb wert
					b = (int)(255 - (h*(height - y))); // wir wollen, dass das intensivste Blau ganz unten ist*/

					/*r = (int)((255.0/width)* x);
					g = 0;
					b = (int)((255.0/height)*y);*/

				int pos = y * width + x;

				int a = 128;
				 r = (int) ((255.0/width)*x);
				 g = (int) ((255.0/height)*y);
				 b = 0;

				pixels[pos] = (a << 24) | (r << 16) | (g << 8) | b;
				// Werte zurueckschreiben
				//pixels[pos] = 0xFF000000 | (r << 16) | (g << 8) |  b;
			}
		}
	}

	private void generateTschechischeFahne(int width, int height, int[] pixels) {

		int r; //Deklaration von r,g,b
		int g;
		int b;
		// Schleife ueber die y-Werte
		for (int y=0; y<height; y++) {
			// Schleife ueber die x-Werte
			for (int x=0; x<width; x++) {
				int pos = y*width + x; // Arrayposition bestimmen

				if( y <= height/2) { //erste Hälfte der Fahne ist Weiss
					r = 255;
					g = 255;
					b = 255;
				}else{ //zweite hälfte der Fahne ist rot
					r = 255;
					g = 0;
					b = 0;
				}

				//Formel fuer das Deieck:
				//Ich nehme die Geradengleichungen der Geraden aus der oberen und unteren linkten Ecke zur Spitze des Dreiecks.
				// Ich sage dann, dass nur die Pixel, die in beiden durch die Geraden definierten Flaechen Blau sein sollen
				if( (((double)(height/2.0)/(double)(width/2.0))*x <= y)&& ((height)-((double)(height/2.0)/(double)(width/2.0))*x >= y)){
				r = 0;
				g = 0;
				b = 255; }


				// Werte zurueckschreiben
				pixels[pos] = 0xFF000000 | (r << 16) | (g << 8) |  b;
			}
		}
	}

	private void generateBangladeschischeFahne(int width, int height, int[] pixels) {

		int r,g,b, rad;
		rad= 100;
		for (int y=0; y<height; y++) {
			// Schleife ueber die x-Werte
			for (int x=0; x<width; x++) {
				int pos = y*width + x; // Arrayposition bestimmen

				//Anlegen von zusätzlichen Variablen zur Vereinfachung der Formel in der if-Bedingung
				double c1 = x-(width/2);
				double c2 = y-(height/2);
				double h1 = Math.pow(c1,2);
				double h2 = Math.pow(c2,2);

				//Formel:
				//(x_Pixel - x_Mittelpunkt)^2+(y_Pixel - y_Mittelpunkt)^2 = Radius^2
				//Der Mittelpunkt meines Kreises soll bei x=width/2 und y= height/2 liegen
				// Die Pixel im Kreis sollen rot sein. Deshalb habe ich <= und nicht == geschrieben.
				// == wäre nur der Kreisbogen und >= wäre die gesamte Fläche mit einem kreisförmigen Loch
				if( h1 + h2 <= Math.pow(rad,2)){
					r = 210; //dunkles rot
					g = 1;
					b = 23;
				}else{
					r= 11; //dunkles grün
					g= 103;
					b = 35;
				}

				// Werte zurueckschreiben
				pixels[pos] = 0xFF000000 | (r << 16) | (g << 8) |  b;
			}
		}
	}

	private void dialog() {
		// Dialog fuer Auswahl der Bilderzeugung
		GenericDialog gd = new GenericDialog("Bildart");
		
		gd.addChoice("Bildtyp", choices, choices[0]);
		
		
		gd.showDialog();	// generiere Eingabefenster
		
		choice = gd.getNextChoice(); // Auswahl uebernehmen
		
		if (gd.wasCanceled())
			System.exit(0);
	}
}

