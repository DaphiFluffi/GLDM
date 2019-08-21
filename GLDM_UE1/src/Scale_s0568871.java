import ij.IJ;
import ij.ImagePlus;
import ij.WindowManager;
import ij.gui.GenericDialog;
import ij.gui.NewImage;
import ij.plugin.filter.PlugInFilter;
import ij.process.ImageProcessor;


public class Scale_s0568871 implements PlugInFilter {
	ImageProcessor imp; // ImagePlus object

	public static void main(String args[]) {

		IJ.open("/users/mcflu/IdeaProjects/GLDM_UE1/src/component.jpg");
		//IJ.open("Z:/Pictures/Beispielbilder/orchid.jpg");

		Scale_s0568871 pw = new Scale_s0568871();
		pw.imp = IJ.getProcessor();
		pw.run(pw.imp);
	}


	public int setup(String arg, ImagePlus imp) {
		if (arg.equals("about"))
		{showAbout(); return DONE;}
		return DOES_RGB+NO_CHANGES;
		// kann RGB-Bilder und veraendert das Original nicht
	}

	public void run(ImageProcessor ip) {

		String[] dropdownmenue = {"Kopie", "Pixelwiederholung", "Bilinear"};

		GenericDialog gd = new GenericDialog("scale");
		gd.addChoice("Methode",dropdownmenue,dropdownmenue[0]);
		gd.addNumericField("Hoehe:",500,0);
		gd.addNumericField("Breite:",400,0);

		gd.showDialog();
		
		int height_n = (int)gd.getNextNumber(); // _n fuer das neue skalierte Bild
		int width_n =  (int)gd.getNextNumber();
		
		int width  = ip.getWidth();  // Breite bestimmen
		int height = ip.getHeight(); // Hoehe bestimmen

		//height_n = height;
		//width_n  = width;
		
		ImagePlus neu = NewImage.createRGBImage("Skaliertes Bild",
		                   width_n, height_n, 1, NewImage.FILL_BLACK);
		
		ImageProcessor ip_n = neu.getProcessor();

		
		int[] pix = (int[])ip.getPixels();
		int[] pix_n = (int[])ip_n.getPixels();

		String choice = gd.getNextChoice();
		if(choice.equals("Kopie")) {
			// Schleife ueber das neue Bild
			for (int y_n = 0; y_n < height_n; y_n++) {
				for (int x_n = 0; x_n < width_n; x_n++) {
					int y = y_n;
					int x = x_n;

					if (y < height && x < width) {
						int pos_n = y_n * width_n + x_n;
						int pos = y * width + x;

						pix_n[pos_n] = pix[pos];
					}
				}
			}
		}
		if(choice.equals("Pixelwiederholung")) {
			for (int y_n = 0; y_n < height_n; y_n++) {
				for (int x_n = 0; x_n < width_n; x_n++) {

						double h = ((double)height_n/(double)height); //Vergrößerungsfaktor bestimmen
						double w = ((double)width_n/(double)width);

						int pos_n = y_n * width_n + x_n; //Position der Pixel im neune Bild aus y_n und x_n berechnen
						//immer den Pixel rechts und links von dem eigentlichen Pixel nehmen
						//Das Casten nach Integer ist essenziell, weil bei floating point zahlen,
						// weil wir uns den RUndungsfehler für die Pixelwiederholung zu Nutze machen.
						int pos = (int)(y_n/h) * width + (int)(x_n/w); // Pixelwiederholung

						pix_n[pos_n] = pix[pos]; //zurückschreiben in das pix_n-Array
				}
			}
		}
		if(choice.equals("Bilinear")) {
			//habe ich leider nicht geschafft
		}
		// neues Bild anzeigen
		neu.show();
		neu.updateAndDraw();
	}

	void showAbout() {
		IJ.showMessage("");
	}

}

