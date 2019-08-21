import ij.IJ;
import ij.ImageJ;
import ij.ImagePlus;
import ij.WindowManager;
import ij.gui.ImageCanvas;
import ij.gui.ImageWindow;
import ij.plugin.PlugIn;
import ij.process.ImageProcessor;

import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Panel;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import javax.swing.BorderFactory;
import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.border.TitledBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

/**
     Opens an image window and adds a panel below the image
 */
public class GRDM_U3 implements PlugIn {

	ImagePlus imp; // ImagePlus object
	private int[] origPixels;
	private int width;
	private int height;

	String[] items = {"Original", "Rot-Kanal", "Graustufen", "Negativ", "Binaerbild", "Dithering", "Sepia", "sechs Farben"};


	public static void main(String args[]) {

		IJ.open("/users/mcflu/IdeaProjects/GLDM_UE1/src/Bear.jpg");
		//IJ.open("Z:/Pictures/Beispielbilder/orchid.jpg");

		GRDM_U3 pw = new GRDM_U3();
		pw.imp = IJ.getImage();
		pw.run("");
	}

	public void run(String arg) {
		if (imp==null) 
			imp = WindowManager.getCurrentImage();
		if (imp==null) {
			return;
		}
		CustomCanvas cc = new CustomCanvas(imp);

		storePixelValues(imp.getProcessor());

		new CustomWindow(imp, cc);
	}


	private void storePixelValues(ImageProcessor ip) {
		width = ip.getWidth();
		height = ip.getHeight();

		origPixels = ((int []) ip.getPixels()).clone();
	}


	class CustomCanvas extends ImageCanvas {

		CustomCanvas(ImagePlus imp) {
			super(imp);
		}

	} // CustomCanvas inner class


	class CustomWindow extends ImageWindow implements ItemListener {

		private String method;
		
		CustomWindow(ImagePlus imp, ImageCanvas ic) {
			super(imp, ic);
			addPanel();
		}

		void addPanel() {
			//JPanel panel = new JPanel();
			Panel panel = new Panel();

			JComboBox cb = new JComboBox(items);
			panel.add(cb);
			cb.addItemListener(this);

			add(panel);
			pack();
		}

		public void itemStateChanged(ItemEvent evt) {

			// Get the affected item
			Object item = evt.getItem();

			if (evt.getStateChange() == ItemEvent.SELECTED) {
				System.out.println("Selected: " + item.toString());
				method = item.toString();
				changePixelValues(imp.getProcessor());
				imp.updateAndDraw();
			} 

		}


