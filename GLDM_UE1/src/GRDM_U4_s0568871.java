import ij.*;
import ij.io.*;
import ij.process.*;
import ij.*;
import ij.gui.*;
import java.awt.*;
import ij.plugin.filter.*;


public class GRDM_U4_s0568871 implements PlugInFilter {

	protected ImagePlus imp;
	final static String[] choices = {"Wischen", "Weiche Blende", "Overlay AB", "Overlay BA", "Schieben", "ChromaKey","Eigenes"};

	public int setup(String arg, ImagePlus imp) {
		this.imp = imp;
		return DOES_RGB+STACK_REQUIRED;
	}
	
	public static void main(String args[]) {
		ImageJ ij = new ImageJ(); // neue ImageJ Instanz starten und anzeigen 
		ij.exitWhenQuitting(true);
		
		IJ.open("/users/mcflu/IdeaProjects/GLDM_UE1/src/StackB.zip");
		
		GRDM_U4_s0568871 sd = new GRDM_U4_s0568871();
		sd.imp = IJ.getImage();
		ImageProcessor B_ip = sd.imp.getProcessor();
		sd.run(B_ip);
	}

	public void run(ImageProcessor B_ip) {
		// Film B wird uebergeben
		ImageStack stack_B = imp.getStack();
		
		int length = stack_B.getSize();
		int width  = B_ip.getWidth();
		int height = B_ip.getHeight();
		
		// ermoeglicht das Laden eines Bildes / Films
		Opener o = new Opener();
		OpenDialog od_A = new OpenDialog("Auswählen des 2. Filmes ...",  "");
				
		// Film A wird dazugeladen
		String dateiA = od_A.getFileName();
		if (dateiA == null) return; // Abbruch
		String pfadA = od_A.getDirectory();
		ImagePlus A = o.openImage(pfadA,dateiA);
		if (A == null) return; // Abbruch

		ImageProcessor A_ip = A.getProcessor();
		ImageStack stack_A  = A.getStack();

		if (A_ip.getWidth() != width || A_ip.getHeight() != height)
		{
			IJ.showMessage("Fehler", "Bildgrößen passen nicht zusammen");
			return;
		}
		
		// Neuen Film (Stack) "Erg" mit der kleineren Laenge von beiden erzeugen
		length = Math.min(length,stack_A.getSize());

		ImagePlus Erg = NewImage.createRGBImage("Ergebnis", width, height, length, NewImage.FILL_BLACK);
		ImageStack stack_Erg  = Erg.getStack();

		// Dialog fuer Auswahl des Ueberlagerungsmodus
		GenericDialog gd = new GenericDialog("Überlagerung");
		gd.addChoice("Methode",choices,"");
		gd.showDialog();

		int methode = 0;		
		String s = gd.getNextChoice();
		if (s.equals("Wischen")) methode = 1;
		if (s.equals("Weiche Blende")) methode = 2;
		if (s.equals("Overlay AB")) methode = 3;
		if (s.equals("Overlay BA")) methode = 4;
		if (s.equals("Schieben")) methode = 5;
		if (s.equals("ChromaKey")) methode = 6;
		if (s.equals("Eigenes")) methode = 7;

		// Arrays fuer die einzelnen Bilder
		int[] pixels_B;
		int[] pixels_A;
		int[] pixels_Erg;

		// Schleife ueber alle Bilder
		for (int z=1; z<=length; z++)
		{
			pixels_B   = (int[]) stack_B.getPixels(z);
			pixels_A   = (int[]) stack_A.getPixels(z);
			pixels_Erg = (int[]) stack_Erg.getPixels(z);

			int pos = 0;
			for (int y=0; y<height; y++)
				for (int x=0; x<width; x++, pos++)
				{
					int cA = pixels_A[pos]; //Pixel in Stack A auslesen
					int rA = (cA & 0xff0000) >> 16;
					int gA = (cA & 0x00ff00) >> 8;
					int bA = (cA & 0x0000ff);

					int cB = pixels_B[pos]; //Pixel im Stack B auslesen
					int rB = (cB & 0xff0000) >> 16;
					int gB = (cB & 0x00ff00) >> 8;
					int bB = (cB & 0x0000ff);


					if (methode == 1) //Wischen
					{
						//mit x+1 wäre es horizontal
						//mit y+1 ist vertikal
					if (y+1 > (z-1)*(double)height/(length-1))
						pixels_Erg[pos] = pixels_B[pos];
					else
						pixels_Erg[pos] = pixels_A[pos];
					}

					if (methode == 2) //weiche blende
					{
						//Normierung für die Schrittlänge
						int Alpha = (255/length+1)*(z-1); //aus der VL

						int r,g,b = 0;

						//macht, dass am Ende Film A kompett zu sehen ist
						if(Alpha > 255){ //damit der Wert nicht zu gross wird
							Alpha = 255;
						}
					//Formel aus der VL: Bildüberlagerung Normal Keying
					 r = (((Alpha*rA) + (255-Alpha)*rB)/255); //Formel der Bildüberlagerung
					 g = (((Alpha*gA) + (255-Alpha)*gB)/255);
					 b = (((Alpha*bA) + (255-Alpha)*bB)/255);

						/*int r,g,b;

						 r = (255 -(255-rA) * (255-rB) )/ 255;
						 g = (255 -(255-gA) * (255-gB) )/ 255;
						 b =(255 -(255-bA) * (255-bB) )/ 255;
						pixels_Erg[pos] = 0xFF000000 | (r << 16) | (g << 8) | b;*/
					 //RGB Werte zurück in den Ergebnis-Film schreiben
						pixels_Erg[pos] = 0xFF000000 + ((r & 0xff) << 16) + ((g & 0xff) << 8) + ( b & 0xff);
						//pixels_Erg[pos] = 0xFF000000 | (r << 16) | (g << 8) | ( b);

					}
					if (methode == 3) //overlay
					{
						int r;
						int b;
						int g;

						//Formel aus der VL S.7 Ineinanderkopieren/Overlay
						//Angewandt auf jeden Farbkanal
						//Overlay(A,B)
						if(rA <= 128) {
							r = rA*rB/128;
						}else{
							r= 255 - (((255-rA)*(255-rB))/128);
						}
						if(gA <= 128) {
							g = gA*gB/128;
						}else{
							g= 255 - (((255-gA)*(255-gB))/128);
						}
						if(bA <= 128) {
							b = bA*bB/128;
						}else{
							b= 255 - (((255-bA) *(255-bB))/128);
						}

						pixels_Erg[pos] = 0xFF000000 + ((r & 0xff) << 16) + ((g & 0xff) << 8) + ( b & 0xff);
					}
					if (methode == 4) //overlay 2
					{
						int r;
						int b;
						int g;

						//Overlay(A,B)
						if(rB <= 128) {
							r = rB*rA/128;
						}else{
							r= 255 - (((255-rB)*(255-rA))/128);
						}
						if(gB <= 128) {
							g = gB*gA/128;
						}else{
							g= 255 - (((255-gB)*(255-gA))/128);
						}
						if(bB <= 128) {
							b = bB*bA/128;
						}else{
							b= 255 - (((255-bB) *(255-bA))/128);
						}

						r = Math.min(Math.max( r,0),255);
						b = Math.min(Math.max( b,0),255);
						g = Math.min(Math.max( g,0),255);

						pixels_Erg[pos] = 0xFF000000 + ((r & 0xff) << 16) + ((g & 0xff) << 8) + ( b & 0xff);
					}
					if (methode == 5) // schieben
					{
						//gleiche if Bedingung wie beim Wischen von links nach rechts
						if (x+1 > (z-1)*(double)width/(length-1)) {
							//die If Bedingung trefft immer nur auf die 1. Zeile des jeweiligen Bildes zu
							//so wird der Anfang des Films B nach rechts geschoben
							pixels_Erg[pos] = pixels_B[pos- ((z-1)*width/(length-1))];
						}else {
							//an sonsten ist der Film A zu sehen
							//der din Film B rausschiebt
							pixels_Erg[pos] = pixels_A[(pos+width)- ((z-1)*width/(length-1))];
							//pixels_Erg[pos] = pixels_A[pos- (z*width/(length-1))];

						}
					}
					if (methode == 6) //chroma key
					{

						int Alpha = (255/length+1)*z-1;
						int r,g,b = 0;

						//ich habe einen Orange ton gewählt, der sehr oft im Film A vorkommt
						// R = 244 G= 268 B = 56
						//3D-Pythagorad zur bestimmung des abstandes einer Farbe zu meinem gewählten orange
						double distance = Math.sqrt(Math.pow((rA-244),2) + Math.pow((gA - 168),2) + + Math.pow((bA - 59),2));

						if(distance < 100){ //wenn die distanz kleiner ist als Hundert
							pixels_Erg[pos] = pixels_B[pos]; //soll Film B durchgucken
						}else if (distance > 100 && distance < 150){ //wenn die diestanz noch ein bisschen größer ist
							//soll Film A unf FIlm B an diesen Stellen weich überblendet werden
							r = (((Alpha*rA) + (255-Alpha)*rB)/255); //Formel der Bildüberlagerung
							g = (((Alpha*gA) + (255-Alpha)*gB)/255);
							b = (((Alpha*bA) + (255-Alpha)*bB)/255);
							pixels_Erg[pos] = 0xFF000000 + ((r & 0xff) << 16) + ((g & 0xff) << 8) + ( b & 0xff);
						}
						else{ // an sonsten soll Film A zu sehen sein
							pixels_Erg[pos] = pixels_A[pos];

						}

					  }
					if(methode == 7)//eigene
					{
						//Fehlversuch bei dem Schieben
						//die zwei Videos tauchen doppelt nebeneinander auf
						// und kommen von oben und unten rein
						// ich verstehe nicht genau warum das passiert
						if (y+1 > (z-1)*(double)height/(length-1)) {
							pixels_Erg[pos] = pixels_B[pos- ((z-1)*height/(length-1))];
						}else {
							pixels_Erg[pos] = pixels_A[(pos+height)- ((z-1)*height/(length-1))];
						}
					}
				}
		}

		// neues Bild anzeigen
		Erg.show();
		Erg.updateAndDraw();

	}

}