		private void changePixelValues(ImageProcessor ip) {

			// Array zum Zurückschreiben der Pixelwerte
			int[] pixels = (int[])ip.getPixels();

			if (method.equals("Original")) {

				for (int y=0; y<height; y++) {
					for (int x=0; x<width; x++) {
						int pos = y*width + x;
						
						pixels[pos] = origPixels[pos];
					}
				}
			}
			
			if (method.equals("Rot-Kanal")) {

				for (int y=0; y<height; y++) {
					for (int x=0; x<width; x++) {
						int pos = y*width + x;
						int argb = origPixels[pos];  // Lesen der Originalwerte 

						int r = (argb >> 16) & 0xff;
						//int g = (argb >>  8) & 0xff;
						//int b =  argb        & 0xff;

						int rn = r;
						int gn = 0;
						int bn = 0;

						// Hier muessen die neuen RGB-Werte wieder auf den Bereich von 0 bis 255 begrenzt werden

						pixels[pos] = (0xFF<<24) | (rn<<16) | (gn<<8) | bn;
					}
				}
			}

			if (method.equals("Negativ")) {

				for (int y=0; y<height; y++) {
					for (int x=0; x<width; x++) {
						int pos = y*width + x;
						int argb = origPixels[pos];  // Lesen der Originalwerte

						int r = (argb >> 16) & 0xff;
						int g = (argb >>  8) & 0xff;
						int b =  argb        & 0xff;

						//von 255 den Rot/grün/Blau Wert abziehen und dann zurückschreiben
						int rn = 255 - r;
						int gn = 255 - g;
						int bn = 255 - b;

						// Hier muessen die neuen RGB-Werte wieder auf den Bereich von 0 bis 255 begrenzt werden

						pixels[pos] = (0xFF<<24) | (rn<<16) | (gn<<8) | bn;
					}
				}
			}

			if (method.equals("Graustufen")) {

				for (int y=0; y<height; y++) {
					for (int x=0; x<width; x++) {
						int pos = y*width + x;
						int argb = origPixels[pos];  // Lesen der Originalwerte

						int r = (argb >> 16) & 0xff;
						int g = (argb >>  8) & 0xff;
						int b =  argb        & 0xff;

						double Y = (r+b+g)/3.0;
						int rn = (int)Y;
						int gn = rn;
						int bn = rn;
						//in jeden Farbkanal die Formel für Graustufen in RGB einsetzen und nach int casten
						/*int rn = (int) ((0.299 * r) + (0.114 * b) + (0.587 * g));
						int gn = (int) ((0.299 * r) + (0.114 * b) + (0.587 * g));
						int bn = (int) ((0.299 * r) + (0.114 * b) + (0.587 * g));*/

						// Hier muessen die neuen RGB-Werte wieder auf den Bereich von 0 bis 255 begrenzt werden

						pixels[pos] = (0xFF<<24) | (rn<<16) | (gn<<8) | bn;
					}
				}
			}

			if (method.equals("Binaerbild")) {

				for (int y=0; y<height; y++) {
					for (int x=0; x<width; x++) {
						int pos = y*width + x;
						int argb = origPixels[pos];  // Lesen der Originalwerte

						int r = (argb >> 16) & 0xff;
						int g = (argb >>  8) & 0xff;
						int b =  argb        & 0xff;

						//Formel aus der Vorlesung für Quantisierung in Code umsetzen
						/*int N = 2;
						int delta = 255/N;
						int deltaStrich = 255/(N-1);
						int Y = ((r+b+g)/3);
						int rn = Y/delta * deltaStrich;
						int gn = rn;
						int bn =  rn;*/

						int Y = (r+b+g)/3;
						int rn = Y;
						int gn = Y;
						int bn = Y;
						if ( rn < 128){
							rn = 0;
						}else{
							rn = 255;
						}
						if ( gn < 128){
							gn = 0;
						}else{
							gn = 255;
						}
						if ( bn < 128){
							bn = 0;
						}else{
							bn = 255;
						}

						// Hier muessen die neuen RGB-Werte wieder auf den Bereich von 0 bis 255 begrenzt werden
						rn = Math.min(Math.max( rn,0),255);
						bn = Math.min(Math.max( bn,0),255);
						gn = Math.min(Math.max( gn,0),255);

						pixels[pos] = (0xFF<<24) | (rn<<16) | (gn<<8) | bn;
					}
				}
			}

			if (method.equals("Dithering")){

				int threshhold = 128;
				int error = 0;

				for (int y=0; y<height; y++) {
					for (int x = 0; x < width; x++) {
						int pos = y * width + x;
						int argb = origPixels[pos];  // Lesen der Originalwerte

						int r = (argb >> 16) & 0xff;
						int g = (argb >> 8) & 0xff;
						int b = argb & 0xff;


						int Y = (r+b+g)/3; //Mittelgrau Wert

						int rn = Y + error;

						if(rn > threshhold){ //wenn mein Wert über 128 liegt
							error = (255-rn)*(-1); // Fehler berechnen (das -1 ist damit der Fehler abgezogen wird)
							rn = 255; // den rn Wert auf weiss setzen
						}else{
							error = (threshhold + rn); //Den Pixelwert an den Schwellenwert addieren
							rn = 0; // den rn auf schwarz setzen
						}

						int bn = rn;
						int gn = rn;


						// Hier muessen die neuen RGB-Werte wieder auf den Bereich von 0 bis 255 begrenzt werden

						rn = Math.min(Math.max( rn,0),255);
						bn = Math.min(Math.max( bn,0),255);
						gn = Math.min(Math.max( gn,0),255);

						pixels[pos] = (0xFF << 24) | (rn << 16) | (gn << 8) | bn;
					}
				}
			} //??

			if (method.equals("Sepia")) {

				for (int y=0; y<height; y++) {
					for (int x=0; x<width; x++) {
						int pos = y*width + x;
						int argb = origPixels[pos];  // Lesen der Originalwerte

						int r = (argb >> 16) & 0xff;
						int g = (argb >>  8) & 0xff;
						int b =  argb        & 0xff;

						//in jeden Farbkanal die Formel für Graustufen in RGB einsetzen und nach int casten und noch mit meinen gelb werten aus color inspektor multiplizieren
						int rn = (int) (((0.299 * r) + (0.114 * b) + (0.587 * g))* 1.22);
						int gn = (int) (((0.299 * r) + (0.114 * b) + (0.587 * g))* 1.14);
						int bn = (int) (((0.299 * r) + (0.114 * b) + (0.587 * g)) * 0.76);

						// Hier muessen die neuen RGB-Werte wieder auf den Bereich von 0 bis 255 begrenzt werden

						rn = Math.min(Math.max( rn,0),255);
						bn = Math.min(Math.max( bn,0),255);
						gn = Math.min(Math.max( gn,0),255);

						pixels[pos] = (0xFF<<24) | (rn<<16) | (gn<<8) | bn;
					}
				}
			}

			if(method.equals("sechs Farben")){
				for (int y=0; y<height; y++) {
					for (int x = 0; x < width; x++) {
						int pos = y * width + x;
						int argb = origPixels[pos];  // Lesen der Originalwerte

						int r = (argb >> 16) & 0xff;
						int g = (argb >> 8) & 0xff;
						int b = argb & 0xff;

						int rn = 0;
						int gn = 0;
						int bn = 0;

						//berechne den Abstand zu meinen 6 ausgewählten Farben
						int error1 =(184-r)*(184-r)+ (177-g)*(177-g)+(175-b)*(175-b);//weissliches grau
						int error2 =(62-r)*(62-r)+ (56-g)*(56-g)+(51-b)*(51-b); //braun
						int error3 =(53-r)*(53-r)+ (104-g)*(104-g)+(140-b)*(140-b);//blau
						int error4 =(82-r)*(82-r)+ (87-g)*(87-g)+(88-b)*(88-b);//grau
						int error5 =(11-r)*(11-r)+ (16-g)*(16-g)+(16-b)*(16-b);//schwarz
						int error6 =(25-r)*(25-r)+ (32-g)*(32-g)+(33-b)*(33-b);//grau schwarz

						//schreibe alle Abstände in ein Array
						int[] errors = {error1, error2, error3, error4, error5, error6};
						//finde für jeden ausgelesenen RGB Vektor den kürzesten Abstand zur nächsten Palettenfarbe
						int min = errors[0];
						for(int i = 0; i < errors.length; i++){
							if(errors[i]< min ){
								min = errors[i];
							}
						}
						//je nach geringstem Abstand sollen die RGB Werte auf die von einer der Farben geändert werden
						if(min == error1){
							rn = 184;
							gn = 177;
							bn = 175;
						}else if (min == error2){
							rn = 62;
							gn = 56;
							bn = 51;
						}else if (min == error3){
							rn = 53;
							gn = 104;
							bn = 140;
						}else if( min == error4){
							rn = 82;
							gn = 87;
							bn = 88;
						}else if (min == error5){
							rn = 11;
							gn = 16;
							bn = 16;
						}else if (min == error6){
							rn = 25;
							gn = 32;
							bn = 33;
						}

						// Hier muessen die neuen RGB-Werte wieder auf den Bereich von 0 bis 255 begrenzt werden
						rn = Math.min(Math.max(rn, 0), 255);
						bn = Math.min(Math.max(bn, 0), 255);
						gn = Math.min(Math.max(gn, 0), 255);

						pixels[pos] = (0xFF << 24) | (rn << 16) | (gn << 8) | bn;
					}
				}
			}
		}


	} // CustomWindow inner class
} 
